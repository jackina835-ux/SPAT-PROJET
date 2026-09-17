import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { messageErreur } from "../api/client";
import logoSpat from "../assets/logo-spat.jpg";

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
      <div className="connexion-vitrine">
        <div className="connexion-vitrine-corps">
          <div className="connexion-logo-carte">
            <img src={logoSpat} alt="Logo SPAT" className="connexion-logo-image" />
          </div>
          <div className="connexion-vitrine-texte-bloc">
            <h1 className="connexion-vitrine-titre">Gestion de projets</h1>
            <p className="connexion-vitrine-texte">
              Societe du Port a gestion Autonome de Toamasina
            </p>
            <p className="connexion-vitrine-texte connexion-vitrine-texte-doux">
              Planification, suivi de l'avancement et respect des delais,
              reunis dans une seule plateforme pour toute l'equipe.
            </p>
          </div>
        </div>
      </div>

      <div className="connexion-zone-formulaire">
        <form className="carte-connexion" onSubmit={valider}>
          <h2 className="connexion-formulaire-titre">Connexion</h2>
          <p className="connexion-sous-titre">
            Entrez vos identifiants pour acceder a votre espace.
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
    </div>
  );
}
