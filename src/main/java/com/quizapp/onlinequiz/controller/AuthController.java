package com.quizapp.onlinequiz.controller;

import com.quizapp.onlinequiz.dto.LoginRequest;
import com.quizapp.onlinequiz.dto.RegisterRequest;
import com.quizapp.onlinequiz.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Kullanıcı kimlik doğrulama işlemlerini (Kayıt Olma ve Giriş Yapma) yönetir.
 * SecurityConfig tarafından şifresiz (public) erişime açık bırakılmıştır.
 */
@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * Yeni bir kullanıcı kaydı oluşturur.
     * 
     * @param request Kullanıcı adı ve şifre bilgilerini içeren DTO
     * @return Başarılı kayıt mesajı
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        userService.registerUser(request);
        return ResponseEntity.ok("Kullanıcı kaydı başarıyla oluşturuldu!");
    }

    /**
     * Kullanıcı girişi işlemini doğrular ve geçerli bir JWT (JSON Web Token) üretir.
     * 
     * @param request Kullanıcı adı ve şifre bilgilerini içeren DTO
     * @return Başarılı girişte üretilen kimlik doğrulama token'ı
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        String token = userService.loginUser(request);
        return ResponseEntity.ok(token);
    }
}