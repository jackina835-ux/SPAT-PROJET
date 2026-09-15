package mg.spat.gestion_projets.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Ce que le frontend envoie sur l'ecran de connexion. */
public class ConnexionDTO {

    @NotBlank(message = "L'adresse electronique est obligatoire")
    @Email(message = "L'adresse electronique n'est pas valide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String motDePasse;

    public ConnexionDTO() {
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
}
