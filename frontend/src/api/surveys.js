import { requestJson } from "./client";

/**
 * Data access layer for hitting surveys API endpoints
 * @type {{answer: (function(*): Promise<*>), get: (function(*): Promise<*>), create: (function(*): Promise<*>)}}
 */
export const surveysApi = {
    list: () => requestJson("/api/surveys"),

    publicList: () => requestJson("/api/public/surveys"),

    get: (id) => requestJson(`/api/surveys/${id}`),

    getPublicByToken: (shareToken) => requestJson(`/api/public/surveys/${shareToken}`),

    getShareLinks: (id) => requestJson(`/api/surveys/${id}/share`),

    getPublicById: (id) => requestJson(`/api/public/surveys/id/${id}`),

    getResults: (id) => requestJson(`/api/surveys/${id}/results`),

    create: (payload) =>
        requestJson("/api/surveys", {
            method: "POST",
            body: JSON.stringify(payload),
        }),

    delete: (id) =>
        requestJson(`/api/surveys/${id}`, {
            method: "DELETE",
        }),

    updateVisibility: (id, visibility) =>
        requestJson(`/api/surveys/${id}/visibility`, {
            method: "PATCH",
            body: JSON.stringify({ visibility }),
        }),

};