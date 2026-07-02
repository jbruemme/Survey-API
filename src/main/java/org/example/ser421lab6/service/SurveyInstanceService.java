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

import org.example.ser421lab6.exception.SurveyNotFoundException;
import org.example.ser421lab6.exception.InvalidSurveyVisibilityException;

import java.util.ArrayList;
import java.util.Comparator;
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
        SurveyInstanceEntity surveyInstance = surveyInstanceRepository.findById(id)
                .orElseThrow(() -> new SurveyNotFoundException("Survey instance with ID " + id + " does not exist;"));

        return toSurveyInstanceDto(surveyInstance);
    }

    /**
     * Function to initiate a survey instance
     * @param surveyId The ID of the survey being used for the instance
     * @return A new survey instance
     */
    @Transactional
    public SurveyInstanceDto createSurveyInstance(Long surveyId) {

        SurveyEntity survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new SurveyNotFoundException("Survey with id: " + surveyId + " does not exist;"));

        if ( survey.getState() == SurveyEntity.SurveyState.DELETED) {
            throw new InvalidSurveyVisibilityException("Survey not found.");
        }

        if (survey.getVisibility() == SurveyEntity.SurveyVisibility.PRIVATE) {
            throw new InvalidSurveyVisibilityException("This survye is private.");
        }

        // Create survey instance
        SurveyInstanceEntity surveyInstance = new SurveyInstanceEntity();
        surveyInstance.setUser(null);
        surveyInstance.setSurvey(survey);
        surveyInstance.setState(SurveyInstanceState.CREATED);

        // Create survey item instances
        List<SurveyItemInstanceEntity> itemInstances = new ArrayList<>();
        for (var item : survey.getItems()) {
            SurveyItemInstanceEntity instanceItem = new SurveyItemInstanceEntity();
            instanceItem.setSurveyInstance(surveyInstance);
            instanceItem.setSurveyItem(item);
            instanceItem.setUserAnswer(null);
            instanceItem.setCorrect(null);
            itemInstances.add(instanceItem);
        }

        // Add survey item instances to the survey instance
        surveyInstance.setItemInstances(itemInstances);

        // Save and return the survey instance
        SurveyInstanceEntity savedSurveyInstance = surveyInstanceRepository.save(surveyInstance);
        return toSurveyInstanceDto(savedSurveyInstance);
    }

    /**
     * Function to answer a survey item for a specific survey instance
     * @param request User answer request
     * @return Updated survey instance
     */
    @Transactional
    public SurveyInstanceDto answerSurveyItem(SubmitAnswerDto request) {
        SurveyInstanceEntity surveyInstance = surveyInstanceRepository
                .findById(request.getSurveyInstanceId())
                .orElseThrow(() ->
                        new SurveyNotFoundException(
                                "Survey instance with ID " + request.getSurveyInstanceId() + " does not exist."
                        )
                );

        if (surveyInstance.getState() == SurveyInstanceEntity.SurveyInstanceState.COMPLETED) {
            throw new IllegalArgumentException("This survey has already been completed.");
        }

        SurveyItemInstanceEntity itemInstance = surveyInstance.getItemInstances()
                .stream()
                .filter(ii -> ii.getId().equals(request.getSurveyItemInstanceId()))
                .findFirst()
                .orElseThrow(() -> new SurveyNotFoundException("Survey item instance not found."));

        String answer = request.getAnswer();

        if (answer == null || answer.isBlank()) {
            throw new IllegalArgumentException("Answer is required.");
        }

        itemInstance.setUserAnswer(answer);

        String correctAnswer = itemInstance.getSurveyItem().getCorrectAnswer();

        if (correctAnswer == null || correctAnswer.isBlank()) {
            itemInstance.setCorrect(null);
        } else {
            itemInstance.setCorrect(answer.equals(correctAnswer));
        }

        boolean allAnswered = surveyInstance.getItemInstances()
                .stream()
                .allMatch(ii -> ii.getUserAnswer() != null && !ii.getUserAnswer().isBlank());

        if (allAnswered) {
            surveyInstance.setState(SurveyInstanceState.COMPLETED);
        } else {
            surveyInstance.setState(SurveyInstanceState.IN_PROGRESS);
        }

        SurveyInstanceEntity saved = surveyInstanceRepository.save(surveyInstance);
        return toSurveyInstanceDto(saved);
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
                surveyItemDtos,
                survey.getShareToken(),
                survey.getVisibility().name()
        );

        // Mapping survey instance items (User's answers)
        List<SurveyItemInstanceDto> itemInstanceDtos = surveyInstance.getItemInstances().stream()
                .sorted(Comparator.comparing(SurveyItemInstanceEntity::getId))
                .map(ii -> new SurveyItemInstanceDto(
                        ii.getId(),
                        ii.getSurveyItem().getId(),
                        ii.getSurveyItem().getQuestion(),
                        ii.getUserAnswer(),
                        ii.getCorrect()
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