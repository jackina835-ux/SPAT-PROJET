package mg.spat.gestion_projets.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Intercepte chaque requete entrante, lit le jeton dans l'en-tete
 * Authorization et, s'il est valide, declare l'utilisateur comme
 * authentifie pour la duree de la requete.
 */
@Component
public class JwtFiltre extends OncePerRequestFilter {

    private static final String ENTETE = "Authorization";
    private static final String PREFIXE = "Bearer ";

    private final JwtService jwtService;

    public JwtFiltre(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest requete,
                                    @NonNull HttpServletResponse reponse,
                                    @NonNull FilterChain chaine)
            throws ServletException, IOException {

        String entete = requete.getHeader(ENTETE);

        if (entete == null || !entete.startsWith(PREFIXE)) {
            chaine.doFilter(requete, reponse);
            return;
        }

        String jeton = entete.substring(PREFIXE.length());

        try {
            if (jwtService.estValide(jeton)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                String email = jwtService.extraireEmail(jeton);
                String role = jwtService.extraireRole(jeton);

                UsernamePasswordAuthenticationToken authentification =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role)));

                authentification.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(requete));

                SecurityContextHolder.getContext().setAuthentication(authentification);
            }
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        chaine.doFilter(requete, reponse);
    }
}
