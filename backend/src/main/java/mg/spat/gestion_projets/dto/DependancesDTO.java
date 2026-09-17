package mg.spat.gestion_projets.dto;

import java.util.List;

/**
 * Les deux sens de la relation, vus depuis une tache donnee.
 */
public class DependancesDTO {

    private Long tacheId;
    private String tacheTitre;

    /** Ce que cette tache attend. */
    private List<TacheLienDTO> dependances;

    /** Ce qui attend cette tache. */
    private List<TacheLienDTO> bloque;

    private boolean estBloquee;
    private int nonTerminees;

    public DependancesDTO() {
    }

    public Long getTacheId() { return tacheId; }
    public void setTacheId(Long tacheId) { this.tacheId = tacheId; }

    public String getTacheTitre() { return tacheTitre; }
    public void setTacheTitre(String tacheTitre) { this.tacheTitre = tacheTitre; }

    public List<TacheLienDTO> getDependances() { return dependances; }
    public void setDependances(List<TacheLienDTO> dependances) { this.dependances = dependances; }

    public List<TacheLienDTO> getBloque() { return bloque; }
    public void setBloque(List<TacheLienDTO> bloque) { this.bloque = bloque; }

    public boolean isEstBloquee() { return estBloquee; }
    public void setEstBloquee(boolean estBloquee) { this.estBloquee = estBloquee; }

    public int getNonTerminees() { return nonTerminees; }
    public void setNonTerminees(int nonTerminees) { this.nonTerminees = nonTerminees; }
}
