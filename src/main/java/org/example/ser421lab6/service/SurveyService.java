package org.example.ser421lab6.service;

import lombok.RequiredArgsConstructor;
import org.example.ser421lab6.dto.SurveyCreateDto;
import org.example.ser421lab6.dto.SurveyDto;
import org.example.ser421lab6.dto.SurveyShareDto;
import org.example.ser421lab6.dto.SurveySummaryDto;
import org.example.ser421lab6.entity.SurveyEntity;
import org.example.ser421lab6.entity.SurveyItemEntity;
import org.example.ser421lab6.repository.SurveyItemRepository;
import org.example.ser421lab6.repository.SurveyRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    @Value("${app.public-base-url}")
    private String publicBaseUrl;

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

    /**
     * Function to retrieve a survey's share link via its ID.
     * @param surveyId The ID of the survey containing the share link
     * @return The survey's share token
     */
    @Transactional(readOnly = true)
    public SurveyShareDto getShareLinks(Long surveyId) {
        SurveyEntity survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new IllegalArgumentException("Survey with: " + surveyId + " does not exist."));

        if (!survey.isPublicShareEnabled()) {
            throw new IllegalArgumentException("Public sharing is disabled for this survey.");
        }

        String publicUrl = buildShareLink(survey.getShareToken());

        SurveyShareDto shareDto = new SurveyShareDto();
        shareDto.setPublicUrl(publicUrl);
        shareDto.setTwitterShareUrl(buildTwitterShareUrl(publicUrl, survey.getTitle()));
        shareDto.setLinkedInShareUrl(buildLinkedInShareUrl(publicUrl, survey.getTitle()));
        shareDto.setFacebookShareUrl(buildFacebookShareUrl(publicUrl, survey.getTitle()));

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
                .orElseThrow(() -> new IllegalArgumentException("Survey with: " + shareToken + " does not exist."));

        if (!survey.isPublicShareEnabled()) {
            throw new IllegalArgumentException("Public sharing is disabled for this survey.");
        }

        if (survey.getState() == SurveyEntity.SurveyState.DELETED) {
            throw new IllegalArgumentException("Survey has been deleted.");
        }

        return toSurveyDto(survey);
    }

    /*
    ===================================== Share Link Helper Methods ====================================================
     */

    /**
     * Helper method for building a shareable link for a survey
     * @param shareToken The UUID share token generated by the survey
     * @return A full shareable url for the survey
     */
    private String buildShareLink(String shareToken) {
        return publicBaseUrl + "/s/" + shareToken;
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
    private String buildLinkedInShareUrl(String publicUrl, String surveyTitle) {
        String text = "Take my survey: " + surveyTitle;
        return "https://www.linkedin.com/sharing/share-offsite/?url=" +encode(text) + "&url=" + encode(publicUrl);
    }

    /**
     * Helper method for building a Facebook share URL link
     * @param publicUrl The public survey URL
     * @return Encoded Facebook share URL link
     */
    private String buildFacebookShareUrl(String publicUrl, String surveyTitle) {
        String text = "Take my survey: " + surveyTitle;
        return "https://www.facebook.com/sharer/sharer.php?u=" + encode(text) + "&url=" + encode(publicUrl);
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
                ).toList(),
                buildShareLink(survey.getShareToken())
        );
    }


}