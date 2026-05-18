package com.quizapp.onlinequiz.service;

import com.quizapp.onlinequiz.dto.CreateQuizRequest;
import com.quizapp.onlinequiz.dto.QuestionRequest;
import com.quizapp.onlinequiz.dto.QuizSubmitRequest;
import com.quizapp.onlinequiz.dto.AnswerRequest;
import com.quizapp.onlinequiz.model.Question;
import com.quizapp.onlinequiz.model.Quiz;
import com.quizapp.onlinequiz.model.User;
import com.quizapp.onlinequiz.repository.QuizRepository;
import com.quizapp.onlinequiz.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;

    // Yeni Quiz Oluşturma
    public Quiz createQuiz(CreateQuizRequest request) {
        Quiz quiz = new Quiz();
        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());

        List<Question> questionList = new ArrayList<>();
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

        quiz.setQuestions(questionList);
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
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));

        int correctAnswersCount = 0;

        // Kullanıcının gönderdiği cevapları döngüyle kontrol ediyoruz
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

        // Her doğru cevap için kullanıcıya 10 puan verelim
        int earnedScore = correctAnswersCount * 10;
        user.setTotalScore(user.getTotalScore() + earnedScore);
        userRepository.save(user); // Kullanıcının yeni toplam skorunu veritabanına kaydet

        return "Quiz tamamlandı! Doğru Sayısı: " + correctAnswersCount + "/" + quiz.getQuestions().size() + ". Kazanılan Puan: " + earnedScore;
    }
}