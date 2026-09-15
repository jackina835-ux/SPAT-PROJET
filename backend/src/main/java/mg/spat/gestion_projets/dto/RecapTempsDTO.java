package mg.spat.gestion_projets.dto;

import java.util.List;

/**
 * Comparaison entre ce qui etait prevu et ce qui a ete fait,
 * pour une tache ou pour un projet entier.
 */
public class RecapTempsDTO {

    private int estimeMinutes;
    private int passeMinutes;
    private String estimeLisible;
    private String passeLisible;

    /** Part du temps estime deja consommee, en pourcentage. */
    private Double consommation;

    /** Vrai si le temps passe depasse l'estimation. */
    private boolean depassement;

    private List<SuiviTempsDTO> saisies;

    public RecapTempsDTO() {
    }

    public int getEstimeMinutes() { return estimeMinutes; }
    public void setEstimeMinutes(int estimeMinutes) { this.estimeMinutes = estimeMinutes; }

    public int getPasseMinutes() { return passeMinutes; }
    public void setPasseMinutes(int passeMinutes) { this.passeMinutes = passeMinutes; }

    public String getEstimeLisible() { return estimeLisible; }
    public void setEstimeLisible(String estimeLisible) { this.estimeLisible = estimeLisible; }

    public String getPasseLisible() { return passeLisible; }
    public void setPasseLisible(String passeLisible) { this.passeLisible = passeLisible; }

    public Double getConsommation() { return consommation; }
    public void setConsommation(Double consommation) { this.consommation = consommation; }

    public boolean isDepassement() { return depassement; }
    public void setDepassement(boolean depassement) { this.depassement = depassement; }

    public List<SuiviTempsDTO> getSaisies() { return saisies; }
    public void setSaisies(List<SuiviTempsDTO> saisies) { this.saisies = saisies; }
}
