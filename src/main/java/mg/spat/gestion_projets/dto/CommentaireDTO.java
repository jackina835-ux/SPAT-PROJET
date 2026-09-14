package mg.spat.gestion_projets.dto;

import mg.spat.gestion_projets.entity.Commentaire;

import java.time.LocalDateTime;

public class CommentaireDTO {

    private Long id;
    private String contenu;
    private LocalDateTime dateCreation;
    private Long tacheId;
    private UtilisateurSimpleDTO auteur;

    public CommentaireDTO() {
    }

    public static CommentaireDTO depuis(Commentaire c) {
        CommentaireDTO dto = new CommentaireDTO();
        dto.id = c.getId();
        dto.contenu = c.getContenu();
        dto.dateCreation = c.getDateCreation();
        dto.tacheId = c.getTache().getId();
        dto.auteur = UtilisateurSimpleDTO.depuis(c.getAuteur());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public Long getTacheId() { return tacheId; }
    public void setTacheId(Long tacheId) { this.tacheId = tacheId; }

    public UtilisateurSimpleDTO getAuteur() { return auteur; }
    public void setAuteur(UtilisateurSimpleDTO auteur) { this.auteur = auteur; }
}
