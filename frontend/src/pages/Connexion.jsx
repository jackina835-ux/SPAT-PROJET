import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { messageErreur } from "../api/client";

export default function Connexion() {
  const [email, setEmail] = useState("");
  const [motDePasse, setMotDePasse] = useState("");
  const [erreur, setErreur] = useState("");
  const [envoi, setEnvoi] = useState(false);

  const { seConnecter } = useAuth();
  const naviguer = useNavigate();

  async function valider(evenement) {
    evenement.preventDefault();
    setErreur("");
    setEnvoi(true);
    try {
      await seConnecter(email, motDePasse);
      naviguer("/accueil");
    } catch (e) {
      setErreur(messageErreur(e, "Connexion impossible"));
    } finally {
      setEnvoi(false);
    }
  }

  return (
    <div className="page-connexion">
      <form className="carte-connexion" onSubmit={valider}>
        <div className="connexion-logo">GP</div>
        <h1>Gestion de projets</h1>
        <p className="connexion-sous-titre">
          Plateforme de suivi des projets et des taches — SPAT
        </p>

        {erreur && <div className="alerte">{erreur}</div>}

        <label className="champ">
          <span>Adresse electronique</span>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="prenom@corompre.mg"
            required
            autoFocus
          />
        </label>

        <label className="champ">
          <span>Mot de passe</span>
          <input
            type="password"
            value={motDePasse}
            onChange={(e) => setMotDePasse(e.target.value)}
            placeholder="Votre mot de passe"
            required
          />
        </label>

        <button className="bouton bouton-principal bouton-large" disabled={envoi}>
          {envoi ? "Connexion…" : "Se connecter"}
        </button>
      </form>
    </div>
  );
}
