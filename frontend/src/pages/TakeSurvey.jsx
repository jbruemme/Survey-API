import styles from "./TakeSurvey.module.css";
import { useEffect, useMemo, useState } from "react";
import { surveysApi } from "../api/surveys";
import { instancesApi } from "../api/instances";
import { useParams } from "react-router-dom";

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
    const [takerName, setTakerName] = useState("");

    // Loaded data
    const [survey, setSurvey] = useState(null);       // full survey (items/options)
    const [instance, setInstance] = useState(null);   // full instance (itemInstances w/ selectedAnswer, correct)

    // UX state
    const [status, setStatus] = useState("");
    const [error, setError] = useState("");
    const [selectedAnswer, setSelectedAnswer] = useState("");
    const [currentIdx, setCurrentIdx] = useState(0);
    const [busy, setBusy] = useState(false);
    const [revealed, setRevealed] = useState(false);

    // Load survey list
    useEffect(() => {
        (async () => {
            try {
                setError("");

                if (shareMode) {
                    const loadedSurvey = await surveysApi.getPublicByToken(shareToken);
                    setSurvey(loadedSurvey);
                    setSurveyId(String(loadedSurvey.id));
                    return
                }

                const list = await surveysApi.list();
                setSurveys(list);
            } catch (e) {
                setError(
                    shareMode
                    ? `Failed to load shared survey: ${e.message}` : `Failed to load surveys: ${e.message}`
                );
            }
        })();
    }, [shareToken, shareMode]);

    // Index survey items by id for quick lookup
    const itemById = useMemo(() => {
        const map = new Map();
        (survey?.items || []).forEach((it) => map.set(it.id, it));
        return map;
    }, [survey]);

    const itemInstances = instance?.itemInstances || [];

    const progress = useMemo(() => {
        const answered = itemInstances.filter(ii => ii.selectedAnswer != null).length;
        return { answered, total: itemInstances.length };
    }, [itemInstances]);

    const isComplete = instance?.state === "COMPLETED" || (progress.total > 0 && progress.answered === progress.total);

    // Determine which question to show: first unanswered, else 0
    useEffect(() => {
        if (!itemInstances.length) return;
        const firstUnanswered = itemInstances.findIndex(ii => ii.selectedAnswer == null);
        setCurrentIdx(firstUnanswered >= 0 ? firstUnanswered : 0);
    }, [instance?.id]); // only when new instance loads

    const currentInstanceItem = itemInstances[currentIdx] || null;
    const currentSurveyItem = currentInstanceItem ? itemById.get(currentInstanceItem.surveyItemId) : null;

    const canStart = Number(surveyId) > 0 && takerName.trim().length > 0 && !busy;

    /**
     * Async function to start a new survey instance, waits for the survey id to be fetched, returns a promise that resolves
     * when the fetch goes through or an error is thrown.
     * @returns {Promise<void>} The promise to be resolved.
     */
    async function startInstance() {
        setError("");
        setStatus("");
        setBusy(true);
        setSelectedAnswer("");
        setRevealed(false);

        try {
            const sid = shareMode ? survey?.id : Number(surveyId);
            const name = takerName.trim();

            if (!sid) {
                throw new Error("Survey not found.");
            }

            let loadedSurvey = survey;
            if (!shareMode){
                loadedSurvey = await surveysApi.get(sid);
                setSurvey(loadedSurvey);
            }

            // Create instance
            const created = await instancesApi.create({ surveyId: sid, user: name });

            // Load full instance
            const loadedInstance = await instancesApi.get(created.id);
            setInstance(loadedInstance);

            setStatus(`Started survey instance #${created.id}`);
        } catch (e) {
            setError(e.message);
        } finally {
            setBusy(false);
        }
    }


    /**
     * Async function to refresh current survey instance from te API. Fetches the latest instance data using
     * /api/survey-instances/${id}, updates the component state, and returns the loaded instance.
     * @returns {Promise<*>} Promise resolve to updated survey instance or undefined if the instance ID is not found.
     */
    async function refreshInstance() {
        if (!instance?.id) return;
        const loaded = await instancesApi.get(instance.id);
        setInstance(loaded);
        return loaded;
    }

    /**
     * Navigation function to got to the previous item instance within the survey instance.
     */
    function gotoPrev() {
        if (!itemInstances.length) return;
        setRevealed(false);
        setSelectedAnswer("");
        setCurrentIdx((i) => (i - 1 + itemInstances.length) % itemInstances.length);
    }

    /**
     * Navigation function to go to the next item instance within the survey instance.
     */
    function gotoNext() {
        if (!itemInstances.length) return;
        setRevealed(false);
        setSelectedAnswer("");
        setCurrentIdx((i) => (i + 1) % itemInstances.length);
    }

    /**
     * Navigation function to go to the first unanswered item instance within the survey instance.
     */
    function gotoFirstUnanswered() {
        const idx = itemInstances.findIndex(ii => ii.selectedAnswer == null);
        if (idx >= 0) {
            setCurrentIdx(idx);
            setSelectedAnswer("");
            setRevealed(false);
        }
    }

    /**
     * Async function for submitting an answer for an item instance, attempts posting an answer payload to
     * /api/survey-instances/answer, returns a promise that resolves when the post succeeds or an error is returned.
     * @returns {Promise<void>} Resolves when the answer is successfully posted.
     */
    async function submitCurrentAnswer() {
        setError("");
        setStatus("");

        if (!instance?.id) return setError("Start a survey first.");
        if (!currentInstanceItem || !currentSurveyItem) return setError("No question loaded.");
        if (!selectedAnswer) return setError("Select an answer.");

        // If already answered, just reveal (don’t resubmit)
        if (currentInstanceItem.selectedAnswer != null) {
            setRevealed(true);
            return;
        }

        setBusy(true);
        try {
            await instancesApi.answer({
                surveyInstanceId: instance.id,
                surveyItemInstanceId: currentInstanceItem.id,
                answer: selectedAnswer
            });

            const loaded = await refreshInstance();
            setRevealed(true);
            setStatus("Answer saved.");

            // Auto advance to next unanswered item
            const freshItems = loaded?.itemInstances || [];
            const nextIdx = freshItems.findIndex(ii => ii.selectedAnswer == null);
            if (nextIdx >= 0) {
                setCurrentIdx(nextIdx);
                setSelectedAnswer("");
                setRevealed(false);
            }
        } catch (e) {
            setError(e.message);
        } finally {
            setBusy(false);
        }
    }

    /**
     * Function to fully reset the session by setting state values back to default. Using to reset component state for
     * a survey component.
     */
    function resetSession() {
        if (!shareMode) {
            setSurvey(null);
            setSurveyId("");
        }

        setInstance(null);
        setSelectedAnswer("");
        setCurrentIdx(0);
        setRevealed(false);
        setStatus("");
        setError("");
        setTakerName("");
    }

    return (
        <div className={styles.page}>
            <div className={styles.shell}>
                <div className={styles.topBar}>
                    <div>
                        <h2 className={styles.h2}>
                            {shareMode ? "Take Shared Survey" : "Take Survey"}
                        </h2>
                        <p className={styles.sub}>Answer questions and see results when complete.</p>
                    </div>

                    {instance && (
                        <div className={styles.badges}>
                            <span className={styles.pill}>Instance #{instance.id}</span>
                            <span className={styles.pill}>State: {instance.state}</span>
                            <span className={styles.pill}>Progress: {progress.answered}/{progress.total}</span>
                        </div>
                    )}
                </div>

                <div className={styles.grid}>
                    <section className={styles.card}>
                        <h3 className={styles.sectionTitle}>Start</h3>

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
                                            #{s.id} — {s.title} ({s.state})
                                        </option>
                                    ))}
                                </select>
                            </>
                        )}

                        {shareMode && survey && (
                            <div style={{ marginBottom: 12 }} className={styles.meta}>
                                <strong>Shared survey:</strong> {survey.title}
                            </div>
                        )}

                        <label className={styles.label}>Your name</label>
                        <input
                            value={takerName}
                            onChange={(e) => setTakerName(e.target.value)}
                            placeholder="Jasyn"
                            className={styles.input}
                        />

                        <div className={styles.row}>
                            <button
                                onClick={startInstance}
                                disabled={!canStart}
                                className={`${styles.btn} ${styles.primary}`}
                            >
                                {busy ? "Working…" : "Start"}
                            </button>
                            <button
                                onClick={resetSession}
                                type="button"
                                disabled={busy}
                                className={`${styles.btn} ${styles.ghost}`}
                            >
                                Reset
                            </button>
                        </div>

                        {status && <div className={styles.statusOk}>{status}</div>}
                        {error && <div className={styles.statusErr}>Error: {error}</div>}

                        {/* keep your instance details; give them meta style */}
                        {instance && (
                            <div style={{ marginTop: 12 }}>
                                <div className={styles.meta}><strong>Instance:</strong> #{instance.id}</div>
                                <div className={styles.meta}><strong>State:</strong> {instance.state}</div>
                                <div className={styles.meta}><strong>Progress:</strong> {progress.answered}/{progress.total}</div>
                            </div>
                        )}

                        {instance && (
                            <div className={styles.row}>
                                <button
                                    onClick={gotoFirstUnanswered}
                                    disabled={busy || isComplete}
                                    className={styles.btn}
                                >
                                    Next Unanswered
                                </button>
                                <button
                                    onClick={refreshInstance}
                                    disabled={busy}
                                    className={styles.btn}
                                >
                                    Refresh
                                </button>
                            </div>
                        )}
                    </section>

                    <section className={styles.card}>
                        <h3 className={styles.sectionTitle}>{survey ? survey.title : "Question"}</h3>

                        {!instance && <div className={styles.meta}>Start a survey to begin.</div>}

                        {instance && isComplete && <Results survey={survey} instance={instance} />}

                        {instance && !isComplete && currentSurveyItem && currentInstanceItem && (
                            <>
                                <div className={styles.meta}>
                                    Question {currentIdx + 1} of {progress.total}
                                </div>

                                <div className={styles.qText}>{currentSurveyItem.question}</div>

                                <div className={styles.options}>
                                    {currentSurveyItem.options.map((opt) => {
                                        const alreadyAnswered = currentInstanceItem.selectedAnswer != null;
                                        const checked =
                                            (alreadyAnswered ? currentInstanceItem.selectedAnswer : selectedAnswer) === opt;

                                        return (
                                            <label
                                                key={opt}
                                                className={`${styles.choice} ${busy || alreadyAnswered ? styles.choiceDisabled : ""}`}
                                            >
                                                <input
                                                    type="radio"
                                                    name={`q-${currentInstanceItem.id}`}
                                                    value={opt}
                                                    checked={checked}
                                                    onChange={() => setSelectedAnswer(opt)}
                                                    disabled={busy || alreadyAnswered}
                                                />
                                                <span>{opt}</span>
                                            </label>
                                        );
                                    })}
                                </div>

                                <div className={styles.row}>
                                    <button onClick={gotoPrev} disabled={busy} className={styles.btn}>Prev</button>
                                    <button onClick={gotoNext} disabled={busy} className={styles.btn}>Next</button>
                                    <button onClick={submitCurrentAnswer} disabled={busy} className={`${styles.btn} ${styles.primary}`}>
                                        {currentInstanceItem.selectedAnswer != null ? "Reveal" : "Submit"}
                                    </button>
                                </div>

                                {(revealed || currentInstanceItem.selectedAnswer != null) && (
                                    <AnswerReveal surveyItem={currentSurveyItem} instanceItem={currentInstanceItem} />
                                )}
                            </>
                        )}
                    </section>
                </div>
            </div>
        </div>
    );
}

