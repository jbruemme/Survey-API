package org.example.ser421lab6.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SurveyInstanceSummaryDto {
    private Long id;
    private String user;
    private String title;
    private String state;
}
