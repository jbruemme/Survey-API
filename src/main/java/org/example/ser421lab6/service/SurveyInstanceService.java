package org.example.ser421lab6.service;

import lombok.RequiredArgsConstructor;
import org.example.ser421lab6.dto.*;
import org.example.ser421lab6.entity.SurveyEntity;
import org.example.ser421lab6.entity.SurveyInstanceEntity;
import org.example.ser421lab6.entity.SurveyInstanceEntity.SurveyInstanceState;
import org.example.ser421lab6.entity.SurveyItemInstanceEntity;
import org.example.ser421lab6.repository.SurveyInstanceRepository;
import org.example.ser421lab6.repository.SurveyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SurveyInstanceService {

    /*
    ===================================== Class Variables =======================================================
     */

    private final SurveyInstanceRepository surveyInstanceRepository;
    private final SurveyRepository surveyRepository;


    /*
    ===================================== Service Methods =======================================================
     */

    /**
     * Function to retrieve a survey instance and its corresponding survey instance items based on survey state. Provides
     * error handling for invalid state inputs.
     * @param state The state of the survey instance
     * @return List of all survey instances matching the state param
     */
    @Transactional(readOnly = true)
    public List<SurveyInstanceSummaryDto> getSurveyInstancesByState(String state) {

        // The list of survey instances
        List<SurveyInstanceEntity> surveyInstances;

        // Validate survey state check
        if(state == null || state.trim().isEmpty()) {
            surveyInstances = surveyInstanceRepository.findAll();
        } else {
            SurveyInstanceState parsedSurveyInstances;
            try {
                parsedSurveyInstances = SurveyInstanceState.valueOf(state.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid state: " + state + ". (Valid states: CREATED, IN_PROGRESS, COMPLETED)");
            }
            surveyInstances = surveyInstanceRepository.findByState(parsedSurveyInstances);
        }
        return surveyInstances.stream().map(this::toSummaryDto).toList();
    }

    /**
     * Function to retrieve survey instance by its ID
     * @param id The ID of the survey instance
     * @return The survey instance with its corresponding survey instance items
     */
    @Transactional(readOnly = true)
    public SurveyInstanceDto getSurveyInstanceById(Long id) {

        // Survey instance by corresponding id
        SurveyInstanceEntity surveyInstance = surveyInstanceRepository.findById(id).orElse(null);

        if (surveyInstance == null) {
            return null;
        }
        return toSurveyInstanceDto(surveyInstance);
    }

    /**
     * Function to initiate a survey instance
     * @param userName The user taking the survey
     * @param surveyId The ID of the survey being used for the instance
     * @return A new survey instance
     */
    @Transactional
    public SurveyInstanceDto createSurveyInstance(String userName, Long surveyId) {

        // Validate user and survey id
        if (userName == null || userName.trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be empty.");
        }

        SurveyEntity survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid survey id: " + surveyId));

        // Create survey instance
        SurveyInstanceEntity surveyInstance = new SurveyInstanceEntity();
        surveyInstance.setUser(userName);
        surveyInstance.setSurvey(survey);
        surveyInstance.setState(SurveyInstanceState.CREATED);

        // Create survey item instances
        List<SurveyItemInstanceEntity> itemInstances = new ArrayList<>();
        for (var item : survey.getItems()) {
            SurveyItemInstanceEntity instanceItem = new SurveyItemInstanceEntity();
            instanceItem.setSurveyInstance(surveyInstance);
            instanceItem.setSurveyItem(item);
            instanceItem.setUserAnswer(null);
            instanceItem.setCorrect(false);
            itemInstances.add(instanceItem);
        }

        // Add survey item instances to the survey instance
        surveyInstance.setItemInstances(itemInstances);

        // Save and return the survey instance
        SurveyInstanceEntity savedSurveyInstance = surveyInstanceRepository.save(surveyInstance);
        return toSurveyInstanceDto(savedSurveyInstance);
    }

    /*
    ===================================== Mapping Helper Methods =======================================================
     */

    /**
     * Helper function to map data from a survey instance to the SurveyInstanceSummaryDto
     * @param surveyInstance The survey instance being mapped
     * @return SurveyInstanceSummaryDto
     */
    private SurveyInstanceSummaryDto toSummaryDto(SurveyInstanceEntity surveyInstance) {
        return new SurveyInstanceSummaryDto(
                surveyInstance.getId(),
                surveyInstance.getUser(),
                surveyInstance.getSurvey().getTitle(),
                surveyInstance.getState().name()
        );
    }

    /**
     * Helper function to map data from a survey instance to the SurveyInstanceDto
     * @param surveyInstance The survey instance being mapped
     * @return SurveyInstanceDto
     */
    private SurveyInstanceDto toSurveyInstanceDto(SurveyInstanceEntity surveyInstance) {

        // Survey instance being mapped
        SurveyEntity survey = surveyInstance.getSurvey();

        // Mapping survey items (questions and possible answers)
        List<SurveyItemDto> surveyItemDtos = survey.getItems().stream()
                .map(item -> new SurveyItemDto(
                        item.getId(),
                        item.getQuestion(),
                        item.getOptions(),
                        item.getCorrectAnswer()
                ))
                .toList();

        // Wrapping in survey dto
        SurveyDto surveyDto = new SurveyDto(
                survey.getId(),
                survey.getTitle(),
                survey.getState().name(),
                surveyItemDtos
        );

        // Mapping survey instance items (User's answers)
        List<SurveyItemInstanceDto> itemInstanceDtos = surveyInstance.getItemInstances().stream()
                .map(ii -> new SurveyItemInstanceDto(
                        ii.getId(),
                        ii.getSurveyItem().getQuestion(),
                        ii.getUserAnswer(),
                        ii.isCorrect()
                ))
                .toList();

        // Wrapping everything in survey instance dto
        return new SurveyInstanceDto(
                surveyInstance.getId(),
                surveyInstance.getUser(),
                surveyDto,
                itemInstanceDtos,
                surveyInstance.getState().name()
        );
    }

}