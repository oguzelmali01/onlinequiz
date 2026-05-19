package com.quizapp.onlinequiz.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Veritabanındaki 'questions' tablosunu temsil eden JPA Entity (Varlık) sınıfı.
 * Bir sınava (Quiz) ait çoktan seçmeli bir soruyu temsil eder.
 */
@Entity
@Table(name = "questions")
@Data
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Sorunun metin içeriği */
    @Column(nullable = false)
    private String text;

    /** Seçenekler (Şıklar) */
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;

    /** 
     * Doğru cevabı tutan alan. 
     * Genellikle "A", "B", "C" veya "D" gibi bir harf değeri içerir.
     */
    @Column(nullable = false)
    private String correctAnswer;
}