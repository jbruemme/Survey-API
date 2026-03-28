import { BrowserRouter, Routes, Route, Link, NavLink } from "react-router-dom";
import "./styles/navbar.css"
import TakeSurvey from "./pages/TakeSurvey";
import CreateSurvey from "./pages/CreateSurvey";
import logo from "./assets/pulse_logo.png"
import favicon from "./assets/pulse_favicon.png"
import navLogo from "./assets/Pulse_logo_navbar.png"

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
 * Application routing
 * @returns {JSX.Element} Initial application layout
 * @constructor
 */
export default function App() {
    return (
        <BrowserRouter>
            <header className="navbar">
                <div className="navbarInner">

                    <Link to="/" className="brand">
                        <img src={favicon} alt="Pulse" className="logo"/>
                    </Link>

                    <nav className="navLinks">
                        <NavLink to="/" className={({isActive}) => isActive ? "navLink active" : "navLink"}>
                            Home
                        </NavLink>

                        <NavLink to="/builder" className={({isActive}) => isActive ? "navLink active" : "navLink"}>
                            Builder
                        </NavLink>

                        <NavLink to="/take" className={({isActive}) => isActive ? "navLink active" : "navLink"}>
                            Take Survey
                        </NavLink>
                    </nav>

                </div>
            </header>

            <Routes>
                <Route path="*" element={<Home/>}/>
                <Route path="/" element={<Home/>}/>
                <Route path="/take" element={<TakeSurvey/>}/>
                <Route path="/builder" element={<CreateSurvey/>}/>
            </Routes>
        </BrowserRouter>
    );
}