package org.example.ser421lab6.controller;

import org.example.ser421lab6.dto.*;
import org.example.ser421lab6.service.SurveyItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class surveyItemController {

    private final SurveyItemService surveyItemService;

    @Autowired
    public surveyItemController(SurveyItemService surveyItemService) {
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

}