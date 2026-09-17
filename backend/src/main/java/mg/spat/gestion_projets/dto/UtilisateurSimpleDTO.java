package mg.spat.gestion_projets.dto;

import mg.spat.gestion_projets.entity.Role;
import mg.spat.gestion_projets.entity.Utilisateur;

/**
 * Vue allegee d'un utilisateur, utilisee a l'interieur des autres
 * reponses (responsable d'un projet, membre d'une equipe...).
 * Ne contient jamais le mot de passe.
 */
public class UtilisateurSimpleDTO {

    private Long id;
    private String nomComplet;
    private String email;
    private Role role;
    private Boolean actif;

    public UtilisateurSimpleDTO() {
    }

    public static UtilisateurSimpleDTO depuis(Utilisateur u) {
        if (u == null) {
            return null;
        }
        UtilisateurSimpleDTO dto = new UtilisateurSimpleDTO();
        dto.id = u.getId();
        dto.nomComplet = u.getNomComplet();
        dto.email = u.getEmail();
        dto.role = u.getRole();
        dto.actif = u.getActif();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomComplet() { return nomComplet; }
    public void setNomComplet(String nomComplet) { this.nomComplet = nomComplet; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }
}
