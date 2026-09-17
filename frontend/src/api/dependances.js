import client from "./client";

export function consulterDependances(tacheId) {
  return client.get(`/dependances/tache/${tacheId}`);
}

export function listerCandidates(tacheId) {
  return client.get(`/dependances/tache/${tacheId}/candidates`);
}

export function ajouterDependance(tacheId, dependDeId) {
  return client.post(`/dependances/tache/${tacheId}/depend-de/${dependDeId}`);
}

export function retirerDependance(tacheId, dependDeId) {
  return client.delete(`/dependances/tache/${tacheId}/depend-de/${dependDeId}`);
}
