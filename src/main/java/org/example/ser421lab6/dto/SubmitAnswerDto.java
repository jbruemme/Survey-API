package org.example.ser421lab6.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmitAnswerDto {
    private Long surveyInstanceId;
    private Long surveyItemInstanceId;
    private String answer;
}

