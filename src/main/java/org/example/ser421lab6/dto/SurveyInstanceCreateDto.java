package org.example.ser421lab6.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SurveyInstanceCreateDto {
    private String user;
    private Long surveyId;
}
