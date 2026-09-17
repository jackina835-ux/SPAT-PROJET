import client, { URL_API } from "./client";

export function listerPieces(tacheId) {
  return client.get(`/pieces-jointes/tache/${tacheId}`);
}

/**
 * Envoi d'un fichier.
 * On ne fixe pas Content-Type a la main : le navigateur doit
 * le composer lui-meme avec la frontiere du multipart.
 */
export function deposerPiece(tacheId, fichier, surProgression) {
  const donnees = new FormData();
  donnees.append("fichier", fichier);

  return client.post(`/pieces-jointes/tache/${tacheId}`, donnees, {
    headers: { "Content-Type": "multipart/form-data" },
    onUploadProgress: (evenement) => {
      if (surProgression && evenement.total) {
        surProgression(Math.round((evenement.loaded * 100) / evenement.total));
      }
    },
  });
}

export function supprimerPiece(id) {
  return client.delete(`/pieces-jointes/${id}`);
}

/** Efface les fichiers du disque sans ligne correspondante en base. */
export function nettoyerOrphelins() {
  return client.post("/pieces-jointes/nettoyer-orphelins");
}

/**
 * Telechargement.
 *
 * On ne peut pas utiliser un simple lien <a href> : le jeton
 * JWT voyage dans un en-tete, et un lien ne permet pas d'en
 * ajouter. On recupere donc le fichier en memoire, puis on
 * declenche l'enregistrement.
 */
export async function telechargerPiece(id, nomFichier) {
  const reponse = await client.get(`/pieces-jointes/${id}/telecharger`, {
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

export { URL_API };
