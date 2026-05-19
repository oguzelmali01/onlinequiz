package com.quizapp.onlinequiz.controller;

import com.quizapp.onlinequiz.dto.CreateQuizRequest;
import com.quizapp.onlinequiz.dto.QuizSubmitRequest;
import com.quizapp.onlinequiz.model.Quiz;
import com.quizapp.onlinequiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Sınav (Quiz) işlemleriyle ilgili genel uç noktaları barındırır.
 * Sınav listeleme, detay görüntüleme, yeni sınav ekleme ve sonuç gönderme işlemleri buradan yönetilir.
 */
@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    /**
     * Yeni bir sınav (Quiz) ve ona bağlı soruları oluşturur.
     * 
     * @param request Sınav başlığı, açıklaması, süresi ve soruları içeren veri transfer objesi (DTO)
     * @return Başarı veya hata mesajı
     */
    @PostMapping
    public ResponseEntity<String> createQuiz(@RequestBody CreateQuizRequest request) {
        quizService.createQuiz(request);
        return ResponseEntity.ok("Quiz ve sorular başarıyla kaydedildi!");
    }

    /**
     * Sistemdeki tüm sınavları getirir.
     * 
     * @return Sınavların listesi
     */
    @GetMapping
    public ResponseEntity<List<Quiz>> getAllQuizzes() {
        return ResponseEntity.ok(quizService.getAllQuizzes());
    }

    /**
     * Belirtilen ID'ye sahip sınavın detaylarını (sorular dahil) getirir.
     * 
     * @param id İstenen sınavın ID'si
     * @return Sınav detayları
     */
    @GetMapping("/{id}")
    public ResponseEntity<Quiz> getQuizById(@PathVariable Long id) {
        return ResponseEntity.ok(quizService.getQuizById(id));
    }

    /**
     * Kullanıcının çözdüğü sınavın cevaplarını alır, doğru/yanlış değerlendirmesi yapar
     * ve kazanılan puanı hesaplayıp kullanıcı hesabına ekler.
     * 
     * @param id Çözülen sınavın ID'si
     * @param request Kullanıcının sorulara verdiği cevapların listesi
     * @return Sınav sonucu (Örn: "2 doğru, 1 yanlış. Toplam Puan: 20")
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<String> submitQuiz(@PathVariable Long id, @RequestBody QuizSubmitRequest request) {
        String result = quizService.submitQuiz(id, request);
        return ResponseEntity.ok(result);
    }
}