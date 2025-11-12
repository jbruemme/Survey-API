package org.example.ser421lab6.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Model class for creating/persisting a surveyItem
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyItem {
    private Long id;
    private String question;
    private List<String> options;
    private String correctAnswer;
}