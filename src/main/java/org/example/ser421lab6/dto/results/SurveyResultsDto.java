package org.example.ser421lab6.dto.results;

import java.util.List;

public record SurveyResultsDto(
        Long SurveyId,
        String surveyTitle,
        int totalResponses,
        List<QuestionResultsDto> questions
) {}
