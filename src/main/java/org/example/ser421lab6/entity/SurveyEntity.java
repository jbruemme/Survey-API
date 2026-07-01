package org.example.ser421lab6.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


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

    public enum SurveyVisibility {
        PUBLIC, UNLISTED, PRIVATE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Enumerated(EnumType.STRING)
    private SurveyState state = SurveyState.CREATED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SurveyVisibility visibility = SurveyVisibility.PRIVATE;

    @ManyToMany
    @JoinTable(
            name = "survey_survey_items",
            joinColumns = @JoinColumn(name = "survey_id"),
            inverseJoinColumns = @JoinColumn(name = "survey_item_id")
    )

    private List<SurveyItemEntity> items = new ArrayList<>();

    @Column(unique = true, nullable = false)
    private String shareToken;

    @PrePersist
    public void prePersist() {
        if (shareToken == null || shareToken.isBlank()) {
            shareToken = UUID.randomUUID().toString();
        }
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private UserEntity creator;


}
