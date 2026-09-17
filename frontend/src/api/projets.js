import client from "./client";

export function listerProjets() {
  return client.get("/projets");
}

export function consulterProjet(id) {
  return client.get(`/projets/${id}`);
}

export function creerProjet(donnees) {
  return client.post("/projets", donnees);
}

export function modifierProjet(id, donnees) {
  return client.put(`/projets/${id}`, donnees);
}

export function supprimerProjet(id) {
  return client.delete(`/projets/${id}`);
}

export function ajouterMembre(projetId, utilisateurId) {
  return client.post(`/projets/${projetId}/membres/${utilisateurId}`);
}

export function retirerMembre(projetId, utilisateurId) {
  return client.delete(`/projets/${projetId}/membres/${utilisateurId}`);
}
