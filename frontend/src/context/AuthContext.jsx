import { createContext, useContext, useEffect, useState } from "react";
import * as apiAuth from "../api/auth";

const AuthContext = createContext(null);

export function FournisseurAuth({ children }) {
  const [utilisateur, setUtilisateur] = useState(null);
  const [chargement, setChargement] = useState(true);

  // Au demarrage : on relit ce qui est garde dans le navigateur.
  useEffect(() => {
    const enregistre = localStorage.getItem("utilisateur");
    if (enregistre) {
      try {
        setUtilisateur(JSON.parse(enregistre));
      } catch {
        localStorage.removeItem("utilisateur");
      }
    }
    setChargement(false);
  }, []);

  async function seConnecter(email, motDePasse) {
    const { data } = await apiAuth.connexion(email, motDePasse);
    localStorage.setItem("jeton", data.jeton);
    localStorage.setItem("utilisateur", JSON.stringify(data.utilisateur));
    setUtilisateur(data.utilisateur);
    return data.utilisateur;
  }

  function seDeconnecter() {
    localStorage.removeItem("jeton");
    localStorage.removeItem("utilisateur");
    setUtilisateur(null);
  }

  const valeur = {
    utilisateur,
    chargement,
    estConnecte: utilisateur !== null,
    seConnecter,
    seDeconnecter,
  };

  return <AuthContext.Provider value={valeur}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const contexte = useContext(AuthContext);
  if (!contexte) {
    throw new Error("useAuth doit etre utilise dans un FournisseurAuth");
  }
  return contexte;
}
