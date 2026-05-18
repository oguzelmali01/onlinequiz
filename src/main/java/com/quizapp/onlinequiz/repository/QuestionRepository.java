package com.quizapp.onlinequiz.repository;

import com.quizapp.onlinequiz.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}