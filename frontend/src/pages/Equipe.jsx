import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import * as apiEquipes from "../api/equipes";
import { messageErreur } from "../api/client";

const LIBELLE_NIVEAU = {
  LIBRE: "Disponible",
  NORMAL: "Charge normale",
  CHARGE: "Bien occupe",
  SURCHARGE: "Surcharge",
};

const LIBELLE_STATUT = {
  A_FAIRE: "A faire",
  EN_COURS: "En cours",
  EN_REVISION: "En revision",
  TERMINEE: "Terminee",
};

const LIBELLE_PRIORITE = {
  BASSE: "Basse",
  MOYENNE: "Moyenne",
  HAUTE: "Haute",
  URGENTE: "Urgente",
};

/** Texte lisible pour la prochaine echeance, en jours. */
function texteEcheance(jours) {
  if (jours === null || jours === undefined) return "Aucune echeance";
  if (jours < 0) return `Depassee de ${Math.abs(jours)} j`;
  if (jours === 0) return "Echeance aujourd'hui";
  if (jours === 1) return "Echeance demain";
  return `Prochaine echeance dans ${jours} j`;
}

export default function Equipe() {
  const { projetId } = useParams();

  const [equipe, setEquipe] = useState(null);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState("");

  useEffect(() => {
    let annule = false;

    async function charger() {
      setChargement(true);
      try {
        const { data } = await apiEquipes.chargerEquipeProjet(projetId);
        if (!annule) {
          setEquipe(data);
          setErreur("");
        }
      } catch (e) {
        if (!annule) {
          setErreur(messageErreur(e, "Impossible de charger la repartition"));
        }
      } finally {
        if (!annule) setChargement(false);
      }
    }

    charger();
    return () => {
      annule = true;
    };
  }, [projetId]);

  if (chargement) {
    return <div className="etat-vide">Calcul de la repartition…</div>;
  }
  if (!equipe) {
    return (
      <main className="conteneur">
        <div className="alerte">{erreur || "Donnees indisponibles"}</div>
        <Link to="/projets" className="bouton bouton-discret">
          Retour aux projets
        </Link>
      </main>
    );
  }

  // Sert d'echelle pour les barres : le plus charge occupe toute la largeur.
  const maximum = Math.max(
    1,
    ...equipe.membres.map((m) => m.heuresEstimees)
  );

  return (
    <main className="conteneur">
      <div className="fil-ariane">
        <Link to="/projets">Projets</Link>
        <span>›</span>
        <Link to={`/projets/${projetId}/kanban`}>{equipe.projetNom}</Link>
        <span>›</span>
        <span>Equipe</span>
      </div>

      <div className="bandeau-page">
        <div>
          <h1>Repartition de la charge</h1>
          <p className="texte-discret">
            {equipe.membres.length} membre{equipe.membres.length > 1 ? "s" : ""} ·{" "}
            {equipe.heuresTotalActives} h restantes ·{" "}
            {equipe.heuresMoyennesParMembre} h en moyenne
          </p>
        </div>
        <Link
          to={`/projets/${projetId}/kanban`}
          className="bouton bouton-discret"
        >
          Voir le tableau
        </Link>
      </div>

      {erreur && <div className="alerte">{erreur}</div>}

      {equipe.tachesNonAssignees > 0 && (
        <p className="note-lecture">
          {equipe.tachesNonAssignees} tache
          {equipe.tachesNonAssignees > 1 ? "s ne sont" : " n'est"} assignee
          {equipe.tachesNonAssignees > 1 ? "s" : ""} a personne.
        </p>
      )}

      {equipe.membres.length === 0 ? (
        <div className="etat-vide">Aucun membre affecte a ce projet.</div>
      ) : (
        <div className="liste-charges">
          {equipe.membres.map((charge) => (
            <article key={charge.membre.id} className="carte carte-charge">
              <div className="charge-entete">
                <span className="pastille pastille-grande">
                  {charge.membre.nomComplet.charAt(0)}
                </span>
                <div className="charge-identite">
                  <strong>{charge.membre.nomComplet}</strong>
                  <small className="texte-discret">{charge.membre.email}</small>
                </div>
                <span className={`etiquette etiquette-niv-${charge.niveau}`}>
                  {LIBELLE_NIVEAU[charge.niveau]}
                </span>
              </div>

              <div className="charge-chiffres">
                <div>
                  <strong>{charge.heuresEstimees}</strong>
                  <small className="texte-discret">heures restantes</small>
                </div>
                <div>
                  <strong>{charge.tachesActives}</strong>
                  <small className="texte-discret">taches en cours</small>
                </div>
                <div>
                  <strong>{charge.tachesTerminees}</strong>
                  <small className="texte-discret">terminees</small>
                </div>
                <div>
                  <strong
                    className={charge.tachesEnRetard > 0 ? "texte-alerte" : undefined}
                  >
                    {charge.tachesEnRetard}
                  </strong>
                  <small className="texte-discret">en retard</small>
                </div>
              </div>

              <div className="barre-progression">
                <div
                  className={`barre-progression-remplie barre-niv-${charge.niveau}`}
                  style={{
                    width: `${(charge.heuresEstimees / maximum) * 100}%`,
                  }}
                />
              </div>

              <p className="texte-discret petit charge-echeance">
                {texteEcheance(charge.prochaineEcheance)}
              </p>

              {charge.tachesActives > 0 && (
                <div className="charge-details">
                  <div>
                    <span className="charge-details-titre">Par statut</span>
                    <ul>
                      {Object.entries(charge.parStatut)
                        .filter(([cle, valeur]) => valeur > 0 && cle !== "TERMINEE")
                        .map(([cle, valeur]) => (
                          <li key={cle}>
                            <span className={`point point-${cle}`} />
                            {LIBELLE_STATUT[cle]} · {valeur}
                          </li>
                        ))}
                    </ul>
                  </div>

                  <div>
                    <span className="charge-details-titre">Par priorite</span>
                    <ul>
                      {Object.entries(charge.parPriorite)
                        .filter(([, valeur]) => valeur > 0)
                        .map(([cle, valeur]) => (
                          <li key={cle}>
                            <span className={`etiquette etiquette-${cle}`}>
                              {LIBELLE_PRIORITE[cle]}
                            </span>
                            {valeur}
                          </li>
                        ))}
                    </ul>
                  </div>
                </div>
              )}
            </article>
          ))}
        </div>
      )}
    </main>
  );
}
