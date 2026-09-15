package mg.spat.gestion_projets.dto;

import mg.spat.gestion_projets.entity.Projet;
import mg.spat.gestion_projets.entity.StatutProjet;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Ce qui sort vers le frontend quand on demande un projet.
 */
public class ProjetDTO {

    private Long id;
    private String nom;
    private String description;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private StatutProjet statut;
    private UtilisateurSimpleDTO responsable;
    private List<UtilisateurSimpleDTO> membres;
    private double avancement;
    private boolean enRetard;
    private int nombreTaches;

    public ProjetDTO() {
    }

    public static ProjetDTO depuis(Projet p) {
        ProjetDTO dto = new ProjetDTO();
        dto.id = p.getId();
        dto.nom = p.getNom();
        dto.description = p.getDescription();
        dto.dateDebut = p.getDateDebut();
        dto.dateFin = p.getDateFin();
        dto.statut = p.getStatut();
        dto.responsable = UtilisateurSimpleDTO.depuis(p.getResponsable());
        dto.membres = p.getMembres().stream()
                .map(UtilisateurSimpleDTO::depuis)
                .collect(Collectors.toList());
        dto.avancement = p.calculerAvancement();
        dto.enRetard = p.estEnRetard();
        dto.nombreTaches = p.getTaches() == null ? 0 : p.getTaches().size();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public StatutProjet getStatut() { return statut; }
    public void setStatut(StatutProjet statut) { this.statut = statut; }

    public UtilisateurSimpleDTO getResponsable() { return responsable; }
    public void setResponsable(UtilisateurSimpleDTO responsable) { this.responsable = responsable; }

    public List<UtilisateurSimpleDTO> getMembres() { return membres; }
    public void setMembres(List<UtilisateurSimpleDTO> membres) { this.membres = membres; }

    public double getAvancement() { return avancement; }
    public void setAvancement(double avancement) { this.avancement = avancement; }

    public boolean isEnRetard() { return enRetard; }
    public void setEnRetard(boolean enRetard) { this.enRetard = enRetard; }

    public int getNombreTaches() { return nombreTaches; }
    public void setNombreTaches(int nombreTaches) { this.nombreTaches = nombreTaches; }
}
