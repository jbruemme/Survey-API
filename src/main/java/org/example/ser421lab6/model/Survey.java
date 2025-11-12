package org.example.ser421lab6.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class for creating/persisting a survey
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Survey {
    public enum SurveyState {
        CREATED,
        COMPLETED,
        DELETED
    }
    private Long id;
    private String title;
    private SurveyState state = SurveyState.CREATED;
    private List<SurveyItem> items = new ArrayList<>();
}