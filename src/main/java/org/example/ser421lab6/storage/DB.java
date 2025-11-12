package org.example.ser421lab6.storage;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.example.ser421lab6.model.Survey;
import org.example.ser421lab6.model.SurveyInstance;
import org.example.ser421lab6.model.SurveyItem;
import org.example.ser421lab6.model.SurveyItemInstance;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to initialize the initial in memory Database. Initializes 5 survey items,  2 surveys, and 1 survey instance.
 */
@Getter
@Component
public class DB {
    // ID counters
    private long nextSurveyItemId = 1;
    private long nextSurveyId = 1;
    private long nextSurveyInstanceId = 1;
    private long nextSurveyItemInstanceId = 1;

    //Storing surveys, survey items and survey instances in respective hashmaps
    public final Map<Long, SurveyItem> surveyItems = new HashMap<>();
    public final Map<Long, Survey> surveys = new HashMap<>();
    public final Map<Long, SurveyInstance> surveyInstances = new HashMap<>();

    //Increment IDs
    public long generateSurveyItemId() {
        return nextSurveyItemId++;
    }
    public long generateSurveyId() {
        return nextSurveyId++;
    }
    public long generateSurveyInstanceId() {
        return nextSurveyInstanceId++;
    }
    public long generateSurveyItemInstanceId() {
        return nextSurveyItemInstanceId++;
    }

    @PostConstruct
    public void init() {
        // Initialize 5 survey items
        SurveyItem question1 = new SurveyItem(generateSurveyItemId(), "What is a primary color?",
                List.of("red","purple","orange","pink"), "red");
        SurveyItem question2 = new SurveyItem(generateSurveyItemId(), "What animal is largest?",
                List.of("cat","dog","rat","horse"),"horse");
        SurveyItem question3 = new SurveyItem(generateSurveyItemId(), "What decade was Emma Watson born in?",
                List.of("1980s","1990s","2000s","2010s"), "1990s");
        SurveyItem question4 = new SurveyItem(generateSurveyItemId(), "Who won the MVP in the 2005/2006 NBA season?",
                List.of("Shaquile O'neal","Kobe Bryant","Steve Nash","Tim Duncan"), "Steve Nash");
        SurveyItem question5 = new SurveyItem(generateSurveyItemId(), "What team won the 2024/2025 Super Bowl",
                List.of("Eagles","Rams","Broncos","Chiefs"), "Eagles");

        surveyItems.put(question1.getId(), question1);
        surveyItems.put(question2.getId(), question2);
        surveyItems.put(question3.getId(), question3);
        surveyItems.put(question4.getId(), question4);
        surveyItems.put(question5.getId(), question5);

        // Initialize 2 surveys
        Survey survey1 = new Survey(generateSurveyId(), "Random Trivia", Survey.SurveyState.COMPLETED,
                new ArrayList<>(List.of(question1, question2, question3)));
        Survey survey2 = new Survey(generateSurveyId(), "Sports Trivia", Survey.SurveyState.COMPLETED,
                new ArrayList<>(List.of(question4, question5)));

        surveys.put(survey1.getId(), survey1);
        surveys.put(survey2.getId(), survey2);

        // Initialize 2 survey instances
        SurveyInstance surveyInstance = new SurveyInstance(
                generateSurveyInstanceId(),
                "Jasyn",
                survey1,
                new ArrayList<>(),
                SurveyInstance.SurveyState.CREATED
        );

        for (SurveyItem item : survey1.getItems()) {
            String answer = switch (item.getQuestion()) {
                case "What is a primary color?" -> "purple";
                case "What animal is largest?" -> "dog";
                case "What decade was Emma Watson born in?" -> "1980s";
                default -> null;
            };
            surveyInstance.getItemInstances().add(
                    new SurveyItemInstance(
                            generateSurveyItemInstanceId(),
                            item,
                            answer,
                            false)
            );
        }
        // Second survey instance
        SurveyInstance surveyInstance2 = new SurveyInstance(
                generateSurveyInstanceId(),
                "Test User",
                survey2,
                new ArrayList<>(),
                SurveyInstance.SurveyState.CREATED
        );

        for (SurveyItem item : survey2.getItems()) {
            surveyInstance2.getItemInstances().add(
                    new SurveyItemInstance(
                            generateSurveyItemInstanceId(),
                            item,
                            null,
                            false
                    )
            );
        }
        surveyInstances.put(surveyInstance.getId(), surveyInstance);
        surveyInstances.put(surveyInstance2.getId(), surveyInstance2);
    }
}
