import client from "./client";

export function chargerEquipeProjet(projetId) {
  return client.get(`/equipes/projet/${projetId}/charge`);
}

export function chargerEquipeGlobale() {
  return client.get("/equipes/charge");
}
