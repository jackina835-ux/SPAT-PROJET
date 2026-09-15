package mg.spat.gestion_projets.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Fichier attache a une tache.
 *
 * Le fichier lui-meme n'est PAS dans la base : seules ses
 * informations le sont. Le contenu est ecrit sur le disque,
 * dans le dossier de stockage configure.
 *
 * Deux noms cohabitent :
 *   nomOriginal : ce que l'utilisateur voit et telecharge
 *                 ("rapport final.pdf")
 *   nomStocke   : le nom reel sur le disque, un identifiant
 *                 unique ("a3f9e1c2-....pdf")
 *
 * Cette separation evite les collisions de noms et empeche
 * qu'un nom de fichier malveillant ("../../config.xml")
 * sorte du dossier de stockage.
 */
@Entity
@Table(name = "piece_jointe")
public class PieceJointe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tache_id", nullable = false)
    private Tache tache;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deposant_id", nullable = false)
    private Utilisateur deposant;

    @Column(name = "nom_original", nullable = false, length = 255)
    private String nomOriginal;

    @Column(name = "nom_stocke", nullable = false, unique = true, length = 120)
    private String nomStocke;

    @Column(name = "type_mime", nullable = false, length = 120)
    private String typeMime;

    @Column(name = "taille_octets", nullable = false)
    private Long tailleOctets;

    @Column(name = "date_depot", nullable = false)
    private LocalDateTime dateDepot = LocalDateTime.now();

    public PieceJointe() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Tache getTache() { return tache; }
    public void setTache(Tache tache) { this.tache = tache; }

    public Utilisateur getDeposant() { return deposant; }
    public void setDeposant(Utilisateur deposant) { this.deposant = deposant; }

    public String getNomOriginal() { return nomOriginal; }
    public void setNomOriginal(String nomOriginal) { this.nomOriginal = nomOriginal; }

    public String getNomStocke() { return nomStocke; }
    public void setNomStocke(String nomStocke) { this.nomStocke = nomStocke; }

    public String getTypeMime() { return typeMime; }
    public void setTypeMime(String typeMime) { this.typeMime = typeMime; }

    public Long getTailleOctets() { return tailleOctets; }
    public void setTailleOctets(Long tailleOctets) { this.tailleOctets = tailleOctets; }

    public LocalDateTime getDateDepot() { return dateDepot; }
    public void setDateDepot(LocalDateTime dateDepot) { this.dateDepot = dateDepot; }
}
