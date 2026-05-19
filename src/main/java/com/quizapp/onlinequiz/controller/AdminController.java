package com.quizapp.onlinequiz.controller;

import com.quizapp.onlinequiz.model.Quiz;
import com.quizapp.onlinequiz.model.User;
import com.quizapp.onlinequiz.repository.QuizRepository;
import com.quizapp.onlinequiz.repository.UserRepository;
import com.quizapp.onlinequiz.model.QuizAttempt;
import com.quizapp.onlinequiz.repository.QuizAttemptRepository;
import com.quizapp.onlinequiz.service.UserService;
import com.quizapp.onlinequiz.dto.CreateQuizRequest;
import com.quizapp.onlinequiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final QuizService quizService;
    private final UserService userService;
    private final QuizAttemptRepository quizAttemptRepository;

    // Tüm kullanıcıları ve skorlarını getirir
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // Belirli bir kullanıcının detaylı sınav geçmişini getirir
    @GetMapping("/users/{id}/history")
    public ResponseEntity<List<QuizAttempt>> getUserHistory(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(quizAttemptRepository.findByUserIdOrderByAttemptDateDesc(id));
    }

    // Kullanıcı silme (ve geçmişini)
    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("Kullanıcı bulunamadı!");
        }
        userService.deleteUserById(id);
        return ResponseEntity.ok("Kullanıcı başarıyla silindi.");
    }

    // Quiz silme
    @DeleteMapping("/quizzes/{id}")
    public ResponseEntity<String> deleteQuiz(@PathVariable Long id) {
        if (!quizRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("Quiz bulunamadı!");
        }
        quizRepository.deleteById(id);
        return ResponseEntity.ok("Quiz başarıyla silindi.");
    }

    // Quiz düzenleme/güncelleme
    @PutMapping("/quizzes/{id}")
    public ResponseEntity<String> updateQuiz(@PathVariable Long id, @RequestBody CreateQuizRequest request) {
        if (!quizRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("Quiz bulunamadı!");
        }
        quizService.updateQuiz(id, request);
        return ResponseEntity.ok("Quiz başarıyla güncellendi.");
    }
}
