import { requestJson } from "./client";

/**
 * Data access layer for hitting survey instance API endpoints
 * @type {{answer: (function(*): Promise<*>), get: (function(*): Promise<*>), create: (function(*): Promise<*>)}}
 */
export const instancesApi = {
    create: (payload) => requestJson("/api/survey-instances", {
        method: "POST",
        body: JSON.stringify(payload),
    }),
    get: (id) => requestJson(`/api/survey-instances/${id}`),
    answer: (payload) => requestJson("/api/survey-instances/answer", {
        method: "POST",
        body: JSON.stringify(payload),
    }),
};