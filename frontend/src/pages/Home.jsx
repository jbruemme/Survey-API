import styles from "./Home.module.css";

/**
 * Home/landing page component, the default export for Home
 * @returns {JSX.Element} Default layout for the page
 * @constructor
 */
export default function Home() {
    return (
        <div className={`${styles.page} fadeIn`}>
            <div className={styles.shell}>
                <section className={styles.hero}>
                    <div className={`${styles.banner} bannerIn`}>
                        <img src="/pulse-banner.png" alt="Survey Platform banner"/>
                    </div>
                    <div>
                        <h1 className={styles.title}>
                            Create, share, and take surveys in one simple app.
                        </h1>
                        <p className={styles.sub}>
                            Build custom surveys, add multiple-choice questions, collect responses,
                            and review results from a clean dashboard.
                        </p>

                        <div className={styles.actions}>
                            <a href="/builder" className={`${styles.btn} ${styles.primary}`}>
                                Build Survey
                            </a>
                            <a href="/take" className={styles.btn}>
                                Take Survey
                            </a>
                        </div>
                    </div>

                    <div className={styles.videoCard}>
                        <video
                            className={styles.videoEmbed}
                            controls
                            preload="metadata"
                            poster="/pulse-preview.png"
                            >
                            <source src="/Pulse-Demo.mp4" type="video/mp4" />
                            Your browser does not support the video tag.
                        </video>
                    </div>
                </section>

                <div className={styles.sectionDivider}/>

                <section className={styles.section}>
                    <h2>What users can do</h2>

                    <div className={styles.grid}>
                        <div className={styles.card}>
                            <h3>Create surveys</h3>
                            <p>
                                Add questions, define answer options, and build surveys from your question bank.
                            </p>
                        </div>

                        <div className={styles.card}>
                            <h3>Support quizzes or surveys</h3>
                            <p>
                                Questions can have a correct answer for scoring, or no correct answer for feedback-style
                                surveys.
                            </p>
                        </div>

                        <div className={styles.card}>
                            <h3>Take surveys</h3>
                            <p>
                                Users can select a survey, answer questions, and see their progress as they move through
                                the survey.
                            </p>
                        </div>

                        <div className={styles.card}>
                            <h3>Review results</h3>
                            <p>
                                Completed surveys show submitted answers, score summaries, and recorded responses.
                            </p>
                        </div>
                    </div>
                </section>

                <div className={styles.sectionDivider}/>

                <section className={styles.section}>
                    <h2>How it works</h2>

                    <div className={styles.steps}>
                        <div className={styles.step}>
                            <span>1</span>
                            <p>Create questions with answer choices.</p>
                        </div>
                        <div className={styles.step}>
                            <span>2</span>
                            <p>Select questions and publish a survey.</p>
                        </div>
                        <div className={styles.step}>
                            <span>3</span>
                            <p>Users take the survey and submit answers.</p>
                        </div>
                        <div className={styles.step}>
                            <span>4</span>
                            <p>Review completed responses and results.</p>
                        </div>
                    </div>
                </section>

                <div className={styles.sectionDivider}/>

                <section className={styles.section}>
                    <h2>Future implementation</h2>

                    <div className={styles.grid}>
                        <div className={styles.card}>
                            <h3>User state</h3>
                            <p>
                                Categorize surveys by user, allowing users to view their created surveys as well as their
                                taken surveys. Customize dashboard to user preferences.
                            </p>
                        </div>

                        <div className={styles.card}>
                            <h3>Dynamic data analysis</h3>
                            <p>
                                Perform dynamic data analysis on your created surveys.
                            </p>
                        </div>

                        <div className={styles.card}>
                            <h3>Survey searching</h3>
                            <p>
                                Search the database for other user surveys via keyword/subject and view their results.
                            </p>
                        </div>

                        <div className={styles.card}>
                            <h3>Video/image support and free response</h3>
                            <p>
                                Survey options will be expanded to include videos/images and survey questions expanded
                                to include free response answers.
                            </p>
                        </div>
                    </div>
                </section>

            </div>
        </div>
    );
}