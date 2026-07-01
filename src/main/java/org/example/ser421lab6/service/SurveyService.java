package org.example.ser421lab6.service;

import lombok.RequiredArgsConstructor;
import org.example.ser421lab6.dto.*;
import org.example.ser421lab6.dto.results.AnswerResultsDto;
import org.example.ser421lab6.dto.results.QuestionResultsDto;
import org.example.ser421lab6.dto.results.SurveyResultsDto;
import org.example.ser421lab6.entity.*;
import org.example.ser421lab6.exception.InvalidSurveyVisibilityException;
import org.example.ser421lab6.exception.SurveyNotFoundException;
import org.example.ser421lab6.exception.UnauthorizedAccessException;
import org.example.ser421lab6.repository.SurveyInstanceRepository;
import org.example.ser421lab6.repository.SurveyItemRepository;
import org.example.ser421lab6.repository.SurveyRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SurveyService {

    /*
    ===================================== Class Variables =======================================================
     */

    private final SurveyRepository surveyRepository;
    private final SurveyItemRepository surveyItemRepository;
    private final SurveyInstanceRepository surveyInstanceRepository;

    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    @Value("${app.public-base-url-prod}")
    private String publicBaseUrlProd;

    /*
    ===================================== Service Methods =======================================================
     */

    /**
     * Function to get all current user's surveys and map them to SurveySummaryDto
     * @return List of SurveySummaryDto objects
     */
    @Transactional(readOnly = true)
    public List<SurveySummaryDto> getCurrentUserSurveys() {
        UserEntity currentUser = getCurrentAuthenticatedUser();
        return surveyRepository
                .findByCreatorAndStateNot(currentUser, SurveyEntity.SurveyState.DELETED)
                .stream()
                .map(this::toSummaryDto)
                .toList();
    }


    /**
     * Function to retrieve a survey and its corresponding survey items based on a survey ID
     * @param id The id of the survey being searched for
     * @return Survey dto object with its corresponding survey items dto objects
     */
    @Transactional(readOnly = true)
    public SurveyDto getSurveyById(Long id) {
        UserEntity currentUser = getCurrentAuthenticatedUser();

        SurveyEntity survey = surveyRepository.findById(id)
                .orElseThrow(() ->
                        new SurveyNotFoundException("Survey with ID " + id + " does not exist.")
                );

        if (!survey.getCreator().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to access this survey.");
        }

        if (survey.getState() == SurveyEntity.SurveyState.DELETED) {
            throw new SurveyNotFoundException("Survey with ID " + id + " does not exist.");
        }

        return surveyRepository.findById(id).map(this::toSurveyDto).orElse(null);
    }


    /**
     * Function to create a new survey
     * @param surveyCreateDto Incoming survey data to build a new survey
     * @return The created survey
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

        if (surveyCreateDto.getItemIds() == null || surveyCreateDto.getItemIds().isEmpty()) {
            throw new IllegalArgumentException("At least one survey item ID is required");
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
        UserEntity currentUser = getCurrentAuthenticatedUser();
        SurveyEntity surveyEntity = new SurveyEntity();
        surveyEntity.setTitle(surveyCreateDto.getTitle());
        surveyEntity.setState(SurveyEntity.SurveyState.CREATED);
        surveyEntity.setItems(surveyItems);
        surveyEntity.setCreator(currentUser);

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
                new SurveyNotFoundException("Survey with: " + id + " does not exist.")
                );

        if (!survey.getCreator().getId().equals(getCurrentAuthenticatedUser().getId())) {
            throw new UnauthorizedAccessException("You do not have permission to delete this survey.");
        }

        //Mark survey as DELETED
        survey.setState(SurveyEntity.SurveyState.DELETED);
        SurveyEntity savedSurvey = surveyRepository.save(survey);

        return toSummaryDto(savedSurvey);
    }

    /**
     * Function to retrieve a survey's share link via its ID.
     * @param surveyId The ID of the survey containing the share link
     * @return The survey's share token
     */
    @Transactional(readOnly = true)
    public SurveyShareDto getShareLinks(Long surveyId) {
        SurveyEntity survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new SurveyNotFoundException("Survey with: " + surveyId + " does not exist."));

        if (survey.getVisibility() == SurveyEntity.SurveyVisibility.PRIVATE) {
            throw new InvalidSurveyVisibilityException("Public sharing is disabled for this survey.");
        }

        if (!survey.getCreator().getId().equals(getCurrentAuthenticatedUser().getId())) {
            throw new UnauthorizedAccessException("You do not have permission to share this survey.");
        }

        String frontendUrl = buildFrontEndShareLink(survey.getShareToken());
        String previewUrl = buildPreviewShareLink(survey.getShareToken());

        SurveyShareDto shareDto = new SurveyShareDto();
        shareDto.setPublicUrl(frontendUrl);
        shareDto.setTwitterShareUrl(buildTwitterShareUrl(previewUrl, survey.getTitle()));
        shareDto.setLinkedInShareUrl(buildLinkedInShareUrl(previewUrl));
        shareDto.setFacebookShareUrl(buildFacebookShareUrl(previewUrl));

        return shareDto;
    }

    /**
     * Function to retrieve a survey based on its share token
     * @param shareToken The UUID share token of the requested survey
     * @return SurveyDto of the survey
     */
    @Transactional(readOnly = true)
    public SurveyDto getSurveyByShareToken(String shareToken) {
        SurveyEntity survey = surveyRepository.findByShareToken(shareToken)
                .orElseThrow(() -> new SurveyNotFoundException("Survey not found."));

        if (survey.getVisibility() == SurveyEntity.SurveyVisibility.PRIVATE) {
            throw new InvalidSurveyVisibilityException("Public sharing is disabled for this survey.");
        }

        if (survey.getState() == SurveyEntity.SurveyState.DELETED) {
            throw new IllegalArgumentException("Survey has been deleted.");
        }

        return toSurveyDto(survey);
    }

    /**
     * Function to update a surveys visibility
     * @param surveyId The ID of the survey to be updated
     * @param request The request containing the new visibility
     * @return SurveySummaryDto containing the updated visibility of the survey
     */
    @Transactional
    public SurveySummaryDto updateSurveyVisibility(Long surveyId, SurveyVisibilityUpdateDto request) {
        // Getting current user and survey to be updated
        UserEntity currentUser = getCurrentAuthenticatedUser();
        SurveyEntity survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new SurveyNotFoundException("Survey with ID: " + surveyId + " does not exist."));

        // Checking that the user who made request has same ID as the owner of the survey
        if (!survey.getCreator().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to update survey.");
        }

        // Attempting to update the visibility, check that the request is a valid visibility
        SurveyEntity.SurveyVisibility newVisibility;
        try {
            newVisibility = SurveyEntity.SurveyVisibility.valueOf(request.visibility().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidSurveyVisibilityException("Invalid visibility: " + request.visibility());
        }

        // Setting the updated visibility, saving survey, and returning updated survey summary
        survey.setVisibility(newVisibility);
        SurveyEntity savedSurvey = surveyRepository.save(survey);
        return toSummaryDto(savedSurvey);

    }

    /**
     * Function to retrieve all public surveys
     * @return List of all public surveys as SurveySummaryDtos
     */
    @Transactional(readOnly = true)
    public List<SurveySummaryDto> getPublicSurveys() {
        return surveyRepository
                .findByVisibilityAndStateNot(
                        SurveyEntity.SurveyVisibility.PUBLIC,
                        SurveyEntity.SurveyState.DELETED
                )
                .stream()
                .map(this::toSummaryDto)
                .toList();

    }

    /**
     * Function to retrieve a public survey by its ID
     * @return SurveyDto of the requested survey
     */
    @Transactional(readOnly = true)
    public SurveyDto getPublicSurveyById(Long id) {

        SurveyEntity survey = surveyRepository.findById(id)
                .orElseThrow(() -> new SurveyNotFoundException("Survey with ID: " + id + " does not exist."));

        if (survey.getState() == SurveyEntity.SurveyState.DELETED) {
            throw new SurveyNotFoundException("Survey not found.");
        }

        if (survey.getVisibility() != SurveyEntity.SurveyVisibility.PUBLIC) {
            throw new InvalidSurveyVisibilityException("Public sharing is disabled for this survey.");
        }

        return toSurveyDto(survey);
    }

    /**
     * Function to retrieve survey results of a requested survey
     * @param surveyId The ID of the survey requesting results
     * @return SurveyResultsDto
     */
    @Transactional(readOnly = true)
    public SurveyResultsDto getSurveyResults(Long surveyId) {

        UserEntity currentUser = getCurrentAuthenticatedUser();

        SurveyEntity survey = surveyRepository.findById(surveyId)
                .orElseThrow(() ->
                        new SurveyNotFoundException("Survey with ID " + surveyId + " does not exist.")
                );

        if(!survey.getCreator().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to view these results.");
        }

        if (survey.getState() == SurveyEntity.SurveyState.DELETED) {
            throw new SurveyNotFoundException("Survey with ID " + surveyId + " does not exist.");
        }

        // Getting list of all completed instances of requested survey and storing total responses
        List<SurveyInstanceEntity> completedInstances =
                surveyInstanceRepository.findBySurveyIdAndState(
                        surveyId,
                        SurveyInstanceEntity.SurveyInstanceState.COMPLETED
                        );
        int totalResponses = completedInstances.size();

        // Looping through all survey questions and creating QuestionResultsDto for each survey question
        List<QuestionResultsDto> questionResults = survey.getItems()
                .stream()
                .map(item -> {
                    // Map for answer options and number of times each is selected
                    Map<String, Integer> answerCounts = new LinkedHashMap<>();

                    // Initializing all options to 0
                    for (String option : item.getOptions()) {
                        answerCounts.put(option, 0);
                    }

                    for (SurveyInstanceEntity instance : completedInstances) {
                        instance.getItemInstances()
                                .stream()
                                .filter(itemInstance ->
                                        itemInstance.getSurveyItem().getId().equals(item.getId())
                                )
                                .findFirst()
                                .ifPresent(itemInstance -> {
                                    String answer = itemInstance.getUserAnswer();
                                    if (answer != null && !answer.isBlank()) {
                                        answerCounts.merge(answer, 1, Integer::sum);
                                    }
                                });
                    }

                    // Counting total answer for current question
                    int totalAnswers = answerCounts.values()
                            .stream()
                            .mapToInt(Integer::intValue)
                            .sum();

                    // Converting each answer into AnswerResultDto
                    List<AnswerResultsDto> answers = answerCounts.entrySet()
                            .stream()
                            .map(entry -> {
                                // Extracting answer and count from map
                               String answer = entry.getKey();
                               int count = entry.getValue();

                               // Calculating percentage
                               double percentage = totalAnswers == 0 ? 0.0 : (count * 100.0 / totalAnswers);

                               // Building AnswerResultDto
                                Boolean correct = null;
                                if (item.getCorrectAnswer() != null && !item.getCorrectAnswer().isBlank()) {
                                    correct = answer.equals(item.getCorrectAnswer());
                                }
                               return new AnswerResultsDto(
                                       answer,
                                       count,
                                       percentage,
                                       correct
                               );
                            })
                            .toList();

                    // Building QuestionResultDto returning result summary for one question
                    return new QuestionResultsDto(
                            item.getId(),
                            item.getQuestion(),
                            item.getCorrectAnswer(),
                            totalAnswers,
                            answers
                    );
                })
                .toList();

        // Building SurveyResultDto returning result summary for full survey
        return new SurveyResultsDto(
                survey.getId(),
                survey.getTitle(),
                totalResponses,
                questionResults
        );
    }

    /*
    ===================================== Share Link Helper Methods ====================================================
     */

    /**
     * Helper method for building a front end shareable link for a survey
     * @param shareToken The UUID share token generated by the survey
     * @return A full shareable url for the survey
     */
    private String buildFrontEndShareLink(String shareToken) {
        return frontendBaseUrl + "/s/" + shareToken;
    }

    /**
     * Helper method for building a shareable link for a survey share card
     * @param shareToken The UUID share token generated by the survey
     * @return A full shareable url for creating a survey share card
     */
    private String buildPreviewShareLink(String shareToken) {
        return publicBaseUrlProd + "/share/" + shareToken;
    }

    /**
     * Helper method for building an X share URL link
     * @param publicUrl The public survey URL
     * @param surveyTitle The title of the survey
     * @return Encoded X share URL link
     */
    private String buildTwitterShareUrl(String publicUrl, String surveyTitle) {
        String text = "Take my survey: " + surveyTitle;
        return "https://twitter.com/intent/tweet?text=" + encode(text) + "&url=" + encode(publicUrl);
    }

    /**
     * Helper method for building a LinkedIn share URL link
     * @param publicUrl The public survey URL
     * @return Encoded LinkedIn share URL link
     */
    private String buildLinkedInShareUrl(String publicUrl) {
        return "https://www.linkedin.com/sharing/share-offsite/?url=" +encode (publicUrl);
    }

    /**
     * Helper method for building a Facebook share URL link
     * @param publicUrl The public survey URL
     * @return Encoded Facebook share URL link
     */
    private String buildFacebookShareUrl(String publicUrl) {
        return "https://www.facebook.com/sharer/sharer.php?u=" + encode(publicUrl);
    }

    /**
     * Helper method to URL-encode strings
     * @param value The String being encoded
     * @return Encoded String
     */
    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
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
                survey.getState().name(),
                survey.getVisibility().name()
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
                ).toList(),
                buildFrontEndShareLink(survey.getShareToken()),
                survey.getVisibility().name()
        );
    }

    /*
    ===================================== User Helper Methods =======================================================
     */

    /**
     * Helper function to retrieve the current authenticated user
     * @return The authenticated UserEntity placed in the Security context through the JWT filter.
     */
    private UserEntity getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (UserEntity) authentication.getPrincipal();
    }

}