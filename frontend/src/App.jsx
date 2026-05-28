import { BrowserRouter, Routes, Route } from "react-router-dom";
import "./styles/navbar.css"
import TakeSurvey from "./pages/TakeSurvey";
import CreateSurvey from "./pages/CreateSurvey";
import Dashboard from "./pages/Dashboard";
import Home from "./pages/Home";
import Login from "./pages/Login";
import Register from "./pages/Register";
import ProtectedRoute from "./components/ProtectedRoute.jsx";
import Navbar from "./components/Navbar.jsx";

/**
 * Application routing
 * @returns {JSX.Element} Initial application layout
 * @constructor
 */
export default function App() {
    return (
        <BrowserRouter>
            <Navbar />

            <Routes>
                <Route path="*" element={<Home/>}/>
                <Route path="/" element={<Home/>}/>
                <Route path="/take" element={<TakeSurvey/>}/>
                <Route path="/builder"
                   element={
                        <ProtectedRoute>
                            <CreateSurvey />
                        </ProtectedRoute>
                    }
               />
                <Route path="/dashboard"
                   element={
                        <ProtectedRoute>
                            <Dashboard />
                        </ProtectedRoute>
                    }
                />
                <Route path="/s/:shareToken" element={<TakeSurvey/>} />
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />
            </Routes>
        </BrowserRouter>
    );
}