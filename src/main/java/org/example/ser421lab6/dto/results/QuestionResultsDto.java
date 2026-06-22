package org.example.ser421lab6.dto.results;

import java.util.List;
import java.util.Map;

public record QuestionResultsDto(
        Long questionId,
        String question,
        String correctAnswer,
        int totalAnswers,
        List<AnswerResultsDto> answers
) {}
