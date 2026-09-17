import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import App from "./App.jsx";
import "./index.css";

const themeStocke = localStorage.getItem("theme");
if (themeStocke) {
  document.documentElement.setAttribute("data-theme", themeStocke);
}

createRoot(document.getElementById("root")).render(
  <StrictMode>
    <App />
  </StrictMode>
);
