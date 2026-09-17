import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { estAdmin } from "../utils/droits";
import Cloche from "./Cloche";
import logoSpat from "../assets/logo-spat.jpg";

const LIBELLE_ROLE = {
  ADMIN: "Administrateur",
  CHEF_PROJET: "Chef de projet",
  MEMBRE: "Membre d'equipe",
};

function IconeSoleil() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="12" r="4" />
      <path d="M12 2v2.5M12 19.5V22M4.2 4.2l1.8 1.8M18 18l1.8 1.8M2 12h2.5M19.5 12H22M4.2 19.8 6 18M18 6l1.8-1.8" />
    </svg>
  );
}

function IconeLune() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8Z" />
    </svg>
  );
}

/** Lit la preference deja posee par main.jsx, ou celle du systeme. */
function themeSombreInitial() {
  const stocke = localStorage.getItem("theme");
  if (stocke) return stocke === "dark";
  return window.matchMedia("(prefers-color-scheme: dark)").matches;
}

export default function Entete() {
  const { utilisateur, seDeconnecter } = useAuth();
  const naviguer = useNavigate();
  const [sombre, setSombre] = useState(themeSombreInitial);

  useEffect(() => {
    document.documentElement.setAttribute("data-theme", sombre ? "dark" : "light");
    localStorage.setItem("theme", sombre ? "dark" : "light");
  }, [sombre]);

  function deconnexion() {
    seDeconnecter();
    naviguer("/connexion");
  }

  return (
    <header className="entete">
      <Link to="/accueil" className="entete-marque">
        <img src={logoSpat} alt="Logo SPAT" className="entete-logo" />
        <span>
          <strong>Gestion de projets</strong>
          <small>SPAT</small>
        </span>
      </Link>

      {utilisateur && (
        <div className="entete-droite">
          <Link to="/projets" className="bouton bouton-discret entete-lien-projets">
            Projets
          </Link>
          {estAdmin(utilisateur) && (
            <Link to="/administration" className="bouton bouton-discret">
              Administration
            </Link>
          )}
          <button
            className="bouton-icone entete-bascule-theme"
            onClick={() => setSombre(!sombre)}
            title={sombre ? "Passer en mode clair" : "Passer en mode sombre"}
          >
            {sombre ? <IconeSoleil /> : <IconeLune />}
          </button>

          <Cloche />

          <div className="entete-profil">
            <strong>{utilisateur.nomComplet}</strong>
            <small>{LIBELLE_ROLE[utilisateur.role] || utilisateur.role}</small>
          </div>

          <button className="bouton bouton-discret" onClick={deconnexion}>
            Deconnexion
          </button>
        </div>
      )}
    </header>
  );
}
