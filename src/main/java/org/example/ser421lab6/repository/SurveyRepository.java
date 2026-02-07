package org.example.ser421lab6.repository;

import org.example.ser421lab6.entity.SurveyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyRepository extends JpaRepository<SurveyEntity, Long> {

}
