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

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    // Yeni Quiz Oluşturma
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

    // Quiz Güncelleme
    public Quiz updateQuiz(Long id, CreateQuizRequest request) {
        Quiz quiz = quizRepository.findById(id).orElseThrow(() -> new RuntimeException("Quiz bulunamadı!"));
        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setCategory(request.getCategory());
        quiz.setTimeLimitSeconds(request.getTimeLimitSeconds());

        // Eski soruları temizle, yenilerini ekle (Basit bir çözüm)
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

    // Tüm Quiz'leri Listeleme (React ana sayfada gösterecek)
    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }

    // Tek bir Quiz'i ID'sine göre getirme (Detay/Çözme ekranı için)
    public Quiz getQuizById(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz bulunamadı!"));
    }

    // Quiz Sonuçlarını Hesaplama ve Skoru Kullanıcıya Ekleme
    public String submitQuiz(Long quizId, QuizSubmitRequest request) {
        Quiz quiz = getQuizById(quizId);
        
        // Güvenlik bağlamından (SecurityContext) o anki giriş yapmış kullanıcının adını al
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));

        int correctAnswersCount = 0;

        // Kullanıcının gönderdiği cevapları döngüyle kontrol ediyoruz
        if (request.getAnswers() != null) {
            for (AnswerRequest answer : request.getAnswers()) {
                // Quiz içindeki doğru soruyu buluyoruz
                for (Question question : quiz.getQuestions()) {
                    if (question.getId().equals(answer.getQuestionId())) {
                        // Eğer kullanıcının şıkkı doğru cevaba eşitse doğru sayısını artır
                        if (question.getCorrectAnswer().equalsIgnoreCase(answer.getSelectedOption())) {
                            correctAnswersCount++;
                        }
                    }
                }
            }
        }

        // Her doğru cevap için kullanıcıya 10 puan verelim
        int earnedScore = correctAnswersCount * 10;

        // YENİ MANTIK: Kullanıcının bu sınavdaki önceki maksimum puanını bul
        List<QuizAttempt> previousAttempts = quizAttemptRepository.findByUserIdAndQuizId(user.getId(), quiz.getId());
        int previousMaxScore = 0;
        for (QuizAttempt pa : previousAttempts) {
            if (pa.getScore() > previousMaxScore) {
                previousMaxScore = pa.getScore();
            }
        }

        // Eğer yeni alınan puan önceki maksimum puandan yüksekse, aradaki farkı genel puana ekle
        if (earnedScore > previousMaxScore) {
            int scoreDifference = earnedScore - previousMaxScore;
            user.setTotalScore(user.getTotalScore() + scoreDifference);
            userRepository.save(user); // Kullanıcının yeni toplam skorunu veritabanına kaydet
        }

        // Yeni: Quiz Attempt Kaydı (Geçmiş için)
        QuizAttempt attempt = new QuizAttempt();
        attempt.setUser(user);
        attempt.setQuiz(quiz);
        attempt.setScore(earnedScore);
        attempt.setCorrectAnswers(correctAnswersCount);
        attempt.setTotalQuestions(quiz.getQuestions().size());
        attempt.setAttemptDate(LocalDateTime.now());
        quizAttemptRepository.save(attempt);

        if (earnedScore > previousMaxScore) {
            return "Quiz tamamlandı! Doğru Sayısı: " + correctAnswersCount + "/" + quiz.getQuestions().size() + ". Kazanılan Toplam Puan: " + earnedScore + " (Genel skorunuza +" + (earnedScore - previousMaxScore) + " eklendi)";
        } else {
            return "Quiz tamamlandı! Doğru Sayısı: " + correctAnswersCount + "/" + quiz.getQuestions().size() + ". Puanınız: " + earnedScore + " (Önceki rekorunuzu geçemediğiniz için genel skorunuza puan eklenmedi).";
        }
    }
}