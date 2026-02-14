package org.example.ser421lab6.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "surveys")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyEntity {

    public enum SurveyState {
        CREATED, COMPLETED, DELETED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Enumerated(EnumType.STRING)
    private SurveyState state = SurveyState.CREATED;

    @ManyToMany
    @JoinTable(
            name = "survey_survey_items",
            joinColumns = @JoinColumn(name = "survey_id"),
            inverseJoinColumns = @JoinColumn(name = "survey_item_id")
    )

    private List<SurveyItemEntity> items = new ArrayList<>();
}
