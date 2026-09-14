package mg.spat.gestion_projets.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import mg.spat.gestion_projets.entity.Utilisateur;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Fabrique et verifie les jetons JWT.
 *
 * Un jeton est une chaine signee qui contient l'identite de
 * l'utilisateur. Le serveur ne garde rien en memoire : il verifie
 * simplement la signature a chaque requete.
 */
@Service
public class JwtService {

    private final SecretKey cle;
    private final long dureeValidite;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration}") long dureeValidite) {
        this.cle = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.dureeValidite = dureeValidite;
    }

    /** Fabrique un jeton pour un utilisateur qui vient de se connecter. */
    public String genererJeton(Utilisateur utilisateur) {
        Date maintenant = new Date();
        Date expiration = new Date(maintenant.getTime() + dureeValidite);

        return Jwts.builder()
                .subject(utilisateur.getEmail())
                .claim("id", utilisateur.getId())
                .claim("role", utilisateur.getRole().name())
                .claim("nomComplet", utilisateur.getNomComplet())
                .issuedAt(maintenant)
                .expiration(expiration)
                .signWith(cle)
                .compact();
    }

    /** Extrait l'email contenu dans le jeton. */
    public String extraireEmail(String jeton) {
        return lireContenu(jeton).getSubject();
    }

    /** Extrait le role contenu dans le jeton. */
    public String extraireRole(String jeton) {
        return lireContenu(jeton).get("role", String.class);
    }

    /** Extrait l'identifiant de l'utilisateur. */
    public Long extraireId(String jeton) {
        Object valeur = lireContenu(jeton).get("id");
        return valeur == null ? null : Long.valueOf(String.valueOf(valeur));
    }

    /** Verifie que le jeton est valide et non expire. */
    public boolean estValide(String jeton) {
        try {
            Claims contenu = lireContenu(jeton);
            return contenu.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public long getDureeValidite() {
        return dureeValidite;
    }

    private Claims lireContenu(String jeton) {
        return Jwts.parser()
                .verifyWith(cle)
                .build()
                .parseSignedClaims(jeton)
                .getPayload();
    }
}
