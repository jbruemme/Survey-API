import styles from "./TakeSurvey.module.css";
import { useEffect, useMemo, useState } from "react";
import { surveysApi } from "../api/surveys";
import { instancesApi } from "../api/instances";
import { Link, useParams } from "react-router-dom";

/**
 * TakeSurvey page component, the default export for TakeSurvey
 * @returns {JSX.Element} Default layout for the page
 * @constructor
 */
export default function TakeSurvey() {
    // Share mode
    const {shareToken}= useParams();
    const shareMode = Boolean(shareToken);

    // Survey selection and survey taker identification
    const [surveys, setSurveys] = useState([]);
    const [surveyId, setSurveyId] = useState("");

    // Loaded data
    const [survey, setSurvey] = useState(null);       // full survey (items/options)
    const [instance, setInstance] = useState(null);   // full instance (itemInstances w/ selectedAnswer, correct)

    // UX state
    const [status, setStatus] = useState("");
    const [error, setError] = useState("");
    const [busy, setBusy] = useState(false);
    const [currentIdx, setCurrentIdx] = useState(0);
    const [answersByItemInstanceId, setAnswersByItemInstanceId] = useState({});
    const isLoggedIn = Boolean(localStorage.getItem("token"));

    // Load survey list
    useEffect(() => {
        async function load() {
            try {
                setError("");

                if (shareMode) {
                    const loadedSurvey = await surveysApi.getPublicByToken(shareToken);
                    setSurvey(loadedSurvey);
                    setSurveyId(String(loadedSurvey.id));
                    return;
                }

                const list = await surveysApi.publicList();
                setSurveys(list);
            } catch (err) {
                setError(
                    shareMode
                        ? `Failed to load shared survey: ${err.message}`
                        : `Failed to load surveys: ${err.message}`
                );
            }
        }

        load();
    }, [shareMode, shareToken]);

    // Index survey items by id for quick lookup
    const itemById = useMemo(() => {
        const map = new Map();
        (survey?.items || []).forEach((item) => map.set(item.id, item));
        return map;
    }, [survey]);

    // Survey instance item variables
    const itemInstances = instance?.itemInstances || [];
    const currentInstanceItem = itemInstances[currentIdx] || null;
    const currentSurveyItem = currentInstanceItem ? itemById.get(currentInstanceItem.surveyItemId) : null;

    // Navigation variables
    const answeredCount = Object.values(answersByItemInstanceId).filter(Boolean).length;
    const totalQuestions = itemInstances.length;
    const isLastQuestion = currentIdx === totalQuestions - 1;
    const isComplete = instance?.state === "COMPLETED";
    const canStart = Number(surveyId) > 0 && !busy;
    const progressPercent = totalQuestions > 0 ? (answeredCount / totalQuestions) * 100 : 0;

    /**
     * Async function to start a new survey instance, waits for the survey id to be fetched, returns a promise that resolves
     * when the fetch goes through or an error is thrown.
     * @returns {Promise<void>} The promise to be resolved.
     */
    async function startInstance() {
        setError("");
        setStatus("");
        setBusy(true);
        setCurrentIdx(0);
        setAnswersByItemInstanceId({});

        try {
            const sid = shareMode ? survey?.id : Number(surveyId);

            if (!sid) {
                throw new Error("Survey not found.");
            }

            let loadedSurvey = survey;
            if (!shareMode){
                loadedSurvey = await surveysApi.getPublicById(sid);
                setSurvey(loadedSurvey);
            }

            // Create and set instance
            const created = await instancesApi.create({ surveyId: sid });
            setInstance(created);

            setStatus(`Started survey instance #${created.id}`);
        } catch (e) {
            setError(e.message);
        } finally {
            setBusy(false);
        }
    }

    /**
     * Function to set selected answer
     * @param answer The answer to be set
     */
    function selectAnswer(answer) {
        if (!currentInstanceItem || isComplete) return;

        setAnswersByItemInstanceId((prev) => ({
            ...prev,
            [currentInstanceItem.id]: answer,
        }))
    }

    /**
     * Function to save the current instance item answer
     * @returns {Promise<void>}
     */
    async function saveCurrentAnswer() {
        if (!instance?.id || !currentInstanceItem) return;

        const answer = answersByItemInstanceId[currentInstanceItem.id];

        if (!answer) {
            throw new Error("Select an answer before continuing.");
        }

        return await instancesApi.answer({
            surveyInstanceId: instance.id,
            surveyItemInstanceId: currentInstanceItem.id,
            answer,
        });
    }

    /**
     * Async function to go to next question
     * @returns {Promise<void>}
     */
    async function goNext() {
        setError("");
        setStatus("");

        try {
            setBusy(true);
            const nextIdx = Math.min(currentIdx + 1, itemInstances.length - 1);
            const updatedInstance = await saveCurrentAnswer();
            setInstance(updatedInstance);
            setCurrentIdx(nextIdx);
        } catch (err) {
            setError(err.message);
        } finally {
            setBusy(false);
        }
    }

    /**
     * Function to go to previous question
     */
    function goPrev() {
        setError("");
        setStatus("");
        setCurrentIdx((idx) => Math.max(idx - 1, 0));
    }

    /**
     * Async function to submit the survey
     * @returns {Promise<void>}
     */
    async function submitSurvey() {
        setError("");
        setStatus("");

        try {
            setBusy(true);
            const updatedInstance = await saveCurrentAnswer();
            setInstance(updatedInstance);
            setStatus("Survey submitted. Thanks for your response!");
        } catch (err) {
            setError(err.message);
        } finally {
            setBusy(false);
        }
    }

    /**
     * Function to reset the survey page state
     */
    function resetSurvey() {
        setInstance(null);
        setSurvey(null);
        setSurveyId("");
        setCurrentIdx(0);
        setAnswersByItemInstanceId({});
        setStatus("");
        setError("");
    }

    return (
        <div className={`${styles.page} fadeIn`}>
            <div className={styles.shell}>
                <div className={styles.topBar}>
                    <div>
                        <h2 className={styles.h2}>
                            {shareMode ? "Take Shared Survey" : "Take Survey"}
                        </h2>
                        <p className={styles.sub}>Answer questions and see results when complete.</p>
                    </div>

                    {instance && !isComplete && (
                        <div className={styles.badges}>
                            <span className={styles.pill}>
                                Question {currentIdx + 1}/{totalQuestions}
                            </span>
                            <span className={styles.pill}>
                                Answered {answeredCount}/{totalQuestions}
                            </span>
                        </div>
                    )}
                </div>

                {status && <div className={styles.statusOk}>{status}</div>}
                {error && <div className={styles.statusErr}>Error: {error}</div>}

                {!instance && (
                    <section className={styles.card}>
                        <h3 className={styles.sectionTitle}>Start Survey</h3>

                        {!shareMode && (
                            <>
                            <label className={styles.label}>Survey</label>
                                <select
                                    value={surveyId}
                                    onChange={(e) => setSurveyId(e.target.value)}
                                    className={styles.input}
                                >
                                    <option value="">Select a survey…</option>
                                    {surveys.map((s) => (
                                        <option key={s.id} value={s.id}>
                                            {s.title}
                                        </option>
                                    ))}
                                </select>
                            </>
                        )}

                        {shareMode && survey && (
                            <div className={styles.meta}>
                                <strong>Shared survey:</strong> {survey.title}
                            </div>
                        )}

                        <div className={styles.row}>
                            <button
                                onClick={startInstance}
                                disabled={!canStart}
                                className={`${styles.btn} ${styles.primary}`}
                            >
                                {busy ? "Starting…" : "Start Survey"}
                            </button>
                        </div>
                    </section>
                )}

                {instance && isComplete && (
                    <section className={styles.card}>
                        <h3 className={styles.completeTitle}>Survey Complete</h3>
                        <p className={styles.completeMessage}>
                            {isLoggedIn
                                ? "Thanks for completing this survey! Your response has been recorded successfully. " +
                                  "Return to your dashboard to continue creating and managing your surveys."
                                : "Thanks for sharing your feedback! Your response has been recorded successfully. " +
                                  "Explore Pulse Polling to learn more about creating and sharing your own surveys."
                            }
                        </p>

                        <div className={styles.row}>
                            <Link
                                to={isLoggedIn ? "/dashboard" : "/"}
                                className={`${styles.btn} ${styles.primary}`}
                            >
                                {isLoggedIn
                                    ? "Return to Dashboard"
                                    : "Explore Pulse Polling"}
                            </Link>

                            {isLoggedIn && (
                                <button
                                    className={`${styles.btn} ${styles.ghost}`}
                                    onClick={resetSurvey}
                                    type="button"
                                >
                                    Take Another Survey
                                </button>
                            )}
                        </div>
                    </section>
                )}

                {instance && !isComplete && currentSurveyItem && currentInstanceItem && (
                    <section className={styles.card}>
                        <div className={styles.meta}>
                            Question {currentIdx + 1} of {totalQuestions}
                        </div>

                        <div className={styles.progressTrack}>
                            <div
                                className={styles.progressFill}
                                style={{
                                    width:`${progressPercent}%`,
                                }}
                            />
                        </div>

                        <h3 className={styles.qText}>{currentSurveyItem.question}</h3>

                        <div className={styles.options}>
                            {currentSurveyItem.options.map((option) => {
                                const checked =
                                    answersByItemInstanceId[currentInstanceItem.id] === option;

                                return (
                                    <label
                                        key={`${currentInstanceItem.id}-${option}`}
                                        className={`${styles.choice} ${
                                            checked ? styles.choiceSelected : ""
                                        }`}
                                    >
                                        <input
                                            type="radio"
                                            name={`q-${currentInstanceItem.id}`}
                                            value={option}
                                            checked={checked}
                                            onChange={() => selectAnswer(option)}
                                            disabled={busy}
                                        />
                                        <span>{option}</span>
                                    </label>
                                );
                            })}
                        </div>

                        <div className={styles.row}>
                            <button
                                onClick={goPrev}
                                disabled={busy || currentIdx === 0}
                                className={styles.btn}
                                type="button"
                            >
                                Previous
                            </button>

                            {isLastQuestion ? (
                                <button
                                    onClick={submitSurvey}
                                    disabled={busy}
                                    className={`${styles.btn} ${styles.primary}`}
                                    type="button"
                                >
                                    {busy ? "Submitting…" : "Submit Survey"}
                                </button>
                            ) : (
                                <button
                                    onClick={goNext}
                                    disabled={busy}
                                    className={`${styles.btn} ${styles.primary}`}
                                    type="button"
                                >
                                    {busy ? "Saving…" : "Next"}
                                </button>
                            )}
                        </div>
                    </section>
                )}
            </div>
        </div>
    );
}