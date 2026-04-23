/**
 * Wrapper for client native fetch API:
 * 1. Includes JSON headers
 * 2. Parses body response
 * 3. Error handling for non 200 OK responses
 * @type {{answer: (function(*): Promise<*>), get: (function(*): Promise<*>), create: (function(*): Promise<*>)}}
 */
const API_BASE = import.meta.env.VITE_API_BASE_URL || "";

export async function requestJson(path, options = {}) {
    const res = await fetch(`${API_BASE}${path}`, {
        headers: { "Content-Type": "application/json", ...(options.headers || {}) },
        ...options
    });

    const text = await res.text();
    const data = text ? JSON.parse(text) : null;

    if (!res.ok) {
        throw new Error(data?.message || res.statusText);
    }
    return data;
}