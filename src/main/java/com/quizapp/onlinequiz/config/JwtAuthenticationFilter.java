package com.quizapp.onlinequiz.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final com.quizapp.onlinequiz.repository.UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String path = request.getServletPath();

        // 1. ADIM: Eğer istek giriş veya kayıt yoluna gidiyorsa, filtreyi tamamen pas geç
        if (path.contains("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. ADIM: Header kontrolü - Eğer Token yoksa veya "Bearer " ile başlamıyorsa devam et
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. ADIM: Token'ı doğrula ve kullanıcıyı içeri al
        try {
            String jwt = authHeader.substring(7);
            String username = jwtService.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtService.isTokenValid(jwt)) {
                    // YENİ: Veritabanından rolü çek
                    com.quizapp.onlinequiz.model.User user = userRepository.findByUsername(username).orElse(null);
                    
                    if (user != null) {
                        java.util.List<org.springframework.security.core.authority.SimpleGrantedAuthority> authorities = new ArrayList<>();
                        authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase()));
                        
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                username, null, authorities
                        );
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }
        } catch (Exception e) {
            // Token hatalıysa hata fırlatma, sadece logla ve devam et (Spring Security 403 basacaktır)
            System.out.println("JWT Doğrulama Hatası: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}