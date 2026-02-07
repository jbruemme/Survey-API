package org.example.ser421lab6.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "survey_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String question;

    @ElementCollection
    @CollectionTable(
            name = "surv_item_options",
            joinColumns = @JoinColumn(name = "survey_item_id")
    )
    @Column(name = "option_value")
    private List<String> options = new ArrayList<>();

    private String correctAnswer;
}
