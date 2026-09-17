import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { FournisseurAuth, useAuth } from "./context/AuthContext";
import RouteProtegee from "./components/RouteProtegee";
import Entete from "./components/Entete";
import Connexion from "./pages/Connexion";
import Accueil from "./pages/Accueil";
import Administration from "./pages/Administration";
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
        element={estConnecte ? <Navigate to="/accueil" replace /> : <Connexion />}
      />

      <Route
        path="/accueil"
        element={
          <RouteProtegee>
            <Disposition>
              <Accueil />
            </Disposition>
          </RouteProtegee>
        }
      />

      <Route
        path="/administration"
        element={
          <RouteProtegee>
            <Disposition>
              <Administration />
            </Disposition>
          </RouteProtegee>
        }
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

      <Route path="*" element={<Navigate to="/accueil" replace />} />
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
