package org.example.ser421lab6.service;

import lombok.RequiredArgsConstructor;
import org.example.ser421lab6.dto.SurveyCreateDto;
import org.example.ser421lab6.dto.SurveyDto;
import org.example.ser421lab6.dto.SurveySummaryDto;
import org.example.ser421lab6.entity.SurveyEntity;
import org.example.ser421lab6.entity.SurveyItemEntity;
import org.example.ser421lab6.repository.SurveyItemRepository;
import org.example.ser421lab6.repository.SurveyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SurveyService {

    /*
    ===================================== Class Variables =======================================================
     */

    private final SurveyRepository surveyRepository;
    private final SurveyItemRepository surveyItemRepository;

    /*
    ===================================== Service Methods =======================================================
     */

    /**
     * Function to get all surveys and map them to SurveySummaryDto
     * @return List of SurveySummaryDto objects
     */
    @Transactional(readOnly = true)
    public List<SurveySummaryDto> getAllSurveys() {
        return surveyRepository.findAll().stream().map(this::toSummaryDto).toList();
    }


    /**
     * Function to retrieve a survey and its corresponding survey items based on a survey ID
     * @param id The id of the survey being searched for
     * @return Survey dto object with its corresponding survey items dto objects
     */
    @Transactional(readOnly = true)
    public SurveyDto getSurveyById(Long id) {
        return surveyRepository.findById(id).map(this::toSurveyDto).orElse(null);
    }


    /**
     * Function to create a new survey
     * @param surveyCreateDto Incoming survey data to build a new survey
     * @return The created survey else IllegalArgumentException
     */
    @Transactional
    public SurveyDto createSurvey(SurveyCreateDto surveyCreateDto) {

        // Survey data validation checks
        if (surveyCreateDto == null) {
            throw new IllegalArgumentException("Survey cannot be null");
        }

        if (surveyCreateDto.getTitle() == null || surveyCreateDto.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Survey title is a required field");
        }

        if (surveyCreateDto.getState() == null || surveyCreateDto.getState().trim().isEmpty()) {
            throw new IllegalArgumentException("Survey state is a required field");
        }

        if (surveyCreateDto.getItemIds() == null || surveyCreateDto.getItemIds().isEmpty()) {
            throw new IllegalArgumentException("At least one survey item ID is required");
        }

        SurveyEntity.SurveyState surveyState;
        try {
            surveyState = SurveyEntity.SurveyState.valueOf(surveyCreateDto.getState().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid survey state: " + surveyCreateDto.getState());
        }


        // Survey item IDs validation, loads items once validated
        List<SurveyItemEntity> surveyItems = new ArrayList<>();
        for (Long itemId : surveyCreateDto.getItemIds()) {
            SurveyItemEntity item = surveyItemRepository.findById(itemId).orElseThrow(() ->
                        new IllegalArgumentException("Invalid survey item ID: " + itemId)
                    );
            surveyItems.add(item);
        }

        // Create and save the new survey
        SurveyEntity surveyEntity = new SurveyEntity();
        surveyEntity.setTitle(surveyCreateDto.getTitle());
        surveyEntity.setState(surveyState);
        surveyEntity.setItems(surveyItems);

        SurveyEntity savedSurvey = surveyRepository.save(surveyEntity);

        return toSurveyDto(savedSurvey);
    }

    /**
     * Function to soft-delete a survey
     * @param id The ID of the survey to be deleted
     * @return Survey summary with the updated survey status
     */
    @Transactional
    public SurveySummaryDto deleteSurvey(Long id) {

        // Search for survey
        SurveyEntity survey = surveyRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Survey with: " + id + " does not exist.")
                );

        //Mark survey as DELETED
        survey.setState(SurveyEntity.SurveyState.DELETED);
        SurveyEntity savedSurvey = surveyRepository.save(survey);

        return toSummaryDto(savedSurvey);

    }

    /*
    ===================================== Mapping Helper Methods =======================================================
     */

    /**
     * Helper function to map data from a survey to the SurveySummaryDto
     * @param survey The survey being mapped
     * @return SurveySummaryDto
     */
    private SurveySummaryDto toSummaryDto(SurveyEntity survey) {
        return new SurveySummaryDto(
                survey.getId(),
                survey.getTitle(),
                survey.getState().name()
        );
    }

    /**
     * Helper function to map data from a survey to the SurveyDto
     * @param survey The survey being mapped
     * @return SurveyDto
     */
    private SurveyDto toSurveyDto(SurveyEntity survey) {
        return new SurveyDto(
                survey.getId(),
                survey.getTitle(),
                survey.getState().name(),
                survey.getItems().stream().map(item ->
                        new org.example.ser421lab6.dto.SurveyItemDto(
                                item.getId(),
                                item.getQuestion(),
                                item.getOptions(),
                                item.getCorrectAnswer()
                        )
                ).toList()
        );
    }


}