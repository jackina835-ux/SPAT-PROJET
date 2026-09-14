package mg.spat.gestion_projets.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import mg.spat.gestion_projets.entity.Role;

/** Creation d'un compte utilisateur. */
public class InscriptionDTO {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 80, message = "Le nom ne peut pas depasser 80 caracteres")
    private String nom;

    @NotBlank(message = "Le prenom est obligatoire")
    @Size(max = 80, message = "Le prenom ne peut pas depasser 80 caracteres")
    private String prenom;

    @NotBlank(message = "L'adresse electronique est obligatoire")
    @Email(message = "L'adresse electronique n'est pas valide")
    @Size(max = 150)
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caracteres")
    private String motDePasse;

    @NotNull(message = "Le role est obligatoire")
    private Role role;

    public InscriptionDTO() {
    }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
