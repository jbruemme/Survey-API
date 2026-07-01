import { Link, NavLink, useNavigate} from "react-router-dom";
import {useEffect, useState} from "react";
import favicon from "../assets/pulse_favicon.png"

export default function Navbar() {
    const navigate = useNavigate();

    const [user, setUser] = useState(() => {
        const savedUser = localStorage.getItem("user");
        return savedUser ? JSON.parse(savedUser) : null;
    });

    useEffect(() => {
        function syncUser() {
            const savedUser = localStorage.getItem("user");
            setUser(savedUser ? JSON.parse(savedUser) : null);
        }

        window.addEventListener("authChanged", syncUser);
        return () => window.removeEventListener("authChanged", syncUser);
    }, []);

    function logout() {
        localStorage.removeItem("user");
        localStorage.removeItem("token");
        setUser(null);
        navigate("/login");
    }

    return (
        <header className="navbar">
            <div className="navbarInner">
                <Link to="/" className="brand">
                    <img src={favicon} alt="Pulse" className="logo" />
                </Link>

                <nav className="navLinks">

                    {user ? (
                        <>
                            <NavLink to="/dashboard" className={({ isActive }) => isActive ? "navLink active" : "navLink"}>
                                My Dashboard
                            </NavLink>

                            <NavLink to="/builder" className={({ isActive }) => isActive ? "navLink active" : "navLink"}>
                                Build Survey
                            </NavLink>

                            <NavLink to="/take" className={({ isActive }) => isActive ? "navLink active" : "navLink"}>
                                Take Survey
                            </NavLink>

                            <NavLink
                                to="/login"
                                onClick={logout}
                                className={({ isActive }) => isActive ? "navLink active" : "navLink"}
                            >
                                Logout
                            </NavLink>
                        </>
                    ) : (
                        <>
                            <NavLink to="/login" className={({ isActive }) => isActive ? "navLink active" : "navLink"}>
                                Login
                            </NavLink>

                            <NavLink to="/register" className={({ isActive }) => isActive ? "navLink active" : "navLink"}>
                                Register
                            </NavLink>
                        </>
                    )}
                </nav>
            </div>
        </header>
    );

}