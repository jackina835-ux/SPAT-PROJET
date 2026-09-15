import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

/** Empeche d'acceder a une page sans etre connecte. */
export default function RouteProtegee({ children }) {
  const { estConnecte, chargement } = useAuth();

  if (chargement) {
    return <div className="etat-vide">Chargement…</div>;
  }
  if (!estConnecte) {
    return <Navigate to="/connexion" replace />;
  }
  return children;
}
