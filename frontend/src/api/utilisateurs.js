import client from "./client";

export function listerUtilisateurs() {
  return client.get("/utilisateurs");
}

export function listerTousUtilisateurs() {
  return client.get("/utilisateurs/tous");
}

export function basculerActivation(id) {
  return client.patch(`/utilisateurs/${id}/activation`);
}
