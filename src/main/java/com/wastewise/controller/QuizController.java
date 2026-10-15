package com.wastewise.controller;

import com.wastewise.entity.QuizAttempt;
import com.wastewise.entity.User;
import com.wastewise.service.QuizService;
import com.wastewise.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/quiz")
public class QuizController {

    private final QuizService quizService;
    private final UserService userService;

    public QuizController(QuizService quizService, UserService userService) {
        this.quizService = quizService;
        this.userService = userService;
    }

    @GetMapping
    public String showQuiz(Model model, Authentication auth) {
        User user = userService.resolveUser(auth);
        List<QuizService.QuizQuestion> questions = quizService.getSdg12QuizQuestions();

        model.addAttribute("currentUser", user);
        model.addAttribute("questions", questions);
        return "quiz";
    }

    @PostMapping("/submit")
    public String submitQuiz(@RequestParam Map<String, String> allParams,
                             Model model,
                             Authentication auth) {
        User user = userService.resolveUser(auth);
        Map<Integer, Integer> answers = new HashMap<>();

        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (entry.getKey().startsWith("q_")) {
                try {
                    int qId = Integer.parseInt(entry.getKey().substring(2));
                    int chosenOption = Integer.parseInt(entry.getValue());
                    answers.put(qId, chosenOption);
                } catch (NumberFormatException ignored) {}
            }
        }

        QuizAttempt attempt = quizService.evaluateQuiz(user, answers);
        List<QuizService.QuizQuestion> questions = quizService.getSdg12QuizQuestions();

        model.addAttribute("currentUser", user);
        model.addAttribute("attempt", attempt);
        model.addAttribute("questions", questions);
        model.addAttribute("userAnswers", answers);

        return "quiz-result";
    }
}
