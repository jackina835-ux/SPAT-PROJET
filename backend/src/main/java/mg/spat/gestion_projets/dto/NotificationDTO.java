package mg.spat.gestion_projets.dto;

import mg.spat.gestion_projets.entity.Notification;
import mg.spat.gestion_projets.entity.TypeNotification;

import java.time.LocalDateTime;

public class NotificationDTO {

    private Long id;
    private String message;
    private TypeNotification type;
    private boolean lu;
    private Long tacheId;
    private Long projetId;
    private LocalDateTime dateCreation;

    public NotificationDTO() {
    }

    public static NotificationDTO depuis(Notification n) {
        NotificationDTO dto = new NotificationDTO();
        dto.id = n.getId();
        dto.message = n.getMessage();
        dto.type = n.getType();
        dto.lu = Boolean.TRUE.equals(n.getLu());
        dto.tacheId = n.getTacheId();
        dto.projetId = n.getProjetId();
        dto.dateCreation = n.getDateCreation();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public TypeNotification getType() { return type; }
    public void setType(TypeNotification type) { this.type = type; }

    public boolean isLu() { return lu; }
    public void setLu(boolean lu) { this.lu = lu; }

    public Long getTacheId() { return tacheId; }
    public void setTacheId(Long tacheId) { this.tacheId = tacheId; }

    public Long getProjetId() { return projetId; }
    public void setProjetId(Long projetId) { this.projetId = projetId; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}
