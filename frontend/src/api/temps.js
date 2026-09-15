import client from "./client";

export function saisirTemps(donnees) {
  return client.post("/temps", donnees);
}

export function recapTache(tacheId) {
  return client.get(`/temps/tache/${tacheId}`);
}

export function recapProjet(projetId) {
  return client.get(`/temps/projet/${projetId}`);
}

export function mesSaisies() {
  return client.get("/temps/moi");
}

export function supprimerSaisie(id) {
  return client.delete(`/temps/${id}`);
}
