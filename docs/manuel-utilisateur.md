# Manuel utilisateur — Plateforme de Gestion de Projets et de Tâches

SPAT (Société du Port à Gestion Autonome de Toamasina)

---

## 1. Se connecter

Ouvrez l'application dans votre navigateur. Sur l'écran de connexion, saisissez
votre adresse e-mail professionnelle et votre mot de passe, puis validez.

Vous restez connecté jusqu'à déconnexion volontaire (bouton **Déconnexion** en
haut à droite) ou expiration de la session (24 heures).

Il existe trois types de comptes, avec des droits différents :

| Rôle | Ce qu'il peut faire |
|---|---|
| **Administrateur** | Tout, plus la gestion des comptes utilisateurs |
| **Chef de projet** | Créer des projets, gérer les siens de bout en bout |
| **Membre d'équipe** | Consulter ses projets, faire avancer ses tâches, commenter, saisir son temps |

Si vous avez oublié votre mot de passe, contactez un administrateur : la
création et la réinitialisation des comptes se font en interne.

## 2. La page Projets

C'est la page d'accueil après connexion. Elle liste tous les projets auxquels
vous avez accès, avec pour chacun :

- son statut (Planifié, En cours, Terminé) et un signal **En retard** si
  l'échéance est dépassée sans que le projet soit terminé ;
- une barre de progression et le nombre de tâches ;
- l'échéance et les avatars des membres.

Cliquez sur une carte de projet pour ouvrir son tableau Kanban.

### Créer un projet

Réservé aux administrateurs et chefs de projet. Bouton **Nouveau projet** en
haut à droite : renseignez le nom (obligatoire), une description, les dates de
début et de fin, puis validez. Vous devenez automatiquement responsable et
membre du projet créé.

## 3. Le tableau Kanban

Chaque projet possède un tableau à quatre colonnes : **À faire**, **En
cours**, **En révision**, **Terminée**.

### Déplacer une tâche

Faites glisser une carte d'une colonne à l'autre. Seuls le chef du projet et la
personne assignée à la tâche peuvent la déplacer ; les autres cartes sont
figées (curseur non actif).

Deux règles empêchent certains déplacements :

- une tâche qui **attend** une autre tâche non terminée (dépendance) ne peut
  pas quitter la colonne « À faire » ;
- une tâche ne peut pas passer à « Terminée » tant que ses sous-tâches ne le
  sont pas toutes.

Si un déplacement est refusé, la carte revient automatiquement à sa place et
un message explique pourquoi.

### Créer une tâche

Réservé à l'administrateur et au responsable du projet. Bouton **Nouvelle
tâche** : titre (obligatoire), description, priorité, échéance et personne
assignée (uniquement parmi les membres du projet).

### Modifier une tâche

Ouvrez une carte pour afficher son détail, puis cliquez sur **Modifier** en
haut du panneau (visible pour l'administrateur et le responsable du projet).
Vous pouvez y corriger le titre, la description, la priorité, le statut,
l'échéance, la charge estimée et la personne assignée. **Enregistrer**
applique les changements immédiatement ; **Annuler** revient au détail sans
rien changer.

### Supprimer une tâche

Bouton **×** sur la carte, dans le tableau Kanban (administrateur et
responsable du projet uniquement). La suppression est définitive et retire
aussi les commentaires liés.

## 4. Le détail d'une tâche

Cliquer sur une carte ouvre un panneau latéral avec :

- **Sous-tâches** : liste des tâches rattachées, avec leur statut.
- **Dépendances** : les tâches que celle-ci attend, et celles qui l'attendent.
  Le responsable du projet et l'administrateur peuvent ajouter ou retirer une
  dépendance depuis ce panneau.
- **Pièces jointes** : tout membre du projet peut déposer un fichier (10 Mo
  maximum : PDF, images, bureautique, archives) et le télécharger en cliquant
  dessus.
- **Temps passé** : bouton **Saisir du temps** pour enregistrer une durée
  (heures et minutes), une date de travail et une courte description de ce qui
  a été fait. Un récapitulatif compare le temps réalisé à la charge estimée.
- **Discussion** : fil de commentaires ouvert à tous les membres du projet.
  Un commentaire peut être supprimé par son auteur, le chef du projet, ou un
  administrateur.

## 5. Gérer les membres d'un projet

Réservé à l'administrateur et au responsable du projet. Depuis le Kanban,
cliquez sur **Membres** pour ouvrir le panneau de gestion :

- la liste des membres actuels s'affiche en haut, avec un bouton **×** pour
  retirer chacun d'eux (le responsable du projet ne peut pas être retiré) ;
- la liste des comptes actifs qui ne sont pas encore membres s'affiche en bas,
  avec un bouton **Ajouter** pour chacun.

Un membre nouvellement ajouté reçoit une notification et peut immédiatement se
voir assigner des tâches.

## 6. La charge de l'équipe

Depuis le Kanban, bouton **Charge de l'équipe** : pour chaque membre du
projet, la page affiche ses heures restantes estimées, ses tâches actives et
terminées, ses tâches en retard, et sa prochaine échéance. Un niveau
(Disponible / Charge normale / Bien occupé / Surcharge) donne une lecture
rapide de qui est libre pour prendre une nouvelle tâche.

## 7. Le calendrier des échéances

Depuis le Kanban, bouton **Calendrier** : vue mensuelle des tâches par date
d'échéance, avec un code couleur par priorité. Les tâches sans échéance sont
listées à part, en bas de la page. Cliquer sur une tâche ouvre son détail.

## 8. Les notifications

La cloche en haut à droite indique le nombre de notifications non lues. Vous
êtes notifié quand :

- une tâche vous est confiée ou retirée ;
- une tâche que vous suivez change de statut ;
- vous êtes ajouté à un projet.

Cliquer sur une notification vous amène au Kanban du projet concerné et la
marque comme lue. **Tout marquer comme lu** efface le compteur d'un coup. Les
notifications sont rafraîchies automatiquement toutes les 30 secondes.

## 9. Conseils

- Le retrait d'un fichier, d'un commentaire, d'une tâche ou d'un membre est
  définitif : une confirmation est toujours demandée avant.
- Si une action semble bloquée ou échoue sans raison apparente, vérifiez le
  message d'erreur affiché en haut de la page : il indique le plus souvent la
  cause exacte (droit insuffisant, dépendance non résolue, sous-tâche non
  terminée…).
- L'interface Kanban n'est pas encore adaptée aux petits écrans : privilégiez
  un ordinateur pour l'usage courant.
