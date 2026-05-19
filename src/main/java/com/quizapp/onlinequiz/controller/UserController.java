package com.quizapp.onlinequiz.controller;

import com.quizapp.onlinequiz.model.User;
import com.quizapp.onlinequiz.model.QuizAttempt;
import com.quizapp.onlinequiz.service.UserService;
import com.quizapp.onlinequiz.repository.QuizAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Sisteme giriş yapmış kullanıcıların kendi profilleri, geçmişleri ve liderlik tablosu 
 * gibi verilere erişmesini sağlayan uç noktaları barındırır.
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final QuizAttemptRepository quizAttemptRepository;

    /**
     * İstekte bulunan (JWT token'ı geçerli olan) aktif kullanıcının kendi profil bilgilerini getirir.
     * 
     * @return Kullanıcı objesi
     */
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    /**
     * Sistemdeki tüm kullanıcılar arasından en yüksek toplama puana sahip
     * ilk 10 kullanıcıyı azalan sırayla (descending) getirir.
     * 
     * @return Liderlik tablosundaki kullanıcıların listesi
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<List<User>> getLeaderboard() {
        return ResponseEntity.ok(userService.getLeaderboard());
    }

    /**
     * Aktif kullanıcının daha önce çözdüğü tüm sınavların listesini
     * en yeniden en eskiye doğru sıralanmış olarak getirir.
     * 
     * @return Sınav deneme (QuizAttempt) geçmişi
     */
    @GetMapping("/my-history")
    public ResponseEntity<List<QuizAttempt>> getMyHistory() {
        User currentUser = userService.getCurrentUser();
        List<QuizAttempt> history = quizAttemptRepository.findByUserIdOrderByAttemptDateDesc(currentUser.getId());
        return ResponseEntity.ok(history);
    }

    /**
     * Aktif kullanıcının kendi hesabını ve ilişkili tüm sınav geçmişini sistemden kalıcı olarak siler.
     * 
     * @return İşlem başarı mesajı
     */
    @DeleteMapping("/me")
    public ResponseEntity<String> deleteMyProfile() {
        userService.deleteCurrentUser();
        return ResponseEntity.ok("Hesap başarıyla silindi.");
    }
}
