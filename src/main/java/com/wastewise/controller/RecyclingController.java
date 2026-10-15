package com.wastewise.controller;

import com.wastewise.dto.RecyclingSubmissionDto;
import com.wastewise.entity.RecyclingSubmission;
import com.wastewise.entity.User;
import com.wastewise.service.RecyclingSubmissionService;
import com.wastewise.service.StorageService;
import com.wastewise.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/recycling")
public class RecyclingController {

    private final RecyclingSubmissionService recyclingSubmissionService;
    private final StorageService storageService;
    private final UserService userService;

    public RecyclingController(RecyclingSubmissionService recyclingSubmissionService,
                               StorageService storageService,
                               UserService userService) {
        this.recyclingSubmissionService = recyclingSubmissionService;
        this.storageService = storageService;
        this.userService = userService;
    }

    @GetMapping
    public String recyclingPage(Model model, Authentication auth) {
        User user = userService.resolveUser(auth);
        List<RecyclingSubmission> submissions = recyclingSubmissionService.getUserSubmissions(user);

        model.addAttribute("currentUser", user);
        model.addAttribute("submissions", submissions);
        model.addAttribute("submissionDto", new RecyclingSubmissionDto());
        return "recycling";
    }

    @PostMapping("/submit")
    public String submitRecycling(@Valid @ModelAttribute("submissionDto") RecyclingSubmissionDto dto,
                                  BindingResult bindingResult,
                                  @RequestParam(value = "photo", required = false) MultipartFile photo,
                                  Authentication auth,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        User user = userService.resolveUser(auth);

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentUser", user);
            model.addAttribute("submissions", recyclingSubmissionService.getUserSubmissions(user));
            return "recycling";
        }

        String photoPath = null;
        if (photo != null && !photo.isEmpty()) {
            try {
                photoPath = storageService.storeFile(photo);
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Error saving uploaded photo: " + e.getMessage());
                return "redirect:/recycling";
            }
        }

        recyclingSubmissionService.createSubmission(user, dto, photoPath);
        redirectAttributes.addFlashAttribute("successMessage",
                "Your recycling activity was submitted! It is now PENDING verification by municipal administrators to award 15 EcoPoints.");
        return "redirect:/recycling";
    }
}
