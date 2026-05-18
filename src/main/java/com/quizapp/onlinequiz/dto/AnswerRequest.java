package com.quizapp.onlinequiz.dto;

import lombok.Data;

@Data
public class AnswerRequest {
    private Long questionId;
    private String selectedOption; // Kullanıcının seçtiği şık: "A", "B", "C" veya "D"
}