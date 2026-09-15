import client from "./client";

export function listerCommentaires(tacheId) {
  return client.get(`/commentaires/tache/${tacheId}`);
}

export function ecrireCommentaire(contenu, tacheId, auteurId) {
  return client.post("/commentaires", { contenu, tacheId, auteurId });
}

export function modifierCommentaire(id, auteurId, contenu) {
  return client.put(`/commentaires/${id}`, { auteurId, contenu });
}

export function supprimerCommentaire(id) {
  return client.delete(`/commentaires/${id}`);
}
