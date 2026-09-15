import { useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import * as apiTaches from "../api/taches";
import * as apiProjets from "../api/projets";
import { messageErreur } from "../api/client";
import PanneauTache from "../components/PanneauTache";

const JOURS = ["Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"];

const MOIS = [
  "Janvier", "Fevrier", "Mars", "Avril", "Mai", "Juin",
  "Juillet", "Aout", "Septembre", "Octobre", "Novembre", "Decembre",
];

/** Transforme un objet Date en chaine "AAAA-MM-JJ". */
function cle(date) {
  const mois = String(date.getMonth() + 1).padStart(2, "0");
  const jour = String(date.getDate()).padStart(2, "0");
  return `${date.getFullYear()}-${mois}-${jour}`;
}

/**
 * Construit la grille du mois : 6 semaines de 7 jours.
 * On commence au lundi qui precede le 1er du mois, et on
 * remplit jusqu'au dimanche qui suit la fin du mois.
 */
function construireGrille(annee, mois) {
  const premier = new Date(annee, mois, 1);

  // getDay() renvoie 0 pour dimanche : on decale pour partir du lundi
  const decalage = (premier.getDay() + 6) % 7;

  const debut = new Date(annee, mois, 1 - decalage);

  const cases = [];
  for (let i = 0; i < 42; i++) {
    const date = new Date(debut);
    date.setDate(debut.getDate() + i);
    cases.push(date);
  }
  return cases;
}

export default function Calendrier() {
  const { projetId } = useParams();

  const aujourdhui = new Date();
  const [annee, setAnnee] = useState(aujourdhui.getFullYear());
  const [mois, setMois] = useState(aujourdhui.getMonth());

  const [taches, setTaches] = useState([]);
  const [projet, setProjet] = useState(null);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState("");
  const [tacheOuverte, setTacheOuverte] = useState(null);

  async function charger() {
    try {
      const [rTaches, rProjet] = await Promise.all([
        apiTaches.listerTachesProjet(projetId),
        apiProjets.consulterProjet(projetId),
      ]);
      setTaches(rTaches.data);
      setProjet(rProjet.data);
      setErreur("");
    } catch (e) {
      setErreur(messageErreur(e, "Impossible de charger le calendrier"));
    } finally {
      setChargement(false);
    }
  }

  useEffect(() => {
    charger();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [projetId]);

  /** Regroupe les taches par date d'echeance, une seule fois. */
  const parDate = useMemo(() => {
    const table = {};
    for (const tache of taches) {
      if (!tache.dateEcheance) continue;
      if (!table[tache.dateEcheance]) table[tache.dateEcheance] = [];
      table[tache.dateEcheance].push(tache);
    }
    return table;
  }, [taches]);

  const grille = useMemo(() => construireGrille(annee, mois), [annee, mois]);

  const sansEcheance = taches.filter((t) => !t.dateEcheance);
  const cleAujourdhui = cle(aujourdhui);

  function moisPrecedent() {
    if (mois === 0) {
      setMois(11);
      setAnnee(annee - 1);
    } else {
      setMois(mois - 1);
    }
  }

  function moisSuivant() {
    if (mois === 11) {
      setMois(0);
      setAnnee(annee + 1);
    } else {
      setMois(mois + 1);
    }
  }

  function revenirAujourdhui() {
    setAnnee(aujourdhui.getFullYear());
    setMois(aujourdhui.getMonth());
  }

  // Compte des echeances tombant dans le mois affiche
  const echeancesDuMois = taches.filter((t) => {
    if (!t.dateEcheance) return false;
    const d = new Date(t.dateEcheance);
    return d.getFullYear() === annee && d.getMonth() === mois;
  }).length;

  if (chargement) {
    return <div className="etat-vide">Chargement du calendrier…</div>;
  }

  return (
    <main className="conteneur conteneur-large">
      <div className="fil-ariane">
        <Link to="/projets">Projets</Link>
        <span>›</span>
        <Link to={`/projets/${projetId}/kanban`}>
          {projet?.nom || "Projet"}
        </Link>
        <span>›</span>
        <span>Calendrier</span>
      </div>

      <div className="bandeau-page">
        <div>
          <h1>Calendrier des echeances</h1>
          <p className="texte-discret">
            {echeancesDuMois} echeance{echeancesDuMois > 1 ? "s" : ""} en{" "}
            {MOIS[mois].toLowerCase()} {annee}
          </p>
        </div>

        <div className="bandeau-actions">
          <Link
            to={`/projets/${projetId}/kanban`}
            className="bouton bouton-discret"
          >
            Voir le tableau
          </Link>
        </div>
      </div>

      {erreur && <div className="alerte">{erreur}</div>}

      <div className="calendrier-barre">
        <button className="bouton bouton-discret" onClick={moisPrecedent}>
          ‹
        </button>
        <strong className="calendrier-mois">
          {MOIS[mois]} {annee}
        </strong>
        <button className="bouton bouton-discret" onClick={moisSuivant}>
          ›
        </button>
        <button
          className="bouton bouton-discret calendrier-aujourdhui"
          onClick={revenirAujourdhui}
        >
          Aujourd'hui
        </button>
      </div>

      <div className="calendrier">
        {JOURS.map((jour) => (
          <div key={jour} className="calendrier-jour-entete">
            {jour}
          </div>
        ))}

        {grille.map((date) => {
          const cleJour = cle(date);
          const duMois = date.getMonth() === mois;
          const estAujourdhui = cleJour === cleAujourdhui;
          const duJour = parDate[cleJour] || [];

          return (
            <div
              key={cleJour}
              className={
                "calendrier-case" +
                (duMois ? "" : " calendrier-case-hors-mois") +
                (estAujourdhui ? " calendrier-case-aujourdhui" : "")
              }
            >
              <span className="calendrier-numero">{date.getDate()}</span>

              <div className="calendrier-taches">
                {duJour.slice(0, 3).map((tache) => (
                  <button
                    key={tache.id}
                    className={
                      "puce-tache" +
                      ` puce-${tache.priorite}` +
                      (tache.statut === "TERMINEE" ? " puce-terminee" : "") +
                      (tache.enRetard ? " puce-retard" : "")
                    }
                    onClick={() => setTacheOuverte(tache.id)}
                    title={
                      tache.titre +
                      (tache.assigneA ? ` — ${tache.assigneA.nomComplet}` : "")
                    }
                  >
                    {tache.titre}
                  </button>
                ))}

                {duJour.length > 3 && (
                  <span className="texte-discret petit">
                    +{duJour.length - 3} autre{duJour.length - 3 > 1 ? "s" : ""}
                  </span>
                )}
              </div>
            </div>
          );
        })}
      </div>

      <div className="calendrier-legende">
        <span><i className="puce-legende puce-URGENTE" /> Urgente</span>
        <span><i className="puce-legende puce-HAUTE" /> Haute</span>
        <span><i className="puce-legende puce-MOYENNE" /> Moyenne</span>
        <span><i className="puce-legende puce-BASSE" /> Basse</span>
        <span><i className="puce-legende puce-terminee" /> Terminee</span>
      </div>

      {sansEcheance.length > 0 && (
        <section className="carte section-sans-echeance">
          <h3 className="panneau-sous-titre">
            Sans echeance
            <span className="compteur">{sansEcheance.length}</span>
          </h3>
          <p className="texte-discret petit">
            Ces taches n'apparaissent dans aucun mois.
          </p>
          <div className="liste-puces">
            {sansEcheance.map((tache) => (
              <button
                key={tache.id}
                className={`puce-tache puce-${tache.priorite}`}
                onClick={() => setTacheOuverte(tache.id)}
              >
                {tache.titre}
              </button>
            ))}
          </div>
        </section>
      )}

      {tacheOuverte && (
        <PanneauTache
          tacheId={tacheOuverte}
          projet={projet}
          onFermer={() => setTacheOuverte(null)}
          onChangement={charger}
        />
      )}
    </main>
  );
}
