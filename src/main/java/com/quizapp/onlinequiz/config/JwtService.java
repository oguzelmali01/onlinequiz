package com.quizapp.onlinequiz.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    // Token'ı şifrelemek için rastgele ve çok güvenli bir anahtar oluşturuyoruz
    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username) // Token'ın kime ait olduğu
                .setIssuedAt(new Date(System.currentTimeMillis())) // Veriliş tarihi (Şu an)
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // Bitiş tarihi (24 Saat geçerli)
                .signWith(key) // Anahtarımızla mühürlüyoruz
                .compact(); // Metne çevirip gönderiyoruz
    }
    // Token'ın içinden kullanıcı adını (username) çıkaran metot
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Token'ın sahte veya süresi geçmiş olup olmadığını kontrol eden metot
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true; // Token sağlamsa true döner
        } catch (Exception e) {
            return false; // Token bozuksa, sahteyse veya süresi dolmuşsa false döner
        }
    }
}