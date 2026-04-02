import { useMemo, useState } from "react";
import styles from "./CreateSurvey.module.css";
import { surveysApi } from "../api/surveys";
import { surveyItemsApi } from "../api/surveyItems";

/**
 * CreateSurvey page component, the default export for CreateSurvey
 * @returns {JSX.Element} Default layout for the page
 * @constructor
 */
export default function CreateSurvey() {
    // Question form
    const [qText, setQText] = useState("");
    const [qOptionsText, setQOptionsText] = useState("");
    const [qCorrect, setQCorrect] = useState("");

    // Survey form
    const [surveyTitle, setSurveyTitle] = useState("");
    const [surveyState, setSurveyState] = useState("CREATED");

    // Local question bank for current session
    // TODO: add GET /api/survey-items to allow for saving of questions between multiple sessions
    const [bankItems, setBankItems] = useState([]);
    const [selectedItemIds, setSelectedItemIds] = useState([]);

    // UX state
    const [status, setStatus] = useState("");
    const [error, setError] = useState("");
    const [busy, setBusy] = useState(false);
    const [raw, setRaw] = useState("(nothing yet)");

    const options = useMemo(() => {
        return qOptionsText
            .split("\n")
            .map((o) => o.trim())
            .filter(Boolean);
    }, [qOptionsText]);

    const canCreateQuestion =
        qText.trim().length > 0 &&
        options.length > 0 &&
        !busy;

    const canCreateSurvey =
        surveyTitle.trim().length > 0 &&
        selectedItemIds.length > 0 &&
        !busy;

    /**
     * Testing function to show raw json in front end
     * @param obj The json object
     */
    // TODO: Remove after debugging
    function showRaw(obj) {
        setRaw(obj ? JSON.stringify(obj, null, 2) : "(nothing yet)");
    }

    /**
     * Function to clear question form
     */
    function clearQuestionForm() {
        setQText("");
        setQOptionsText("");
        setQCorrect("");
    }

    /**
     * Toggle function for selecting survey items
     * @param itemId survey item id
     */
    function toggleSelected(itemId) {
        setSelectedItemIds((prev) =>
            prev.includes(itemId)
                ? prev.filter((id) => id !== itemId)
                : [...prev, itemId]
        );
    }

    /**
     * Function to create a survey question
     * @returns {Promise<void>}
     */
    async function createQuestion() {
        setError("");
        setStatus("");
        setBusy(true);

        try {
            const created = await surveyItemsApi.create({
                question: qText.trim(),
                options,
                correctAnswer: qCorrect || null,
            });

            setBankItems((prev) => [...prev, created]);
            setStatus(`Created question #${created.id}`);
            showRaw(created);
            clearQuestionForm();
        } catch (e) {
            setError(e.message);
        } finally {
            setBusy(false);
        }
    }

    /**
     * Function to create a survey
     * @returns {Promise<void>}
     */
    async function createSurvey() {
        setError("");
        setStatus("");
        setBusy(true);

        try {
            const created = await surveysApi.create({
                title: surveyTitle.trim(),
                state: surveyState,
                itemIds: selectedItemIds,
            });

            setStatus(`Created survey #${created.id}`);
            showRaw(created);

            setSurveyTitle("");
            setSurveyState("CREATED");
            setSelectedItemIds([]);
        } catch (e) {
            setError(e.message);
        } finally {
            setBusy(false);
        }
    }

    return (
        <div className={styles.page}>
            <div className={styles.shell}>
                <div className={styles.topBar}>
                    <div>
                        <h2 className={styles.h2}>Create Survey</h2>
                        <p className={styles.sub}>
                            Create questions, add them to a question bank, and build surveys.
                        </p>
                    </div>

                    <div className={styles.badges}>
            <span className={styles.pill}>
              Selected: {selectedItemIds.length}
            </span>
                        <span className={styles.pill}>
              Bank: {bankItems.length}
            </span>
                    </div>
                </div>

                {(status || error) && (
                    <div>
                        {status && <div className={styles.statusOk}>{status}</div>}
                        {error && <div className={styles.statusErr}>Error: {error}</div>}
                    </div>
                )}

                <div className={styles.grid}>
                    {/* Create Question */}
                    <section className={styles.card}>
                        <div className={styles.cardHeader}>
                            <h3 className={styles.sectionTitle}>1) Create Question</h3>
                            <span className={styles.pill}>POST /api/survey-items</span>
                        </div>

                        <label className={styles.label}>Question</label>
                        <input
                            value={qText}
                            onChange={(e) => setQText(e.target.value)}
                            placeholder="What is the capital of Idaho?"
                            className={styles.input}
                        />

                        <label className={styles.label}>Options (one per line)</label>
                        <textarea
                            value={qOptionsText}
                            onChange={(e) => {
                                setQOptionsText(e.target.value);

                                const nextOptions = e.target.value
                                    .split("\n")
                                    .map((o) => o.trim())
                                    .filter(Boolean);

                                if (qCorrect && !nextOptions.includes(qCorrect)) {
                                    setQCorrect("");
                                }
                            }}
                            className={styles.textarea}
                        />

                        <label className={styles.label}>Correct Answer</label>
                        <select
                            value={qCorrect}
                            onChange={(e) => setQCorrect(e.target.value)}
                            className={styles.select}
                        >
                            <option value="">Select correct answer</option>
                            {options.map((o) => (
                                <option key={o} value={o}>
                                    {o}
                                </option>
                            ))}
                        </select>

                        <div className={styles.row}>
                            <button
                                onClick={createQuestion}
                                disabled={!canCreateQuestion}
                                className={`${styles.btn} ${styles.primary}`}
                            >
                                {busy ? "Working…" : "Create Question"}
                            </button>

                            <button
                                onClick={clearQuestionForm}
                                type="button"
                                disabled={busy}
                                className={`${styles.btn} ${styles.ghost}`}
                            >
                                Clear
                            </button>
                        </div>

                        <div className={styles.debugSection}>
                            <h4 className={styles.debugTitle}>Debug</h4>
                            <pre className={styles.pre}>{raw}</pre>
                        </div>
                    </section>

                    {/* Question Bank + Create Survey */}
                    <section className={styles.card}>
                        <div className={styles.cardHeader}>
                            <h3 className={styles.sectionTitle}>2) Question Bank</h3>
                            <span className={styles.pill}>select → itemIds</span>
                        </div>

                        {bankItems.length === 0 ? (
                            <div className={styles.meta}>No questions yet.</div>
                        ) : (
                            <div className={styles.bankList}>
                                {bankItems.map((item) => (
                                    <label key={item.id} className={styles.choice}>
                                        <input
                                            type="checkbox"
                                            checked={selectedItemIds.includes(item.id)}
                                            onChange={() => toggleSelected(item.id)}
                                        />
                                        <span>
                      <strong>#{item.id}</strong> — {item.question}
                    </span>
                                    </label>
                                ))}
                            </div>
                        )}

                        <div className={styles.divider} />

                        <div className={styles.cardHeader}>
                            <h3 className={styles.sectionTitle}>3) Create Survey</h3>
                            <span className={styles.pill}>POST /api/surveys</span>
                        </div>

                        <label className={styles.label}>Survey Title</label>
                        <input
                            value={surveyTitle}
                            onChange={(e) => setSurveyTitle(e.target.value)}
                            placeholder="My New Survey"
                            className={styles.input}
                        />

                        <label className={styles.label}>Survey State</label>
                        <select
                            value={surveyState}
                            onChange={(e) => setSurveyState(e.target.value)}
                            className={styles.select}
                        >
                            <option value="CREATED">CREATED</option>
                            <option value="COMPLETED">COMPLETED</option>
                        </select>

                        <div className={styles.row}>
                            <button
                                onClick={createSurvey}
                                disabled={!canCreateSurvey}
                                className={`${styles.btn} ${styles.primary}`}
                            >
                                {busy ? "Working…" : "Create Survey"}
                            </button>
                        </div>
                    </section>
                </div>
            </div>
        </div>
    );
}