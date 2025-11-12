package org.example.ser421lab6.service;

import org.example.ser421lab6.dto.SurveyCreateDto;
import org.example.ser421lab6.dto.SurveyDto;
import org.example.ser421lab6.dto.SurveyItemDto;
import org.example.ser421lab6.dto.SurveySummaryDto;
import org.example.ser421lab6.model.Survey;
import org.example.ser421lab6.model.SurveyItem;
import org.example.ser421lab6.storage.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SurveyService {
    private final DB db;

    @Autowired
    public SurveyService(DB db){
        this.db = db;
    }

    /**
     * Function to get all surveys and map them to SurveySummaryDto
     * @return List of SurveySummaryDto objects
     */
    public List<SurveySummaryDto> getAllSurveys() {
        return db.getSurveys().values().stream().map(this::toSummary).toList();
    }

    /**
     * Conversion logic for the necessary data from a survey to be mapped to the SurveySummaryDto
     * @param survey The survey being parsed
     * @return A summary of the survey
     */
    private SurveySummaryDto toSummary(Survey survey) {
        return new SurveySummaryDto(
                survey.getId(),
                survey.getTitle(),
                survey.getState().toString()
        );
    }

    /**
     * Function to retrieve a survey and its corresponding survey items based on a survey ID
     * @param id The id of the survey being searched for
     * @return Survey dto object with its corresponding survey items dto objects
     */
    public SurveyDto getSurveyById(Long id) {
        //Retrieve survey based on ID
        Survey survey = db.getSurveys().get(id);
        if(survey == null) return null;
        //Map survey items
        List<SurveyItemDto> items = survey.getItems().stream().map(surveyItem -> new SurveyItemDto(
                surveyItem.getId(),
                surveyItem.getQuestion(),
                surveyItem.getOptions(),
                surveyItem.getCorrectAnswer()
        )).toList();
        //Create the dto
        return new SurveyDto(
                survey.getId(),
                survey.getTitle(),
                survey.getState().toString(),
                items
        );
    }


    /**
     * Function to create a new survey
     * @param surveyCreateDto Incoming survey data to build az new survey
     * @return The created survey else i=IllegalArgumentException
     */
    public SurveyDto createSurvey(SurveyCreateDto surveyCreateDto) {
        if (surveyCreateDto.getTitle() == null || surveyCreateDto.getState() == null) {
            throw new IllegalArgumentException("Survey title and Survey state are required fields");
        }

        // Get survey item objects from survey item IDs
        List<SurveyItem> items = surveyCreateDto.getItemIds().stream()
                .map(id -> {
                    SurveyItem item = db.getSurveyItems().get(id);
                    if (item == null) throw new IllegalArgumentException("Invalid survey item ID: " + id);
                    return item;
                })
                .toList();

        // Create and save the new survey
        Survey survey = new Survey(
                db.generateSurveyId(),
                surveyCreateDto.getTitle(),
                Survey.SurveyState.valueOf(surveyCreateDto.getState().toUpperCase()),
                new ArrayList<>(items)
        );

        db.getSurveys().put(survey.getId(), survey);

        // Map all fields to surveyDto for response
        List<SurveyItemDto> itemDtos = items.stream()
                .map(item -> new SurveyItemDto(item.getId(), item.getQuestion(), item.getOptions(), item.getCorrectAnswer()))
                .toList();

        return new SurveyDto(survey.getId(), survey.getTitle(), survey.getState().toString(), itemDtos);
    }

    /**
     * Funcion to delete a survey
     * @param id The ID of the survey to be deleted
     * @return Survey summary with the updated survey status
     */
    public SurveySummaryDto deleteSurvey(Long id) {
        // Search for survey if null return IllegalArgument exception (Doesn't exist)
        Survey survey = db.getSurveys().get(id);
        if (survey == null) {
            throw new IllegalArgumentException("Survey with ID: " + id + " does not exist.");
        }
        //Mark survey as DELETED
        survey.setState(Survey.SurveyState.DELETED);

        //Return the DTO with updated survey status
        return new SurveySummaryDto(
                survey.getId(),
                survey.getTitle(),
                survey.getState().toString()
        );

    }
}