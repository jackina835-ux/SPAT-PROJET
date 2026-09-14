package mg.spat.gestion_projets.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Intercepte les exceptions levees par les controleurs
 * et renvoie une reponse JSON coherente au lieu d'une page d'erreur.
 */
@RestControllerAdvice
public class GestionnaireErreurs {

    @ExceptionHandler(RessourceIntrouvableException.class)
    public ResponseEntity<Map<String, Object>> introuvable(RessourceIntrouvableException ex) {
        return reponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(RegleMetierException.class)
    public ResponseEntity<Map<String, Object>> regleMetier(RegleMetierException ex) {
        return reponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> accesRefuse(AccessDeniedException ex) {
        return reponse(HttpStatus.FORBIDDEN,
                "Vous n'avez pas les droits necessaires pour cette action");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> authentification(AuthenticationException ex) {
        return reponse(HttpStatus.UNAUTHORIZED,
                "Authentification requise ou jeton invalide");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex) {
        Map<String, String> champs = new HashMap<>();
        for (FieldError erreur : ex.getBindingResult().getFieldErrors()) {
            champs.put(erreur.getField(), erreur.getDefaultMessage());
        }
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.BAD_REQUEST.value());
        corps.put("message", "Donnees invalides");
        corps.put("champs", champs);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corps);
    }

    private ResponseEntity<Map<String, Object>> reponse(HttpStatus statut, String message) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", statut.value());
        corps.put("message", message);
        return ResponseEntity.status(statut).body(corps);
    }
}
