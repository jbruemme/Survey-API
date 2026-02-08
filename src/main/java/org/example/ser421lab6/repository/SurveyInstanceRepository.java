package org.example.ser421lab6.repository;

import org.example.ser421lab6.entity.SurveyInstanceEntity;
import org.example.ser421lab6.entity.SurveyInstanceEntity.SurveyInstanceState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurveyInstanceRepository extends JpaRepository<SurveyInstanceEntity, Long> {
    List<SurveyInstanceEntity> findByState(SurveyInstanceState state);
}
