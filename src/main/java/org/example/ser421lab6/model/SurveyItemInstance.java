package org.example.ser421lab6.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyItemInstance {
    private Long id;
    private SurveyItem surveyItem;
    private String userAnswer;
    private boolean correct;
}