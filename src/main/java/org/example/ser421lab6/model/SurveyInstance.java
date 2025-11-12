package org.example.ser421lab6.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyInstance {
    public enum SurveyState {
        CREATED,
        IN_PROGRESS,
        COMPLETED
    }
    private Long id;
    private String user;
    private Survey survey;
    private List<SurveyItemInstance> itemInstances = new ArrayList<>();
    private SurveyState state;
}