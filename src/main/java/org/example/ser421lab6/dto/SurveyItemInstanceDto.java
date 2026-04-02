package org.example.ser421lab6.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
@Getter
public class SurveyItemInstanceDto {
    private Long id;
    private Long surveyItemId;
    private String question;
    private String selectedAnswer;
    private Boolean correct;
}
