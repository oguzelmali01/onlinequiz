package com.quizapp.onlinequiz.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Veritabanındaki 'users' tablosunu temsil eden JPA Entity (Varlık) sınıfı.
 * Sisteme kayıtlı her bir öğrenciyi/kullanıcıyı veya yöneticiyi temsil eder.
 */
@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Kullanıcının sisteme giriş yaparken kullandığı benzersiz (unique) takma adı */
    @Column(unique = true, nullable = false)
    private String username;

    /** Güvenlik için BCrypt ile şifrelenmiş (hash) şifre */
    @Column(nullable = false)
    private String password;

    /** Kullanıcının yetkisi. Genellikle 'USER' (Standart) veya 'ADMIN' (Yönetici) */
    private String role;

    /** Kullanıcının şimdiye kadar tüm sınavlardan kazandığı toplam (kümülatif) liderlik puanı */
    private Integer totalScore = 0;
}