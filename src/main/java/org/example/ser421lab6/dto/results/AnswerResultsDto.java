package org.example.ser421lab6.dto.results;

public record AnswerResultsDto(
        String answer,
        int count,
        double percentage,
        Boolean correct
) {}
