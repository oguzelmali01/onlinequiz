package com.quizapp.onlinequiz.dto;

import lombok.Data;
import java.util.List;

/**
 * İstemciden (React) yeni bir sınav oluşturmak veya güncellemek 
 * istendiğinde gönderilen verileri taşıyan DTO (Data Transfer Object).
 */
@Data
public class CreateQuizRequest {
    private String title;
    private String description;
    private String category;
    private Integer timeLimitSeconds;
    private List<QuestionRequest> questions;
}