import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

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
      <Link to="/projets" className="entete-marque">
        <span className="entete-logo">GP</span>
        <span>
          <strong>Gestion de projets</strong>
          <small>SPAT</small>
        </span>
      </Link>

      {utilisateur && (
        <div className="entete-droite">
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
