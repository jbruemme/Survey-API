package org.example.ser421lab6.repository;

import org.example.ser421lab6.entity.SurveyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SurveyRepository extends JpaRepository<SurveyEntity, Long> {

    Optional<SurveyEntity> findByShareToken(String shareToken);

}
