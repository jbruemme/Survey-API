import { requestJson } from "./client";

/**
 * Data access layer for hitting surveyItems API endpoints
 * @type {{answer: (function(*): Promise<*>), get: (function(*): Promise<*>), create: (function(*): Promise<*>)}}
 */
export const surveyItemsApi = {
    create: (payload) =>
        requestJson("/api/survey-items", {
            method: "POST",
            body: JSON.stringify(payload),
        }),
};