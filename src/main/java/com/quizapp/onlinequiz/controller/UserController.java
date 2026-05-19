package com.quizapp.onlinequiz.controller;

import com.quizapp.onlinequiz.model.User;
import com.quizapp.onlinequiz.model.QuizAttempt;
import com.quizapp.onlinequiz.service.UserService;
import com.quizapp.onlinequiz.repository.QuizAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final QuizAttemptRepository quizAttemptRepository;

    // O anki giriş yapan kullanıcının bilgilerini (Profil) getirir
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    // En yüksek puana sahip 10 kullanıcıyı getirir
    @GetMapping("/leaderboard")
    public ResponseEntity<List<User>> getLeaderboard() {
        return ResponseEntity.ok(userService.getLeaderboard());
    }

    // Giriş yapan kullanıcının geçmiş sınav sonuçlarını getirir
    @GetMapping("/my-history")
    public ResponseEntity<List<QuizAttempt>> getMyHistory() {
        User currentUser = userService.getCurrentUser();
        List<QuizAttempt> history = quizAttemptRepository.findByUserIdOrderByAttemptDateDesc(currentUser.getId());
        return ResponseEntity.ok(history);
    }
}
