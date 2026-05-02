import styles from "./Dashboard.module.css";
import { useEffect, useState } from "react";
import { surveysApi } from "../api/surveys";

const DEV = import.meta.env.DEV;

export default function Dashboard() {
    const [surveys, setSurveys] = useState([]);
    const [shareDataById, setShareDataById] = useState({});
    const [openShareId, setOpenShareId] = useState(null);
    const [error, setError] = useState("");
    const [status, setStatus] = useState("");

    useEffect(() => {
        (async () => {
            try {
                const list = await surveysApi.list();
                setSurveys(list);
            } catch (e) {
                setError(`Failed to load surveys: ${e.message}`);
            }
        })();
    }, []);

    async function toggleShare(surveyId) {
        setError("");
        setStatus("");

        if (openShareId === surveyId) {
            setOpenShareId(null);
            return;
        }

        try {
            if (!shareDataById[surveyId]) {
                const shareData = await surveysApi.getShareLinks(surveyId);
                setShareDataById((prev) => ({
                    ...prev,
                    [surveyId]: shareData,
                }));
            }

            setOpenShareId(surveyId);
        } catch (e) {
            setError(`Failed to load share options: ${e.message}`);
        }
    }

    async function copyLink(publicUrl) {
        try {
            await navigator.clipboard.writeText(publicUrl);
            setStatus("Share link copied.");
        } catch {
            setError("Failed to copy share link.");
        }
    }

    return (
        <div className={`${styles.page} fadeIn`}>
            <div className={styles.shell}>
                <div className={styles.topBar}>
                    <div>
                        <h2 className={styles.h2}>Survey Dashboard</h2>
                        <p className={styles.sub}>Manage and share your created surveys.</p>
                    </div>
                </div>

                {status && <div className={styles.statusOk}>{status}</div>}
                {error && <div className={styles.statusErr}>Error: {error}</div>}

                {!surveys.length && !error && (
                    <div className={styles.empty}>No surveys found yet.</div>
                )}

                <div className={styles.grid}>
                    {surveys.map((survey) => {
                        const share = shareDataById[survey.id];
                        const isOpen = openShareId === survey.id;
                        return (
                            <section key={survey.id} className={styles.card}>
                                <div className={styles.cardHeader}>
                                    <div>
                                        <h3 className={styles.title}>{survey.title}</h3>
                                        <div className={styles.meta}>
                                            #{survey.id} · {survey.state}
                                        </div>
                                    </div>

                                    <button
                                        onClick={() => toggleShare(survey.id)}
                                        className={`${styles.btn} ${styles.primary}`}
                                        type="button"
                                    >
                                        {isOpen ? "Close Share" : "Share"}
                                    </button>
                                </div>

                                {isOpen && share && (
                                    <div className={styles.sharePanel}>
                                        {DEV && (
                                            <pre>{JSON.stringify(share, null, 2)}</pre>
                                        )}
                                        <label className={styles.label}>Public Link</label>
                                        <div className={styles.shareRow}>
                                            <input
                                                className={styles.input}
                                                value={share.publicUrl}
                                                readOnly
                                            />
                                            <button
                                                className={`${styles.btn} ${styles.ghost}`}
                                                type="button"
                                                onClick={() => copyLink(share.publicUrl)}
                                            >
                                                Copy Link
                                            </button>
                                        </div>

                                        <div className={styles.actions}>
                                            <a
                                                href={share.twitterShareUrl}
                                                target="_blank"
                                                rel="noreferrer"
                                                className={styles.linkBtn}
                                            >
                                                Share on X
                                            </a>

                                            <a
                                                href={share.linkedInShareUrl}
                                                target="_blank"
                                                rel="noreferrer"
                                                className={styles.linkBtn}
                                            >
                                                Share on LinkedIn
                                            </a>

                                            <a
                                                href={share.facebookShareUrl}
                                                target="_blank"
                                                rel="noreferrer"
                                                className={styles.linkBtn}
                                            >
                                                Share on Facebook
                                            </a>
                                        </div>
                                    </div>
                                )}
                            </section>
                        );
                    })}
                </div>
            </div>
        </div>
    );
}