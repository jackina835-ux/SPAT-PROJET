# Documentation technique — Plateforme de Gestion de Projets et de Tâches

Stage à la SPAT (Société du Port à Gestion Autonome de Toamasina)
Autrice : RAZANADRAKOTO Dina Jackina — L3 Informatique, Électronique et Télécommunications, Université de Toamasina

---

## 1. Vue d'ensemble

L'application permet à une entreprise d'organiser son travail en projets et en
tâches : planification, répartition entre les membres d'une équipe, suivi de
l'avancement et respect des délais.

Trois rôles :

- **Administrateur** : gère les comptes utilisateurs.
- **Chef de projet** : crée des projets, y affecte des membres, les découpe en
  tâches et les distribue.
- **Membre d'équipe** : retrouve ses tâches, les fait avancer dans un tableau
  Kanban, échange via des commentaires attachés aux tâches.

## 2. Architecture

Architecture 3-tiers classique :

```
React (navigateur)          présentation
      ↓ API REST / JSON
Spring Boot (serveur)       logique métier, sécurité
      ↓ JPA / Hibernate
PostgreSQL                  données
```

| Composant | Version |
|---|---|
| Java | 21 LTS (Temurin) |
| Spring Boot | 4.1.1 |
| Maven | 3.9.16 |
| PostgreSQL | 15.19 |
| Node.js | ≥ 18 |
| React | 19 (Vite) |
| Authentification | JWT (jjwt 0.12.6) |

### Emplacements

```
C:\Projet_Gestion\
      ├── SPAT-PROJET\          dépôt Git
      │     ├── backend\        Spring Boot  (port 8081)
      │     └── frontend\       React        (port 5173)
      └── fichiers\             pièces jointes déposées sur le disque
```

Dépôt : `https://github.com/jackina835-ux/SPAT-PROJET`

### Lancement

```bash
# Terminal 1 — backend
cd C:\Projet_Gestion\SPAT-PROJET\backend
taskkill /F /IM java.exe
.\mvnw spring-boot:run

# Terminal 2 — frontend
cd C:\Projet_Gestion\SPAT-PROJET\frontend
npm run dev
```

Le backend écoute sur `http://localhost:8081`, le frontend sur
`http://localhost:5173` (ou 5174/5175 si le port est occupé — `SecurityConfig`
autorise déjà ces trois ports en CORS).

## 3. Base de données

Base `gestion_projets`, 9 tables.

| Table | Rôle |
|---|---|
| `utilisateur` | comptes, rôle, mot de passe BCrypt, indicateur `actif` |
| `projet` | nom, dates, statut, responsable |
| `projet_membre` | table de liaison projet ↔ utilisateur (clé primaire composée) |
| `tache` | titre, priorité, statut, échéance, charge estimée, projet, assigné, tâche parente |
| `commentaire` | contenu, tâche, auteur |
| `notification` | message, type, lu, destinataire |
| `suivi_temps` | durée en minutes, date de travail, tâche, utilisateur |
| `piece_jointe` | nom original, nom stocké, type MIME, taille |
| `dependance_tache` | liaison tâche ↔ tâche (clé primaire composée) |

### Valeurs énumérées

Écrites en MAJUSCULES sans accent, pour correspondre aux `enum` Java.

- `utilisateur.role` : `ADMIN` · `CHEF_PROJET` · `MEMBRE`
- `projet.statut` : `PLANIFIE` · `EN_COURS` · `TERMINE`
- `tache.statut` : `A_FAIRE` · `EN_COURS` · `EN_REVISION` · `TERMINEE`
- `tache.priorite` : `BASSE` · `MOYENNE` · `HAUTE` · `URGENTE`

### Décisions de conception

- **L'état « en retard » n'est pas stocké.** Il est calculé à l'affichage en
  comparant la date de fin/échéance à la date du jour (`Projet.estEnRetard()`,
  `Tache.estEnRetard()`). Une donnée calculée ne peut jamais se désynchroniser.
- **`ddl-auto=validate`.** Hibernate ne crée ni ne modifie jamais les tables.
  Tout changement de schéma passe par un script SQL exécuté à la main, avant le
  redémarrage de l'application.
- **Suppression logique des comptes.** Un utilisateur n'est jamais supprimé, il
  est désactivé (`actif = false`). Ses tâches et commentaires restent lisibles.
