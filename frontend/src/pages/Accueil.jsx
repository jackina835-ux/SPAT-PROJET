import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import * as apiTaches from "../api/taches";
import { messageErreur } from "../api/client";
import { useAuth } from "../context/AuthContext";

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

function formaterDate(valeur) {
  if (!valeur) return null;
  return new Date(valeur).toLocaleDateString("fr-FR", {
    day: "2-digit",
    month: "short",
  });
}

/** Vrai si l'echeance tombe entre aujourd'hui et les 7 jours suivants. */
function dansLaSemaine(dateEcheance) {
  if (!dateEcheance) return false;
  const aujourdhui = new Date();
  aujourdhui.setHours(0, 0, 0, 0);
  const limite = new Date(aujourdhui);
  limite.setDate(limite.getDate() + 7);
  const echeance = new Date(dateEcheance + "T00:00:00");
  return echeance >= aujourdhui && echeance <= limite;
}

function CarteTacheAccueil({ tache }) {
  return (
    <Link
      to={`/projets/${tache.projetId}/kanban`}
      className="carte carte-tache-accueil"
    >
      <div className="carte-tache-haut">
        <span className={`etiquette etiquette-${tache.priorite}`}>
          {LIBELLE_PRIORITE[tache.priorite]}
        </span>
        {tache.enRetard && (
          <span className="etiquette etiquette-retard">En retard</span>
        )}
      </div>

      <strong>{tache.titre}</strong>
      <span className="texte-discret petit">{tache.projetNom}</span>

      <div className="carte-tache-bas">
        <span
          className={`point point-${tache.statut}`}
          title={LIBELLE_STATUT[tache.statut]}
        />
        <span className="texte-discret petit">
          {LIBELLE_STATUT[tache.statut]}
        </span>
        {tache.dateEcheance && (
          <span
            className={`date-echeance ${
              tache.enRetard ? "date-echeance-retard" : ""
            }`}
          >
            {formaterDate(tache.dateEcheance)}
          </span>
        )}
      </div>
    </Link>
  );
}

function SectionTaches({ titre, taches, messageVide, variante }) {
  const classeCompteur =
    variante && taches.length > 0 ? `compteur-${variante}` : "";

  return (
    <section>
      <h2 className="titre-section">
        {titre}
        <span className={`compteur ${classeCompteur}`}>
          {taches.length}
        </span>
      </h2>

      {taches.length === 0 ? (
        <p className="texte-discret petit">{messageVide}</p>
      ) : (
        <div className="grille-taches-accueil">
          {taches.map((t) => (
            <CarteTacheAccueil key={t.id} tache={t} />
          ))}
        </div>
      )}
    </section>
  );
}

/**
 * Page d'accueil personnelle : un coup d'oeil sur ce qui concerne
 * l'utilisateur connecte, tous projets confondus.
 */
export default function Accueil() {
  const { utilisateur } = useAuth();

  const [taches, setTaches] = useState([]);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState("");

  useEffect(() => {
    let annule = false;

    async function charger() {
      setChargement(true);
      try {
        const { data } = await apiTaches.listerTachesUtilisateur(utilisateur.id);
        if (!annule) {
          setTaches(data);
          setErreur("");
        }
      } catch (e) {
        if (!annule) setErreur(messageErreur(e, "Impossible de charger vos taches"));
      } finally {
        if (!annule) setChargement(false);
      }
    }

    charger();
    return () => {
      annule = true;
    };
  }, [utilisateur.id]);

  const enCours = useMemo(
    () =>
      taches
        .filter((t) => t.statut !== "TERMINEE")
        .sort((a, b) => (a.dateEcheance || "9999-99-99").localeCompare(
          b.dateEcheance || "9999-99-99"
        )),
    [taches]
  );

  const cetteSemaine = useMemo(
    () => enCours.filter((t) => dansLaSemaine(t.dateEcheance)),
    [enCours]
  );

  const enRetard = useMemo(() => taches.filter((t) => t.enRetard), [taches]);

  if (chargement) {
    return <div className="etat-vide">Chargement de votre tableau de bord…</div>;
  }

  const prenom = utilisateur.nomComplet.split(" ")[0];

  return (
    <main className="conteneur conteneur-large">
      <div className="bandeau-page">
        <div>
          <h1>Bonjour {prenom}</h1>
          <p className="texte-discret">Voici un coup d'oeil sur votre semaine.</p>
        </div>
        <Link to="/projets" className="bouton bouton-discret">
          Voir tous les projets
        </Link>
      </div>

      {erreur && <div className="alerte">{erreur}</div>}

      <div className="accueil-stats">
        <div className="stat-tuile stat-tuile-alerte">
          <span className="stat-tuile-valeur">{enRetard.length}</span>
          <span className="stat-tuile-libelle">
            tache{enRetard.length > 1 ? "s" : ""} en retard
          </span>
        </div>
        <div className="stat-tuile stat-tuile-avertissement">
          <span className="stat-tuile-valeur">{cetteSemaine.length}</span>
          <span className="stat-tuile-libelle">
            echeance{cetteSemaine.length > 1 ? "s" : ""} cette semaine
          </span>
        </div>
        <div className="stat-tuile stat-tuile-succes">
          <span className="stat-tuile-valeur">{enCours.length}</span>
          <span className="stat-tuile-libelle">
            tache{enCours.length > 1 ? "s" : ""} en cours
          </span>
        </div>
      </div>

      <SectionTaches
        titre="Mes retards"
        taches={enRetard}
        messageVide="Aucun retard. Tout est a jour."
        variante="alerte"
      />

      <SectionTaches
        titre="Mes echeances de la semaine"
        taches={cetteSemaine}
        messageVide="Aucune echeance dans les 7 prochains jours."
        variante="avertissement"
      />

      <SectionTaches
        titre="Mes taches en cours"
        taches={enCours}
        messageVide="Aucune tache active pour le moment."
        variante="succes"
      />
    </main>
  );
}
