package mg.spat.gestion_projets.exception;

/**
 * Levee quand une regle de gestion est violee.
 * Traduite en reponse HTTP 400 par le GestionnaireErreurs.
 */
public class RegleMetierException extends RuntimeException {

    public RegleMetierException(String message) {
        super(message);
    }
}
