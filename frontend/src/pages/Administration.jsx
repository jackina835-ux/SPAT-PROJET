import { useEffect, useState } from "react";
import { Navigate } from "react-router-dom";
import * as apiAuth from "../api/auth";
import * as apiUtilisateurs from "../api/utilisateurs";
import * as apiPieces from "../api/pieces";
import { messageErreur } from "../api/client";
import { useAuth } from "../context/AuthContext";
import { estAdmin, LIBELLE_ROLE } from "../utils/droits";

/** "3,4 Mo", "128 Ko" : lisible pour un rapport de nettoyage. */
function formaterOctets(octets) {
  if (octets < 1024) return `${octets} o`;
  if (octets < 1024 * 1024) return `${Math.round(octets / 1024)} Ko`;
  return `${(octets / (1024 * 1024)).toFixed(1)} Mo`;
}

const COMPTE_VIDE = {
  nom: "",
  prenom: "",
  email: "",
  motDePasse: "",
  role: "MEMBRE",
};

/**
 * Ecran d'administration des comptes : creation et activation.
 * Reserve a l'administrateur. La protection reelle est cote
 * serveur (@PreAuthorize sur /api/utilisateurs/tous et
 * /api/auth/inscription) ; cette page se contente de ne pas
 * s'afficher pour les autres roles.
 */
