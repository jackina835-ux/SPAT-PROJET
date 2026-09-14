package mg.spat.gestion_projets.config;

import mg.spat.gestion_projets.entity.Utilisateur;
import mg.spat.gestion_projets.repository.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * S'execute une seule fois au demarrage.
 *
 * Les utilisateurs inseres a la main en SQL ont leur mot de passe
 * en clair. Cette classe les remplace par leur version chiffree
 * avec BCrypt. Une fois convertis, ils ne sont plus touches.
 *
 * Cette classe pourra etre supprimee quand tous les comptes
 * auront ete crees par l'application elle-meme.
 */
@Component
public class MigrationMotsDePasse implements CommandLineRunner {

    private static final Logger journal = LoggerFactory.getLogger(MigrationMotsDePasse.class);

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public MigrationMotsDePasse(UtilisateurRepository utilisateurRepository,
                                PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        List<Utilisateur> tous = utilisateurRepository.findAll();
        int convertis = 0;

        for (Utilisateur u : tous) {
            String motDePasse = u.getMotDePasse();
            // Un hachage BCrypt commence toujours par $2a$, $2b$ ou $2y$
            if (motDePasse != null && !motDePasse.startsWith("$2")) {
                u.setMotDePasse(passwordEncoder.encode(motDePasse));
                utilisateurRepository.save(u);
                convertis++;
            }
        }

        if (convertis > 0) {
            journal.info("Migration des mots de passe : {} compte(s) chiffre(s) avec BCrypt",
                    convertis);
        }
    }
}
