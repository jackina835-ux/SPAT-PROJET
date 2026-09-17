import { useEffect, useState } from "react";
import * as apiTaches from "../api/taches";
import * as apiCommentaires from "../api/commentaires";
import * as apiTemps from "../api/temps";
import * as apiPieces from "../api/pieces";
import * as apiDependances from "../api/dependances";
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

const ETIQUETTE_FICHIER = {
  PDF: "PDF",
  IMAGE: "IMG",
  DOCUMENT: "DOC",
  TABLEUR: "XLS",
  PRESENTATION: "PPT",
  TEXTE: "TXT",
  ARCHIVE: "ZIP",
  AUTRE: "FIC",
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

  const [edition, setEdition] = useState(false);
  const [formulaire, setFormulaire] = useState(null);
  const [enregistrement, setEnregistrement] = useState(false);

  const [dependances, setDependances] = useState(null);
  const [candidates, setCandidates] = useState([]);
  const [ajoutOuvert, setAjoutOuvert] = useState(false);

  const [pieces, setPieces] = useState([]);
  const [envoiFichier, setEnvoiFichier] = useState(false);
  const [progression, setProgression] = useState(0);

  const [recap, setRecap] = useState(null);
  const [saisieOuverte, setSaisieOuverte] = useState(false);
  const [saisie, setSaisie] = useState({
    heures: "",
    minutes: "",
    dateTravail: new Date().toISOString().slice(0, 10),
    commentaire: "",
  });

  const gestionnaire = estAdmin(utilisateur) || estResponsable(utilisateur, projet);
  const estMembre = projet?.membres?.some((m) => m.id === utilisateur?.id);

  useEffect(() => {
    let annule = false;

    async function charger() {
      setChargement(true);
      try {
        const [rTache, rSous, rComm, rTemps, rPieces, rDep] = await Promise.all([
          apiTaches.consulterTache(tacheId),
          apiTaches.listerSousTaches(tacheId),
          apiCommentaires.listerCommentaires(tacheId),
          apiTemps.recapTache(tacheId),
          apiPieces.listerPieces(tacheId),
          apiDependances.consulterDependances(tacheId),
        ]);
        if (annule) return;
        setTache(rTache.data);
        setSousTaches(rSous.data);
        setCommentaires(rComm.data);
        setRecap(rTemps.data);
        setPieces(rPieces.data);
        setDependances(rDep.data);
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

  async function ouvrirAjoutDependance() {
    const nouvelEtat = !ajoutOuvert;
    setAjoutOuvert(nouvelEtat);

    if (nouvelEtat) {
      try {
        const { data } = await apiDependances.listerCandidates(tacheId);
        setCandidates(data);
      } catch (e) {
        setErreur(messageErreur(e, "Impossible de lister les taches"));
        setCandidates([]);
      }
    }
  }

  async function ajouterDependance(dependDeId) {
    try {
      const { data } = await apiDependances.ajouterDependance(
        tacheId,
        dependDeId
      );
      setDependances(data);
      setAjoutOuvert(false);
      onChangement?.();
      setErreur("");
    } catch (e) {
      setErreur(messageErreur(e, "Ajout impossible"));
    }
  }

  async function retirerDependance(dependDeId) {
    try {
      const { data } = await apiDependances.retirerDependance(
        tacheId,
        dependDeId
      );
      setDependances(data);
      onChangement?.();
    } catch (e) {
      setErreur(messageErreur(e, "Suppression impossible"));
    }
  }

  async function rechargerPieces() {
    const { data } = await apiPieces.listerPieces(tacheId);
    setPieces(data);
  }

  async function choisirFichier(evenement) {
    const fichier = evenement.target.files?.[0];
    if (!fichier) return;

    setEnvoiFichier(true);
    setProgression(0);
    try {
      await apiPieces.deposerPiece(tacheId, fichier, setProgression);
      await rechargerPieces();
      onChangement?.();
      setErreur("");
    } catch (e) {
      setErreur(messageErreur(e, "Envoi impossible"));
    } finally {
      setEnvoiFichier(false);
      setProgression(0);
      evenement.target.value = "";
    }
  }

  async function telecharger(piece) {
    try {
      await apiPieces.telechargerPiece(piece.id, piece.nomOriginal);
    } catch (e) {
      setErreur(messageErreur(e, "Telechargement impossible"));
    }
  }

  async function supprimerPiece(id) {
    if (!window.confirm("Supprimer ce fichier ?")) return;
    try {
      await apiPieces.supprimerPiece(id);
      await rechargerPieces();
      onChangement?.();
    } catch (e) {
      setErreur(messageErreur(e, "Suppression impossible"));
    }
  }

  async function rechargerTemps() {
    const { data } = await apiTemps.recapTache(tacheId);
    setRecap(data);
  }

  async function enregistrerTemps(evenement) {
    evenement.preventDefault();

    const h = parseInt(saisie.heures || "0", 10);
    const m = parseInt(saisie.minutes || "0", 10);
    const total = h * 60 + m;

    if (total <= 0) {
      setErreur("Indiquez une duree superieure a zero");
      return;
    }

    try {
      await apiTemps.saisirTemps({
        tacheId: Number(tacheId),
        dureeMinutes: total,
        dateTravail: saisie.dateTravail,
        commentaire: saisie.commentaire || null,
      });
      setSaisie({
        heures: "",
        minutes: "",
        dateTravail: new Date().toISOString().slice(0, 10),
        commentaire: "",
      });
      setSaisieOuverte(false);
      await rechargerTemps();
      onChangement?.();
      setErreur("");
    } catch (e) {
      setErreur(messageErreur(e, "Saisie impossible"));
    }
  }

  async function supprimerSaisie(id) {
    if (!window.confirm("Supprimer cette saisie de temps ?")) return;
    try {
      await apiTemps.supprimerSaisie(id);
      await rechargerTemps();
      onChangement?.();
    } catch (e) {
      setErreur(messageErreur(e, "Suppression impossible"));
    }
  }

  function ouvrirEdition() {
    setFormulaire({
      titre: tache.titre,
      description: tache.description || "",
      priorite: tache.priorite,
      statut: tache.statut,
      dateEcheance: tache.dateEcheance || "",
      chargeEstimee: tache.chargeEstimee ?? "",
      assigneAId: tache.assigneA ? String(tache.assigneA.id) : "",
    });
    setEdition(true);
  }

  async function enregistrerModification(evenement) {
    evenement.preventDefault();
    setEnregistrement(true);
    try {
      const { data } = await apiTaches.modifierTache(tacheId, {
        titre: formulaire.titre,
        description: formulaire.description || null,
        priorite: formulaire.priorite,
        statut: formulaire.statut,
        dateEcheance: formulaire.dateEcheance || null,
        chargeEstimee: formulaire.chargeEstimee
          ? Number(formulaire.chargeEstimee)
          : null,
        projetId: tache.projetId,
        assigneAId: formulaire.assigneAId ? Number(formulaire.assigneAId) : null,
        tacheParentId: tache.tacheParentId,
      });
      setTache(data);
      setEdition(false);
      onChangement?.();
      setErreur("");
    } catch (e) {
      setErreur(messageErreur(e, "Modification impossible"));
    } finally {
      setEnregistrement(false);
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
          <div className="panneau-entete-actions">
            {gestionnaire && tache && !edition && (
              <button
                className="lien-discret"
                onClick={ouvrirEdition}
                title="Modifier la tache"
              >
                Modifier
              </button>
            )}
            <button className="bouton-icone" onClick={onFermer} title="Fermer">
              ×
            </button>
          </div>
        </header>

        {chargement ? (
          <div className="etat-vide">Chargement…</div>
        ) : !tache ? (
          <div className="alerte">{erreur || "Tache introuvable"}</div>
        ) : edition ? (
          <div className="panneau-corps">
            <h2 className="panneau-titre">Modifier la tache</h2>

            {erreur && <div className="alerte">{erreur}</div>}

            <form className="carte formulaire" onSubmit={enregistrerModification}>
              <label className="champ">
                <span>Titre</span>
                <input
                  value={formulaire.titre}
                  onChange={(e) =>
                    setFormulaire({ ...formulaire, titre: e.target.value })
                  }
                  required
                />
              </label>

              <label className="champ">
                <span>Description</span>
                <textarea
                  rows="3"
                  value={formulaire.description}
                  onChange={(e) =>
                    setFormulaire({ ...formulaire, description: e.target.value })
                  }
                />
              </label>

              <div className="ligne-champs">
                <label className="champ">
                  <span>Priorite</span>
                  <select
                    value={formulaire.priorite}
                    onChange={(e) =>
                      setFormulaire({ ...formulaire, priorite: e.target.value })
                    }
                  >
                    <option value="BASSE">Basse</option>
                    <option value="MOYENNE">Moyenne</option>
                    <option value="HAUTE">Haute</option>
                    <option value="URGENTE">Urgente</option>
                  </select>
                </label>

                <label className="champ">
                  <span>Statut</span>
                  <select
                    value={formulaire.statut}
                    onChange={(e) =>
                      setFormulaire({ ...formulaire, statut: e.target.value })
                    }
                  >
                    <option value="A_FAIRE">A faire</option>
                    <option value="EN_COURS">En cours</option>
                    <option value="EN_REVISION">En revision</option>
                    <option value="TERMINEE">Terminee</option>
                  </select>
                </label>
              </div>

              <div className="ligne-champs">
                <label className="champ">
                  <span>Echeance</span>
                  <input
                    type="date"
                    value={formulaire.dateEcheance}
                    onChange={(e) =>
                      setFormulaire({ ...formulaire, dateEcheance: e.target.value })
                    }
                  />
                </label>

                <label className="champ">
                  <span>Charge estimee (h)</span>
                  <input
                    type="number"
                    min="1"
                    value={formulaire.chargeEstimee}
                    onChange={(e) =>
                      setFormulaire({ ...formulaire, chargeEstimee: e.target.value })
                    }
                  />
                </label>
              </div>

              <label className="champ">
                <span>Assignee a</span>
                <select
                  value={formulaire.assigneAId}
                  onChange={(e) =>
                    setFormulaire({ ...formulaire, assigneAId: e.target.value })
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

              <div className="ligne-champs">
                <button
                  type="button"
                  className="bouton bouton-discret"
                  onClick={() => setEdition(false)}
                  disabled={enregistrement}
                >
                  Annuler
                </button>
                <button
                  className="bouton bouton-principal"
                  disabled={enregistrement}
                >
                  {enregistrement ? "Enregistrement…" : "Enregistrer"}
                </button>
              </div>
            </form>
          </div>
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

            {dependances?.estBloquee && (
              <div className="bandeau-bloquee">
                Cette tache est bloquee : {dependances.nonTerminees} tache
                {dependances.nonTerminees > 1 ? "s" : ""} doi
                {dependances.nonTerminees > 1 ? "vent" : "t"} etre terminee
                {dependances.nonTerminees > 1 ? "s" : ""} avant qu'elle puisse
                avancer.
              </div>
            )}

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
                Dependances
                {gestionnaire && (
                  <button
                    className="lien-discret temps-ajouter"
                    onClick={ouvrirAjoutDependance}
                  >
                    {ajoutOuvert ? "Annuler" : "Ajouter"}
                  </button>
                )}
              </h3>

              {ajoutOuvert && (
                <div className="liste-candidates">
                  {candidates.length === 0 ? (
                    <p className="texte-discret petit">
                      Aucune tache disponible. Les taches deja liees et celles
                      qui creeraient un cycle sont exclues.
                    </p>
                  ) : (
                    candidates.map((c) => (
                      <button
                        key={c.id}
                        className="candidate"
                        onClick={() => ajouterDependance(c.id)}
                      >
                        <span className={`point point-${c.statut}`} />
                        {c.titre}
                      </button>
                    ))
                  )}
                </div>
              )}

              <p className="texte-discret petit etiquette-groupe">
                Cette tache attend
              </p>
              {!dependances || dependances.dependances.length === 0 ? (
                <p className="texte-discret petit">Rien. Elle peut avancer.</p>
              ) : (
                <ul className="liste-dependances">
                  {dependances.dependances.map((d) => (
                    <li
                      key={d.id}
                      className={d.terminee ? "dep dep-ok" : "dep dep-attente"}
                    >
                      <span className={`point point-${d.statut}`} />
                      <span
                        className={d.terminee ? "texte-barre" : undefined}
                      >
                        {d.titre}
                      </span>
                      {gestionnaire && (
                        <button
                          className="bouton-icone dep-supprimer"
                          title="Retirer cette dependance"
                          onClick={() => retirerDependance(d.id)}
                        >
                          ×
                        </button>
                      )}
                    </li>
                  ))}
                </ul>
              )}

              {dependances && dependances.bloque.length > 0 && (
                <>
                  <p className="texte-discret petit etiquette-groupe">
                    Taches qui attendent celle-ci
                  </p>
                  <ul className="liste-dependances">
                    {dependances.bloque.map((b) => (
                      <li key={b.id} className="dep">
                        <span className={`point point-${b.statut}`} />
                        <span>{b.titre}</span>
                      </li>
                    ))}
                  </ul>
                </>
              )}
            </section>

            <section className="panneau-section">
              <h3 className="panneau-sous-titre">
                Pieces jointes
                <span className="compteur">{pieces.length}</span>
              </h3>

              {pieces.length === 0 ? (
                <p className="texte-discret petit">Aucun fichier joint.</p>
              ) : (
                <ul className="liste-pieces">
                  {pieces.map((piece) => (
                    <li key={piece.id} className="piece">
                      <span className={`piece-icone piece-${piece.categorie}`}>
                        {ETIQUETTE_FICHIER[piece.categorie] || "FIC"}
                      </span>

                      <button
                        className="piece-corps"
                        onClick={() => telecharger(piece)}
                        title="Telecharger"
                      >
                        <span className="piece-nom">{piece.nomOriginal}</span>
                        <span className="texte-discret petit">
                          {piece.tailleLisible} · {piece.deposant.nomComplet}
                        </span>
                      </button>

                      <button
                        className="bouton-icone piece-supprimer"
                        title="Supprimer"
                        onClick={() => supprimerPiece(piece.id)}
                      >
                        ×
                      </button>
                    </li>
                  ))}
                </ul>
              )}

              {(estMembre || gestionnaire) && (
                <div className="depot-fichier">
                  <label className="bouton bouton-discret bouton-fichier">
                    {envoiFichier ? `Envoi… ${progression}%` : "Joindre un fichier"}
                    <input
                      type="file"
                      onChange={choisirFichier}
                      disabled={envoiFichier}
                      hidden
                    />
                  </label>
                  <span className="texte-discret petit">
                    10 Mo maximum · pdf, images, bureautique, archives
                  </span>
                </div>
              )}

              {envoiFichier && (
                <div className="barre-progression">
                  <div
                    className="barre-progression-remplie"
                    style={{ width: `${progression}%` }}
                  />
                </div>
              )}
            </section>

            <section className="panneau-section">
              <h3 className="panneau-sous-titre">
                Temps passe
                <button
                  className="lien-discret temps-ajouter"
                  onClick={() => setSaisieOuverte(!saisieOuverte)}
                >
                  {saisieOuverte ? "Annuler" : "Saisir du temps"}
                </button>
              </h3>

              {recap && (
                <>
                  <div className="temps-resume">
                    <div>
                      <strong>{recap.passeLisible}</strong>
                      <small className="texte-discret">realise</small>
                    </div>
                    <div>
                      <strong>{recap.estimeLisible}</strong>
                      <small className="texte-discret">estime</small>
                    </div>
                    {recap.consommation !== null && (
                      <div>
                        <strong
                          className={recap.depassement ? "texte-alerte" : undefined}
                        >
                          {recap.consommation}%
                        </strong>
                        <small className="texte-discret">consomme</small>
                      </div>
                    )}
                  </div>

                  {recap.consommation !== null && (
                    <div className="barre-progression">
                      <div
                        className={
                          "barre-progression-remplie" +
                          (recap.depassement ? " barre-depassement" : "")
                        }
                        style={{
                          width: `${Math.min(100, recap.consommation)}%`,
                        }}
                      />
                    </div>
                  )}

                  {recap.consommation === null && (
                    <p className="texte-discret petit">
                      Aucune charge estimee sur cette tache : impossible de
                      calculer un pourcentage.
                    </p>
                  )}
                </>
              )}

              {saisieOuverte && (
                <form className="formulaire-temps" onSubmit={enregistrerTemps}>
                  <div className="ligne-champs">
                    <label className="champ">
                      <span>Heures</span>
                      <input
                        type="number"
                        min="0"
                        max="16"
                        value={saisie.heures}
                        onChange={(e) =>
                          setSaisie({ ...saisie, heures: e.target.value })
                        }
                        placeholder="2"
                      />
                    </label>
                    <label className="champ">
                      <span>Minutes</span>
                      <input
                        type="number"
                        min="0"
                        max="59"
                        step="5"
                        value={saisie.minutes}
                        onChange={(e) =>
                          setSaisie({ ...saisie, minutes: e.target.value })
                        }
                        placeholder="30"
                      />
                    </label>
                    <label className="champ">
                      <span>Date</span>
                      <input
                        type="date"
                        value={saisie.dateTravail}
                        max={new Date().toISOString().slice(0, 10)}
                        onChange={(e) =>
                          setSaisie({ ...saisie, dateTravail: e.target.value })
                        }
                        required
                      />
                    </label>
                  </div>

                  <label className="champ">
                    <span>Ce qui a ete fait</span>
                    <input
                      value={saisie.commentaire}
                      onChange={(e) =>
                        setSaisie({ ...saisie, commentaire: e.target.value })
                      }
                      placeholder="Redaction du premier jet"
                    />
                  </label>

                  <button className="bouton bouton-principal">
                    Enregistrer
                  </button>
                </form>
              )}

              {recap && recap.saisies.length > 0 && (
                <ul className="liste-saisies">
                  {recap.saisies.map((s) => (
                    <li key={s.id} className="saisie">
                      <span className="saisie-duree">{s.dureeLisible}</span>
                      <span className="saisie-corps">
                        <span className="saisie-texte">
                          {s.commentaire || "Sans precision"}
                        </span>
                        <span className="texte-discret petit">
                          {s.utilisateur.nomComplet} ·{" "}
                          {new Date(s.dateTravail).toLocaleDateString("fr-FR", {
                            day: "2-digit",
                            month: "short",
                          })}
                        </span>
                      </span>
                      <button
                        className="bouton-icone saisie-supprimer"
                        title="Supprimer"
                        onClick={() => supprimerSaisie(s.id)}
                      >
                        ×
                      </button>
                    </li>
                  ))}
                </ul>
              )}
            </section>

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
