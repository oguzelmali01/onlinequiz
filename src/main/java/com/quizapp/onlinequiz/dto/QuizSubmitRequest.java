package com.quizapp.onlinequiz.dto;

import lombok.Data;
import java.util.List;

@Data
public class QuizSubmitRequest {
    private Long userId; // Testi çözen kullanıcının ID'si
    private List<AnswerRequest> answers; // Kullanıcının verdiği cevapların listesi
}