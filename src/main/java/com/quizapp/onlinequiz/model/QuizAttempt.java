package com.quizapp.onlinequiz.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Veritabanındaki 'quiz_attempts' tablosunu temsil eden JPA Entity (Varlık) sınıfı.
 * Kullanıcıların girdiği sınavların (denemelerin) kayıtlarını, puanlarını ve tarihlerini tutar.
 */
@Entity
@Table(name = "quiz_attempts")
@Data
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Sınava giren kullanıcı. 
     * JsonIgnore sayesinde sonsuz döngü (infinite recursion) engellenmiştir.
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Denemenin yapıldığı sınav (Quiz) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    /** Bu denemeden kazanılan toplam puan */
    private Integer score;
    
    /** Doğru cevaplanan soru sayısı */
    private Integer correctAnswers;
    
    /** Sınavdaki toplam soru sayısı */
    private Integer totalQuestions;
    
    /** Denemenin gerçekleştirildiği tarih ve saat */
    private LocalDateTime attemptDate;
}
