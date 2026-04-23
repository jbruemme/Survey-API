package org.example.ser421lab6.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SurveyShareDto {
    private String publicUrl;
    private String twitterShareUrl;
    private String linkedInShareUrl;
    private String facebookShareUrl;
}
