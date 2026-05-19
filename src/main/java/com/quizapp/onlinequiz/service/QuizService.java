package com.quizapp.onlinequiz.service;

import com.quizapp.onlinequiz.dto.CreateQuizRequest;
import com.quizapp.onlinequiz.dto.QuestionRequest;
import com.quizapp.onlinequiz.dto.QuizSubmitRequest;
import com.quizapp.onlinequiz.dto.AnswerRequest;
import com.quizapp.onlinequiz.model.Question;
import com.quizapp.onlinequiz.model.Quiz;
import com.quizapp.onlinequiz.model.QuizAttempt;
import com.quizapp.onlinequiz.model.User;
import com.quizapp.onlinequiz.repository.QuizRepository;
import com.quizapp.onlinequiz.repository.UserRepository;
import com.quizapp.onlinequiz.repository.QuizAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Sınav (Quiz) ile ilgili temel iş mantıklarını (business logic) yöneten servis sınıfı.
 * Yeni sınav ekleme, güncelleme, listeleme ve özellikle sınav sonuçlarının 
 * değerlendirilip puan hesaplamalarının yapılması bu sınıfta gerçekleşir.
 */
@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    /**
     * Yönetici tarafından gönderilen verilere göre yeni bir sınav (Quiz) ve ona bağlı soruları (Question) oluşturur.
     * 
     * @param request Yeni sınavın başlık, kategori ve soru listesini barındıran obje
     * @return Veritabanına kaydedilen Quiz objesi
     */
    public Quiz createQuiz(CreateQuizRequest request) {
        Quiz quiz = new Quiz();
        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setCategory(request.getCategory());
        quiz.setTimeLimitSeconds(request.getTimeLimitSeconds());

        List<Question> questionList = new ArrayList<>();
        if (request.getQuestions() != null) {
            for (QuestionRequest qReq : request.getQuestions()) {
                Question question = new Question();
                question.setText(qReq.getText());
                question.setOptionA(qReq.getOptionA());
                question.setOptionB(qReq.getOptionB());
                question.setOptionC(qReq.getOptionC());
                question.setOptionD(qReq.getOptionD());
                question.setCorrectAnswer(qReq.getCorrectAnswer());

                questionList.add(question);
            }
        }

        quiz.setQuestions(questionList);
        return quizRepository.save(quiz);
    }

    /**
     * Var olan bir sınavın bilgilerini ve soru listesini günceller.
     * Güncelleme sırasında eski soruları silip tamamen yeni gönderilen soruları kaydeder.
     * 
     * @param id Güncellenecek sınavın kimliği
     * @param request Yeni sınav bilgileri
     * @return Güncellenmiş ve veritabanına kaydedilmiş Quiz objesi
     */
    public Quiz updateQuiz(Long id, CreateQuizRequest request) {
        Quiz quiz = quizRepository.findById(id).orElseThrow(() -> new RuntimeException("Quiz bulunamadı!"));
        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setCategory(request.getCategory());
        quiz.setTimeLimitSeconds(request.getTimeLimitSeconds());

        // Eski soruları temizle, yenilerini ekle (Bağlı tablolar JPA tarafından yönetilir)
        quiz.getQuestions().clear();

        if (request.getQuestions() != null) {
            for (QuestionRequest qReq : request.getQuestions()) {
                Question question = new Question();
                question.setText(qReq.getText());
                question.setOptionA(qReq.getOptionA());
                question.setOptionB(qReq.getOptionB());
                question.setOptionC(qReq.getOptionC());
                question.setOptionD(qReq.getOptionD());
                question.setCorrectAnswer(qReq.getCorrectAnswer());
                quiz.getQuestions().add(question);
            }
        }
        return quizRepository.save(quiz);
    }

    /**
     * Sistemdeki kayıtlı tüm sınavları getirir.
     * 
     * @return Sınav listesi
     */
    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }

    /**
     * Belirtilen ID'ye ait sınavı getirir. Bulunamazsa çalışma zamanı hatası fırlatır.
     * 
     * @param id Sınav ID'si
     * @return Bulunan Quiz objesi
     */
    public Quiz getQuizById(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz bulunamadı!"));
    }

    /**
     * Kullanıcının sınava verdiği yanıtları doğrular, kazanılan puanı hesaplar ve kullanıcının genel
     * profil puanını (Liderlik tablosu için) adil bir şekilde günceller.
     * 
     * Puan Hesaplama Mantığı:
     * - Her doğru cevap 10 puandır.
     * - Kullanıcı aynı sınavı birden fazla kez çözebilir. 
     * - Genel skora sadece kullanıcının o sınavdaki 'en yüksek' performansı etki eder (Fark hesaplanır).
     * 
     * @param quizId Çözülen sınavın ID'si
     * @param request Kullanıcının sorulara verdiği cevaplar listesi
     * @return Kullanıcıya gösterilecek sonuç ve puan metni
     */
    public String submitQuiz(Long quizId, QuizSubmitRequest request) {
        Quiz quiz = getQuizById(quizId);
        
        // SecurityContext üzerinden o anki isteği yapan kimliği doğrulanmış kullanıcıyı al
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));

        int correctAnswersCount = 0;

        // Kullanıcının gönderdiği cevapları kontrol et
        if (request.getAnswers() != null) {
            for (AnswerRequest answer : request.getAnswers()) {
                for (Question question : quiz.getQuestions()) {
                    if (question.getId().equals(answer.getQuestionId())) {
                        if (question.getCorrectAnswer().equalsIgnoreCase(answer.getSelectedOption())) {
                            correctAnswersCount++;
                        }
                    }
                }
            }
        }

        // Toplam doğru sayısına göre puan hesaplama
        int earnedScore = correctAnswersCount * 10;

        // Kullanıcının bu sınavdaki geçmiş en yüksek puanını (rekounu) tespit etme
        List<QuizAttempt> previousAttempts = quizAttemptRepository.findByUserIdAndQuizId(user.getId(), quiz.getId());
        int previousMaxScore = 0;
        for (QuizAttempt pa : previousAttempts) {
            if (pa.getScore() > previousMaxScore) {
                previousMaxScore = pa.getScore();
            }
        }

        // Adil Puanlama: Eğer yeni alınan puan önceki rekorundan yüksekse, sadece aradaki "farkı" genel toplama ekle.
        if (earnedScore > previousMaxScore) {
            int scoreDifference = earnedScore - previousMaxScore;
            user.setTotalScore(user.getTotalScore() + scoreDifference);
            userRepository.save(user);
        }

        // Sınav geçmişini (QuizAttempt tablosu) kaydetme
        QuizAttempt attempt = new QuizAttempt();
        attempt.setUser(user);
        attempt.setQuiz(quiz);
        attempt.setScore(earnedScore);
        attempt.setCorrectAnswers(correctAnswersCount);
        attempt.setTotalQuestions(quiz.getQuestions().size());
        attempt.setAttemptDate(LocalDateTime.now());
        quizAttemptRepository.save(attempt);

        // Kullanıcı arayüzüne gönderilecek geri bildirim metninin oluşturulması
        if (earnedScore > previousMaxScore) {
            return "Quiz tamamlandı! Doğru Sayısı: " + correctAnswersCount + "/" + quiz.getQuestions().size() + ". Kazanılan Toplam Puan: " + earnedScore + " (Genel skorunuza +" + (earnedScore - previousMaxScore) + " eklendi)";
        } else {
            return "Quiz tamamlandı! Doğru Sayısı: " + correctAnswersCount + "/" + quiz.getQuestions().size() + ". Puanınız: " + earnedScore + " (Önceki rekorunuzu geçemediğiniz için genel skorunuza puan eklenmedi).";
        }
    }
}