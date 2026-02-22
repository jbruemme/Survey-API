import { BrowserRouter, Routes, Route, Link, NavLink } from "react-router-dom";
import TakeSurvey from "./pages/TakeSurvey";

/**
 * Simple home page placeholder
 * @returns {JSX.Element} Default home page
 * @constructor
 */
function Home() {
    return (
        <div style={{ maxWidth: 1100, margin: "0 auto", padding: 18 }}>
            <h2 style={{ marginTop: 0 }}>Survey App</h2>
            <p style={{ color: "var(--muted)" }}>Welcome to your Survey Platform.</p>
        </div>
    );
}

/**
 * Simple Builder page placeholder
 * @returns {JSX.Element} Default builder page
 * @constructor
 */
function Builder() {
    return (
        <div style={{ maxWidth: 1100, margin: "0 auto", padding: 18 }}>
            <h2 style={{ marginTop: 0 }}>Survey Builder</h2>
            <p style={{ color: "var(--muted)" }}>Builder UI coming next.</p>
        </div>
    );
}

const navWrap = {
    borderBottom: "1px solid var(--border)",
    background: "var(--panel)",
};

const navInner = {
    maxWidth: 1100,
    margin: "0 auto",
    padding: "12px 18px",
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
    gap: 16,
};

const navLinks = {
    display: "flex",
    gap: 10,
    alignItems: "center",
    flexWrap: "wrap",
};

const brand = { fontWeight: 800, letterSpacing: 0.2 };

const linkStyle = ({ isActive }) => ({
    padding: "8px 10px",
    borderRadius: 10,
    border: "1px solid var(--border-soft)",
    background: isActive ? "rgba(88,101,242,0.18)" : "var(--panel2)",
    textDecoration: "none",
});

/**
 * Application routing
 * @returns {JSX.Element} Initial application layout
 * @constructor
 */
export default function App() {
    return (
        <BrowserRouter>
            <header style={navWrap}>
                <div style={navInner}>
                    <Link to="/" style={brand}>Survey Platform</Link>

                    <nav style={navLinks}>
                        <NavLink to="/" style={linkStyle}>Home</NavLink>
                        <NavLink to="/builder" style={linkStyle}>Builder</NavLink>
                        <NavLink to="/take" style={linkStyle}>Take Survey</NavLink>
                    </nav>
                </div>
            </header>

            <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/builder" element={<Builder />} />
                <Route path="/take" element={<TakeSurvey />} />
            </Routes>
        </BrowserRouter>
    );
}