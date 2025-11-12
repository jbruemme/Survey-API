package org.example.ser421lab6.controller;

import org.example.ser421lab6.dto.*;
import org.example.ser421lab6.service.surveyItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class surveyItemController {

    private final surveyItemService surveyItemService;

    @Autowired
    public surveyItemController(surveyItemService surveyItemService) {
        this.surveyItemService = surveyItemService;
    }

    /*
     * @api {post} /api/survey-items Create a new Survey Item
     * @apiName CreateSurveyItem
     * @apiGroup SurveyItem
     *
     * @apiParam (Request body) {String} question The text of the survey item question.
     * @apiParam (Request body) {String[]} options The possible answers for the question.
     * @apiParam (Request body) {String} correctAnswer The correct answer for the survey item.
     *
     * @apiSuccess {Number} id Survey Item unique ID.
     * @apiSuccess {String} question Question text.
     * @apiSuccess {String[]} options Answer options.
     * @apiSuccess {String} correctAnswer Correct answer.
     * @apiSuccessExample {json} Success-Response:
     *   HTTP/1.1 201 Created
     *   {
     *     "id": 6,
     *     "question": "What is the capital of Idaho?",
     *     "options": [
     *         "Boise",
     *         "Caldwell",
     *         "Twin Falls",
     *         "Pocatello"
     *     ],
     *     "correctAnswer": "Boise"
     * }
     *
     * @apiError 400 Bad Request Validation failed, invalid input.
     * @apiErrorExample {json} Error-Response:
     *   HTTP/1.1 400 Bad Request
     *   {
     *   "message": "Survey item options, question, and correct answer are required"
     *   }
     */
    @PostMapping("/survey-items")
    public ResponseEntity<?> createSurveyItem(@RequestBody SurveyItemDto surveyItemDto) {
        try {
            SurveyItemDto newSurveyItem = surveyItemService.createSurveyItem(surveyItemDto);
            return ResponseEntity.status(201).body(newSurveyItem);
        }catch(IllegalArgumentException e) {
            return ResponseEntity.status(400).body(new ErrorResponse(e.getMessage()));
        }
    }

    /*
     * @api {post} /api/survey-instances/answer Submit answer for a survey item instance
     * @apiName SubmitAnswer
     * @apiGroup SurveyInstance
     *
     * @apiBody {Number} surveyInstanceId ID of the survey instance.
     * @apiBody {Number} surveyItemInstanceId ID of the survey item instance.
     * @apiBody {String} answer User's selected answer.
     *
     * @apiSuccess {Number} id Survey item instance ID.
     * @apiSuccess {String} question Question text.
     * @apiSuccess {String} userAnswer Submitted answer.
     * @apiSuccess {Boolean} correct Whether the answer was correct.
     *
     * @apiSuccessExample {json} Success-Response:
     *   HTTP/1.1 200 OK
     *   {
     *     "id": 4,
     *     "question": "Who won the MVP in the 2005/2006 NBA season?",
     *     "selectedAnswer": "Steve Nash",
     *     "correct": true
     * }
     *
     * @apiError 400 Bad Request Invalid IDs or answer already submitted.
     */
    @PostMapping("/survey-instances/answer")
    public ResponseEntity<?> submitAnswer(@RequestBody SubmitAnswerDto request) {
        try {
            SurveyItemInstanceDto result = surveyItemService.submitAnswer(
                    request.getSurveyInstanceId(),
                    request.getSurveyItemInstanceId(),
                    request.getAnswer()
            );
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(new ErrorResponse(e.getMessage()));
        }
    }



}