/**
 * Function to check if a survey item has been answered, checks the survey item instance's answer against the survey
 * item answer to determine if it's correct or not. Returns a response card based on whether the user answered the survey
 * item correct or not.
 * @param surveyItem The individual item of the survey being taken
 * @param instanceItem The instance of the survey item (user answer)
 * @returns {JSX.Element} Correct or incorrect response card
 */
function AnswerReveal({ surveyItem, instanceItem }) {
    const answered = instanceItem.selectedAnswer ?? "(none)";
    const isGradable = surveyItem?.correctAnswer != null;
    const ok = instanceItem.correct === true;

    return (
        <div className={styles.reveal}>
            <div><strong>Your answer:</strong> {answered}</div>

            {isGradable ? (
                <>
                    <div><strong>Correct answer:</strong> {surveyItem.correctAnswer}</div>
                    <div style={{ marginTop: 8 }} className={ok ? styles.ok : styles.bad}>
                        {ok ? "✅ Correct" : "❌ Incorrect"}
                    </div>
                </>
            ) : (
                <div className={styles.meta} style={{ marginTop: 8 }}>
                    Response recorded.
                </div>
            )}
        </div>
    );
}

/**
 * Function to return the result summary for a completed survey instance. Displays the total score and a result
 * card for each survey item.
 * @param survey The survey being taken (survey items and their correct answers).
 * @param instance The instance of the survey being taken (survey with user answers).
 * @returns {JSX.Element} The result summary card
 */
