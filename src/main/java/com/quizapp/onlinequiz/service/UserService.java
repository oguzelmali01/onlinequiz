package com.quizapp.onlinequiz.service;

import com.quizapp.onlinequiz.config.JwtService;
import com.quizapp.onlinequiz.dto.LoginRequest;
import com.quizapp.onlinequiz.dto.RegisterRequest;
import com.quizapp.onlinequiz.model.User;
import com.quizapp.onlinequiz.repository.UserRepository;
import com.quizapp.onlinequiz.repository.QuizAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService; // YENİ EKLENDİ
    private final QuizAttemptRepository quizAttemptRepository;

    // Kayıt Olma Metodu (Aynı kalıyor)
    public User registerUser(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Bu kullanıcı adı zaten kullanılıyor!");
        }

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        
        // Basit bir admin mantığı: Kullanıcı adı 'admin' ise yetkisi ADMIN olsun
        if ("admin".equalsIgnoreCase(request.getUsername())) {
            newUser.setRole("ADMIN");
        } else {
            newUser.setRole("USER");
        }
        
        newUser.setTotalScore(0);

        return userRepository.save(newUser);
    }

    // YENİ: Giriş Yapma (Login) Metodu
    public String loginUser(LoginRequest request) {
        // 1. Kullanıcıyı veritabanında bul
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));

        // 2. Gelen şifre ile veritabanındaki şifrelenmiş (kriptolu) şifre eşleşiyor mu kontrol et
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Hatalı şifre!");
        }

        // 3. Şifre doğruysa, kullanıcıya özel Token üret ve gönder
        return jwtService.generateToken(user.getUsername());
    }

    // O anki giriş yapmış kullanıcıyı getirir
    public User getCurrentUser() {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));
    }

    // Liderlik tablosunu getirir (En yüksek puanlı 10 kullanıcı)
    public java.util.List<User> getLeaderboard() {
        return userRepository.findTop10ByOrderByTotalScoreDesc();
    }

    // O anki giriş yapmış kullanıcıyı siler
    public void deleteCurrentUser() {
        User currentUser = getCurrentUser();
        // Önce kullanıcının geçmiş sınav sonuçlarını sil (Foreign Key hatası almamak için)
        java.util.List<com.quizapp.onlinequiz.model.QuizAttempt> attempts = quizAttemptRepository.findByUserIdOrderByAttemptDateDesc(currentUser.getId());
        quizAttemptRepository.deleteAll(attempts);
        // Sonra kullanıcıyı sil
        userRepository.delete(currentUser);
    }

    // Adminin belirli bir kullanıcıyı silmesi
    public void deleteUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        // Önce kullanıcının geçmiş sınav sonuçlarını sil
        java.util.List<com.quizapp.onlinequiz.model.QuizAttempt> attempts = quizAttemptRepository.findByUserIdOrderByAttemptDateDesc(user.getId());
        quizAttemptRepository.deleteAll(attempts);
        // Sonra kullanıcıyı sil
        userRepository.delete(user);
    }
}