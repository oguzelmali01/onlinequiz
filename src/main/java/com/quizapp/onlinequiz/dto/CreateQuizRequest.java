package com.quizapp.onlinequiz.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateQuizRequest {
    private String title;
    private String description;
    private String category;
    private Integer timeLimitSeconds;
    private List<QuestionRequest> questions;
}