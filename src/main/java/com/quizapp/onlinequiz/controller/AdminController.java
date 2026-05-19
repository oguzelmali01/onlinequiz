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

/**
 * Yalnızca yönetici (ADMIN) rolüne sahip kullanıcıların erişebildiği endpointleri barındırır.
 * Kullanıcı yönetimi, sınav silme ve düzenleme işlemlerinden sorumludur.
 */
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

    /**
     * Sistemdeki tüm kullanıcıları ve skorlarını listeler.
     * 
     * @return Kullanıcıların listesi (List<User>)
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    /**
     * Belirli bir kullanıcının çözdüğü tüm sınavların detaylı geçmişini getirir.
     * 
     * @param id Geçmişi istenen kullanıcının ID'si
     * @return Sınav denemelerinin listesi (List<QuizAttempt>)
     */
    @GetMapping("/users/{id}/history")
    public ResponseEntity<List<QuizAttempt>> getUserHistory(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(quizAttemptRepository.findByUserIdOrderByAttemptDateDesc(id));
    }

    /**
     * Bir kullanıcıyı ve onunla ilişkili tüm sınav geçmişini (QuizAttempt) siler.
     * 
     * @param id Silinecek kullanıcının ID'si
     * @return İşlem başarı/hata mesajı
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("Kullanıcı bulunamadı!");
        }
        userService.deleteUserById(id);
        return ResponseEntity.ok("Kullanıcı başarıyla silindi.");
    }

    /**
     * Sistemden belirtilen ID'ye sahip sınavı (Quiz) siler.
     * 
     * @param id Silinecek sınavın ID'si
     * @return İşlem başarı/hata mesajı
     */
    @DeleteMapping("/quizzes/{id}")
    public ResponseEntity<String> deleteQuiz(@PathVariable Long id) {
        if (!quizRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("Quiz bulunamadı!");
        }
        quizRepository.deleteById(id);
        return ResponseEntity.ok("Quiz başarıyla silindi.");
    }

    /**
     * Mevcut bir sınavın başlık, kategori, açıklama, süre ve sorularını günceller.
     * 
     * @param id Güncellenecek sınavın ID'si
     * @param request Yeni sınav verilerini içeren DTO objesi
     * @return İşlem başarı/hata mesajı
     */
    @PutMapping("/quizzes/{id}")
    public ResponseEntity<String> updateQuiz(@PathVariable Long id, @RequestBody CreateQuizRequest request) {
        if (!quizRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("Quiz bulunamadı!");
        }
        quizService.updateQuiz(id, request);
        return ResponseEntity.ok("Quiz başarıyla güncellendi.");
    }
}
