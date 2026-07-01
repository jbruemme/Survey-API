package org.example.ser421lab6.dto.results;

import java.util.List;

public record QuestionResultsDto(
        Long questionId,
        String question,
        String correctAnswer,
        int totalAnswers,
        List<AnswerResultsDto> answers
) {}
