package mg.spat.gestion_projets.dto;

/** Ce que le serveur renvoie apres une connexion reussie. */
public class ReponseConnexionDTO {

    private String jeton;
    private String type = "Bearer";
    private long expireDansMs;
    private UtilisateurSimpleDTO utilisateur;

    public ReponseConnexionDTO() {
    }

    public ReponseConnexionDTO(String jeton, long expireDansMs,
                               UtilisateurSimpleDTO utilisateur) {
        this.jeton = jeton;
        this.expireDansMs = expireDansMs;
        this.utilisateur = utilisateur;
    }

    public String getJeton() { return jeton; }
    public void setJeton(String jeton) { this.jeton = jeton; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public long getExpireDansMs() { return expireDansMs; }
    public void setExpireDansMs(long expireDansMs) { this.expireDansMs = expireDansMs; }

    public UtilisateurSimpleDTO getUtilisateur() { return utilisateur; }
    public void setUtilisateur(UtilisateurSimpleDTO utilisateur) { this.utilisateur = utilisateur; }
}
