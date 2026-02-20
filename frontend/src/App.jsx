import { useEffect, useState } from "react";

export default function App() {
    const [surveys, setSurveys] = useState([]);
    const [err, setErr] = useState("");

    useEffect(() => {
        (async () => {
            try {
                const res = await fetch("/api/surveys");
                const data = await res.json();
                if (!res.ok) throw new Error(data?.message || res.statusText);
                setSurveys(data);
            } catch (e) {
                setErr(e.message);
            }
        })();
    }, []);

    return (
        <div style={{ padding: 16 }}>
            <h1>Survey SPA</h1>

            {err && <div style={{ color: "crimson" }}>Error: {err}</div>}

            <h2>Surveys</h2>
            <ul>
                {surveys.map(s => (
                    <li key={s.id}>
                        #{s.id} — {s.title} ({s.state})
                    </li>
                ))}
            </ul>
        </div>
    );
}