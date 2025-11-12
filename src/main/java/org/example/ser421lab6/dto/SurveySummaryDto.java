package org.example.ser421lab6.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Data
public class SurveySummaryDto {
    private Long id;
    private String title;
    private String state;
}
