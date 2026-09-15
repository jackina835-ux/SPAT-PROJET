package mg.spat.gestion_projets.dto;

import java.util.Map;

/**
 * Charge de travail d'une personne : combien de taches elle porte,
 * comment elles se repartissent, et combien d'heures restent
 * estimees sur ce qui n'est pas termine.
 */
public class ChargeMembreDTO {

    private UtilisateurSimpleDTO membre;

    private int tachesTotal;
    private int tachesActives;      // tout sauf TERMINEE
    private int tachesTerminees;
    private int tachesEnRetard;

    private Map<String, Long> parStatut;
    private Map<String, Long> parPriorite;

    private int heuresEstimees;     // sur les taches actives
    private Integer prochaineEcheance; // jours restants, null si aucune

    private String niveau;          // LIBRE, NORMAL, CHARGE, SURCHARGE

    public ChargeMembreDTO() {
    }

    public UtilisateurSimpleDTO getMembre() { return membre; }
    public void setMembre(UtilisateurSimpleDTO membre) { this.membre = membre; }

    public int getTachesTotal() { return tachesTotal; }
    public void setTachesTotal(int tachesTotal) { this.tachesTotal = tachesTotal; }

    public int getTachesActives() { return tachesActives; }
    public void setTachesActives(int tachesActives) { this.tachesActives = tachesActives; }

    public int getTachesTerminees() { return tachesTerminees; }
    public void setTachesTerminees(int tachesTerminees) { this.tachesTerminees = tachesTerminees; }

    public int getTachesEnRetard() { return tachesEnRetard; }
    public void setTachesEnRetard(int tachesEnRetard) { this.tachesEnRetard = tachesEnRetard; }

    public Map<String, Long> getParStatut() { return parStatut; }
    public void setParStatut(Map<String, Long> parStatut) { this.parStatut = parStatut; }

    public Map<String, Long> getParPriorite() { return parPriorite; }
    public void setParPriorite(Map<String, Long> parPriorite) { this.parPriorite = parPriorite; }

    public int getHeuresEstimees() { return heuresEstimees; }
    public void setHeuresEstimees(int heuresEstimees) { this.heuresEstimees = heuresEstimees; }

    public Integer getProchaineEcheance() { return prochaineEcheance; }
    public void setProchaineEcheance(Integer prochaineEcheance) {
        this.prochaineEcheance = prochaineEcheance;
    }

    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }
}
