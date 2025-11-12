package org.example.ser421lab6.controller;

import org.example.ser421lab6.dto.ErrorResponse;
import org.example.ser421lab6.dto.SurveyInstanceCreateDto;
import org.example.ser421lab6.dto.SurveyInstanceDto;
import org.example.ser421lab6.service.SurveyInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SurveyInstanceController {
    private final SurveyInstanceService surveyInstanceService;

    @Autowired
    public SurveyInstanceController(SurveyInstanceService surveyInstanceService) {
        this.surveyInstanceService = surveyInstanceService;
    }

    /*
     * @api {get} /api/survey-instances Get all survey instances
     * @apiName GetSurveyInstances
     * @apiGroup SurveyInstance
     *
     * @apiParam {String} [state] Optional. Filter survey instances by state (CREATED, IN_PROGRESS, COMPLETED).
     *
     * @apiSuccess {Number} id Survey instance ID.
     * @apiSuccess {String} userName of the user who started the survey.
     * @apiSuccess {String} surveyTitle Title of the survey.
     * @apiSuccess {String} state Survey instance state.
     *
     * @apiSuccessExample {json} Success-Response:
     *   HTTP/1.1 200 OK
     *   [
     *     {
     *       "id": 1,
     *       "user": "Jasyn",
     *       "surveyTitle": "Random Trivia",
     *       "state": "CREATED",
     *     }
     *   ]
     *
     * @apiError 400 Bad Request Invalid state filter.
     */
    @GetMapping("/survey-instances")
    public ResponseEntity<?> getSurveyInstances(@RequestParam(required = false) String state) {
        try {
            return ResponseEntity.ok(surveyInstanceService.getSurveyInstancesByState(state));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /*
     * @api {get} /api/survey-instances/:id Get survey instance by ID
     * @apiName GetSurveyInstance
     * @apiGroup SurveyInstance
     *
     * @apiParam {Number} id SurveyInstance unique ID.
     *
     * @apiSuccess {Number} id SurveyInstance ID.
     * @apiSuccess {String} user The user who took the survey.
     * @apiSuccess {Object} survey The survey details.
     * @apiSuccess {Array} itemInstances The answered survey items.
     * @apiSuccess {String} state The survey instance state.
     *
     * @apiSuccessExample {json} Success-Response:
     *   HTTP/1.1 200 OK
     *   {
     *     "id": 1,
     *     "user": "Jasyn",
     *     "survey": {
     *       "id": 1,
     *       "title": "Random Trivia"
     *     },
     *     "itemInstances": [
     *       {
     *         "id": 1,
     *         "question": "What is a primary color?",
     *         "selectedAnswer": null,
     *         "correct": false
     *       }
     *     ],
     *     "state": "CREATED"
     *   }
     *
     * @apiError 404 Not Found The SurveyInstance with the given ID does not exist.
     */
    @GetMapping("/survey-instances/{id}")
    public ResponseEntity<?> getSurveyInstance(@PathVariable Long id) {
        SurveyInstanceDto surveyInstance = surveyInstanceService.getSurveyInstanceById(id);
        if (surveyInstance == null) {
            return ResponseEntity.status(404).body(new ErrorResponse("SurveyInstance with ID: " + id + " not found"));
        }
        return ResponseEntity.ok(surveyInstance);
    }

    /*
     * @api {post} /api/survey-instances Create a survey instance
     * @apiName CreateSurveyInstance
     * @apiGroup SurveyInstance
     *
     * @apiDescription
     * Creates a new survey instance for a given user and survey.
     * A survey instance consists of one or more survey instance items and
     * represents a user taking a survey.
     *
     * @apiBody {String} userName of the user starting the survey.
     * @apiBody {Number} surveyId ID of the survey being taken.
     *
     * @apiSuccess {Number} id Survey instance ID.
     * @apiSuccess {String} user User’s name.
     * @apiSuccess {Object} survey Survey data.
     * @apiSuccess {Number} survey.id Survey ID.
     * @apiSuccess {String} survey.title Survey title.
     * @apiSuccess {String} survey.state Survey state.
     * @apiSuccess {Object[]} itemInstances List of survey item instances.
     * @apiSuccess {Number} itemInstances.id Survey item instance ID.
     * @apiSuccess {String} itemInstances.question Survey item question.
     * @apiSuccess {String[]} itemInstances.options Survey item options.
     * @apiSuccess {String} itemInstances.answer User’s answer (initially null).
     * @apiSuccess {Boolean} itemInstances.correct Whether the answer is correct.
     *
     * @apiSuccessExample {json} Success-Response:
     *   HTTP/1.1 201 Created
     *   {
     *     "id": 2,
     *     "user": "Test User",
     *     "survey": {
     *         "id": 1,
     *         "title": "Random Trivia",
     *         "state": "COMPLETED",
     *         "items": [
     *             {
     *                 "id": 1,
     *                 "question": "What is a primary color?",
     *                 "options": [
     *                     "red",
     *                     "purple",
     *                     "orange",
     *                     "pink"
     *                 ],
     *                 "correctAnswer": "red"
     *             },
     *             {
     *                 "id": 2,
     *                 "question": "What animal is largest?",
     *                 "options": [
     *                     "cat",
     *                     "dog",
     *                     "rat",
     *                     "horse"
     *                 ],
     *                 "correctAnswer": "horse"
     *             },
     *             {
     *                 "id": 3,
     *                 "question": "What decade was Emma Watson born in?",
     *                 "options": [
     *                     "1980s",
     *                     "1990s",
     *                     "2000s",
     *                     "2010s"
     *                 ],
     *                 "correctAnswer": "1990s"
     *             }
     *         ]
     *     },
     *     "itemInstances": [
     *         {
     *             "id": 4,
     *             "question": "What is a primary color?",
     *             "selectedAnswer": null,
     *             "correct": false
     *         },
     *         {
     *             "id": 5,
     *             "question": "What animal is largest?",
     *             "selectedAnswer": null,
     *             "correct": false
     *         },
     *         {
     *             "id": 6,
     *             "question": "What decade was Emma Watson born in?",
     *             "selectedAnswer": null,
     *             "correct": false
     *         }
     *     ],
     *     "state": "CREATED"
     * }
     *
     * @apiError 400 BadRequest Missing or invalid user/surveyId.
     * @apiError 404 NotFound Survey with given ID does not exist.
     */
    @PostMapping("/survey-instances")
    public ResponseEntity<?> createSurveyInstance(@RequestBody SurveyInstanceCreateDto request) {
        try {
            SurveyInstanceDto surveyInstance = surveyInstanceService.createSurveyInstance(
                    request.getUser(), request.getSurveyId()
            );
            return ResponseEntity.status(201).body(surveyInstance);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(new ErrorResponse(e.getMessage()));
        }
    }



}