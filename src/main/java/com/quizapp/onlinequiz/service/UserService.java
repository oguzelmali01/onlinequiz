package com.quizapp.onlinequiz.service;

import com.quizapp.onlinequiz.config.JwtService;
import com.quizapp.onlinequiz.dto.LoginRequest;
import com.quizapp.onlinequiz.dto.RegisterRequest;
import com.quizapp.onlinequiz.model.User;
import com.quizapp.onlinequiz.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService; // YENİ EKLENDİ

    // Kayıt Olma Metodu (Aynı kalıyor)
    public User registerUser(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Bu kullanıcı adı zaten kullanılıyor!");
        }

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRole("USER");
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
}