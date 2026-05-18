package com.quizapp.onlinequiz.controller;

import com.quizapp.onlinequiz.dto.CreateQuizRequest;
import com.quizapp.onlinequiz.dto.QuizSubmitRequest;
import com.quizapp.onlinequiz.model.Quiz;
import com.quizapp.onlinequiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    // Quiz Oluşturma (POST)
    @PostMapping
    public ResponseEntity<String> createQuiz(@RequestBody CreateQuizRequest request) {
        quizService.createQuiz(request);
        return ResponseEntity.ok("Quiz ve sorular başarıyla kaydedildi!");
    }

    // Tüm Quiz'leri Getirme (GET)
    @GetMapping
    public ResponseEntity<List<Quiz>> getAllQuizzes() {
        return ResponseEntity.ok(quizService.getAllQuizzes());
    }

    // Belirli bir Quiz'i Getirme (GET)
    @GetMapping("/{id}")
    public ResponseEntity<Quiz> getQuizById(@PathVariable Long id) {
        return ResponseEntity.ok(quizService.getQuizById(id));
    }

    // Quiz Cevaplarını Gönderme ve Skor Hesaplama (POST)
    @PostMapping("/{id}/submit")
    public ResponseEntity<String> submitQuiz(@PathVariable Long id, @RequestBody QuizSubmitRequest request) {
        String result = quizService.submitQuiz(id, request);
        return ResponseEntity.ok(result);
    }
}