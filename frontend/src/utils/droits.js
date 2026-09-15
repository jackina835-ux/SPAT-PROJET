/**
 * Regles d'affichage selon le role et la situation.
 *
 * ATTENTION : ceci ne protege rien. C'est du confort d'interface,
 * pour ne pas montrer des boutons qui echoueraient de toute facon.
 * La vraie protection est dans le backend, avec @PreAuthorize.
 * Un utilisateur malveillant peut modifier ce fichier dans son
 * navigateur : le serveur refusera quand meme.
 */

export function estAdmin(utilisateur) {
  return utilisateur?.role === "ADMIN";
}

export function estChefDeProjet(utilisateur) {
  return utilisateur?.role === "CHEF_PROJET";
}

/** Peut creer un nouveau projet. */
export function peutCreerProjet(utilisateur) {
  return estAdmin(utilisateur) || estChefDeProjet(utilisateur);
}

/** Est responsable de ce projet precis. */
export function estResponsable(utilisateur, projet) {
  if (!utilisateur || !projet?.responsable) return false;
  return projet.responsable.id === utilisateur.id;
}

/** Peut modifier ou supprimer ce projet. */
export function peutGererProjet(utilisateur, projet) {
  return estAdmin(utilisateur) || estResponsable(utilisateur, projet);
}

/** Peut creer, modifier ou supprimer les taches de ce projet. */
export function peutGererTaches(utilisateur, projet) {
  return estAdmin(utilisateur) || estResponsable(utilisateur, projet);
}

/** Peut deplacer cette carte : le chef du projet, ou l'assigne. */
export function peutDeplacerTache(utilisateur, projet, tache) {
  if (estAdmin(utilisateur)) return true;
  if (estResponsable(utilisateur, projet)) return true;
  return tache?.assigneA?.id === utilisateur?.id;
}

export const LIBELLE_ROLE = {
  ADMIN: "Administrateur",
  CHEF_PROJET: "Chef de projet",
  MEMBRE: "Membre d'equipe",
};
