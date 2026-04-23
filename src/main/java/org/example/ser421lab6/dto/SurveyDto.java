package org.example.ser421lab6.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SurveyDto {
    private Long id;
    private String title;
    private String state;
    private List<SurveyItemDto> items;
    private String shareUrl;
}
