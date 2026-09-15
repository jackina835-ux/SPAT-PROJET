import client from "./client";

export function connexion(email, motDePasse) {
  return client.post("/auth/connexion", { email, motDePasse });
}

export function inscription(donnees) {
  return client.post("/auth/inscription", donnees);
}

export function profil() {
  return client.get("/auth/moi");
}
