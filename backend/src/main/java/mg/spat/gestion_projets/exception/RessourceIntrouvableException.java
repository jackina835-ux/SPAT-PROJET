package mg.spat.gestion_projets.exception;

/**
 * Levee quand une entite demandee n'existe pas en base.
 * Traduite en reponse HTTP 404 par le GestionnaireErreurs.
 */
public class RessourceIntrouvableException extends RuntimeException {

    public RessourceIntrouvableException(String message) {
        super(message);
    }

    public static RessourceIntrouvableException pour(String type, Long id) {
        return new RessourceIntrouvableException(type + " introuvable avec l'identifiant " + id);
    }
}
