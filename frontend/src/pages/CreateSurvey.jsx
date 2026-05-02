import { useMemo, useState } from "react";
import styles from "./CreateSurvey.module.css";
import { surveysApi } from "../api/surveys";
import { surveyItemsApi } from "../api/surveyItems";

const DEV = import.meta.env.DEV;

/**
 * CreateSurvey page component, the default export for CreateSurvey
 * @returns {JSX.Element} Default layout for the page
 * @constructor
 */
export default function CreateSurvey() {
    // Question form
    const [qText, setQText] = useState("");
    const [qOptions, setQOptions] = useState(["", ""]);
    const [qCorrect, setQCorrect] = useState("");

    // Survey form
    const [surveyTitle, setSurveyTitle] = useState("");

    // Local question bank for current session
    // TODO: add GET /api/survey-items to allow for saving of questions between multiple sessions
    const [bankItems, setBankItems] = useState([]);
    const [selectedItemIds, setSelectedItemIds] = useState([]);

    // UX state
    const [status, setStatus] = useState("");
    const [error, setError] = useState("");
    const [busy, setBusy] = useState(false);
    const [raw, setRaw] = useState("(nothing yet)");

    const canCreateSurvey =
        surveyTitle.trim().length > 0 &&
        selectedItemIds.length > 0 &&
        !busy;

    const options = useMemo(() => {
        return qOptions.map(o => o.trim()).filter(Boolean);
    }, [qOptions]);

    const hasDuplicateOptions = new Set(options.map(o => o.toLowerCase())).size !== options.length;

    const canCreateQuestion =
        qText.trim().length > 0 &&
        options.length >= 2 &&
        !hasDuplicateOptions &&
        !busy;

    /**
     * Testing function to show raw json in front end
     * @param obj The json object
     */
    function showRaw(obj) {
        if (!DEV) return;
        setRaw(obj ? JSON.stringify(obj, null, 2) : "(nothing yet)");
    }

    /**
     * Update/edit a question option
     * @param index Index of the selected option
     * @param value Value of the index
     */
    function updateOption(index, value) {
        setQOptions(prev => {
            const oldValue = prev[index];

            if (qCorrect && qCorrect === oldValue) {
                setQCorrect("");
            }

            return prev.map((opt, i) => (i === index ? value : opt));
        });
    }

    /**
     * Add a question option
     */
    function addOption() {
        setQOptions(prev => [...prev, ""]);
    }

    /**
     * Remove a question option
     * @param index Index of the selected option to be removed
     */
    function removeOption(index) {
        setQOptions(prev => {
            const removed = prev[index];

            if (qCorrect === removed) {
                setQCorrect("");
            }

            return prev.filter((_, i) => i !== index);
        });
    }

    /**
     * Function to clear question form
     */
    function clearQuestionForm() {
        setQText("");
        setQOptions(["", ""]);
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
                itemIds: selectedItemIds,
            });

            setStatus(`Created survey #${created.id}`);
            showRaw(created);
            setSurveyTitle("");
            setSelectedItemIds([]);
        } catch (e) {
            setError(e.message);
        } finally {
            setBusy(false);
        }
    }

    return (
        <div className={`${styles.page} fadeIn`}>
            <div className={styles.shell}>
                <div className={styles.topBar}>
                    <div>
                        <h2 className={styles.h2}>Create Survey</h2>
                        <p className={styles.sub}>
                            Create questions, add them to a question bank, and build surveys.
                        </p>
                    </div>

                    {DEV && (
                        <div className={styles.badges}>
                            <span className={styles.pill}>
                              Selected: {selectedItemIds.length}
                            </span>
                            <span className={styles.pill}>
                              Bank: {bankItems.length}
                            </span>
                        </div>
                    )}
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
                            {DEV && (
                                <span className={styles.pill}>POST /api/survey-items</span>
                            )}
                        </div>

                        <label className={styles.label}>Question</label>
                        <input
                            value={qText}
                            onChange={(e) => setQText(e.target.value)}
                            placeholder="What is the capital of Idaho?"
                            className={styles.input}
                        />

                        <label className={styles.label}>Options</label>

                        <div className={styles.optionList}>
                            {qOptions.map((option, index) => (
                                <div key={index} className={styles.optionRow}>
                                    <input
                                        value={option}
                                        onChange={(e) => updateOption(index, e.target.value)}
                                        placeholder={`Option ${index + 1}`}
                                        className={styles.input}
                                    />

                                    <button
                                        type="button"
                                        onClick={() => removeOption(index)}
                                        disabled={busy || qOptions.length <= 2}
                                        className={`${styles.btn} ${styles.ghost}`}
                                    >
                                        Remove
                                    </button>
                                </div>
                            ))}
                        </div>

                        <button
                            type="button"
                            onClick={addOption}
                            disabled={busy}
                            className={styles.btn}
                            style={{marginTop: 10}}
                        >
                            + Add option
                        </button>

                        {hasDuplicateOptions && (
                            <div className={styles.statusErr}>
                                Options must be unique.
                            </div>
                        )}

                        <label className={styles.label}>Correct Answer (Optional)</label>
                        <select
                            value={qCorrect}
                            onChange={(e) => setQCorrect(e.target.value)}
                            className={styles.select}
                        >
                            <option value="">No Correct Answer</option>
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

                        {DEV && (
                            <div className={styles.debugSection}>
                                <h4 className={styles.debugTitle}>Debug</h4>
                                <pre className={styles.pre}>{raw}</pre>
                            </div>
                        )}

                    </section>

                    {/* Question Bank */}
                    <section className={styles.card}>
                        <div className={styles.cardHeader}>
                            <h3 className={styles.sectionTitle}>2) Question Bank</h3>
                            {DEV && (
                                <span className={styles.pill}>select → itemIds</span>
                            )}
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
                    </section>

                    {/* Create Survey */}
                    <section className={styles.card}>
                        <div className={styles.cardHeader}>
                            <h3 className={styles.sectionTitle}>3) Create Survey</h3>
                            {DEV && (
                                <span className={styles.pill}>POST /api/surveys</span>
                            )}
                        </div>

                        <label className={styles.label}>Survey Title</label>
                        <input
                            value={surveyTitle}
                            onChange={(e) => setSurveyTitle(e.target.value)}
                            placeholder="My New Survey"
                            className={styles.input}
                        />

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
    )
        ;
}