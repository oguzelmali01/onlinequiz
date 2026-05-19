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

/**
 * Kullanıcı yönetimi ile ilgili temel iş mantıklarını (business logic) çalıştıran servis sınıfı.
 * Kayıt olma, giriş yapma, şifre doğrulama, token üretme ve kullanıcı verilerini
 * getirme/silme işlemleri bu sınıf üzerinden yürütülür.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final QuizAttemptRepository quizAttemptRepository;

    /**
     * Yeni bir kullanıcının sisteme kayıt işlemini gerçekleştirir.
     * Güvenlik gereği kullanıcı şifreleri veritabanına açık metin (plain text) olarak değil,
     * BCrypt algoritması ile şifrelenerek kaydedilir.
     * 
     * @param request Yeni kullanıcının bilgilerini içeren DTO
     * @return Kaydedilen kullanıcı objesi
     */
    public User registerUser(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Bu kullanıcı adı zaten kullanılıyor!");
        }

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        
        // Basit yetkilendirme mantığı: Kullanıcı adı 'admin' olan hesaplara doğrudan ADMIN rolü verilir.
        if ("admin".equalsIgnoreCase(request.getUsername())) {
            newUser.setRole("ADMIN");
        } else {
            newUser.setRole("USER");
        }
        
        newUser.setTotalScore(0);

        return userRepository.save(newUser);
    }

    /**
     * Kullanıcı giriş (Login) işlemlerini yönetir ve şifre doğrulamasını yapar.
     * 
     * @param request Kullanıcı adı ve şifre içeren istek
     * @return Doğrulama başarılı olursa üretilen JWT token dizesi
     */
    public String loginUser(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));

        // Girilen şifrenin, veritabanındaki kriptolu şifre ile eşleşip eşleşmediğini kontrol et
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Hatalı şifre!");
        }

        // Doğrulama başarılıysa Spring Security üzerinden işlem yapabilmek için JWT üret
        return jwtService.generateToken(user.getUsername());
    }

    /**
     * İstekte bulunan aktif (giriş yapmış) kullanıcıyı SecurityContext üzerinden getirir.
     * 
     * @return İstekte bulunan User objesi
     */
    public User getCurrentUser() {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));
    }

    /**
     * Sistemdeki tüm kullanıcıları toplam puanlarına göre azalan sırada sıralayıp ilk 10'unu getirir.
     * 
     * @return Liderlik tablosu kullanıcı listesi
     */
    public java.util.List<User> getLeaderboard() {
        return userRepository.findTop10ByOrderByTotalScoreDesc();
    }

    /**
     * İstekte bulunan aktif kullanıcının kendi hesabını ve ilgili tüm sınav geçmişini siler.
     * Veritabanında Foreign Key bütünlüğünü bozmamak için önce bağlı kayıtlar (QuizAttempt) silinir.
     */
    public void deleteCurrentUser() {
        User currentUser = getCurrentUser();
        // Foreign Key hatasını önlemek için kullanıcının deneme geçmişini (attempts) sil
        java.util.List<com.quizapp.onlinequiz.model.QuizAttempt> attempts = quizAttemptRepository.findByUserIdOrderByAttemptDateDesc(currentUser.getId());
        quizAttemptRepository.deleteAll(attempts);
        
        userRepository.delete(currentUser);
    }

    /**
     * Sadece yetkililerin (Admin) kullanabileceği, sistemdeki herhangi bir kullanıcıyı silme metodu.
     * 
     * @param id Silinecek kullanıcının ID'si
     */
    public void deleteUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        
        // Bağlı kayıtları temizle
        java.util.List<com.quizapp.onlinequiz.model.QuizAttempt> attempts = quizAttemptRepository.findByUserIdOrderByAttemptDateDesc(user.getId());
        quizAttemptRepository.deleteAll(attempts);
        
        userRepository.delete(user);
    }
}