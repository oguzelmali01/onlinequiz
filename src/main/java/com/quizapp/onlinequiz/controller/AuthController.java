package com.quizapp.onlinequiz.controller;

import com.quizapp.onlinequiz.dto.LoginRequest;
import com.quizapp.onlinequiz.dto.RegisterRequest;
import com.quizapp.onlinequiz.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        userService.registerUser(request);
        return ResponseEntity.ok("Kullanıcı kaydı başarıyla oluşturuldu!");
    }

    // YENİ EKLENDİ: Giriş Yapma Uç Noktası
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        String token = userService.loginUser(request);
        // Başarılı giriş yaparsa token'ı geri döndür
        return ResponseEntity.ok(token);
    }
}