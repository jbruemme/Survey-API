import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { authApi } from "../api/auth";
import styles from "./Auth.module.css"

export default function Login() {
    const navigate = useNavigate();

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [busy, setBusy] = useState(false);

    async function handleSubmit(e) {
        e.preventDefault();
        setError("");
        setBusy(true);

        try {
            const data = await authApi.login({ email, password });

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
                <h2 className={styles.title}>Welcome back</h2>
                <p className={styles.sub}>Log in to manage and share your surveys.</p>


                <form className={styles.form} onSubmit={handleSubmit}>
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

                    <button className={styles.button} type="submit" disabled={busy}>
                        {busy ? "Logging in..." : "Login"}
                    </button>
                </form>

                {error && <p style={{color: "red"}}>{error}</p>}

                <div className={styles.footer}>
                    New to Pulse Polling? <Link to="/register">Create an account</Link>
                </div>
            </section>
        </div>
    );
}