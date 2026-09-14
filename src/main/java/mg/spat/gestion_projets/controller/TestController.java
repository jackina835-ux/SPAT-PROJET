package mg.spat.gestion_projets.controller;

import mg.spat.gestion_projets.entity.Projet;
import mg.spat.gestion_projets.entity.Tache;
import mg.spat.gestion_projets.entity.Utilisateur;
import mg.spat.gestion_projets.repository.ProjetRepository;
import mg.spat.gestion_projets.repository.TacheRepository;
import mg.spat.gestion_projets.repository.UtilisateurRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final UtilisateurRepository utilisateurRepository;
    private final ProjetRepository projetRepository;
    private final TacheRepository tacheRepository;

    public TestController(UtilisateurRepository utilisateurRepository,
                          ProjetRepository projetRepository,
                          TacheRepository tacheRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.projetRepository = projetRepository;
        this.tacheRepository = tacheRepository;
    }

    @GetMapping("/bonjour")
    public String bonjour() {
        return "Le backend fonctionne. Connexion a la base etablie.";
    }

    @GetMapping("/utilisateurs")
    public List<Map<String, Object>> listerUtilisateurs() {
        List<Map<String, Object>> resultat = new ArrayList<>();
        for (Utilisateur u : utilisateurRepository.findAll()) {
            Map<String, Object> ligne = new HashMap<>();
            ligne.put("id", u.getId());
            ligne.put("nomComplet", u.getNomComplet());
            ligne.put("email", u.getEmail());
            ligne.put("role", u.getRole());
            resultat.add(ligne);
        }
        return resultat;
    }

    @GetMapping("/projets")
    public List<Map<String, Object>> listerProjets() {
        List<Map<String, Object>> resultat = new ArrayList<>();
        for (Projet p : projetRepository.findAll()) {
            Map<String, Object> ligne = new HashMap<>();
            ligne.put("id", p.getId());
            ligne.put("nom", p.getNom());
            ligne.put("statut", p.getStatut());
            ligne.put("dateFin", p.getDateFin());
            ligne.put("responsable", p.getResponsable().getNomComplet());
            ligne.put("avancement", p.calculerAvancement());
            ligne.put("enRetard", p.estEnRetard());
            resultat.add(ligne);
        }
        return resultat;
    }

    @GetMapping("/projets/{id}/taches")
    public List<Map<String, Object>> listerTaches(@PathVariable Long id) {
        List<Map<String, Object>> resultat = new ArrayList<>();
        for (Tache t : tacheRepository.findByProjetId(id)) {
            Map<String, Object> ligne = new HashMap<>();
            ligne.put("id", t.getId());
            ligne.put("titre", t.getTitre());
            ligne.put("statut", t.getStatut());
            ligne.put("priorite", t.getPriorite());
            ligne.put("dateEcheance", t.getDateEcheance());
            ligne.put("assigneA", t.getAssigneA() == null
                    ? null
                    : t.getAssigneA().getNomComplet());
            resultat.add(ligne);
        }
        return resultat;
    }
}
