package mg.spat.gestion_projets.config;

import mg.spat.gestion_projets.security.JwtFiltre;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuration de la securite.
 *
 * Seule la connexion est ouverte. Tout le reste exige un jeton
 * JWT valide, et les droits fins sont poses methode par methode
 * dans les controleurs grace a @PreAuthorize.
 *
 * @EnableMethodSecurity est ce qui active ces annotations.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFiltre jwtFiltre;

    public SecurityConfig(JwtFiltre jwtFiltre) {
        this.jwtFiltre = jwtFiltre;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Requetes preliminaires envoyees par le navigateur
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Seule porte ouverte : se connecter
                .requestMatchers(HttpMethod.POST, "/api/auth/connexion").permitAll()
                // Tout le reste exige un jeton
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFiltre, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * BCrypt : algorithme de hachage des mots de passe.
     * Un mot de passe hache ne peut pas etre relu, seulement compare.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:3000"));
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
