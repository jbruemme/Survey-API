import {useMemo, useState} from "react";
import { Link, useNavigate } from "react-router-dom";
import { authApi } from "../api/auth";
import styles from "./Auth.module.css"

export default function Register() {
    const navigate = useNavigate();

    const [displayName, setDisplayName] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [busy, setBusy] = useState(false);

    const passwordChecks = useMemo(() => [
        {
            label: "At least 8 characters",
            met: password.length >= 8,
        },
        {
            label: "One uppercase letter",
            met: /[A-Z]/.test(password),
        },
        {
            label: "One lowercase letter",
            met: /[a-z]/.test(password),
        },
        {
            label: "One number",
            met: /\d/.test(password),
        },
        {
            label: "One special character",
            met: /[@$!%*?&]/.test(password),
        },
    ], [password]);

    async function handleSubmit(e) {
        e.preventDefault();
        setError("");
        setBusy(true);

        try {
            const data = await authApi.register({
                displayName,
                email,
                password
            });

            localStorage.setItem("token", data.token);
            localStorage.setItem("user", JSON.stringify(data.user));
            window.dispatchEvent(new Event("authChanged"));
            navigate("/dashboard");
        } catch (e) {
            setError(e.message);
        } finally {
            setBusy(false);
        }
    }

    return (
        <div className={styles.page}>
            <section className={styles.card}>
                <h2 className={styles.title}>Create your account</h2>
                <p className={styles.sub}>Start building and sharing surveys with Pulse Polling.</p>

                <form className={styles.form} onSubmit={handleSubmit}>
                    <input
                        className={styles.input}
                        value={displayName}
                        onChange={(e) => setDisplayName(e.target.value)}
                        placeholder="Display name"
                    />

                    <input
                        className={styles.input}
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        placeholder="Email"
                        type="email"
                    />

                    <input
                        className={styles.input}
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        placeholder="Password"
                        type="password"
                    />

                    <div className={styles.requirements}>
                        {passwordChecks.map((check) => (
                            <div
                                key={check.label}
                                className={`${styles.requirement} ${
                                    check.met ? styles.met : styles.unmet
                                }`}
                            >
                                {check.met ? "✓" : "•"} {check.label}
                            </div>
                        ))}
                    </div>

                    <button className={styles.button} type="submit" disabled={busy}>
                        {busy ? "Creating account..." : "Register"}
                    </button>
                </form>

                {error && <p style={{color: "red"}}>{error}</p>}

                <div className={styles.footer}>
                    Already have an account? <Link to="/login">Log in</Link>
                </div>
            </section>
        </div>
    );
}