- **Cascades asymétriques.** Supprimer un projet supprime ses tâches. Supprimer
  un utilisateur ne supprime jamais ses tâches : elles doivent pouvoir être
  réattribuées.
- **`tache_id` dans `notification` n'est pas une clé étrangère.** Une
  notification doit survivre à la suppression de ce dont elle parle.
- **Le temps est stocké en minutes**, jamais en heures décimales (1 h 45 =
  105). `tache.charge_estimee` est en **heures** — incohérence héritée de la
  conception initiale, traitée à un seul endroit (`SuiviTempsService`).

## 4. Backend

Paquet racine `mg.spat.gestion_projets`, 70+ fichiers Java.

| Paquet | Contenu |
|---|---|
| `entity` | classes JPA correspondant aux tables + énumérations |
| `repository` | interfaces Spring Data JPA |
| `dto` | objets d'entrée et de sortie de l'API |
| `service` | règles de gestion — toute la logique est ici, annoté `@Transactional` |
| `controller` | points d'entrée HTTP, aucune logique |
| `security` | JWT, filtre, droits contextuels (`ServiceSecurite`) |
| `config` | configuration Spring Security, migration des mots de passe |
| `exception` | exceptions métier et gestionnaire centralisé |

### Conventions

- Le contrôleur reçoit et renvoie, rien d'autre.
- Le service porte les règles et est annoté `@Transactional`.
- Les DTO empêchent deux problèmes : l'exposition du mot de passe, et les
  boucles infinies de sérialisation (Projet → Tâche → Projet → …).
- `open-in-view=false` : toute lecture de relation différée doit se faire à
  l'intérieur d'une méthode `@Transactional`.

### Gestion des erreurs

`GestionnaireErreurs` traduit les exceptions en JSON cohérent :

| Exception | Code |
|---|---|
| `RessourceIntrouvableException` | 404 |
| `RegleMetierException` | 400 |
| `AccessDeniedException` | 403 |
| `MethodArgumentNotValidException` | 400 + détail par champ |
| `MaxUploadSizeExceededException` | 400 |

Un 403 au corps vide ne vient jamais de ce gestionnaire : c'est Spring
Security qui bloque, souvent parce qu'une exception dans un contrôleur a
déclenché une redirection vers `/error` (qui doit rester `permitAll()`).

### Points d'entrée de l'API

Toutes les routes sont préfixées par `/api`. Sauf mention contraire, une route
exige un jeton JWT valide (`Authorization: Bearer ...`).

#### `/auth` — `AuthController`

| Méthode | Route | Accès |
|---|---|---|
| POST | `/auth/connexion` | ouvert à tous |
| POST | `/auth/inscription` | ADMIN |
| GET | `/auth/moi` | connecté |
| POST | `/auth/mot-de-passe` | connecté |

#### `/utilisateurs` — `UtilisateurController`

| Méthode | Route | Accès |
|---|---|---|
| GET | `/utilisateurs` | ADMIN, CHEF_PROJET — annuaire des comptes actifs |

#### `/projets` — `ProjetController`

| Méthode | Route | Accès |
|---|---|---|
| GET | `/projets` | connecté |
| GET | `/projets/{id}` | connecté |
| GET | `/projets/utilisateur/{id}` | connecté |
| GET | `/projets/en-retard` | connecté |
| POST | `/projets` | ADMIN, CHEF_PROJET |
| PUT | `/projets/{id}` | ADMIN, responsable du projet |
| DELETE | `/projets/{id}` | ADMIN, responsable du projet |
| POST | `/projets/{id}/membres/{utilisateurId}` | ADMIN, responsable du projet |
| DELETE | `/projets/{id}/membres/{utilisateurId}` | ADMIN, responsable du projet |

#### `/taches` — `TacheController`

| Méthode | Route | Accès |
|---|---|---|
| GET | `/taches/{id}` | connecté |
| GET | `/taches/projet/{projetId}` | connecté |
| GET | `/taches/projet/{projetId}/kanban` | connecté |
| GET | `/taches/projet/{projetId}/en-retard` | connecté |
| GET | `/taches/utilisateur/{id}` | connecté |
| GET | `/taches/{id}/sous-taches` | connecté |
| POST | `/taches` | ADMIN, responsable du projet |
| PUT | `/taches/{id}` | ADMIN, responsable du projet |
| PATCH | `/taches/{id}/statut` | ADMIN, responsable, ou personne assignée |
| PATCH | `/taches/{tacheId}/assignation/{utilisateurId}` | ADMIN, responsable du projet |
| DELETE | `/taches/{tacheId}/assignation` | ADMIN, responsable du projet |
| DELETE | `/taches/{id}` | ADMIN, responsable du projet |

