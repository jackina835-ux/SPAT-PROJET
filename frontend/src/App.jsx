import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { FournisseurAuth, useAuth } from "./context/AuthContext";
import RouteProtegee from "./components/RouteProtegee";
import Entete from "./components/Entete";
import Connexion from "./pages/Connexion";
import ListeProjets from "./pages/ListeProjets";
import Kanban from "./pages/Kanban";
import Equipe from "./pages/Equipe";
import Calendrier from "./pages/Calendrier";

function Disposition({ children }) {
  return (
    <>
      <Entete />
      {children}
    </>
  );
}

function Routage() {
  const { estConnecte } = useAuth();

  return (
    <Routes>
      <Route
        path="/connexion"
        element={estConnecte ? <Navigate to="/projets" replace /> : <Connexion />}
      />

      <Route
        path="/projets"
        element={
          <RouteProtegee>
            <Disposition>
              <ListeProjets />
            </Disposition>
          </RouteProtegee>
        }
      />

      <Route
        path="/projets/:projetId/kanban"
        element={
          <RouteProtegee>
            <Disposition>
              <Kanban />
            </Disposition>
          </RouteProtegee>
        }
      />

      <Route
        path="/projets/:projetId/equipe"
        element={
          <RouteProtegee>
            <Disposition>
              <Equipe />
            </Disposition>
          </RouteProtegee>
        }
      />

      <Route
        path="/projets/:projetId/calendrier"
        element={
          <RouteProtegee>
            <Disposition>
              <Calendrier />
            </Disposition>
          </RouteProtegee>
        }
      />

      <Route path="*" element={<Navigate to="/projets" replace />} />
    </Routes>
  );
}

export default function App() {
  return (
    <FournisseurAuth>
      <BrowserRouter>
        <Routage />
      </BrowserRouter>
    </FournisseurAuth>
  );
}
