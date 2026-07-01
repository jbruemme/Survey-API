import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { surveysApi } from "../api/surveys";
import styles from "./SurveyResults.module.css";

export default function SurveyResults() {
    const { id } = useParams();
    const navigate = useNavigate();

    const [results, setResults] = useState(null);
    const [error, setError] = useState("");
    const [busy, setBusy] = useState(true);

    useEffect(() => {
        async function loadResults() {
            try {
                const data = await surveysApi.getResults(id);
                setResults(data);
            } catch (err) {
                setError(err.message);
            } finally {
                setBusy(false);
            }
        }

        loadResults();
    }, [id]);

    if (busy) {
        return (
            <div className={styles.page}>
                <div className={styles.shell}>Loading results...</div>
            </div>
        );
    }

    if (error) {
        return (
            <div className={styles.page}>
                <div className={styles.shell}>Error: {error}</div>
            </div>
        );
    }

    return (
        <div className={`${styles.page} fadeIn`}>
            <div className={styles.shell}>
                <button
                    className={styles.backBtn}
                    onClick={() => navigate("/dashboard")}
                    type="button"
                >
                    ← Back to dashboard
                </button>

                <div className={styles.header}>
                    <div>
                        <h2>{results.surveyTitle}</h2>
                        <p>Survey results and response analytics.</p>
                    </div>

                    <div className={styles.statCard}>
                        <span>Total Responses</span>
                        <strong>{results.totalResponses}</strong>
                    </div>
                </div>

                <div className={styles.questions}>
                    {results.questions.map((question) => {
                        const topAnswer = question.answers.reduce(
                            (top, answer) =>
                                answer.count > top.count ? answer : top,
                            {
                                answer: "No answers",
                                count: 0,
                                percentage: 0,
                            }
                        );

                        const hasCorrectAnswer = question.answers.some(
                            (answer) => answer.correct !== null
                        );

                        const correctAnswer = question.answers.find(
                            (answer) => answer.correct === true
                        );

                        const correctCount = question.answers
                            .filter((answer) => answer.correct === true)
                            .reduce((sum, answer) => sum + answer.count, 0);

                        const correctRate =
                            question.totalAnswers === 0
                                ? 0
                                : (correctCount * 100) / question.totalAnswers;

                        return (
                            <section
                                key={question.questionId}
                                className={styles.card}
                            >
                                <div className={styles.cardHeader}>
                                    <div>
                                        <h3>{question.question}</h3>
                                        <p>
                                            {question.totalAnswers} answers
                                            recorded
                                        </p>
                                    </div>

                                    {hasCorrectAnswer && correctAnswer && (
                                        <div className={styles.correctBadge}>
                                            Correct: {correctAnswer.answer}
                                        </div>
                                    )}
                                </div>

                                <div className={styles.metricsGrid}>
                                    <div className={styles.metricCard}>
                                        <span>Total answers</span>
                                        <strong>{question.totalAnswers}</strong>
                                    </div>

                                    <div className={styles.metricCard}>
                                        <span>Top answer</span>
                                        <strong>{topAnswer.answer}</strong>
                                        <small>
                                            {topAnswer.percentage.toFixed(1)}%
                                        </small>
                                    </div>

                                    {hasCorrectAnswer && (
                                        <div className={styles.metricCard}>
                                            <span>Correct rate</span>
                                            <strong>
                                                {correctRate.toFixed(1)}%
                                            </strong>
                                            <small>
                                                {correctCount} correct
                                            </small>
                                        </div>
                                    )}
                                </div>

                                <div className={styles.answerBars}>
                                    {question.answers.map((answer) => (
                                        <div
                                            key={answer.answer}
                                            className={styles.answerBarCard}
                                        >
                                            <div
                                                className={
                                                    styles.answerTopLine
                                                }
                                            >
                                                <span
                                                    className={
                                                        styles.answerText
                                                    }
                                                >
                                                    {answer.answer}
                                                </span>

                                                <strong>
                                                    {answer.percentage.toFixed(
                                                        1
                                                    )}
                                                    %
                                                </strong>
                                            </div>

                                            <div
                                                className={
                                                    styles.progressTrack
                                                }
                                            >
                                                <div
                                                    className={`${
                                                        styles.progressFill
                                                    } ${
                                                        answer.correct === true
                                                            ? styles.correctFill
                                                            : answer.correct ===
                                                            false
                                                                ? styles.incorrectFill
                                                                : ""
                                                    }`}
                                                    style={{
                                                        width: `${answer.percentage}%`,
                                                    }}
                                                />
                                            </div>

                                            <div
                                                className={styles.answerMeta}
                                            >
                                                <span>
                                                    {answer.count} votes
                                                </span>

                                                {answer.correct === true && (
                                                    <span
                                                        className={
                                                            styles.correctText
                                                        }
                                                    >
                                                        Correct
                                                    </span>
                                                )}

                                                {answer.correct === false && (
                                                    <span
                                                        className={
                                                            styles.incorrectText
                                                        }
                                                    >
                                                        Incorrect
                                                    </span>
                                                )}
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </section>
                        );
                    })}
                </div>
            </div>
        </div>
    );
}