export default function Administration() {
  const { utilisateur } = useAuth();

  const [comptes, setComptes] = useState([]);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState("");
  const [succes, setSucces] = useState("");
  const [enCours, setEnCours] = useState(null);

  const [formulaireOuvert, setFormulaireOuvert] = useState(false);
  const [nouveau, setNouveau] = useState(COMPTE_VIDE);
  const [creation, setCreation] = useState(false);

  const [nettoyage, setNettoyage] = useState(false);
  const [rapportNettoyage, setRapportNettoyage] = useState(null);

  async function charger() {
    setChargement(true);
    try {
      const { data } = await apiUtilisateurs.listerTousUtilisateurs();
      setComptes(data);
      setErreur("");
    } catch (e) {
      setErreur(messageErreur(e, "Impossible de charger les comptes"));
    } finally {
      setChargement(false);
    }
  }

  useEffect(() => {
    if (estAdmin(utilisateur)) charger();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (!estAdmin(utilisateur)) {
    return <Navigate to="/accueil" replace />;
  }

  async function creer(evenement) {
    evenement.preventDefault();
    setCreation(true);
    setSucces("");
    try {
      await apiAuth.inscription(nouveau);
      setNouveau(COMPTE_VIDE);
      setFormulaireOuvert(false);
      await charger();
      setSucces("Compte cree.");
      setErreur("");
    } catch (e) {
      setErreur(messageErreur(e, "Creation impossible"));
    } finally {
      setCreation(false);
    }
  }

  async function basculer(compte) {
    const action = compte.actif ? "desactiver" : "reactiver";
    if (!window.confirm(`Confirmer : ${action} le compte de ${compte.nomComplet} ?`)) {
      return;
    }
    setEnCours(compte.id);
    setSucces("");
    try {
      await apiUtilisateurs.basculerActivation(compte.id);
      await charger();
      setErreur("");
    } catch (e) {
      setErreur(messageErreur(e, "Action impossible"));
    } finally {
      setEnCours(null);
    }
  }

  async function lancerNettoyage() {
    if (!window.confirm(
      "Effacer du disque tous les fichiers qui n'ont plus de ligne "
      + "correspondante en base ? Cette action est irreversible."
    )) {
      return;
    }
    setNettoyage(true);
    setSucces("");
    setRapportNettoyage(null);
    try {
      const { data } = await apiPieces.nettoyerOrphelins();
      setRapportNettoyage(data);
      setErreur("");
    } catch (e) {
      setErreur(messageErreur(e, "Nettoyage impossible"));
    } finally {
      setNettoyage(false);
    }
  }

  return (
    <main className="conteneur">
      <div className="bandeau-page">
        <div>
          <h1>Administration des comptes</h1>
          <p className="texte-discret">
            {comptes.length} compte{comptes.length > 1 ? "s" : ""} ·{" "}
            {comptes.filter((c) => c.actif).length} actif
            {comptes.filter((c) => c.actif).length > 1 ? "s" : ""}
          </p>
        </div>

        <button
          className="bouton bouton-principal"
          onClick={() => setFormulaireOuvert(!formulaireOuvert)}
        >
          {formulaireOuvert ? "Annuler" : "Nouveau compte"}
        </button>
      </div>

      {erreur && <div className="alerte">{erreur}</div>}
      {succes && <div className="succes">{succes}</div>}

      {formulaireOuvert && (
        <form className="carte formulaire" onSubmit={creer}>
          <div className="ligne-champs">
            <label className="champ">
              <span>Prenom</span>
              <input
                value={nouveau.prenom}
                onChange={(e) =>
                  setNouveau({ ...nouveau, prenom: e.target.value })
                }
                required
              />
            </label>
            <label className="champ">
              <span>Nom</span>
              <input
                value={nouveau.nom}
                onChange={(e) =>
                  setNouveau({ ...nouveau, nom: e.target.value })
                }
                required
              />
            </label>
          </div>

          <label className="champ">
            <span>Adresse electronique</span>
            <input
              type="email"
              value={nouveau.email}
              onChange={(e) =>
                setNouveau({ ...nouveau, email: e.target.value })
              }
              placeholder="prenom@corompre.mg"
              required
            />
          </label>

          <div className="ligne-champs">
            <label className="champ">
              <span>Mot de passe</span>
              <input
                type="password"
                value={nouveau.motDePasse}
                onChange={(e) =>
                  setNouveau({ ...nouveau, motDePasse: e.target.value })
                }
                placeholder="8 caracteres minimum"
                minLength={8}
                required
              />
            </label>
            <label className="champ">
              <span>Role</span>
              <select
                value={nouveau.role}
                onChange={(e) =>
                  setNouveau({ ...nouveau, role: e.target.value })
                }
              >
                <option value="MEMBRE">Membre d'equipe</option>
                <option value="CHEF_PROJET">Chef de projet</option>
                <option value="ADMIN">Administrateur</option>
              </select>
            </label>
          </div>

          <button className="bouton bouton-principal" disabled={creation}>
            {creation ? "Creation…" : "Creer le compte"}
          </button>
        </form>
      )}

      {chargement ? (
        <div className="etat-vide">Chargement des comptes…</div>
      ) : (
        <div className="liste-comptes">
          {comptes.map((compte) => (
            <article
              key={compte.id}
              className={`carte ligne-compte ${!compte.actif ? "ligne-compte-inactif" : ""}`}
            >
              <span className="pastille pastille-grande">
                {compte.nomComplet.charAt(0)}
              </span>

              <div className="compte-identite">
                <strong>{compte.nomComplet}</strong>
                <span className="texte-discret petit">{compte.email}</span>
              </div>

              <span className="etiquette etiquette-role">
                {LIBELLE_ROLE[compte.role] || compte.role}
              </span>

              <span
                className={`etiquette ${
                  compte.actif ? "etiquette-TERMINE" : "etiquette-retard"
                }`}
              >
                {compte.actif ? "Actif" : "Desactive"}
              </span>

              {compte.id === utilisateur.id ? (
                <span className="texte-discret petit">Vous</span>
              ) : (
                <button
                  className="bouton bouton-discret"
                  disabled={enCours === compte.id}
                  onClick={() => basculer(compte)}
                >
                  {compte.actif ? "Desactiver" : "Reactiver"}
                </button>
              )}
            </article>
          ))}
        </div>
      )}

      <section className="carte section-maintenance">
        <h3 className="panneau-sous-titre">Maintenance</h3>
        <p className="texte-discret petit">
          Efface du disque les fichiers qui n'ont plus de ligne
          correspondante en base (upload interrompu, ligne supprimee
          manuellement...).
        </p>

        <button
          className="bouton bouton-discret"
          onClick={lancerNettoyage}
          disabled={nettoyage}
        >
          {nettoyage ? "Nettoyage…" : "Nettoyer les fichiers orphelins"}
        </button>

        {rapportNettoyage && (
          <p className="texte-discret petit maintenance-rapport">
            {rapportNettoyage.fichiersSupprimes === 0
              ? "Aucun fichier orphelin trouve."
              : `${rapportNettoyage.fichiersSupprimes} fichier${
                  rapportNettoyage.fichiersSupprimes > 1 ? "s" : ""
                } supprime${rapportNettoyage.fichiersSupprimes > 1 ? "s" : ""}, `
                + `${formaterOctets(rapportNettoyage.octetsLiberes)} liberes.`}
          </p>
        )}
      </section>
    </main>
  );
}
