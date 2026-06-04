package org.example.ser421lab6.controller;

import org.example.ser421lab6.dto.*;
import org.example.ser421lab6.service.SurveyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SurveyController {

    private final SurveyService surveyService;

    @Autowired
    public SurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    /*
     * @api {get} /api/surveys Get all current user's surveys
     * @apiName GetUserSurveys
     * @apiGroup Survey
     *
     * @apiDescription
     * Retrieves all surveys created by the currently authenticated user.
     * This endpoint requires a valid JWT token.
     *
     * @apiHeader {String} Authorization Bearer JWT access token.
     *
     * @apiSuccess {Object[]} surveys List of surveys created by the authenticated user.
     * @apiSuccess {Number} surveys.id Survey unique ID.
     * @apiSuccess {String} surveys.title Survey title.
     * @apiSuccess {String} surveys.state Survey state (CREATED, COMPLETED, DELETED).
     * @apiSuccess {String} surveys.visibility Survey visibility (PRIVATE, UNLISTED, PUBLIC).
     *
     * @apiSuccessExample {json} Success-Response:
     *   HTTP/1.1 200 OK
     *   [
     *     {
     *       "id": 1,
     *       "title": "Coding Quiz",
     *       "state": "CREATED",
     *       "visibility": "PRIVATE"
     *     },
     *     {
     *       "id": 2,
     *       "title": "Java Basics Survey",
     *       "state": "CREATED",
     *       "visibility": "PUBLIC"
     *     }
     *   ]
     *
     * @apiError 401 Unauthorized Missing or invalid JWT token.
     * @apiError 403 Forbidden Authenticated user does not have access.
     */
    @GetMapping("/surveys")
    public ResponseEntity<List<SurveySummaryDto>> getCurrentUserSurveys() {
        return ResponseEntity.ok(surveyService.getCurrentUserSurveys());
    }

    /*
     * @api {get} /api/surveys/:id Get a specific survey
     * @apiName GetSurvey
     * @apiGroup Survey
     *
     * @apiParam {Number} id Survey unique ID.
     *
     * @apiSuccess {Number} id Survey ID.
     * @apiSuccess {String} title Survey title.
     * @apiSuccess {String} state Survey state (CREATED, COMPLETED, DELETED).
     * @apiSuccess {Object[]} items List of survey items in the survey.
     * @apiSuccess {Number} items.id Survey item ID.
     * @apiSuccess {String} items.question Question text.
     * @apiSuccess {String[]} items.options Answer options.
     * @apiSuccess {String} items.correctAnswer Correct answer.
     *
     * @apiSuccessExample {json} Success-Response:
     *   HTTP/1.1 200 OK
     *   {
     *     "id": 1,
     *     "title": "Random Trivia",
     *     "state": "COMPLETED",
     *     "items": [
     *         {
     *             "id": 1,
     *             "question": "What is a primary color?",
     *             "options": [
     *                 "red",
     *                 "purple",
     *                 "orange",
     *                 "pink"
     *             ],
     *             "correctAnswer": "red"
     *         },
     *         {
     *             "id": 2,
     *             "question": "What animal is largest?",
     *             "options": [
     *                 "cat",
     *                 "dog",
     *                 "rat",
     *                 "horse"
     *             ],
     *             "correctAnswer": "horse"
     *         },
     *         {
     *             "id": 3,
     *             "question": "What decade was Emma Watson born in?",
     *             "options": [
     *                 "1980s",
     *                 "1990s",
     *                 "2000s",
     *                 "2010s"
     *             ],
     *             "correctAnswer": "1990s"
     *         }
     *     ]
     * }
     *
     * @apiError 404 Not Found Survey with given ID does not exist.
     */
    @GetMapping("/surveys/{id}")
    public ResponseEntity<?> getSurvey(@PathVariable Long id) {
        SurveyDto survey = surveyService.getSurveyById(id);
        if(survey == null) {
            return ResponseEntity.status(404).body(new ErrorResponse("Survey with ID " + id + " does not exist."));
        }
        return ResponseEntity.ok(survey);
    }

    /*
     * @api {post} /api/surveys-create Create a new survey
     * @apiName CreateSurvey
     * @apiGroup Survey
     *
     * @apiParam {String} title Survey title.
     * @apiParam {String} state Survey state (CREATED, COMPLETED, DELETED).
     * @apiParam {Number[]} itemIds Array of survey item IDs to include in the survey.
     *
     * @apiParamExample {json} Request-Example:
     *   {
     *     "title": "Test Survey",
     *     "state": "CREATED",
     *     "itemIds": [1, 2, 5]
     *   }
     *
     * @apiSuccess {Number} id Survey ID.
     * @apiSuccess {String} title Survey title.
     * @apiSuccess {String} state Survey state.
     * @apiSuccess {Object[]} items Array of survey items in the survey.
     * @apiSuccess {Number} items.id Survey item ID.
     * @apiSuccess {String} items.question Question text.
     * @apiSuccess {String[]} items.options Answer options.
     * @apiSuccess {String} items.correctAnswer Correct answer.
     *
     * @apiSuccessExample {json} Success-Response:
     *   HTTP/1.1 201 Created
     *   {
     *     "id": 3,
     *     "title": "Test Survey",
     *     "state": "CREATED",
     *     "items": [
         *       {
         *         "id": 1,
         *         "question": "What is a primary color?",
         *         "options": ["red","purple","orange","pink"],
         *         "correctAnswer": "red"
         *       },
     *         {
     *             "id": 2,
     *             "question": "What animal is largest?",
     *             "options": [
     *                 "cat",
     *                 "dog",
     *                 "rat",
     *                 "horse"
     *             ],
     *             "correctAnswer": "horse"
     *         },
     *         {
     *             "id": 5,
     *             "question": "What team won the 2024/2025 Super Bowl",
     *             "options": [
     *                 "Eagles",
     *                 "Rams",
     *                 "Broncos",
     *                 "Chiefs"
     *             ],
     *             "correctAnswer": "Eagles"
     *         }
     *     ]
     *   }
     *
     * @apiError 400 Bad Request Invalid survey data (e.g., missing title or invalid item IDs).
     * @apiErrorExample {json} Error-Response:
     *   HTTP/1.1 400 Bad Request
     *   {
     *     "message": "Survey title and Survey state are required fields"
     *   }
     *   {
     *     "message": "Invalid survey item ID: "
     *   }
     */
    @PostMapping("/surveys")
    public ResponseEntity<?> createSurvey(@RequestBody SurveyCreateDto survey) {
        try {
            SurveyDto surveyDto = surveyService.createSurvey(survey);
            return ResponseEntity.status(201).body(surveyDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(new ErrorResponse(e.getMessage()));
        }
    }

    /*
     * @api {delete} /api/surveys/:id Delete a survey
     * @apiName DeleteSurvey
     * @apiGroup Survey
     *
     * @apiParam {Number} id Survey ID.
     *
     * @apiSuccess {Number} id ID of the survey marked as deleted.
     * @apiSuccess {String} title Title of the survey.
     * @apiSuccess {String} state New state of the survey ("DELETED").
     * @apiSuccessExample {json} Success-Response:
     *   HTTP/1.1 200 OK
     *   {
     *     "id": 1,
     *     "title": "Random Trivia",
     *     "state": "DELETED"
     *   }
     *
     * @apiError 400 Bad Request Survey does not exist.
     * @apiErrorExample {json} Error-Response:
     *   HTTP/1.1 400 Bad Request
     *   {
     *     "message": "Survey with ID 999 does not exist."
     *   }
     */
    @DeleteMapping("/surveys/{id}")
    public ResponseEntity<?> deleteSurvey(@PathVariable Long id) {
        try {
            SurveySummaryDto deletedSurvey = surveyService.deleteSurvey(id);
            return ResponseEntity.ok(deletedSurvey);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/public/surveys/{shareToken}")
    public ResponseEntity<?> getSurveyByShareToken(@PathVariable String shareToken) {
        try {
            SurveyDto survey = surveyService.getSurveyByShareToken(shareToken);
            return ResponseEntity.ok(survey);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/surveys/{id}/share")
    public ResponseEntity<?> getShareLink(@PathVariable Long id) {
        try {
            SurveyShareDto shareDto = surveyService.getShareLinks(id);
            return ResponseEntity.ok(shareDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(new ErrorResponse(e.getMessage()));
        }
    }

}