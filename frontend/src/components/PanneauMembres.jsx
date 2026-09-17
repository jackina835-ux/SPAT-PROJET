import { useEffect, useState } from "react";
import * as apiProjets from "../api/projets";
import * as apiUtilisateurs from "../api/utilisateurs";
import { messageErreur } from "../api/client";
import { LIBELLE_ROLE } from "../utils/droits";

/**
 * Panneau lateral pour ajouter ou retirer des membres d'un projet.
 * Le responsable ne peut pas etre retire : c'est ProjetService qui
 * l'impose, ce panneau se contente de ne pas proposer le bouton.
 */
export default function PanneauMembres({ projet, onFermer, onChangement }) {
  const [utilisateurs, setUtilisateurs] = useState([]);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState("");
  const [enCours, setEnCours] = useState(null);

  useEffect(() => {
    let annule = false;

    async function charger() {
      setChargement(true);
      try {
        const { data } = await apiUtilisateurs.listerUtilisateurs();
        if (!annule) {
          setUtilisateurs(data);
          setErreur("");
        }
      } catch (e) {
        if (!annule) setErreur(messageErreur(e, "Impossible de charger les comptes"));
      } finally {
        if (!annule) setChargement(false);
      }
    }

    charger();
    return () => {
      annule = true;
    };
  }, []);

  useEffect(() => {
    function surTouche(evenement) {
      if (evenement.key === "Escape") onFermer();
    }
    window.addEventListener("keydown", surTouche);
    return () => window.removeEventListener("keydown", surTouche);
  }, [onFermer]);

  const idsMembres = new Set(projet.membres.map((m) => m.id));
  const candidats = utilisateurs.filter((u) => !idsMembres.has(u.id));

  async function ajouter(utilisateurId) {
    setEnCours(utilisateurId);
    try {
      await apiProjets.ajouterMembre(projet.id, utilisateurId);
      await onChangement();
      setErreur("");
    } catch (e) {
      setErreur(messageErreur(e, "Ajout impossible"));
    } finally {
      setEnCours(null);
    }
  }

  async function retirer(utilisateurId) {
    if (!window.confirm("Retirer ce membre du projet ?")) return;
    setEnCours(utilisateurId);
    try {
      await apiProjets.retirerMembre(projet.id, utilisateurId);
      await onChangement();
      setErreur("");
    } catch (e) {
      setErreur(messageErreur(e, "Retrait impossible"));
    } finally {
      setEnCours(null);
    }
  }

  return (
    <>
      <div className="voile" onClick={onFermer} />

      <aside className="panneau" role="dialog" aria-modal="true">
        <header className="panneau-entete">
          <span className="panneau-titre-petit">Membres du projet</span>
          <button className="bouton-icone" onClick={onFermer} title="Fermer">
            ×
          </button>
        </header>

        <div className="panneau-corps">
          <h2 className="panneau-titre">{projet.nom}</h2>

          {erreur && <div className="alerte">{erreur}</div>}

          <section className="panneau-section">
            <h3 className="panneau-sous-titre">
              Membres actuels
              <span className="compteur">{projet.membres.length}</span>
            </h3>
            <ul className="liste-candidates">
              {projet.membres.map((m) => (
                <li key={m.id} className="candidate candidate-membre">
                  <span className="pastille" title={m.nomComplet}>
                    {m.nomComplet.charAt(0)}
                  </span>
                  <span className="candidate-corps">
                    <span>{m.nomComplet}</span>
                    <span className="texte-discret petit">
                      {LIBELLE_ROLE[m.role] || m.role}
                    </span>
                  </span>
                  {m.id === projet.responsable?.id ? (
                    <span className="etiquette etiquette-moi">Responsable</span>
                  ) : (
                    <button
                      className="bouton-icone"
                      title="Retirer du projet"
                      disabled={enCours === m.id}
                      onClick={() => retirer(m.id)}
                    >
                      ×
                    </button>
                  )}
                </li>
              ))}
            </ul>
          </section>

          <section className="panneau-section">
            <h3 className="panneau-sous-titre">Ajouter un membre</h3>
            {chargement ? (
              <p className="texte-discret petit">Chargement des comptes…</p>
            ) : candidats.length === 0 ? (
              <p className="texte-discret petit">
                Tous les comptes actifs sont deja membres de ce projet.
              </p>
            ) : (
              <ul className="liste-candidates">
                {candidats.map((u) => (
                  <li key={u.id} className="candidate candidate-membre">
                    <span className="pastille" title={u.nomComplet}>
                      {u.nomComplet.charAt(0)}
                    </span>
                    <span className="candidate-corps">
                      <span>{u.nomComplet}</span>
                      <span className="texte-discret petit">
                        {LIBELLE_ROLE[u.role] || u.role}
                      </span>
                    </span>
                    <button
                      className="bouton bouton-discret"
                      disabled={enCours === u.id}
                      onClick={() => ajouter(u.id)}
                    >
                      Ajouter
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </section>
        </div>
      </aside>
    </>
  );
}
