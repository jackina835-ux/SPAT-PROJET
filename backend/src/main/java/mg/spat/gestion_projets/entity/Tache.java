package mg.spat.gestion_projets.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tache")
public class Tache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priorite priorite = Priorite.MOYENNE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutTache statut = StatutTache.A_FAIRE;

    @Column(name = "date_echeance")
    private LocalDate dateEcheance;

    @Column(name = "charge_estimee")
    private Integer chargeEstimee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "projet_id", nullable = false)
    private Projet projet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigne_a_id")
    private Utilisateur assigneA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tache_parent_id")
    private Tache tacheParent;

    @OneToMany(mappedBy = "tacheParent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tache> sousTaches = new ArrayList<>();

    @OneToMany(mappedBy = "tache", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Commentaire> commentaires = new ArrayList<>();

    /**
     * Taches qui doivent etre terminees avant que celle-ci
     * puisse avancer.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "dependance_tache",
        joinColumns = @JoinColumn(name = "tache_id"),
        inverseJoinColumns = @JoinColumn(name = "depend_de_id")
    )
    private Set<Tache> dependances = new HashSet<>();

    /**
     * L'autre sens de la meme relation : les taches qui
     * attendent celle-ci. Aucune table supplementaire,
     * c'est la meme lue a l'envers.
     */
    @ManyToMany(mappedBy = "dependances", fetch = FetchType.LAZY)
    private Set<Tache> bloque = new HashSet<>();

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    public Tache() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Priorite getPriorite() { return priorite; }
    public void setPriorite(Priorite priorite) { this.priorite = priorite; }

    public StatutTache getStatut() { return statut; }
    public void setStatut(StatutTache statut) { this.statut = statut; }

    public LocalDate getDateEcheance() { return dateEcheance; }
    public void setDateEcheance(LocalDate dateEcheance) { this.dateEcheance = dateEcheance; }

    public Integer getChargeEstimee() { return chargeEstimee; }
    public void setChargeEstimee(Integer chargeEstimee) { this.chargeEstimee = chargeEstimee; }

    public Projet getProjet() { return projet; }
    public void setProjet(Projet projet) { this.projet = projet; }

    public Utilisateur getAssigneA() { return assigneA; }
    public void setAssigneA(Utilisateur assigneA) { this.assigneA = assigneA; }

    public Tache getTacheParent() { return tacheParent; }
    public void setTacheParent(Tache tacheParent) { this.tacheParent = tacheParent; }

    public List<Tache> getSousTaches() { return sousTaches; }
    public void setSousTaches(List<Tache> sousTaches) { this.sousTaches = sousTaches; }

    public List<Commentaire> getCommentaires() { return commentaires; }
    public void setCommentaires(List<Commentaire> commentaires) { this.commentaires = commentaires; }

    public Set<Tache> getDependances() { return dependances; }
    public void setDependances(Set<Tache> dependances) { this.dependances = dependances; }

    public Set<Tache> getBloque() { return bloque; }
    public void setBloque(Set<Tache> bloque) { this.bloque = bloque; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public boolean estEnRetard() {
        return dateEcheance != null
                && dateEcheance.isBefore(LocalDate.now())
                && statut != StatutTache.TERMINEE;
    }

    public boolean estSousTache() {
        return tacheParent != null;
    }

    public void changerStatut(StatutTache nouveauStatut) {
        this.statut = nouveauStatut;
    }

    /** Dependances qui ne sont pas encore terminees. */
    public List<Tache> dependancesNonTerminees() {
        return dependances.stream()
                .filter(d -> d.getStatut() != StatutTache.TERMINEE)
                .toList();
    }

    /** Vrai si au moins une dependance reste ouverte. */
    public boolean estBloquee() {
        return !dependancesNonTerminees().isEmpty();
    }
}
