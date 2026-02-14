package org.example.ser421lab6.service;

import org.example.ser421lab6.dto.SurveyItemDto;
import org.example.ser421lab6.dto.SurveyItemInstanceDto;
import org.example.ser421lab6.entity.SurveyInstanceEntity;
import org.example.ser421lab6.entity.SurveyItemEntity;
import org.example.ser421lab6.entity.SurveyItemInstanceEntity;
import org.example.ser421lab6.repository.SurveyInstanceRepository;
import org.example.ser421lab6.repository.SurveyItemInstanceRepository;
import org.example.ser421lab6.repository.SurveyItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the SurveyItemService class
 * Validation for: blank answer, missing instance, missing item instance, incorrect association between item and survey
 * instances, duplicate answers.
 * Asserts: answer setting and answer correctness, updated instance state based on survey completion.
 * Persistence behavior: correct repository methods are called for SurveyItemService functions.
 *
 */
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class SurveyItemServiceTest {
    @Mock
    SurveyItemRepository surveyItemRepository;
    @Mock
    SurveyInstanceRepository surveyInstanceRepository;
    @Mock
    SurveyItemInstanceRepository surveyItemInstanceRepository;

    @InjectMocks
    SurveyItemService surveyItemService;

    @BeforeEach
    void setup() {

    }

    /*
    ===================================== createSurveyItem() Unit Tests =======================================================
     */

    /**
     * Unit test for successfully creating/saving a survey item and returning a survey item dto
     * 1. Creates valid SurveyItemDto request
     * 2. Mocks surveyItemRepository.save() to return the entity with an ID
     * 3. Calls surveyItemService.createSurveyItem()
     * 4. Asserts returned DTO is not null, has saved ID and expected fields
     * 5. Asserts question, correctAnswer, and options passed correctly into repository save() function
     */
    @Test
    void createSurveyItem_success_savesAndReturnsDto() {

        // Create instance of SurveyItemDto
        SurveyItemDto request = new SurveyItemDto(
                null,
                "What is the capital of Idaho?",
                List.of("Boise", "Caldwell", "Twin Falls", "Pocatello"),
                "Boise"
        );

        // Create instance of SurveyItemEntity
        SurveyItemEntity savedEntity = new SurveyItemEntity();
        savedEntity.setId(10L);
        savedEntity.setQuestion(request.getQuestion().trim());
        savedEntity.setOptions(request.getOptions());
        savedEntity.setCorrectAnswer(request.getCorrectAnswer().trim());

        when(surveyItemRepository.save(any(SurveyItemEntity.class))).thenReturn(savedEntity);

        SurveyItemDto result = surveyItemService.createSurveyItem(request);

        // Assert entity returned with ID and expected fields
        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("What is the capital of Idaho?", result.getQuestion());
        assertEquals(List.of("Boise", "Caldwell", "Twin Falls", "Pocatello"), result.getOptions());
        assertEquals("Boise", result.getCorrectAnswer());

        // Verify that SurveyItemEntity class passed into  repository save()
        ArgumentCaptor<SurveyItemEntity> captor = ArgumentCaptor.forClass(SurveyItemEntity.class);
        verify(surveyItemRepository, times(1)).save(captor.capture());
        SurveyItemEntity toSave = captor.getValue();

        // Assert entity values correctly passed
        assertEquals("What is the capital of Idaho?", toSave.getQuestion());
        assertEquals("Boise", toSave.getCorrectAnswer());
        assertEquals(4, toSave.getOptions().size());
    }

    /**
     * Unit test for creating a surveyItemDto missing required fields
     * 1. Creates a SurveyItemDto with null inputs for required fields
     * 2. Calls createSurveyItem() with the bad DTO instance
     * 3. Asserts IllegalArgument exception is thrown for required fields
     * 4. Verifies the repository is never called with the bad request
     */
    @Test
    void createSurveyItem_missingRequiredFields_throws() {
        SurveyItemDto bad = new SurveyItemDto(null, null, null, null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> surveyItemService.createSurveyItem(bad)
        );
        verifyNoInteractions(surveyItemRepository);

        assertTrue(ex.getMessage().toLowerCase().contains("cannot be empty"));
    }

    /**
     * Unit test for checking that the correct answer of a survey item is one of the options of the instance
     * 1. Creates a SurveyItemDto with correctAnswer as not one of the Options
     * 2. Calls createSurveyItem() with the bad instance
     * 3. Asserts IllegalArgumentException is thrown
     * 4. Verifies the repository is never called with the bad request
     */
    @Test
    void createSurveyItem_correctAnswerNotInOptions_throws() {
        SurveyItemDto bad = new SurveyItemDto(
                null,
                "Q?",
                List.of("A", "B", "C"),
                "Z"
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> surveyItemService.createSurveyItem(bad)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("correct answer"));
        verifyNoInteractions(surveyItemRepository);
    }

    /*
    ===================================== submitAnswer() Unit Tests =======================================================
     */

    /**
     * Unit test for successfully submitting an answer and completing a survey instance
     * 1. Creates a SurveyInstanceEntity with two SurveyItemInstanceEntity rows
     * 2. Mocks findById(surveyInstanceId) and returns the instance
     * 3. Mocks findById(itemInstanceId) returns targeted item
     * 4. Mocks save() returns same object
     * 5. Calls submitAnswer(instanceId, targetId, 4)
     * 6. Asserts the returned DTO shows selectedAnswer=4 a nd correct=true, instance state is updated to COMPLETED,
     *    save(target) is called and save(instance) is called.
     */
    @Test
    void submitAnswer_success_updatesItemInstanceAndSetsCompletedWhenAllAnswered() {
        Long surveyInstanceId = 1L;
        Long itemInstanceId = 100L;

        // Survey item entity with correct answer
        SurveyItemEntity surveyItem = new SurveyItemEntity();
        surveyItem.setId(50L);
        surveyItem.setQuestion("What is 2+2?");
        surveyItem.setOptions(List.of("3", "4", "5"));
        surveyItem.setCorrectAnswer("4");

        // Survey instance entity
        SurveyInstanceEntity instance = new SurveyInstanceEntity();
        instance.setId(surveyInstanceId);
        instance.setUser("Jasyn");
        instance.setState(SurveyInstanceEntity.SurveyInstanceState.CREATED);
        instance.setItemInstances(new ArrayList<>());

        // Item instance 1 = item being answered (currently unanswered)
        SurveyItemInstanceEntity target = new SurveyItemInstanceEntity();
        target.setId(itemInstanceId);
        target.setSurveyInstance(instance);
        target.setSurveyItem(surveyItem);
        target.setUserAnswer(null);
        target.setCorrect(false);

        // Item instance 2 = already answered, so after answering target → all answered
        SurveyItemInstanceEntity other = new SurveyItemInstanceEntity();
        other.setId(101L);
        other.setSurveyInstance(instance);
        other.setSurveyItem(surveyItem);
        other.setUserAnswer("3"); // answered
        other.setCorrect(false);

        instance.getItemInstances().add(target);
        instance.getItemInstances().add(other);

        when(surveyInstanceRepository.findById(surveyInstanceId)).thenReturn(Optional.of(instance));
        when(surveyItemInstanceRepository.findById(itemInstanceId)).thenReturn(Optional.of(target));
        when(surveyItemInstanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(surveyInstanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SurveyItemInstanceDto result = surveyItemService.submitAnswer(surveyInstanceId, itemInstanceId, "4");

        assertEquals(itemInstanceId, result.getId());
        assertEquals("What is 2+2?", result.getQuestion());
        assertEquals("4", result.getSelectedAnswer());
        assertTrue(result.isCorrect());

        // Verify state update
        assertEquals(SurveyInstanceEntity.SurveyInstanceState.COMPLETED, instance.getState());

        verify(surveyItemInstanceRepository, times(1)).save(target);
        verify(surveyInstanceRepository, times(1)).save(instance);
    }

    @Test
    void submitAnswer_setsInProgressWhenNotAllAnswered() {
        Long surveyInstanceId = 1L;
        Long itemInstanceId = 100L;

        SurveyItemEntity surveyItem = new SurveyItemEntity();
        surveyItem.setCorrectAnswer("Boise");
        surveyItem.setQuestion("Capital?");

        SurveyInstanceEntity instance = new SurveyInstanceEntity();
        instance.setId(surveyInstanceId);
        instance.setState(SurveyInstanceEntity.SurveyInstanceState.CREATED);
        instance.setItemInstances(new ArrayList<>());

        SurveyItemInstanceEntity target = new SurveyItemInstanceEntity();
        target.setId(itemInstanceId);
        target.setSurveyInstance(instance);
        target.setSurveyItem(surveyItem);
        target.setUserAnswer(null);

        SurveyItemInstanceEntity stillUnanswered = new SurveyItemInstanceEntity();
        stillUnanswered.setId(101L);
        stillUnanswered.setSurveyInstance(instance);
        stillUnanswered.setSurveyItem(surveyItem);
        stillUnanswered.setUserAnswer(null); // remains unanswered

        instance.getItemInstances().add(target);
        instance.getItemInstances().add(stillUnanswered);

        when(surveyInstanceRepository.findById(surveyInstanceId)).thenReturn(Optional.of(instance));
        when(surveyItemInstanceRepository.findById(itemInstanceId)).thenReturn(Optional.of(target));
        when(surveyItemInstanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(surveyInstanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        surveyItemService.submitAnswer(surveyInstanceId, itemInstanceId, "Boise");

        assertEquals(SurveyInstanceEntity.SurveyInstanceState.IN_PROGRESS, instance.getState());
        verify(surveyInstanceRepository).save(instance);
    }

    @Test
    void submitAnswer_blankAnswer_throws() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> surveyItemService.submitAnswer(1L, 2L, "   ")
        );
        assertTrue(ex.getMessage().toLowerCase().contains("answer"));
        verifyNoInteractions(surveyInstanceRepository, surveyItemInstanceRepository);
    }

    /**
     * Unit test to handle a SurveyInstance not being found in the repository.
     * 1. Asserts IllegalArgumentException is thrown when querying the surveyInstanceRepository with an invalid
     *    SurveyInstance ID.
     * 2. Asserts item instances are never loaded for the invalid survey.
     */
    @Test
    void submitAnswer_surveyInstanceNotFound_throws() {
        when(surveyInstanceRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> surveyItemService.submitAnswer(1L, 2L, "x")
        );

        assertTrue(ex.getMessage().toLowerCase().contains("survey instance"));
        verify(surveyItemInstanceRepository, never()).findById(anyLong());
    }

    /**
     * Unit test to handle a SurveyItemInstance not being found in the repository.
     * 1. Creates a SurveyInstanceEntity with no survey item instances
     * 2. Asserts IllegalArgumentException is thrown when using submitAnswer() with an itemInstanceId, since the survey
     *    instance should have no survey items.
     * 3. Verifies that the SurveyItemInstance does not exist.
     */
    @Test
    void submitAnswer_itemInstanceNotFound_throws() {
        SurveyInstanceEntity instance = new SurveyInstanceEntity();
        instance.setId(1L);

        when(surveyInstanceRepository.findById(1L)).thenReturn(Optional.of(instance));
        when(surveyItemInstanceRepository.findById(2L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> surveyItemService.submitAnswer(1L, 2L, "x")
        );

        assertTrue(ex.getMessage().toLowerCase().contains("survey item instance"));
    }

    /**
     * Unit test to check that a SurveyItemInstance belongs to the current SurveyInstance
     * 1. Creates two SurveyInstances (ID=1 & ID=999) and a SurveyItemInstance (SurveyInstance ID=999).
     *    Sets Item instance to the wrong Survey instance.
     * 2. Asserts that IllegalArgumentException is thrown when using submitAnswer() since the item does not belong to the survey.
     * 3. Asserts that nothing is saved to the repository.
     */
    @Test
    void submitAnswer_itemInstanceDoesNotBelongToSurveyInstance_throws() {

        // Create the two surveys
        SurveyInstanceEntity instance = new SurveyInstanceEntity();
        instance.setId(1L);

        SurveyInstanceEntity otherInstance = new SurveyInstanceEntity();
        otherInstance.setId(999L);

        // Set the item to the wrong survey
        SurveyItemInstanceEntity itemInstance = new SurveyItemInstanceEntity();
        itemInstance.setId(2L);
        itemInstance.setSurveyInstance(otherInstance);

        when(surveyInstanceRepository.findById(1L)).thenReturn(Optional.of(instance));
        when(surveyItemInstanceRepository.findById(2L)).thenReturn(Optional.of(itemInstance));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> surveyItemService.submitAnswer(1L, 2L, "x")
        );

        assertTrue(ex.getMessage().toLowerCase().contains("does not belong"));
        verify(surveyItemInstanceRepository, never()).save(any());
    }

    /**
     * Unit test to ensure double submission does not happen for a SurveyItemInstance (one submission per question)
     * 1. Creates surveyItemInstanceEntity that already has a selectedAnswer field
     * 2. Asserts IllegalArgumentException is thrown when trying to double submit
     * 3. Asserts the repository doesn't save anything
     */
    @Test
    void submitAnswer_alreadyAnswered_throws() {
        SurveyInstanceEntity instance = new SurveyInstanceEntity();
        instance.setId(1L);

        SurveyItemInstanceEntity itemInstance = new SurveyItemInstanceEntity();
        itemInstance.setId(2L);
        itemInstance.setSurveyInstance(instance);
        itemInstance.setUserAnswer("already");

        when(surveyInstanceRepository.findById(1L)).thenReturn(Optional.of(instance));
        when(surveyItemInstanceRepository.findById(2L)).thenReturn(Optional.of(itemInstance));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> surveyItemService.submitAnswer(1L, 2L, "x")
        );

        assertTrue(ex.getMessage().toLowerCase().contains("already"));
        verify(surveyItemInstanceRepository, never()).save(any());
    }

}
