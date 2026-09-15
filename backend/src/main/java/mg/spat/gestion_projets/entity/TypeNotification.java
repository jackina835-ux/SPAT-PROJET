package mg.spat.gestion_projets.entity;

public enum TypeNotification {
    ASSIGNATION,        // une tache vous est confiee
    DESASSIGNATION,     // une tache vous est retiree
    CHANGEMENT_STATUT,  // une de vos taches change de colonne
    COMMENTAIRE,        // quelqu'un commente une de vos taches
    AJOUT_PROJET,       // vous rejoignez un projet
    ECHEANCE_PROCHE     // reserve a un usage ulterieur
}
