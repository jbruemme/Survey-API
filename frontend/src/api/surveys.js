import { requestJson } from "./client";

/**
 * Data access layer for hitting surveys API endpoints
 * @type {{answer: (function(*): Promise<*>), get: (function(*): Promise<*>), create: (function(*): Promise<*>)}}
 */
export const surveysApi = {
    list: () => requestJson("/api/surveys"),
    get: (id) => requestJson(`/api/surveys/${id}`)
};