function Results({ survey, instance }) {
    const itemById = new Map((survey?.items || []).map(i => [i.id, i]));
    const items = instance?.itemInstances || [];

    const gradableItems = items.filter(ii => ii.correct != null);
    const gradableTotal = gradableItems.length;
    const correctCount = gradableItems.filter(ii => ii.correct === true).length;

    return (
        <div>
            <h4 style={{ marginTop: 0 }}>Results</h4>

            <div className={styles.pill} style={{ marginBottom: 12 }}>
                {gradableTotal > 0 ? (
                    <>
                        Score:
                        <strong style={{ color: "var(--text", marginLeft: 6}}>
                            {correctCount}/{gradableTotal}
                        </strong>
                    </>
                ) : (
                    <strong style={{ color: "var(--text"}}>Completed</strong>
                )}
            </div>

            <div style={{ display: "grid", gap: 10 }}>
                {items.map(ii => {
                    const item = itemById.get(ii.surveyItemId);
                    const isGradable = item?.correctAnswer != null;

                    return (
                        <div key={ii.id} className={styles.resultCard}>
                            <div style={{ fontWeight: 800 }}>
                                {item?.question || "(unknown question)"}
                            </div>

                            <div className={styles.meta} style={{ marginTop: 8 }}>
                                Your answer:{" "}
                                <strong style={{ color: "var(--text)" }}>{ii.selectedAnswer ?? "(none)"}</strong>
                            </div>

                            {isGradable ? (
                                <div className={styles.meta}>
                                    Correct{" "}
                                    <strong style={{ color: "var(--text" }}>
                                        {item?.correctAnswer ?? "(unknown"}
                                    </strong>{" "}
                                    . {ii.correct === true ? "✅" : "❌"}
                                </div>
                            ) : (
                                <div className={styles.meta}>
                                    Response recorded.
                                </div>
                            )}
                        </div>
                    );
                })}
            </div>

            {!survey && (
                <div className={styles.statusErr} style={{ marginTop: 12 }}>
                    Note: survey details weren’t loaded, so questions/correct answers may show as unknown.
                </div>
            )}
        </div>
    );
}