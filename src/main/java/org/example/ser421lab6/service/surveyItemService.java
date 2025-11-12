package org.example.ser421lab6.service;

import org.example.ser421lab6.dto.SurveyItemDto;
import org.example.ser421lab6.dto.SurveyItemInstanceDto;
import org.example.ser421lab6.model.SurveyInstance;
import org.example.ser421lab6.model.SurveyItem;
import org.example.ser421lab6.model.SurveyItemInstance;
import org.example.ser421lab6.storage.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class surveyItemService {

    private final DB db;

    @Autowired
    public surveyItemService(DB db) {
        this.db = db;
    }

    /**
     * Function to create a new survey item and put it in the DB
     * @param surveyItemDto The incoming new survey item request
     * @return The survey item from the DB else IllegalArgument exception
     */
    public SurveyItemDto createSurveyItem(SurveyItemDto surveyItemDto) {
        //Check for all necessary data
        if (surveyItemDto.getOptions() == null || surveyItemDto.getQuestion() == null ||
                surveyItemDto.getCorrectAnswer() == null) {
            throw new IllegalArgumentException("Survey item options, question, and correct answer are required");
        }
        //Create survey item
        SurveyItem item = new SurveyItem(
                db.generateSurveyItemId(),
                surveyItemDto.getQuestion(),
                surveyItemDto.getOptions(),
                surveyItemDto.getCorrectAnswer()
        );

       //Put survey item in database and return it from the database as response
        db.getSurveyItems().put(item.getId(), item);
        return new SurveyItemDto(
                item.getId(),
                item.getQuestion(),
                item.getOptions(),
                item.getCorrectAnswer()
        );
    }

    /**
     *Function to submit an answer for a survey item instance.
     * @param surveyInstanceId The ID of the survey instance
     * @param itemInstanceId The ID of the survey item instance
     * @param answer The answer being submitted
     * @return SurveyItemInstanceDto with the submitted answer
     */
    public SurveyItemInstanceDto submitAnswer(Long surveyInstanceId, Long itemInstanceId, String answer) {
        // Search for the survey instance in the DB if null return IllegalArgument exception
        SurveyInstance instance = db.getSurveyInstances().get(surveyInstanceId);
        if (instance == null) {
            throw new IllegalArgumentException("Survey instance with ID " + surveyInstanceId + " does not exist.");
        }

        // Get the survey Item instances from the survey instance check if answer has already been submitted
        SurveyItemInstance itemInstance = instance.getItemInstances().stream()
                .filter(i -> i.getId().equals(itemInstanceId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Survey item instance with ID " + itemInstanceId + " does not exist in this survey instance."));

        if (itemInstance.getUserAnswer() != null) {
            throw new IllegalArgumentException("Answer already submitted for this item.");
        }

        itemInstance.setUserAnswer(answer);
        itemInstance.setCorrect(answer.equals(itemInstance.getSurveyItem().getCorrectAnswer()));

        // Check if all answers have been updated and update status accordingly
        boolean allAnswered = instance.getItemInstances().stream().allMatch(i -> i.getUserAnswer() != null);
        if (allAnswered) {
            instance.setState(SurveyInstance.SurveyState.COMPLETED);
        } else {
            instance.setState(SurveyInstance.SurveyState.IN_PROGRESS);
        }

        // Update and return SurveyInstance
        return new SurveyItemInstanceDto(
                itemInstance.getId(),
                itemInstance.getSurveyItem().getQuestion(),
                itemInstance.getUserAnswer(),
                itemInstance.isCorrect()
        );
    }
}