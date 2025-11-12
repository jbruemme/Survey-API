package org.example.ser421lab6.service;

import org.example.ser421lab6.dto.*;
import org.example.ser421lab6.model.Survey;
import org.example.ser421lab6.model.SurveyInstance;
import org.example.ser421lab6.model.SurveyItem;
import org.example.ser421lab6.model.SurveyItemInstance;
import org.example.ser421lab6.storage.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SurveyInstanceService {
    private final DB db;

    @Autowired
    public SurveyInstanceService(DB db) {
        this.db = db;
    }

    /**
     * Function to retrieve a survey instance and its corresponding survey instance items based on survey state. Provides
     * error handling for invalid state inputs.
     * @param state The state of the survey instance
     * @return List of all survey instances matching the state param
     */
    public List<SurveyInstanceSummaryDto> getSurveyInstancesByState(String state) {
        if(state != null) {
            try {
                SurveyInstance.SurveyState.valueOf(state.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Invalid state: " + state + ". (Valid states: CREATED, IN_PROGRESS, COMPLETED");
            }
        }
        return db.getSurveyInstances().values().stream()
                .filter(surveyInstances -> state == null || surveyInstances.getState().toString().equalsIgnoreCase(state))
                .map(surveyInstance -> new SurveyInstanceSummaryDto(
                        surveyInstance.getId(),
                        surveyInstance.getUser(),
                        surveyInstance.getSurvey().getTitle(),
                        surveyInstance.getState().toString()
                ))
                .toList();
    }

    /**
     * Function to retrieve survey instance by its ID
     * @param id The ID of the survey instance
     * @return The survey instance with its corresponding survey instance items
     */
    public SurveyInstanceDto getSurveyInstanceById(Long id) {
        SurveyInstance instance = db.getSurveyInstances().get(id);
        if (instance == null) {
            return null;
        }
        // Map SurveyItems to SurveyItemDto
        List<SurveyItemDto> surveyItemDtos = instance.getSurvey().getItems().stream()
                .map(item -> new SurveyItemDto(
                        item.getId(),
                        item.getQuestion(),
                        item.getOptions(),
                        item.getCorrectAnswer()
                ))
                .toList();

        SurveyDto surveyDto = new SurveyDto(
                instance.getSurvey().getId(),
                instance.getSurvey().getTitle(),
                instance.getSurvey().getState().toString(),
                surveyItemDtos
        );
        // Map SurveyItemInstances to SurveyItemInstanceDto
        List<SurveyItemInstanceDto> itemInstanceDtos = instance.getItemInstances().stream()
                .map(item -> new SurveyItemInstanceDto(
                        item.getId(),
                        item.getSurveyItem().getQuestion(),
                        item.getUserAnswer(),
                        item.isCorrect()
                ))
                .toList();
        // Build Survey Instance
        return new SurveyInstanceDto(
                instance.getId(),
                instance.getUser(),
                surveyDto,
                itemInstanceDtos,
                instance.getState().toString()
        );
    }

    /**
     * Function to initiate a survey instance
     * @param userName THe user taking the survey
     * @param surveyId The ID of the survey being used for the instance
     * @return A new survey instance
     */
    public SurveyInstanceDto createSurveyInstance(String userName, Long surveyId) {
        // Get survey from the DB
        Survey survey = db.getSurveys().get(surveyId);
        if (survey == null) {
            throw new IllegalArgumentException("Survey with ID " + surveyId + " does not exist.");
        }
        // Create a new survey instance
        SurveyInstance instance = new SurveyInstance(
                db.generateSurveyInstanceId(),
                userName,
                survey,
                new ArrayList<>(),
                SurveyInstance.SurveyState.CREATED
        );
        // Create survey item instances
        for (SurveyItem item : survey.getItems()) {
            instance.getItemInstances().add(
                    new SurveyItemInstance(
                            db.generateSurveyItemInstanceId(),
                            item,
                            null,
                            false
                    )
            );
        }
        // Map item instances into survey instance. Save and return the survey instance.
        db.getSurveyInstances().put(instance.getId(), instance);
        return new SurveyInstanceDto(
                instance.getId(),
                instance.getUser(),
                new SurveyDto(
                        survey.getId(),
                        survey.getTitle(),
                        survey.getState().toString(),
                        survey.getItems().stream()
                                .map(si -> new SurveyItemDto(
                                        si.getId(),
                                        si.getQuestion(),
                                        si.getOptions(),
                                        si.getCorrectAnswer()
                                ))
                                .toList()
                ),
                instance.getItemInstances().stream()
                        .map(ii -> new SurveyItemInstanceDto(
                                ii.getId(),
                                ii.getSurveyItem().getQuestion(),
                                ii.getUserAnswer(),
                                ii.isCorrect()
                        ))
                        .toList(),
                instance.getState().toString()
        );
    }

}