package mg.spat.gestion_projets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CommentaireCreationDTO {

    @NotBlank(message = "Le contenu du commentaire est obligatoire")
    private String contenu;

    @NotNull(message = "La tache est obligatoire")
    private Long tacheId;

    @NotNull(message = "L'auteur est obligatoire")
    private Long auteurId;

    public CommentaireCreationDTO() {
    }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public Long getTacheId() { return tacheId; }
    public void setTacheId(Long tacheId) { this.tacheId = tacheId; }

    public Long getAuteurId() { return auteurId; }
    public void setAuteurId(Long auteurId) { this.auteurId = auteurId; }
}
