package com.wastewise.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wastewise.dto.WasteAnalysisResultDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class GeminiVisionClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiVisionClient.class);

    @Value("${wastewise.ai.gemini.api-key:}")
    private String apiKey;

    @Value("${wastewise.ai.gemini.model:gemini-3-flash-preview}")
    private String modelName;

    @Value("${wastewise.ai.gemini.endpoint:https://generativelanguage.googleapis.com/v1beta/models}")
    private String endpoint;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    // Models with vision support to try in priority order
    private static final List<String> FALLBACK_MODELS = List.of(
            "gemini-3-flash-preview",
            "gemini-3.8-flash",
            "gemini-flash-latest"
    );

    public WasteAnalysisResultDto analyzeImage(byte[] imageBytes, String contentType, String filename) {
        if (apiKey != null && !apiKey.trim().isEmpty() && !apiKey.equalsIgnoreCase("YOUR_GEMINI_API_KEY")) {
            List<String> modelsToTry = new ArrayList<>();
            if (modelName != null && !modelName.isBlank()) {
                modelsToTry.add(modelName.trim());
            }
            for (String fm : FALLBACK_MODELS) {
                if (!modelsToTry.contains(fm)) {
                    modelsToTry.add(fm);
                }
            }

            Exception lastException = null;
            for (String currentModel : modelsToTry) {
                try {
                    log.info("Attempting Gemini Vision API call with model: {}", currentModel);
                    return callGeminiVisionApi(currentModel, imageBytes, contentType);
                } catch (Exception e) {
                    lastException = e;
                    log.warn("Model '{}' failed: {}. Trying next available model...", currentModel, e.getMessage());
                }
            }

            log.warn("All Gemini API vision models failed, falling back to Demo Mode: {}", 
                     lastException != null ? lastException.getMessage() : "Unknown error");
            return getDemoFallbackResult(filename, "API call failed (" + 
                     (lastException != null ? lastException.getMessage() : "Offline") + "). Evaluated using offline rule-based demo heuristics.");
        } else {
            log.info("No Gemini API key configured. Executing clearly labeled Demo Mode fallback.");
            return getDemoFallbackResult(filename, "Gemini API key is not configured in environment (GEMINI_API_KEY). Running in Demo Fallback Mode.");
        }
    }

    private WasteAnalysisResultDto callGeminiVisionApi(String model, byte[] imageBytes, String contentType) throws Exception {
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        String mimeType = (contentType != null && !contentType.isBlank() && contentType.startsWith("image/")) 
                ? contentType : "image/jpeg";

        String url = String.format("%s/%s:generateContent?key=%s", endpoint, model, apiKey);

        String systemPrompt = """
            You are WasteWise AI, an expert agent in municipal waste segregation and environmental sustainability conforming to UN SDG 12.
            Analyze the uploaded image and identify the household waste item(s).
            Respond ONLY with a valid, clean JSON object (do not include markdown ticks, do not include ```json ... ``` wrapper) matching this exact schema:
            {
              "itemName": "Specific item name (e.g. Empty PET Water Bottle, Banana Peel)",
              "predictedCategory": "One of: Organic / wet waste, Recyclable plastic, Paper and cardboard, Glass, Metal, General / non-recyclable waste, Electronic waste, Batteries, Potentially hazardous waste, Unknown / needs clarification",
              "aiExplanation": "Clear 2-3 sentence explanation of the material and why it belongs to this category.",
              "disposalRecommendation": "Specific, actionable instructions for clean and responsible disposal or segregation bin color.",
              "reuseRecycleTips": "Practical tips to reuse, upcycle, or properly prepare for recycling (e.g. rinse, remove cap).",
              "safetyPrecautions": "Crucial safety warnings especially for chemicals, glass, lithium batteries, or e-waste.",
              "confidenceLevel": "High, Medium, Low, or Uncertain",
              "uncertain": false,
              "clarificationNeeded": false
            }
            Important rules:
            - If the image does not clearly depict waste or is blurry, set uncertain: true and predictedCategory: "Unknown / needs clarification".
            - Never guess dangerous chemicals or batteries without clear cautionary advice.
            - Do not invent guarantees of actual recycling facilities.
            """;

        Map<String, Object> partText = Map.of("text", systemPrompt);
        Map<String, Object> inlineData = Map.of(
            "mime_type", mimeType,
            "data", base64Image
        );
        Map<String, Object> partImage = Map.of("inline_data", inlineData);

        Map<String, Object> content = Map.of("parts", List.of(partText, partImage));
        Map<String, Object> requestBody = Map.of("contents", List.of(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            WasteAnalysisResultDto result = parseGeminiResponse(response.getBody());
            log.info("Gemini Vision API ({}) successfully identified: {}", model, result.getItemName());
            return result;
        } else {
            throw new RuntimeException("Gemini API returned HTTP " + response.getStatusCode());
        }
    }

    private WasteAnalysisResultDto parseGeminiResponse(String responseJson) throws Exception {
        JsonNode root = objectMapper.readTree(responseJson);
        JsonNode candidates = root.path("candidates");
        if (candidates.isArray() && !candidates.isEmpty()) {
            JsonNode textNode = candidates.get(0).path("content").path("parts").get(0).path("text");
            String rawText = textNode.asText().trim();

            // Strip markdown code fences if Gemini added them
            if (rawText.startsWith("```")) {
                rawText = rawText.replaceAll("^```[a-zA-Z]*\\n?", "").replaceAll("```$", "").trim();
            }

            JsonNode dataNode = objectMapper.readTree(rawText);
            WasteAnalysisResultDto dto = new WasteAnalysisResultDto();
            dto.setItemName(dataNode.path("itemName").asText("Identified Waste Item"));
            dto.setPredictedCategory(dataNode.path("predictedCategory").asText("General / non-recyclable waste"));
            dto.setAiExplanation(dataNode.path("aiExplanation").asText("AI analysis completed via Gemini Vision."));
            dto.setDisposalRecommendation(dataNode.path("disposalRecommendation").asText("Segregate according to local municipal guidelines."));
            dto.setReuseRecycleTips(dataNode.path("reuseRecycleTips").asText("Ensure item is clean and dry before segregation."));
            dto.setSafetyPrecautions(dataNode.path("safetyPrecautions").asText("Standard handling precautions apply."));
            dto.setConfidenceLevel(dataNode.path("confidenceLevel").asText("High"));
            dto.setUncertain(dataNode.path("uncertain").asBoolean(false));
            dto.setClarificationNeeded(dataNode.path("clarificationNeeded").asBoolean(false));
            dto.setDemoFallbackUsed(false);
            return dto;
        }
        throw new RuntimeException("Malformed response structure from Gemini API");
    }

    /**
     * Clearly labeled deterministic Demo Mode fallback.
     */
    public WasteAnalysisResultDto getDemoFallbackResult(String filename, String fallbackReason) {
        String lowerName = (filename != null) ? filename.toLowerCase() : "";

        WasteAnalysisResultDto dto = new WasteAnalysisResultDto();
        dto.setDemoFallbackUsed(true);

        if (lowerName.contains("bottle") || lowerName.contains("plastic") || lowerName.contains("pet")) {
            dto.setItemName("PET Plastic Beverage Container [DEMO]");
            dto.setPredictedCategory("Recyclable plastic");
            dto.setAiExplanation("DEMO MODE RESULT: Detected clear polyethylene terephthalate (PET #1) beverage container based on rule-based heuristics. " + fallbackReason);
            dto.setDisposalRecommendation("Empty all residual liquid, rinse briefly with clean water, compress container to conserve space, and place into your Blue Dry Recyclables bin.");
            dto.setReuseRecycleTips("Can be reused for household watering or sent to a local plastic processing center for transformation into recycled polyester.");
            dto.setSafetyPrecautions("Ensure container did not hold motor oil, pesticides, or corrosive chemicals.");
            dto.setConfidenceLevel("Medium (Demo Rule-based)");
            dto.setUncertain(false);
        } else if (lowerName.contains("battery") || lowerName.contains("cell") || lowerName.contains("duracell")) {
            dto.setItemName("Household AA/AAA Alkaline Battery [DEMO]");
            dto.setPredictedCategory("Batteries");
            dto.setAiExplanation("DEMO MODE RESULT: Detected cylindrical dry-cell battery. Batteries contain heavy metals that can leak into soil and groundwater. " + fallbackReason);
            dto.setDisposalRecommendation("DO NOT discard in general curbside waste. Tape contact terminals with electrical tape and deposit at an authorized e-waste or battery collection drop-box.");
            dto.setReuseRecycleTips("Consider switching to rechargeable NiMH or Lithium-ion cells to eliminate single-use battery waste.");
            dto.setSafetyPrecautions("CAUTION: Damaged or swollen batteries present chemical leakage and fire risks. Store in a cool, dry container away from metal objects.");
            dto.setConfidenceLevel("High (Demo Rule-based)");
            dto.setUncertain(false);
        } else if (lowerName.contains("box") || lowerName.contains("paper") || lowerName.contains("carton")) {
            dto.setItemName("Corrugated Cardboard Shipping Box [DEMO]");
            dto.setPredictedCategory("Paper and cardboard");
            dto.setAiExplanation("DEMO MODE RESULT: Detected corrugated paper packaging material. " + fallbackReason);
            dto.setDisposalRecommendation("Remove plastic packing tape and shipping labels if possible, flatten the box completely, and place in the dry paper recycling stream.");
            dto.setReuseRecycleTips("Excellent for storage, shipping reuse, or shredding for backyard composting.");
            dto.setSafetyPrecautions("Do not recycle if heavily contaminated with grease or food oil (e.g. pizza residue). Composting is preferred for soiled paper.");
            dto.setConfidenceLevel("High (Demo Rule-based)");
            dto.setUncertain(false);
        } else if (lowerName.contains("apple") || lowerName.contains("banana") || lowerName.contains("food") || lowerName.contains("peel") || lowerName.contains("organic")) {
            dto.setItemName("Organic Kitchen Food Scraps [DEMO]");
            dto.setPredictedCategory("Organic / wet waste");
            dto.setAiExplanation("DEMO MODE RESULT: Detected biodegradable household food waste suitable for organic composting. " + fallbackReason);
            dto.setDisposalRecommendation("Deposit in your Green Wet Waste bin or home compost tumbler. Keep free from plastic wraps and twist ties.");
            dto.setReuseRecycleTips("Turn into nutrient-rich vermicompost for home garden soil enrichment.");
            dto.setSafetyPrecautions("Segregate promptly to prevent anaerobic odors and insect attraction.");
            dto.setConfidenceLevel("High (Demo Rule-based)");
            dto.setUncertain(false);
        } else if (lowerName.contains("can") || lowerName.contains("tin") || lowerName.contains("metal") || lowerName.contains("foil")) {
            dto.setItemName("Aluminium Beverage Can [DEMO]");
            dto.setPredictedCategory("Metal");
            dto.setAiExplanation("DEMO MODE RESULT: Detected infinitely recyclable aluminium packaging. " + fallbackReason);
            dto.setDisposalRecommendation("Rinse remaining beverage residue, crush lightly, and deposit into your Dry Recyclable stream.");
            dto.setReuseRecycleTips("Recycling aluminium saves 95% of the energy needed to produce new aluminium from raw bauxite.");
            dto.setSafetyPrecautions("Watch out for sharp metal rim edges.");
            dto.setConfidenceLevel("High (Demo Rule-based)");
            dto.setUncertain(false);
        } else {
            dto.setItemName("Household Recyclable Packaging [DEMO]");
            dto.setPredictedCategory("Recyclable plastic");
            dto.setAiExplanation("DEMO MODE RESULT: Evaluated item using default offline demo rules. " + fallbackReason);
            dto.setDisposalRecommendation("Separate clean rigid plastics and paper products from wet kitchen waste. Deposit in dry recyclable collection.");
            dto.setReuseRecycleTips("Inspect resin identification code (#1 through #7) to verify municipal collection compatibility.");
            dto.setSafetyPrecautions("Wash and dry to prevent contamination of other recyclable materials.");
            dto.setConfidenceLevel("Medium (Demo Rule-based)");
            dto.setUncertain(false);
        }

        return dto;
    }
}
