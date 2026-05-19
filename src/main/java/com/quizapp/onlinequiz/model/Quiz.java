package com.quizapp.onlinequiz.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

/**
 * Veritabanındaki 'quizzes' tablosunu temsil eden JPA Entity (Varlık) sınıfı.
 * Yöneticiler tarafından oluşturulan, kategorize edilmiş ve süre sınırı olabilen sınavları temsil eder.
 */
@Entity
@Table(name = "quizzes")
@Data
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Sınavın ana başlığı (Örn: "Temel Matematik") */
    @Column(nullable = false)
    private String title;

    /** Sınavın kısa açıklaması */
    private String description;

    /** 
     * Sınava ait soruların listesi.
     * Sınav silindiğinde veya güncellendiğinde bağlı olduğu tüm sorular da (CascadeType.ALL) etkilenir.
     */
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "quiz_id")
    private List<Question> questions;
    
    /** Sınavın ait olduğu kategori (Örn: "Genel Kültür", "Tarih") */
    private String category;
    
    /** Sınavı tamamlamak için verilen maksimum süre (Saniye cinsinden). Null/0 ise süre sınırı yoktur. */
    private Integer timeLimitSeconds;
}