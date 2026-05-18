package com.quizapp.onlinequiz.controller;

import com.quizapp.onlinequiz.dto.RegisterRequest;
import com.quizapp.onlinequiz.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        userService.registerUser(request);
        return ResponseEntity.ok("Kullanıcı kaydı başarıyla oluşturuldu!");
    }
}