import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import * as apiProjets from "../api/projets";
import { messageErreur } from "../api/client";
import { useAuth } from "../context/AuthContext";
import { peutCreerProjet } from "../utils/droits";

const LIBELLE_STATUT = {
  PLANIFIE: "Planifie",
  EN_COURS: "En cours",
  TERMINE: "Termine",
};

function formaterDate(valeur) {
  if (!valeur) return "—";
  return new Date(valeur).toLocaleDateString("fr-FR", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}

export default function ListeProjets() {
  const [projets, setProjets] = useState([]);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState("");
  const [formulaireOuvert, setFormulaireOuvert] = useState(false);
  const [ongletArchives, setOngletArchives] = useState(false);
  const { utilisateur } = useAuth();

  const autoriseACreer = peutCreerProjet(utilisateur);

  const [nouveau, setNouveau] = useState({
    nom: "",
    description: "",
    dateDebut: "",
    dateFin: "",
  });

  useEffect(() => {
    charger();
  }, []);

  async function charger() {
    setChargement(true);
    try {
      const { data } = await apiProjets.listerProjets();
      setProjets(data);
      setErreur("");
    } catch (e) {
      setErreur(messageErreur(e, "Impossible de charger les projets"));
    } finally {
      setChargement(false);
    }
  }

  async function creer(evenement) {
    evenement.preventDefault();
    try {
      await apiProjets.creerProjet({
        nom: nouveau.nom,
        description: nouveau.description || null,
        dateDebut: nouveau.dateDebut || null,
        dateFin: nouveau.dateFin || null,
        statut: "PLANIFIE",
        responsableId: utilisateur.id,
      });
      setNouveau({ nom: "", description: "", dateDebut: "", dateFin: "" });
      setFormulaireOuvert(false);
      charger();
    } catch (e) {
      setErreur(messageErreur(e, "Creation impossible"));
    }
  }

  // Un projet termine ne se voit plus au quotidien : il est range
  // dans les archives, consultable a la demande.
  const projetsActifs = projets.filter((p) => p.statut !== "TERMINE");
  const projetsArchives = projets.filter((p) => p.statut === "TERMINE");
  const projetsAffiches = ongletArchives ? projetsArchives : projetsActifs;

  return (
    <main className="conteneur">
      <div className="bandeau-page">
        <div>
          <h1>Projets</h1>
          <p className="texte-discret">
            {projets.length} projet{projets.length > 1 ? "s" : ""} au total
          </p>
        </div>

        {autoriseACreer && (
          <button
            className="bouton bouton-principal"
            onClick={() => setFormulaireOuvert(!formulaireOuvert)}
          >
            {formulaireOuvert ? "Annuler" : "Nouveau projet"}
          </button>
        )}
      </div>

      {erreur && <div className="alerte">{erreur}</div>}

      <div className="onglets-projets">
        <button
          className={`onglet ${!ongletArchives ? "onglet-actif" : ""}`}
          onClick={() => setOngletArchives(false)}
        >
          Actifs
          <span className="compteur">{projetsActifs.length}</span>
        </button>
        <button
          className={`onglet ${ongletArchives ? "onglet-actif" : ""}`}
          onClick={() => setOngletArchives(true)}
        >
          Archives
          <span className="compteur">{projetsArchives.length}</span>
        </button>
      </div>

      {formulaireOuvert && autoriseACreer && (
        <form className="carte formulaire" onSubmit={creer}>
          <label className="champ">
            <span>Nom du projet</span>
            <input
              value={nouveau.nom}
              onChange={(e) => setNouveau({ ...nouveau, nom: e.target.value })}
              placeholder="Campagne de sensibilisation"
              required
            />
          </label>

          <label className="champ">
            <span>Description</span>
            <textarea
              rows="2"
              value={nouveau.description}
              onChange={(e) =>
                setNouveau({ ...nouveau, description: e.target.value })
              }
              placeholder="Objectif du projet"
            />
          </label>

          <div className="ligne-champs">
            <label className="champ">
              <span>Date de debut</span>
              <input
                type="date"
                value={nouveau.dateDebut}
                onChange={(e) =>
                  setNouveau({ ...nouveau, dateDebut: e.target.value })
                }
              />
            </label>
            <label className="champ">
              <span>Date de fin</span>
              <input
                type="date"
                value={nouveau.dateFin}
                onChange={(e) =>
                  setNouveau({ ...nouveau, dateFin: e.target.value })
                }
              />
            </label>
          </div>

          <button className="bouton bouton-principal">Creer le projet</button>
        </form>
      )}

      {chargement ? (
        <div className="etat-vide">Chargement des projets…</div>
      ) : projetsAffiches.length === 0 ? (
        <div className="etat-vide">
          {ongletArchives
            ? "Aucun projet archive pour l'instant."
            : autoriseACreer
            ? "Aucun projet pour l'instant. Creez le premier."
            : "Aucun projet ne vous a encore ete confie."}
        </div>
      ) : (
        <div className="grille-projets">
          {projetsAffiches.map((projet) => (
            <Link
              key={projet.id}
              to={`/projets/${projet.id}/kanban`}
              className="carte carte-projet"
            >
              <div className="carte-projet-haut">
                <span className={`etiquette etiquette-${projet.statut}`}>
                  {LIBELLE_STATUT[projet.statut] || projet.statut}
                </span>
                {projet.enRetard && (
                  <span className="etiquette etiquette-retard">En retard</span>
                )}
                {projet.responsable?.id === utilisateur?.id && (
                  <span className="etiquette etiquette-moi">Je dirige</span>
                )}
              </div>

              <h2>{projet.nom}</h2>
              <p className="texte-discret carte-projet-description">
                {projet.description || "Aucune description"}
              </p>

              <div className="barre-progression">
                <div
                  className="barre-progression-remplie"
                  style={{ width: `${projet.avancement}%` }}
                />
              </div>
              <p className="texte-discret petit">
                {projet.avancement}% — {projet.nombreTaches} tache
                {projet.nombreTaches > 1 ? "s" : ""}
              </p>

              <div className="carte-projet-bas">
                <span className="texte-discret petit">
                  Echeance : {formaterDate(projet.dateFin)}
                </span>
                <div className="pastilles">
                  {projet.membres.slice(0, 4).map((m) => (
                    <span key={m.id} className="pastille" title={m.nomComplet}>
                      {m.nomComplet.charAt(0)}
                    </span>
                  ))}
                  {projet.membres.length > 4 && (
                    <span className="pastille pastille-plus">
                      +{projet.membres.length - 4}
                    </span>
                  )}
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </main>
  );
}
