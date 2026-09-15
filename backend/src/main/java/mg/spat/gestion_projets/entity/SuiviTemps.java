package mg.spat.gestion_projets.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Une saisie de temps : X minutes passees sur une tache,
 * tel jour, par telle personne.
 *
 * Le temps est stocke en MINUTES et non en heures. Un entier
 * de minutes evite toute approximation : 1 h 45 se note 105,
 * pas 1,75 avec ses arrondis.
 */
@Entity
@Table(name = "suivi_temps")
public class SuiviTemps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tache_id", nullable = false)
    private Tache tache;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(name = "duree_minutes", nullable = false)
    private Integer dureeMinutes;

    @Column(name = "date_travail", nullable = false)
    private LocalDate dateTravail;

    @Column(length = 200)
    private String commentaire;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    public SuiviTemps() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Tache getTache() { return tache; }
    public void setTache(Tache tache) { this.tache = tache; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }

    public Integer getDureeMinutes() { return dureeMinutes; }
    public void setDureeMinutes(Integer dureeMinutes) { this.dureeMinutes = dureeMinutes; }

    public LocalDate getDateTravail() { return dateTravail; }
    public void setDateTravail(LocalDate dateTravail) { this.dateTravail = dateTravail; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}
