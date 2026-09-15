package mg.spat.gestion_projets.dto;

import java.util.List;

/**
 * Vue d'ensemble d'une equipe : la charge de chaque membre,
 * plus quelques totaux pour situer l'ensemble.
 */
public class ChargeEquipeDTO {

    private Long projetId;
    private String projetNom;

    private List<ChargeMembreDTO> membres;

    private int tachesNonAssignees;
    private int heuresTotalActives;
    private double heuresMoyennesParMembre;

    public ChargeEquipeDTO() {
    }

    public Long getProjetId() { return projetId; }
    public void setProjetId(Long projetId) { this.projetId = projetId; }

    public String getProjetNom() { return projetNom; }
    public void setProjetNom(String projetNom) { this.projetNom = projetNom; }

    public List<ChargeMembreDTO> getMembres() { return membres; }
    public void setMembres(List<ChargeMembreDTO> membres) { this.membres = membres; }

    public int getTachesNonAssignees() { return tachesNonAssignees; }
    public void setTachesNonAssignees(int tachesNonAssignees) {
        this.tachesNonAssignees = tachesNonAssignees;
    }

    public int getHeuresTotalActives() { return heuresTotalActives; }
    public void setHeuresTotalActives(int heuresTotalActives) {
        this.heuresTotalActives = heuresTotalActives;
    }

    public double getHeuresMoyennesParMembre() { return heuresMoyennesParMembre; }
    public void setHeuresMoyennesParMembre(double heuresMoyennesParMembre) {
        this.heuresMoyennesParMembre = heuresMoyennesParMembre;
    }
}
