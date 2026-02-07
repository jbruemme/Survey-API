package org.example.ser421lab6.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "survey_item_instances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyItemInstanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Survey question that this instance is currently answering
    @ManyToOne(optional = false)
    @JoinColumn(name = "survey_item_id")
    private SurveyItemEntity surveyItem;

    // Survey instance that this survey item instance belongs to
    @ManyToOne(optional = false)
    @JoinColumn(name = "survey_instance_id")
    private SurveyInstanceEntity surveyInstance;

    private String userAnswer;

    private boolean correct;
}