#### `/dependances` — `DependanceController`

| Méthode | Route | Accès |
|---|---|---|
| GET | `/dependances/tache/{id}` | membre du projet, ADMIN |
| GET | `/dependances/tache/{id}/candidates` | responsable du projet, ADMIN |
| POST | `/dependances/tache/{id}/depend-de/{autreId}` | responsable du projet, ADMIN |
| DELETE | `/dependances/tache/{id}/depend-de/{autreId}` | responsable du projet, ADMIN |

#### `/commentaires` — `CommentaireController`

| Méthode | Route | Accès |
|---|---|---|
| GET | `/commentaires/tache/{tacheId}` | connecté |
| POST | `/commentaires` | membre du projet (vérifié dans le service) |
| PUT | `/commentaires/{id}` | auteur |
| DELETE | `/commentaires/{id}` | ADMIN, auteur, chef du projet |

#### `/pieces-jointes` — `PieceJointeController`

| Méthode | Route | Accès |
|---|---|---|
| POST | `/pieces-jointes/tache/{tacheId}` | membre du projet (vérifié dans le service) |
| GET | `/pieces-jointes/tache/{tacheId}` | membre du projet, ADMIN |
| GET | `/pieces-jointes/{id}/telecharger` | membre du projet (vérifié dans le service) |
| DELETE | `/pieces-jointes/{id}` | membre du projet (vérifié dans le service) |

#### `/temps` — `SuiviTempsController`

| Méthode | Route | Accès |
|---|---|---|
| POST | `/temps` | membre du projet (vérifié dans le service) |
| GET | `/temps/tache/{tacheId}` | membre du projet, ADMIN |
| GET | `/temps/projet/{projetId}` | membre du projet, ADMIN |
| GET | `/temps/moi` | connecté |
| DELETE | `/temps/{id}` | auteur, chef du projet (vérifié dans le service) |

#### `/equipes` — `EquipeController`

| Méthode | Route | Accès |
|---|---|---|
| GET | `/equipes/projet/{projetId}/charge` | membre du projet, ADMIN |
| GET | `/equipes/charge` | ADMIN, CHEF_PROJET |

#### `/notifications` — `NotificationController`

| Méthode | Route | Accès |
|---|---|---|
| GET | `/notifications` | connecté (portée par le jeton) |
| GET | `/notifications/non-lues` | connecté |
| PATCH | `/notifications/{id}/lue` | connecté |
| PATCH | `/notifications/tout-lu` | connecté |
| DELETE | `/notifications/{id}` | connecté |

## 5. Sécurité

### Authentification

JWT sans session serveur. Connexion → jeton signé (24 h, champ `jeton` dans la
réponse) → envoyé dans l'en-tête `Authorization: Bearer ...` à chaque requête.
Mots de passe hachés avec BCrypt. Seule `POST /api/auth/connexion` est ouverte.

### Droits

Deux niveaux combinés :

- `hasRole('...')` — lit le rôle dans le jeton.
- `@securite.methode(#id)` — interroge la base (`ServiceSecurite`), pour les
  droits qui dépendent des données (un chef de projet n'a pas tous les droits
  sur **tous** les projets, seulement sur les siens).

| Action | Qui |
|---|---|
| Consulter projets et tâches | tout utilisateur connecté |
| Créer un projet | ADMIN, CHEF_PROJET |
| Modifier / supprimer un projet | ADMIN, responsable du projet |
| Gérer les membres | ADMIN, responsable du projet |
| Créer / modifier / supprimer une tâche | ADMIN, responsable du projet |
| Changer le statut d'une tâche | ADMIN, responsable, ou personne assignée |
| Écrire un commentaire | membre du projet |
| Supprimer un commentaire | ADMIN, auteur, chef du projet |
| Saisir du temps | membre du projet |
| Déposer / télécharger un fichier | membre du projet |
| Modifier les dépendances | ADMIN, chef du projet |
| Créer un compte | ADMIN uniquement |
| Consulter l'annuaire des comptes | ADMIN, CHEF_PROJET |

