package org.example.ser421lab6.repository;

import org.example.ser421lab6.entity.SurveyEntity;
import org.example.ser421lab6.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SurveyRepository extends JpaRepository<SurveyEntity, Long> {

    Optional<SurveyEntity> findByShareToken(String shareToken);

    List<SurveyEntity> findByCreatorAndStateNot(
            UserEntity creator,
            SurveyEntity.SurveyState state
    );

    List<SurveyEntity> findByVisibilityAndStateNot(
            SurveyEntity.SurveyVisibility visibility,
            SurveyEntity.SurveyState state
    );

}
