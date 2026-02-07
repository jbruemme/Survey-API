package org.example.ser421lab6.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "survey_instances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyInstanceEntity {

    public enum SurveyInstanceState {
        CREATED, IN_PROGRESS, COMPLETED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "survey_id")
    private SurveyEntity survey;

    @OneToMany(
            mappedBy = "surveyInstance",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<SurveyItemInstanceEntity> itemInstances = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private SurveyInstanceState state = SurveyInstanceState.CREATED;
}
