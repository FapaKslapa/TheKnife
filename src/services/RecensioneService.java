package services;

import com.example.cache.DataManager;
import com.example.cache.JsonRepository;
import com.example.models.Recensione;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

/**
 * Servizio per la gestione delle recensioni.
 * Si occupa di alcune operazioni come salvataggio e creazione, modifica ed eliminazione.
 */
public class RecensioneService {
    private final JsonRepository<Recensione> recensioneRepository;

    /**
     * Costruttore che inizializza il repository delle recensioni e lo registra nel DataManager se non è già stato registrato.
     */
    public RecensioneService() {
        DataManager dataManager = DataManager.getInstance();
        // Registra il repository per le recensioni
        dataManager.registerEntityRepository(Recensione.class, "data/recensioni.json");
        this.recensioneRepository = dataManager.getRepository(Recensione.class);
    }

    /**
     * Salva una nuova recensione o aggiorna una esistente.
     *
     * @param recensione oggetto Recensione da salvare
     * @return la recensione salvata (con eventuale ID aggiornato)
     */
    public Recensione salvaRecensione(Recensione recensione) {
        if (recensione.getDate() == null) {
            recensione.setDate(LocalDateTime.now());
        }
        return recensioneRepository.save(recensione);
    }

    /**
     * Crea e salva una nuova recensione.
     *
     * @param idRistorante identificativo del ristorante
     * @param idUtente     identificativo dell'utente
     * @param voto         valutazione da 1 a 5
     * @param titolo       titolo della recensione
     * @param testo        contenuto testuale della recensione
     * @return la recensione creata e salvata
     */
    public Recensione creaRecensione(String idRistorante, String idUtente, int voto, String titolo, String testo) {
        Recensione recensione = new Recensione(idRistorante, idUtente, LocalDateTime.now(), voto, titolo, testo);
        return salvaRecensione(recensione);
    }

    /**
     * Elimina una recensione dato il suo ID.
     *
     * @param recensioneId ID della recensione da eliminare
     */
    public void eliminaRecensione(String recensioneId) {
        recensioneRepository.deleteById(recensioneId);
    }

    /**
     * Modifica i campi principali (titolo, testo e voto) di una recensione esistente.
     *
     * @param recensioneId ID della recensione da modificare
     * @param titolo       nuovo titolo della recensione
     * @param testo        nuovo testo della recensione
     * @param voto         nuova valutazione
     * @return Optional contenente la recensione aggiornata, o vuoto se non trovata
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
     * Recupera l'elenco completo di tutte le recensioni memorizzate.
     *
     * @return lista di oggetti Recensione
     */
    public List<Recensione> getAllRecensioni() {
        return recensioneRepository.findAll();
    }

    /**
     * Recupera tutte le recensioni scritte da un determinato utente.
     *
     * @param idUtente identificativo dell'utente
     * @return lista di recensioni associate all'utente
     */
    public List<Recensione> getRecensioniByUtente(String idUtente) {
        return recensioneRepository.findAll().stream()
                .filter(r -> r.getKey_user().equals(idUtente))
                .collect(Collectors.toList());
    }

    /**
     * Recupera tutte le recensioni per un determinato ristorante.
     *
     * @param idRistorante identificativo del ristorante
     * @return lista di recensioni associate al ristorante
     */
    public List<Recensione> getRecensioniByRistorante(String idRistorante) {
        return recensioneRepository.findAll().stream()
                .filter(r -> r.getKey_r().equals(idRistorante))
                .collect(Collectors.toList());
    }

    /**
     * Recupera recensioni filtrate per ristorante e voto esatto.
     *
     * @param idRistorante identificativo del ristorante
     * @param voto         voto esatto da filtrare
     * @return lista di recensioni che corrispondono al filtro
     */
    public List<Recensione> getRecensioniByRistoranteAndVoto(String idRistorante, int voto) {
        return recensioneRepository.findAll().stream()
                .filter(r -> r.getKey_r().equals(idRistorante) && r.getRate() == voto)
                .collect(Collectors.toList());
    }

    /**
     * Recupera recensioni filtrate per ristorante e voto minimo.
     *
     * @param idRistorante identificativo del ristorante
     * @param votoMinimo   voto minimo accettato
     * @return lista di recensioni filtrate per voto minimo
     */
    public List<Recensione> getRecensioniByRistoranteAndVotoMinimo(String idRistorante, int votoMinimo) {
        return recensioneRepository.findAll().stream()
                .filter(r -> r.getKey_r().equals(idRistorante) && r.getRate() >= votoMinimo)
                .collect(Collectors.toList());
    }

    /**
     * Recupera recensioni filtrate per ristorante e voto massimo.
     *
     * @param idRistorante identificativo del ristorante
     * @param votoMassimo  voto massimo accettato
     * @return lista di recensioni filtrate per voto massimo
     */
    public List<Recensione> getRecensioniByRistoranteAndVotoMassimo(String idRistorante, int votoMassimo) {
        return recensioneRepository.findAll().stream()
                .filter(r -> r.getKey_r().equals(idRistorante) && r.getRate() <= votoMassimo)
                .collect(Collectors.toList());
    }

    /**
     * Verifica se una recensione è positiva (voto >= 3)
     *
     * @param voto voto numerico
     * @return true se il voto è positivo, false altrimenti
     */
    public boolean isRecensionePositiva(int voto) {
        return voto >= 3;
    }

    /**
     * Converte un voto numerico in descrizione testuale
     *
     * @param voto voto numerico da 1 a 5
     * @return descrizione testuale ("Pessimo", "Buono", ecc.)
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
     *
     * @param data data da confrontare con l'istante attuale
     * @return stringa che indica il tempo trascorso
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