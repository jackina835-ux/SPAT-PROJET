import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { estAdmin } from "../utils/droits";
import Cloche from "./Cloche";

const LIBELLE_ROLE = {
  ADMIN: "Administrateur",
  CHEF_PROJET: "Chef de projet",
  MEMBRE: "Membre d'equipe",
};

export default function Entete() {
  const { utilisateur, seDeconnecter } = useAuth();
  const naviguer = useNavigate();

  function deconnexion() {
    seDeconnecter();
    naviguer("/connexion");
  }

  return (
    <header className="entete">
      <Link to="/accueil" className="entete-marque">
        <span className="entete-logo">GP</span>
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
