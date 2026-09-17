import client from "./client";

export function chargerKanban(projetId) {
  return client.get(`/taches/projet/${projetId}/kanban`);
}

export function listerTachesProjet(projetId) {
  return client.get(`/taches/projet/${projetId}`);
}

export function listerTachesUtilisateur(utilisateurId) {
  return client.get(`/taches/utilisateur/${utilisateurId}`);
}

export function consulterTache(id) {
  return client.get(`/taches/${id}`);
}

export function listerSousTaches(id) {
  return client.get(`/taches/${id}/sous-taches`);
}

export function creerTache(donnees) {
  return client.post("/taches", donnees);
}

export function modifierTache(id, donnees) {
  return client.put(`/taches/${id}`, donnees);
}

export function changerStatut(id, statut) {
  return client.patch(`/taches/${id}/statut`, { statut });
}

export function assignerTache(tacheId, utilisateurId) {
  return client.patch(`/taches/${tacheId}/assignation/${utilisateurId}`);
}

export function desassignerTache(tacheId) {
  return client.delete(`/taches/${tacheId}/assignation`);
}

export function supprimerTache(id) {
  return client.delete(`/taches/${id}`);
}

/**
 * Export Excel des taches d'un projet.
 *
 * Comme pour les pieces jointes, le jeton JWT voyage dans un
 * en-tete : un simple lien <a href> ne peut pas le porter. On
 * recupere donc le fichier en memoire avant de declencher
 * l'enregistrement.
 */
export async function exporterTaches(projetId, nomFichier) {
  const reponse = await client.get(`/taches/projet/${projetId}/export`, {
    responseType: "blob",
  });

  const url = window.URL.createObjectURL(new Blob([reponse.data]));
  const lien = document.createElement("a");
  lien.href = url;
  lien.setAttribute("download", nomFichier);
  document.body.appendChild(lien);
  lien.click();

  lien.remove();
  window.URL.revokeObjectURL(url);
}
