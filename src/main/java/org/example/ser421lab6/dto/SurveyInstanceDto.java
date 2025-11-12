package org.example.ser421lab6.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SurveyInstanceDto {
    private Long id;
    private String user;
    private SurveyDto survey;
    private List<SurveyItemInstanceDto> itemInstances;
    private String state;
}
