package org.example.ser421lab6.service;

import lombok.RequiredArgsConstructor;
import org.example.ser421lab6.dto.SurveyItemDto;
import org.example.ser421lab6.dto.SurveyItemInstanceDto;
import org.example.ser421lab6.entity.SurveyInstanceEntity;
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

    /**
     *Function to submit an answer for a survey item instance.
     * @param surveyInstanceId The ID of the survey instance
     * @param itemInstanceId The ID of the survey item instance
     * @param answer The answer being submitted
     * @return SurveyItemInstanceDto with the submitted answer
     */
    @Transactional
    public SurveyItemInstanceDto submitAnswer(Long surveyInstanceId, Long itemInstanceId, String answer) {

        // Retrieve and validate answer, item instance, survey instance
        if (answer == null || answer.trim().isEmpty()) {
            throw new IllegalArgumentException("Answer cannot be empty");
        }

        SurveyInstanceEntity surveyInstance = surveyInstanceRepository.findById(surveyInstanceId)
                .orElseThrow(() -> new IllegalArgumentException("Survey instance with id: " + surveyInstanceId
                        + " not found."
                ));

        SurveyItemInstanceEntity itemInstance = surveyItemInstanceRepository.findById(itemInstanceId)
                .orElseThrow(() -> new IllegalArgumentException("Survey item instance with id: " + itemInstanceId
                + " not found."
                ));

        // Validate the survey item instance belongs to the survey instance
        if (!itemInstance.getSurveyInstance().getId().equals(surveyInstance.getId())) {
            throw new IllegalArgumentException(
                    "Survey item instance with id: " + itemInstanceId + " does not belong to survey instance with id: "
                    + surveyInstanceId + "."
            );
        }

        // Validate the item instance hasn't already been answered
        if (itemInstance.getUserAnswer() != null) {
            throw new IllegalArgumentException(
                    "Answer already submitted for survey item with id: " + itemInstanceId
            );
        }

        // Set user answer for item instance and set answer correctness
        String trimmedAnswer = answer.trim();
        itemInstance.setUserAnswer(trimmedAnswer);

        String correctAnswer = itemInstance.getSurveyItem().getCorrectAnswer();
        Boolean isAnswerCorrect = null;

        if (correctAnswer != null && !correctAnswer.isBlank()) {
            isAnswerCorrect = trimmedAnswer.equalsIgnoreCase(correctAnswer);
        }
        itemInstance.setCorrect(isAnswerCorrect);

        // Save and return the updated survey instance item
        SurveyItemInstanceEntity savedSurveyInstanceItem = surveyItemInstanceRepository.save(itemInstance);

        // Check if all item instances are answered for the survey instance and update survey instance state accordingly
        boolean allItemsAnswered = surveyInstance.getItemInstances().stream().allMatch(i -> i.getUserAnswer() != null);
        if (allItemsAnswered) {
            surveyInstance.setState(SurveyInstanceEntity.SurveyInstanceState.COMPLETED);
        } else {
            surveyInstance.setState(SurveyInstanceEntity.SurveyInstanceState.IN_PROGRESS);
        }
        surveyInstanceRepository.save(surveyInstance);

        return toSurveyInstanceItemDto(savedSurveyInstanceItem);

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