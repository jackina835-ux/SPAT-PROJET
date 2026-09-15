import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import * as apiNotifications from "../api/notifications";

/** Toutes les 30 secondes, on redemande le nombre de non lues. */
const INTERVALLE_MS = 30000;

const ICONE = {
  ASSIGNATION: "→",
  DESASSIGNATION: "←",
  CHANGEMENT_STATUT: "⇄",
  COMMENTAIRE: "✎",
  AJOUT_PROJET: "+",
  ECHEANCE_PROCHE: "!",
};

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
  if (jours < 7) return `il y a ${jours} j`;

  return date.toLocaleDateString("fr-FR", { day: "2-digit", month: "short" });
}

export default function Cloche() {
  const [ouvert, setOuvert] = useState(false);
  const [nonLues, setNonLues] = useState(0);
  const [notifications, setNotifications] = useState([]);
  const [chargement, setChargement] = useState(false);

  const conteneur = useRef(null);
  const naviguer = useNavigate();

  const rafraichirCompteur = useCallback(async () => {
    try {
      const { data } = await apiNotifications.compterNonLues();
      setNonLues(data.nombre);
    } catch {
      // Silencieux : un compteur qui echoue ne doit pas
      // perturber la navigation.
    }
  }, []);

  // Sondage regulier du compteur
  useEffect(() => {
    rafraichirCompteur();
    const minuterie = setInterval(rafraichirCompteur, INTERVALLE_MS);
    return () => clearInterval(minuterie);
  }, [rafraichirCompteur]);

  // Fermeture au clic exterieur
  useEffect(() => {
    function surClic(evenement) {
      if (conteneur.current && !conteneur.current.contains(evenement.target)) {
        setOuvert(false);
      }
    }
    document.addEventListener("mousedown", surClic);
    return () => document.removeEventListener("mousedown", surClic);
  }, []);

  async function basculer() {
    const nouvelEtat = !ouvert;
    setOuvert(nouvelEtat);

    if (nouvelEtat) {
      setChargement(true);
      try {
        const { data } = await apiNotifications.listerNotifications();
        setNotifications(data);
      } catch {
        setNotifications([]);
      } finally {
        setChargement(false);
      }
    }
  }

  async function ouvrirNotification(notification) {
    setOuvert(false);

    if (!notification.lu) {
      try {
        await apiNotifications.marquerLue(notification.id);
        setNonLues((n) => Math.max(0, n - 1));
      } catch {
        // sans consequence
      }
    }

    if (notification.projetId) {
      naviguer(`/projets/${notification.projetId}/kanban`);
    }
  }

  async function toutLire() {
    try {
      await apiNotifications.toutMarquerLu();
      setNotifications((liste) => liste.map((n) => ({ ...n, lu: true })));
      setNonLues(0);
    } catch {
      // sans consequence
    }
  }

  return (
    <div className="cloche-conteneur" ref={conteneur}>
      <button
        className="cloche"
        onClick={basculer}
        title="Notifications"
        aria-label={`Notifications${nonLues > 0 ? ` (${nonLues} non lues)` : ""}`}
      >
        <svg viewBox="0 0 24 24" width="19" height="19" aria-hidden="true">
          <path
            d="M12 3a5.5 5.5 0 0 0-5.5 5.5v3.2L5 15h14l-1.5-3.3V8.5A5.5 5.5 0 0 0 12 3z"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.7"
            strokeLinejoin="round"
          />
          <path
            d="M10 18a2 2 0 0 0 4 0"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.7"
            strokeLinecap="round"
          />
        </svg>

        {nonLues > 0 && (
          <span className="cloche-pastille">
            {nonLues > 9 ? "9+" : nonLues}
          </span>
        )}
      </button>

      {ouvert && (
        <div className="cloche-panneau">
          <div className="cloche-entete">
            <strong>Notifications</strong>
            {nonLues > 0 && (
              <button className="lien-discret" onClick={toutLire}>
                Tout marquer comme lu
              </button>
            )}
          </div>

          <div className="cloche-liste">
            {chargement ? (
              <p className="cloche-vide">Chargement…</p>
            ) : notifications.length === 0 ? (
              <p className="cloche-vide">Aucune notification</p>
            ) : (
              notifications.map((notification) => (
                <button
                  key={notification.id}
                  className={
                    "cloche-element" + (notification.lu ? "" : " cloche-element-neuf")
                  }
                  onClick={() => ouvrirNotification(notification)}
                >
                  <span className={`cloche-icone cloche-icone-${notification.type}`}>
                    {ICONE[notification.type] || "•"}
                  </span>
                  <span className="cloche-texte">
                    <span className="cloche-message">{notification.message}</span>
                    <span className="texte-discret petit">
                      {momentRelatif(notification.dateCreation)}
                    </span>
                  </span>
                  {!notification.lu && <span className="cloche-point" />}
                </button>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
}
