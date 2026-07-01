import { requestJson } from "./client";

export const authApi = {
    register: (payload) =>
        requestJson("/api/auth/register", {
            method: "POST",
            body: JSON.stringify(payload),
        }),

    login: (payload) =>
        requestJson("/api/auth/login", {
            method: "POST",
            body: JSON.stringify(payload),
        }),

    me: () => requestJson("/api/auth/me"),
};