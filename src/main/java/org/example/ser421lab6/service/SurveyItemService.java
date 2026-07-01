package org.example.ser421lab6.service;

import lombok.RequiredArgsConstructor;
import org.example.ser421lab6.dto.SurveyItemDto;
import org.example.ser421lab6.dto.SurveyItemInstanceDto;
import org.example.ser421lab6.entity.SurveyItemEntity;
import org.example.ser421lab6.entity.SurveyItemInstanceEntity;
import org.example.ser421lab6.repository.SurveyInstanceRepository;
import org.example.ser421lab6.repository.SurveyItemInstanceRepository;
import org.example.ser421lab6.repository.SurveyItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SurveyItemService {

    /*
    ===================================== Class Variables =======================================================
     */

    private final SurveyItemRepository surveyItemRepository;
    private final SurveyItemInstanceRepository surveyItemInstanceRepository;
    private final SurveyInstanceRepository surveyInstanceRepository;

    /*
    ===================================== Service Methods =======================================================
     */

    /**
     * Function to create a new survey item and save it in the DB
     * @param surveyItemDto The incoming new survey item request
     * @return The survey item from the DB else IllegalArgument exception
     */
    @Transactional
    public SurveyItemDto createSurveyItem(SurveyItemDto surveyItemDto) {

        // Survey item data validation
        if (surveyItemDto.getQuestion() == null || surveyItemDto.getQuestion().trim().isEmpty()) {
            throw new IllegalArgumentException("Question cannot be empty");
        }

        if (surveyItemDto.getOptions() == null || surveyItemDto.getOptions().isEmpty()) {
            throw new IllegalArgumentException("Options cannot be empty");
        }

        // Check if correct answer is null, if blank set to null, if not null validate correct answer is one of the options
        String rawCorrectAnswer = surveyItemDto.getCorrectAnswer();
        final String correctAnswer =
                (rawCorrectAnswer == null || rawCorrectAnswer.isBlank() ? null : rawCorrectAnswer.trim());

        // Validate correctAnswer is one of the options if a correct answer is provided
        if (correctAnswer != null) {
            boolean answerInOptions = surveyItemDto.getOptions().stream()
                    .anyMatch(option -> option != null && option.trim()
                            .equalsIgnoreCase(correctAnswer.trim()));
            if (!answerInOptions) {
                throw new IllegalArgumentException("Correct answer must match one of the options");
            }
        }

        // Create the survey item entity
        SurveyItemEntity surveyItemEntity = new SurveyItemEntity();
        surveyItemEntity.setQuestion(surveyItemDto.getQuestion().trim());
        surveyItemEntity.setOptions(surveyItemDto.getOptions().stream().map(String::trim).toList());
        surveyItemEntity.setCorrectAnswer(correctAnswer);

        SurveyItemEntity savedSurveyItem = surveyItemRepository.save(surveyItemEntity);
        return toSurveyItemDto(savedSurveyItem);
    }

    /*
    ===================================== Mapping Helper Methods =======================================================
     */

    /**
     * Helper function to map data from a survey item to the SurveyItemDto
     * @param surveyItem The survey item being mapped
     * @return SurveyInstanceSummaryDto
     */
    private SurveyItemDto toSurveyItemDto(SurveyItemEntity surveyItem) {
        return new SurveyItemDto(
                surveyItem.getId(),
                surveyItem.getQuestion(),
                surveyItem.getOptions(),
                surveyItem.getCorrectAnswer()
        );
    }

    /**
     * Helper function to map data from a survey instance item to the SurveyInstanceItemDto
     * @param surveyItemInstance The survey item being mapped
     * @return SurveyInstanceSummaryDto
     */
    private SurveyItemInstanceDto toSurveyInstanceItemDto(SurveyItemInstanceEntity surveyItemInstance) {
        return new SurveyItemInstanceDto(
                surveyItemInstance.getId(),
                surveyItemInstance.getSurveyItem().getId(),
                surveyItemInstance.getSurveyItem().getQuestion(),
                surveyItemInstance.getUserAnswer(),
                surveyItemInstance.getCorrect()
        );
    }

}