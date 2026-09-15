import axios from "axios";

// Adresse du backend Spring Boot.
// Si tu reviens au port 8080, change uniquement cette ligne.
export const URL_API = "http://localhost:8081/api";

const client = axios.create({
  baseURL: URL_API,
  headers: { "Content-Type": "application/json" },
});

// Avant chaque requete : ajoute le jeton s'il existe.
client.interceptors.request.use((config) => {
  const jeton = localStorage.getItem("jeton");
  if (jeton) {
    config.headers.Authorization = `Bearer ${jeton}`;
  }
  return config;
});

// Apres chaque reponse : si le jeton est expire, on deconnecte.
client.interceptors.response.use(
  (reponse) => reponse,
  (erreur) => {
    if (erreur.response?.status === 401) {
      localStorage.removeItem("jeton");
      localStorage.removeItem("utilisateur");
      if (window.location.pathname !== "/connexion") {
        window.location.href = "/connexion";
      }
    }
    return Promise.reject(erreur);
  }
);

/** Extrait un message lisible depuis une erreur axios. */
export function messageErreur(erreur, secours = "Une erreur est survenue") {
  const donnees = erreur.response?.data;
  if (!donnees) return secours;
  if (donnees.champs) {
    return Object.values(donnees.champs).join(" · ");
  }
  return donnees.message || secours;
}

export default client;
