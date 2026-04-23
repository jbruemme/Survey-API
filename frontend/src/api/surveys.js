import { requestJson } from "./client";

/**
 * Data access layer for hitting surveys API endpoints
 * @type {{answer: (function(*): Promise<*>), get: (function(*): Promise<*>), create: (function(*): Promise<*>)}}
 */
export const surveysApi = {
    list: () => requestJson("/api/surveys"),
    get: (id) => requestJson(`/api/surveys/${id}`),
    create: (payload) =>
        requestJson("/api/surveys", {
            method: "POST",
            body: JSON.stringify(payload),
        }),
    delete: (id) =>
        requestJson(`/api/surveys/${id}`, {
            method: "DELETE",
        }),
    getPublicByToken: (shareToken) =>
        requestJson(`/api/public/surveys/${shareToken}`),
    getShareLinks: (id) =>
        requestJson(`/api/surveys/${id}/share`),
};