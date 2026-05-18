package com.quizapp.onlinequiz.repository;

import com.quizapp.onlinequiz.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
}