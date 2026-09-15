import { useCallback, useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { DragDropContext, Droppable, Draggable } from "@hello-pangea/dnd";
import * as apiTaches from "../api/taches";
import * as apiProjets from "../api/projets";
import { messageErreur } from "../api/client";
import { useAuth } from "../context/AuthContext";
import { peutGererTaches, peutDeplacerTache } from "../utils/droits";
import PanneauTache from "../components/PanneauTache";

const COLONNES = [
  { cle: "A_FAIRE", libelle: "A faire" },
  { cle: "EN_COURS", libelle: "En cours" },
  { cle: "EN_REVISION", libelle: "En revision" },
  { cle: "TERMINEE", libelle: "Terminee" },
];

const LIBELLE_PRIORITE = {
  BASSE: "Basse",
  MOYENNE: "Moyenne",
  HAUTE: "Haute",
  URGENTE: "Urgente",
};

function formaterDate(valeur) {
  if (!valeur) return null;
  return new Date(valeur).toLocaleDateString("fr-FR", {
    day: "2-digit",
    month: "short",
  });
}

export default function Kanban() {
  const { projetId } = useParams();
  const { utilisateur } = useAuth();

  const [kanban, setKanban] = useState(null);
  const [projet, setProjet] = useState(null);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState("");
  const [formulaireOuvert, setFormulaireOuvert] = useState(false);
  const [tacheOuverte, setTacheOuverte] = useState(null);

  const gestionnaire = peutGererTaches(utilisateur, projet);

  const [nouvelle, setNouvelle] = useState({
    titre: "",
    description: "",
    priorite: "MOYENNE",
    dateEcheance: "",
    assigneAId: "",
  });

  const charger = useCallback(async () => {
    try {
      const [reponseKanban, reponseProjet] = await Promise.all([
        apiTaches.chargerKanban(projetId),
        apiProjets.consulterProjet(projetId),
      ]);
      setKanban(reponseKanban.data);
      setProjet(reponseProjet.data);
      setErreur("");
    } catch (e) {
      setErreur(messageErreur(e, "Impossible de charger le tableau"));
    } finally {
      setChargement(false);
    }
  }, [projetId]);

  useEffect(() => {
    charger();
  }, [charger]);

  /**
   * Deplacement d'une carte.
   * On deplace d'abord a l'ecran pour que ce soit fluide,
   * puis on previent le serveur. En cas de refus, on remet en place.
   */
  async function surFinDeplacement(resultat) {
    const { source, destination, draggableId } = resultat;

    if (!destination) return;
    if (
      source.droppableId === destination.droppableId &&
      source.index === destination.index
    ) {
      return;
    }

    const ancienEtat = kanban;
    const colonnes = { ...kanban.colonnes };
    const depart = Array.from(colonnes[source.droppableId]);
    const [carte] = depart.splice(source.index, 1);
    colonnes[source.droppableId] = depart;

    const arrivee = Array.from(colonnes[destination.droppableId]);
    arrivee.splice(destination.index, 0, {
      ...carte,
      statut: destination.droppableId,
    });
    colonnes[destination.droppableId] = arrivee;

    const compteurs = {};
    COLONNES.forEach((c) => {
      compteurs[c.cle] = (colonnes[c.cle] || []).length;
    });

    setKanban({ ...kanban, colonnes, compteurs });

    try {
      await apiTaches.changerStatut(draggableId, destination.droppableId);
      charger();
    } catch (e) {
      setKanban(ancienEtat);
      setErreur(messageErreur(e, "Deplacement refuse"));
    }
  }

  async function creerTache(evenement) {
    evenement.preventDefault();
    try {
      await apiTaches.creerTache({
        titre: nouvelle.titre,
        description: nouvelle.description || null,
        priorite: nouvelle.priorite,
        statut: "A_FAIRE",
        dateEcheance: nouvelle.dateEcheance || null,
        projetId: Number(projetId),
        assigneAId: nouvelle.assigneAId ? Number(nouvelle.assigneAId) : null,
      });
      setNouvelle({
        titre: "",
        description: "",
        priorite: "MOYENNE",
        dateEcheance: "",
        assigneAId: "",
      });
      setFormulaireOuvert(false);
      charger();
    } catch (e) {
      setErreur(messageErreur(e, "Creation impossible"));
    }
  }

  async function supprimer(id) {
    if (!window.confirm("Supprimer cette tache et ses commentaires ?")) return;
    try {
      await apiTaches.supprimerTache(id);
      charger();
    } catch (e) {
      setErreur(messageErreur(e, "Suppression impossible"));
    }
  }

  if (chargement) {
    return <div className="etat-vide">Chargement du tableau…</div>;
  }
  if (!kanban) {
    return (
      <main className="conteneur">
        <div className="alerte">{erreur || "Tableau introuvable"}</div>
        <Link to="/projets" className="bouton bouton-discret">
          Retour aux projets
        </Link>
      </main>
    );
  }

  return (
    <main className="conteneur conteneur-large">
      <div className="fil-ariane">
        <Link to="/projets">Projets</Link>
        <span>›</span>
        <span>{kanban.projetNom}</span>
      </div>

      <div className="bandeau-page">
        <div>
          <h1>{kanban.projetNom}</h1>
          <p className="texte-discret">
            {kanban.totalTaches} tache{kanban.totalTaches > 1 ? "s" : ""} ·{" "}
            {kanban.avancement}% termine
            {kanban.tachesEnRetard > 0 && (
              <span className="texte-alerte">
                {" "}
                · {kanban.tachesEnRetard} en retard
              </span>
            )}
          </p>
        </div>

        <div className="bandeau-actions">
          <Link
            to={`/projets/${projetId}/calendrier`}
            className="bouton bouton-discret"
          >
            Calendrier
          </Link>

          <Link
            to={`/projets/${projetId}/equipe`}
            className="bouton bouton-discret"
          >
            Charge de l'equipe
          </Link>

          {gestionnaire && (
            <button
              className="bouton bouton-principal"
              onClick={() => setFormulaireOuvert(!formulaireOuvert)}
            >
              {formulaireOuvert ? "Annuler" : "Nouvelle tache"}
            </button>
          )}
        </div>
      </div>

      <div className="barre-progression barre-progression-large">
        <div
          className="barre-progression-remplie"
          style={{ width: `${kanban.avancement}%` }}
        />
      </div>

      {!gestionnaire && (
        <p className="note-lecture">
          Vous consultez ce projet en tant que membre. Vous pouvez deplacer
          les cartes qui vous sont assignees.
        </p>
      )}

      {erreur && <div className="alerte">{erreur}</div>}

      {formulaireOuvert && gestionnaire && (
        <form className="carte formulaire" onSubmit={creerTache}>
          <label className="champ">
            <span>Titre de la tache</span>
            <input
              value={nouvelle.titre}
              onChange={(e) =>
                setNouvelle({ ...nouvelle, titre: e.target.value })
              }
              placeholder="Rediger les textes des affiches"
              required
            />
          </label>

          <label className="champ">
            <span>Description</span>
            <textarea
              rows="2"
              value={nouvelle.description}
              onChange={(e) =>
                setNouvelle({ ...nouvelle, description: e.target.value })
              }
            />
          </label>

          <div className="ligne-champs">
            <label className="champ">
              <span>Priorite</span>
              <select
                value={nouvelle.priorite}
                onChange={(e) =>
                  setNouvelle({ ...nouvelle, priorite: e.target.value })
                }
              >
                <option value="BASSE">Basse</option>
                <option value="MOYENNE">Moyenne</option>
                <option value="HAUTE">Haute</option>
                <option value="URGENTE">Urgente</option>
              </select>
            </label>

            <label className="champ">
              <span>Echeance</span>
              <input
                type="date"
                value={nouvelle.dateEcheance}
                onChange={(e) =>
                  setNouvelle({ ...nouvelle, dateEcheance: e.target.value })
                }
              />
            </label>

            <label className="champ">
              <span>Assignee a</span>
              <select
                value={nouvelle.assigneAId}
                onChange={(e) =>
                  setNouvelle({ ...nouvelle, assigneAId: e.target.value })
                }
              >
                <option value="">Personne</option>
                {projet?.membres.map((m) => (
                  <option key={m.id} value={m.id}>
                    {m.nomComplet}
                  </option>
                ))}
              </select>
            </label>
          </div>

          <button className="bouton bouton-principal">Creer la tache</button>
        </form>
      )}

      <DragDropContext onDragEnd={surFinDeplacement}>
        <div className="tableau-kanban">
          {COLONNES.map((colonne) => (
            <Droppable droppableId={colonne.cle} key={colonne.cle}>
              {(fourni, etat) => (
                <div
                  className={`colonne ${etat.isDraggingOver ? "colonne-survolee" : ""}`}
                  ref={fourni.innerRef}
                  {...fourni.droppableProps}
                >
                  <div className="colonne-entete">
                    <span className={`point point-${colonne.cle}`} />
                    <strong>{colonne.libelle}</strong>
                    <span className="compteur">
                      {kanban.compteurs[colonne.cle] ?? 0}
                    </span>
                  </div>

                  <div className="colonne-contenu">
                    {(kanban.colonnes[colonne.cle] || []).map((tache, index) => {
                      const deplacable = peutDeplacerTache(
                        utilisateur,
                        projet,
                        tache
                      );

                      return (
                        <Draggable
                          draggableId={String(tache.id)}
                          index={index}
                          key={tache.id}
                          isDragDisabled={!deplacable}
                        >
                          {(fourniCarte, etatCarte) => (
                            <article
                              className={
                                "carte-tache" +
                                (etatCarte.isDragging
                                  ? " carte-tache-deplacee"
                                  : "") +
                                (deplacable ? "" : " carte-tache-figee")
                              }
                              ref={fourniCarte.innerRef}
                              {...fourniCarte.draggableProps}
                              {...fourniCarte.dragHandleProps}
                              onClick={() => setTacheOuverte(tache.id)}
                              title={
                                deplacable
                                  ? "Cliquer pour ouvrir le detail"
                                  : "Seul le chef de projet ou la personne assignee peut deplacer cette carte"
                              }
                            >
                              <div className="carte-tache-haut">
                                <span
                                  className={`etiquette etiquette-${tache.priorite}`}
                                >
                                  {LIBELLE_PRIORITE[tache.priorite]}
                                </span>
                                {gestionnaire && (
                                  <button
                                    type="button"
                                    className="bouton-icone"
                                    title="Supprimer"
                                    onClick={(evenement) => {
                                      evenement.stopPropagation();
                                      supprimer(tache.id);
                                    }}
                                  >
                                    ×
                                  </button>
                                )}
                              </div>

                              <h3>{tache.titre}</h3>
                              {tache.description && (
                                <p className="texte-discret petit">
                                  {tache.description}
                                </p>
                              )}

                              <div className="carte-tache-bas">
                                {tache.assigneA ? (
                                  <span
                                    className="pastille"
                                    title={tache.assigneA.nomComplet}
                                  >
                                    {tache.assigneA.nomComplet.charAt(0)}
                                  </span>
                                ) : (
                                  <span className="pastille pastille-vide">
                                    ?
                                  </span>
                                )}

                                {tache.dateEcheance && (
                                  <span
                                    className={`date-echeance ${
                                      tache.enRetard
                                        ? "date-echeance-retard"
                                        : ""
                                    }`}
                                  >
                                    {formaterDate(tache.dateEcheance)}
                                  </span>
                                )}

                                {tache.nombreCommentaires > 0 && (
                                  <span className="texte-discret petit">
                                    {tache.nombreCommentaires} com.
                                  </span>
                                )}
                              </div>
                            </article>
                          )}
                        </Draggable>
                      );
                    })}
                    {fourni.placeholder}

                    {(kanban.colonnes[colonne.cle] || []).length === 0 && (
                      <div className="colonne-vide">Aucune tache</div>
                    )}
                  </div>
                </div>
              )}
            </Droppable>
          ))}
        </div>
      </DragDropContext>

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
