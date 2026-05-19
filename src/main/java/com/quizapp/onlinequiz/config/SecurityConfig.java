package com.quizapp.onlinequiz.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * Uygulamanın genel güvenlik (Spring Security) ayarlarını barındıran konfigürasyon sınıfı.
 * Hangi uç noktaların (endpoint) kimlere açık olacağı, şifreleme algoritmaları,
 * CORS ayarları ve JWT (JSON Web Token) filtrelerinin entegrasyonu burada yapılır.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    /**
     * Kullanıcı şifrelerini güvenli bir şekilde şifrelemek (hash) için BCrypt algoritmasını ayarlar.
     * 
     * @return BCryptPasswordEncoder instance'ı
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Güvenlik filtre zincirini (SecurityFilterChain) yapılandırır.
     * CSRF'i kapatır, state'i stateless (RESTful) yapar ve endpoint izinlerini tanımlar.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(org.springframework.security.config.Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**", "/h2-console/**", "/api/quizzes/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                // Sunucu tarafında oturum (session) tutulmayacağını belirtir (JWT mantığı)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // H2 Console'un iFrame içinde çalışabilmesi için frame seçeneklerini kapatır
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                // Gelen her isteği, kendi yazdığımız JWT Filtresinden geçmesi için ekler
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Farklı domainlerden (örneğin React frontend localhost:5173'den) gelen
     * CORS (Cross-Origin Resource Sharing) isteklerine izin vermek için gerekli yapılandırma.
     */
    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}