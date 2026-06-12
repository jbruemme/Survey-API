import styles from "./Dashboard.module.css";
import { useEffect, useState } from "react";
import { surveysApi } from "../api/surveys";
import { Share2, Eye, Trash2, MoreHorizontal } from "lucide-react";

const DEV = import.meta.env.DEV;

export default function Dashboard() {
    const [surveys, setSurveys] = useState([]);
    const [shareDataById, setShareDataById] = useState({});
    const [openShareId, setOpenShareId] = useState(null);
    const [error, setError] = useState("");
    const [status, setStatus] = useState("");
    const [openMenuId, setOpenMenuId] = useState(null);
    const [openVisibilityId, setOpenVisibilityId] = useState(null);

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

    async function changeVisibility(id, visibility) {
        try {
            const updatedSurvey =
                await surveysApi.updateVisibility(id, visibility);

            setSurveys(prev =>
                prev.map(survey =>
                    survey.id === id
                        ? {
                            ...survey,
                            visibility: updatedSurvey.visibility
                        }
                        : survey
                )
            );

            setOpenVisibilityId(null);
            setOpenMenuId(null);

            setStatus(
                `Survey visibility updated to ${visibility}.`
            );
        } catch (e) {
            setError(e.message);
        }
    }

    async function deleteSurvey(id) {
        const confirmed = window.confirm("Are you sure you want to delete this survey?");

        if (!confirmed) return;

        try {
            await surveysApi.delete(id);

            setSurveys(prev =>
                prev.filter(survey => survey.id !== id)
            );

            setOpenMenuId(null);
            setOpenVisibilityId(null);
            setOpenShareId(null);

            setStatus("Survey deleted.");
        } catch (e) {
            setError(`Failed to delete survey: ${e.message}`);
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
                                        <div
                                            className={`${styles.badge} ${
                                                styles[survey.visibility.toLowerCase()]
                                            }`}
                                        >
                                            {survey.visibility}
                                        </div>
                                    </div>

                                    <div className={styles.menuWrap}>
                                        <button
                                            className={styles.menuButton}
                                            type="button"
                                            onClick={() =>
                                                setOpenMenuId(openMenuId === survey.id ? null : survey.id)
                                            }
                                        >
                                            <MoreHorizontal size={22} strokeWidth={2.6} />
                                        </button>

                                        {openMenuId === survey.id && (
                                            <div className={styles.menu}>
                                                <button
                                                    type="button"
                                                    className={styles.menuItem}
                                                    onClick={() => {
                                                        setOpenMenuId(null);
                                                        toggleShare(survey.id);
                                                    }}
                                                    disabled={survey.visibility === "PRIVATE"}
                                                    title="Share"
                                                >
                                                    <Share2 size={18}/>
                                                </button>

                                                <div className={styles.visibilityWrap}>
                                                    <button
                                                        type="button"
                                                        className={styles.menuItem}
                                                        onClick={() =>
                                                            setOpenVisibilityId(
                                                                openVisibilityId === survey.id ? null : survey.id
                                                            )
                                                        }
                                                    >
                                                        <Eye size={18}/>
                                                    </button>

                                                    {openVisibilityId === survey.id && (
                                                        <div className={styles.visibilityMenu}>
                                                            <button
                                                                className={styles.visibilityOption}
                                                                onClick={() =>
                                                                    changeVisibility(survey.id, "PRIVATE")
                                                                }
                                                            >
                                                                Private
                                                            </button>

                                                            <button
                                                                className={styles.visibilityOption}
                                                                onClick={() =>
                                                                    changeVisibility(survey.id, "UNLISTED")
                                                                }
                                                            >
                                                                Unlisted
                                                            </button>

                                                            <button
                                                                className={styles.visibilityOption}
                                                                onClick={() =>
                                                                    changeVisibility(survey.id, "PUBLIC")
                                                                }
                                                            >
                                                                Public
                                                            </button>
                                                        </div>
                                                    )}
                                                </div>

                                                <button
                                                    type="button"
                                                    className={`${styles.menuItem} ${styles.danger}`}
                                                    onClick={() => deleteSurvey(survey.id)}
                                                    title="Delete"
                                                >
                                                    <Trash2 size={18}/>
                                                </button>
                                            </div>
                                        )}
                                    </div>
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