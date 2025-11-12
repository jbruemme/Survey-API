package org.example.ser421lab6.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SurveyItemDto {
    private Long id;
    private String question;
    private List<String> options;
    private String correctAnswer;
}