`frontend/src/utils/droits.js` **ne protège rien** : il cache les boutons
inutiles. La vraie sécurité est côté serveur.

## 6. Frontend

Vite + React 19.

| Dossier | Contenu |
|---|---|
| `api/` | un fichier par domaine, tous basés sur `client.js` |
| `context/` | `AuthContext` — utilisateur connecté, localStorage |
| `components/` | `Entete`, `Cloche`, `PanneauTache`, `PanneauMembres`, `RouteProtegee` |
| `pages/` | `Connexion`, `ListeProjets`, `Kanban`, `Equipe`, `Calendrier` |
| `utils/` | `droits.js` — affichage conditionnel |

### Points clés

- **`client.js` centralise tout.** Un intercepteur axios ajoute le jeton à
  chaque requête. Un second déconnecte automatiquement sur un 401. L'URL du
  backend est définie à un seul endroit.
- **Le déplacement de carte Kanban est optimiste.** La carte bouge à l'écran
  avant la réponse du serveur ; en cas de refus, elle revient à sa place.
- **`PanneauTache` est le composant central.** Il agrège cinq sections : détail
  (avec un mode édition pour titre/description/priorité/statut/échéance/charge/
  assignation), dépendances, pièces jointes, temps passé, discussion. Il charge
  tout en parallèle avec `Promise.all`.
- **`PanneauMembres`** ajoute et retire des membres d'un projet ; accessible
  depuis le bouton « Membres » du Kanban, réservé à l'administrateur et au
  responsable du projet.
- **Le téléchargement passe par un blob.** Un simple `<a href>` ne permet pas
  d'ajouter l'en-tête `Authorization`.

### Comptes de test

Mot de passe identique pour les trois : `motdepasse_test`

| Compte | Rôle |
|---|---|
| `jackina@corompre.mg` | ADMIN |
| `rado@corompre.mg` | CHEF_PROJET |
| `miora@corompre.mg` | MEMBRE |

## 7. Pièges déjà rencontrés

- **Port 8080/8081 occupé** par des processus Java fantômes →
  `taskkill /F /IM java.exe` avant chaque lancement.
- **Erreur CORS après redémarrage de Vite** (il prend 5174 si 5173 est occupé)
  → `SecurityConfig` autorise 5173, 5174, 5175.
- **Copie de fichiers Windows qui efface tout** : toujours choisir
  « Fusionner », jamais « Remplacer ».
- **Collage SQL dans psql** : coller les instructions une par une, sans
  commentaires, sinon tout passe derrière un `--`.
- **403 au corps vide** : vient de Spring Security, pas du gestionnaire
  d'erreurs. `/error` doit rester `permitAll()`.
- **`LazyInitializationException` masquée en 403** : tout le traitement d'une
  relation différée doit rester dans une seule méthode `@Transactional`
  (`open-in-view=false`).
- **Rechercher-remplacer global dangereux** : le package est en minuscules
  avec underscore (`gestion_projets`), le nom de classe en CamelCase sans
  underscore (`GestionProjetsApplication`) — ce sont deux choses différentes.
- **Machine à mémoire limitée** : sur un poste avec peu de RAM disponible, le
  démarrage de `spring-boot:run` peut échouer par manque de mémoire virtuelle
  (`insufficient memory ... G1 virtual space`). Fermer les autres programmes
  avant de lancer le backend, ou limiter le tas de la JVM :
  `mvnw -Dspring-boot.run.jvmArguments="-Xmx384m" spring-boot:run`.

## 8. Conventions à respecter

- Tout en français : classes, méthodes, variables, messages d'erreur.
- Pas d'accent dans le code Java ni dans les valeurs d'énumération.
- Les règles de gestion vont dans les services, jamais dans les contrôleurs.
- Chaque nouvelle entité exige un script SQL exécuté à la main avant le
  redémarrage (`ddl-auto=validate`).
- Chaque nouvelle route doit recevoir une annotation `@PreAuthorize`, sauf si
  le service filtre déjà par l'identité du jeton.
- Les DTO ne doivent jamais exposer de mot de passe ni de relation circulaire.
