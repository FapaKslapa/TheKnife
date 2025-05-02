package services;

import com.example.cache.DataManager;
import com.example.cache.JsonRepository;
import com.example.models.Recensione;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

public class RecensioneService {
    private final JsonRepository<Recensione> recensioneRepository;

    public RecensioneService() {
        DataManager dataManager = DataManager.getInstance();
        // Registra il repository per le recensioni
        dataManager.registerEntityRepository(Recensione.class, "data/recensioni.json");
        this.recensioneRepository = dataManager.getRepository(Recensione.class);
    }

    /**
     * Salva una nuova recensione o aggiorna una esistente
     */
    public Recensione salvaRecensione(Recensione recensione) {
        if (recensione.getDate() == null) {
            recensione.setDate(LocalDateTime.now());
        }
        return recensioneRepository.save(recensione);
    }

    /**
     * Crea e salva una nuova recensione
     */
    public Recensione creaRecensione(String idRistorante, String idUtente, int voto, String titolo, String testo) {
        Recensione recensione = new Recensione(idRistorante, idUtente, LocalDateTime.now(), voto, titolo, testo);
        return salvaRecensione(recensione);
    }

    /**
     * Elimina una recensione dato il suo ID
     */
    public void eliminaRecensione(String recensioneId) {
        recensioneRepository.deleteById(recensioneId);
    }

    /**
     * Modifica una recensione esistente
     */
    public Optional<Recensione> modificaRecensione(String recensioneId, String titolo, String testo, int voto) {
        Optional<Recensione> recensioneOpt = recensioneRepository.findById(recensioneId);

        if (recensioneOpt.isPresent()) {
            Recensione recensione = recensioneOpt.get();
            recensione.setTitle(titolo);
            recensione.setText(testo);
            recensione.setRate(voto);
            // Aggiorna la data di modifica
            recensione.setDate(LocalDateTime.now());
            return Optional.of(salvaRecensione(recensione));
        }

        return Optional.empty();
    }

    /**
     * Recupera tutte le recensioni nel sistema
     */
    public List<Recensione> getAllRecensioni() {
        return recensioneRepository.findAll();
    }

    /**
     * Recupera tutte le recensioni scritte da un determinato utente
     */
    public List<Recensione> getRecensioniByUtente(String idUtente) {
        return recensioneRepository.findAll().stream()
                .filter(r -> r.getKey_user().equals(idUtente))
                .collect(Collectors.toList());
    }

    /**
     * Recupera tutte le recensioni per un determinato ristorante
     */
    public List<Recensione> getRecensioniByRistorante(String idRistorante) {
        return recensioneRepository.findAll().stream()
                .filter(r -> r.getKey_r().equals(idRistorante))
                .collect(Collectors.toList());
    }

    /**
     * Recupera recensioni filtrate per ristorante e voto esatto
     */
    public List<Recensione> getRecensioniByRistoranteAndVoto(String idRistorante, int voto) {
        return recensioneRepository.findAll().stream()
                .filter(r -> r.getKey_r().equals(idRistorante) && r.getRate() == voto)
                .collect(Collectors.toList());
    }

    /**
     * Recupera recensioni filtrate per ristorante e voto minimo
     */
    public List<Recensione> getRecensioniByRistoranteAndVotoMinimo(String idRistorante, int votoMinimo) {
        return recensioneRepository.findAll().stream()
                .filter(r -> r.getKey_r().equals(idRistorante) && r.getRate() >= votoMinimo)
                .collect(Collectors.toList());
    }

    /**
     * Recupera recensioni filtrate per ristorante e voto massimo
     */
    public List<Recensione> getRecensioniByRistoranteAndVotoMassimo(String idRistorante, int votoMassimo) {
        return recensioneRepository.findAll().stream()
                .filter(r -> r.getKey_r().equals(idRistorante) && r.getRate() <= votoMassimo)
                .collect(Collectors.toList());
    }

    /**
     * Verifica se una recensione è positiva (voto >= 3)
     */
    public boolean isRecensionePositiva(int voto) {
        return voto >= 3;
    }

    /**
     * Converte un voto numerico in descrizione testuale
     */
    public String getDescrizioneVoto(int voto) {
        switch (voto) {
            case 1:
                return "Pessimo";
            case 2:
                return "Scarso";
            case 3:
                return "Nella media";
            case 4:
                return "Buono";
            case 5:
                return "Eccellente";
            default:
                return "Non valutato";
        }
    }

    /**
     * Calcola il tempo trascorso da una data in formato leggibile
     */
    public String getTempoTrascorso(LocalDateTime data) {
        if (data == null) {
            return "Data non disponibile";
        }

        LocalDateTime adesso = LocalDateTime.now();

        if (ChronoUnit.YEARS.between(data, adesso) > 0) {
            long anni = ChronoUnit.YEARS.between(data, adesso);
            return anni == 1 ? "1 anno fa" : anni + " anni fa";
        }

        if (ChronoUnit.MONTHS.between(data, adesso) > 0) {
            long mesi = ChronoUnit.MONTHS.between(data, adesso);
            return mesi == 1 ? "1 mese fa" : mesi + " mesi fa";
        }

        if (ChronoUnit.DAYS.between(data, adesso) > 0) {
            long giorni = ChronoUnit.DAYS.between(data, adesso);
            return giorni == 1 ? "1 giorno fa" : giorni + " giorni fa";
        }

        if (ChronoUnit.HOURS.between(data, adesso) > 0) {
            long ore = ChronoUnit.HOURS.between(data, adesso);
            return ore == 1 ? "1 ora fa" : ore + " ore fa";
        }

        if (ChronoUnit.MINUTES.between(data, adesso) > 0) {
            long minuti = ChronoUnit.MINUTES.between(data, adesso);
            return minuti == 1 ? "1 minuto fa" : minuti + " minuti fa";
        }

        long secondi = ChronoUnit.SECONDS.between(data, adesso);
        return secondi <= 1 ? "adesso" : secondi + " secondi fa";
    }
}