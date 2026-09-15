import { useEffect, useState } from "react";
import * as apiTaches from "../api/taches";
import * as apiCommentaires from "../api/commentaires";
import { messageErreur } from "../api/client";
import { useAuth } from "../context/AuthContext";
import { estAdmin, estResponsable } from "../utils/droits";

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

function formaterDateLongue(valeur) {
  if (!valeur) return "—";
  return new Date(valeur).toLocaleDateString("fr-FR", {
    day: "2-digit",
    month: "long",
    year: "numeric",
  });
}

/** "il y a 3 heures", "hier", "le 12 septembre" selon l'anciennete. */
function momentRelatif(valeur) {
  if (!valeur) return "";
  const date = new Date(valeur);
  const minutes = Math.floor((Date.now() - date.getTime()) / 60000);

  if (minutes < 1) return "a l'instant";
  if (minutes < 60) return `il y a ${minutes} min`;

  const heures = Math.floor(minutes / 60);
  if (heures < 24) return `il y a ${heures} h`;

  const jours = Math.floor(heures / 24);
  if (jours === 1) return "hier";
  if (jours < 7) return `il y a ${jours} jours`;

  return date.toLocaleDateString("fr-FR", { day: "2-digit", month: "long" });
}

/**
 * Panneau lateral affichant le detail d'une tache
 * et le fil de discussion qui lui est attache.
 */
export default function PanneauTache({ tacheId, projet, onFermer, onChangement }) {
  const { utilisateur } = useAuth();

  const [tache, setTache] = useState(null);
  const [sousTaches, setSousTaches] = useState([]);
  const [commentaires, setCommentaires] = useState([]);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState("");

  const [brouillon, setBrouillon] = useState("");
  const [envoi, setEnvoi] = useState(false);

  const gestionnaire = estAdmin(utilisateur) || estResponsable(utilisateur, projet);
  const estMembre = projet?.membres?.some((m) => m.id === utilisateur?.id);

  useEffect(() => {
    let annule = false;

    async function charger() {
      setChargement(true);
      try {
        const [rTache, rSous, rComm] = await Promise.all([
          apiTaches.consulterTache(tacheId),
          apiTaches.listerSousTaches(tacheId),
          apiCommentaires.listerCommentaires(tacheId),
        ]);
        if (annule) return;
        setTache(rTache.data);
        setSousTaches(rSous.data);
        setCommentaires(rComm.data);
        setErreur("");
      } catch (e) {
        if (!annule) setErreur(messageErreur(e, "Impossible de charger la tache"));
      } finally {
        if (!annule) setChargement(false);
      }
    }

    charger();
    return () => {
      annule = true;
    };
  }, [tacheId]);

  // Fermeture au clavier avec Echap
  useEffect(() => {
    function surTouche(evenement) {
      if (evenement.key === "Escape") onFermer();
    }
    window.addEventListener("keydown", surTouche);
    return () => window.removeEventListener("keydown", surTouche);
  }, [onFermer]);

  async function rechargerCommentaires() {
    const { data } = await apiCommentaires.listerCommentaires(tacheId);
    setCommentaires(data);
  }

  async function publier(evenement) {
    evenement.preventDefault();
    if (!brouillon.trim()) return;

    setEnvoi(true);
    try {
      await apiCommentaires.ecrireCommentaire(
        brouillon.trim(),
        Number(tacheId),
        utilisateur.id
      );
      setBrouillon("");
      await rechargerCommentaires();
      onChangement?.();
      setErreur("");
    } catch (e) {
      setErreur(messageErreur(e, "Publication impossible"));
    } finally {
      setEnvoi(false);
    }
  }

  async function supprimerCommentaire(id) {
    if (!window.confirm("Supprimer ce commentaire ?")) return;
    try {
      await apiCommentaires.supprimerCommentaire(id);
      await rechargerCommentaires();
      onChangement?.();
    } catch (e) {
      setErreur(messageErreur(e, "Suppression impossible"));
    }
  }

  function peutSupprimerCommentaire(commentaire) {
    return (
      estAdmin(utilisateur) ||
      commentaire.auteur?.id === utilisateur?.id ||
      estResponsable(utilisateur, projet)
    );
  }

  return (
    <>
      <div className="voile" onClick={onFermer} />

      <aside className="panneau" role="dialog" aria-modal="true">
        <header className="panneau-entete">
          <span className="panneau-titre-petit">Detail de la tache</span>
          <button className="bouton-icone" onClick={onFermer} title="Fermer">
            ×
          </button>
        </header>

        {chargement ? (
          <div className="etat-vide">Chargement…</div>
        ) : !tache ? (
          <div className="alerte">{erreur || "Tache introuvable"}</div>
        ) : (
          <div className="panneau-corps">
            <div className="panneau-etiquettes">
              <span className={`etiquette etiquette-${tache.priorite}`}>
                {LIBELLE_PRIORITE[tache.priorite]}
              </span>
              <span className="etiquette etiquette-statut">
                {LIBELLE_STATUT[tache.statut]}
              </span>
              {tache.enRetard && (
                <span className="etiquette etiquette-retard">En retard</span>
              )}
            </div>

            <h2 className="panneau-titre">{tache.titre}</h2>

            {tache.description ? (
              <p className="panneau-description">{tache.description}</p>
            ) : (
              <p className="texte-discret petit">Aucune description</p>
            )}

            <dl className="fiche">
              <div>
                <dt>Assignee a</dt>
                <dd>{tache.assigneA ? tache.assigneA.nomComplet : "Personne"}</dd>
              </div>
              <div>
                <dt>Echeance</dt>
                <dd
                  className={tache.enRetard ? "texte-alerte" : undefined}
                >
                  {formaterDateLongue(tache.dateEcheance)}
                </dd>
              </div>
              <div>
                <dt>Charge estimee</dt>
                <dd>
                  {tache.chargeEstimee ? `${tache.chargeEstimee} h` : "—"}
                </dd>
              </div>
              <div>
                <dt>Projet</dt>
                <dd>{tache.projetNom}</dd>
              </div>
            </dl>

            {sousTaches.length > 0 && (
              <section className="panneau-section">
                <h3 className="panneau-sous-titre">
                  Sous-taches
                  <span className="compteur">{sousTaches.length}</span>
                </h3>
                <ul className="liste-sous-taches">
                  {sousTaches.map((st) => (
                    <li key={st.id}>
                      <span
                        className={`point point-${st.statut}`}
                        title={LIBELLE_STATUT[st.statut]}
                      />
                      <span
                        className={
                          st.statut === "TERMINEE" ? "texte-barre" : undefined
                        }
                      >
                        {st.titre}
                      </span>
                    </li>
                  ))}
                </ul>
              </section>
            )}

            <section className="panneau-section">
              <h3 className="panneau-sous-titre">
                Discussion
                <span className="compteur">{commentaires.length}</span>
              </h3>

              {erreur && <div className="alerte">{erreur}</div>}

              {commentaires.length === 0 ? (
                <p className="texte-discret petit">
                  Aucun commentaire pour l'instant.
                </p>
              ) : (
                <ul className="fil-discussion">
                  {commentaires.map((commentaire) => (
                    <li key={commentaire.id} className="commentaire">
                      <span className="pastille" title={commentaire.auteur.nomComplet}>
                        {commentaire.auteur.nomComplet.charAt(0)}
                      </span>

                      <div className="commentaire-corps">
                        <div className="commentaire-entete">
                          <strong>{commentaire.auteur.nomComplet}</strong>
                          <span className="texte-discret petit">
                            {momentRelatif(commentaire.dateCreation)}
                          </span>
                          {peutSupprimerCommentaire(commentaire) && (
                            <button
                              className="bouton-icone commentaire-supprimer"
                              title="Supprimer"
                              onClick={() => supprimerCommentaire(commentaire.id)}
                            >
                              ×
                            </button>
                          )}
                        </div>
                        <p className="commentaire-texte">{commentaire.contenu}</p>
                      </div>
                    </li>
                  ))}
                </ul>
              )}

              {estMembre || gestionnaire ? (
                <form className="formulaire-commentaire" onSubmit={publier}>
                  <textarea
                    rows="3"
                    value={brouillon}
                    onChange={(e) => setBrouillon(e.target.value)}
                    placeholder="Ecrire un commentaire…"
                  />
                  <button
                    className="bouton bouton-principal"
                    disabled={envoi || !brouillon.trim()}
                  >
                    {envoi ? "Publication…" : "Publier"}
                  </button>
                </form>
              ) : (
                <p className="texte-discret petit">
                  Seuls les membres du projet peuvent commenter.
                </p>
              )}
            </section>
          </div>
        )}
      </aside>
    </>
  );
}
