package services;

import example.cache.DataManager;
import example.cache.JsonRepository;
import example.cache.RelationRepository;
import example.models.Recensione;
import example.models.Risposta;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servizio per la gestione delle recensioni nell'applicazione TheKnife.
 * Questa classe fornisce un'interfaccia completa per manipolare le recensioni e le relative risposte,
 * incluse operazioni di CRUD (creazione, lettura, aggiornamento, eliminazione) e funzionalità specializzate
 * come il filtraggio delle recensioni per ristorante o per utente.
 * Gestisce anche le relazioni tra recensioni e like/dislike, oltre all'interazione con le risposte alle recensioni.
 */
public class RecensioneService {
    /** Repository per la gestione della persistenza delle recensioni */
    private final JsonRepository<Recensione> recensioneRepository;
    /** Repository per la gestione delle relazioni tra utenti e recensioni "piaciute" */
    private final RelationRepository likeRepository;
    /** Repository per la gestione delle relazioni tra utenti e recensioni "non piaciute" */
    private final RelationRepository dislikeRepository;
    /** Servizio per la gestione delle risposte alle recensioni */
    private RisposteService risposteService;

    /**
     * Costruttore predefinito che inizializza i repository e registra le entità necessarie nel DataManager.
     * Configura le dipendenze per gestire recensioni, like, dislike e risposte, preparando
     * tutti i repository necessari e inizializzando il servizio di gestione delle risposte.
     */
    public RecensioneService() {
        DataManager dataManager = DataManager.getInstance();
        dataManager.registerEntityRepository(Recensione.class, "data/recensioni.json");
        this.recensioneRepository = dataManager.getRepository(Recensione.class);

        dataManager.registerRelationRepository("recensioniLike", "data/recensioni_like.json");
        dataManager.registerRelationRepository("recensioniDislike", "data/recensioni_dislike.json");
        this.likeRepository = dataManager.getRelationRepository("recensioniLike");
        this.dislikeRepository = dataManager.getRelationRepository("recensioniDislike");

        this.risposteService = new RisposteService();
    }

    /**
     * Salva o aggiorna una recensione nel repository.
     * Se la recensione non ha una data impostata, imposta automaticamente la data corrente.
     *
     * @param recensione La recensione da salvare
     * @return La recensione salvata, con ID assegnato se nuova
     */
    public Recensione salvaRecensione(Recensione recensione) {
        if (recensione.getDate() == null) {
            recensione.setDate(LocalDateTime.now());
        }
        return recensioneRepository.save(recensione);
    }

    /**
     * Crea una nuova recensione con i dati specificati.
     * Questo metodo semplifica la creazione di recensioni fornendo un'interfaccia più chiara
     * rispetto alla creazione diretta dell'oggetto Recensione.
     *
     * @param idRistorante ID del ristorante oggetto della recensione
     * @param idUtente ID dell'utente che ha scritto la recensione
     * @param voto Valutazione numerica del ristorante (generalmente da 1 a 5)
     * @param titolo Titolo della recensione
     * @param testo Contenuto testuale della recensione
     * @return La recensione creata e salvata, con ID assegnato
     */
    public Recensione creaRecensione(String idRistorante, String idUtente, int voto, String titolo, String testo) {
        Recensione recensione = new Recensione(idRistorante, idUtente, LocalDateTime.now(), voto, titolo, testo);
        return salvaRecensione(recensione);
    }

    /**
     * Elimina una recensione dato il suo ID.
     * Se la recensione esiste, elimina anche tutte le risposte associate.
     *
     * @param recensioneId ID della recensione da eliminare
     * @return true se la recensione è stata eliminata con successo, false se non esisteva
     */
    public boolean eliminaRecensione(String recensioneId) {
        if (recensioneRepository.findById(recensioneId).isPresent()) {
            risposteService.eliminaRispostaByRecensione(recensioneId);
            recensioneRepository.deleteById(recensioneId);
            return true;
        }
        return false;
    }

    /**
     * Elimina tutte le recensioni associate a un utente specifico.
     * Questo metodo è utile quando si elimina un account utente e si vogliono rimuovere
     * anche tutte le recensioni associate.
     *
     * @param idUtente ID dell'utente di cui eliminare le recensioni
     * @return Il numero di recensioni eliminate
     */
    public int eliminaRecensioniByUtente(String idUtente) {
        List<Recensione> recensioniUtente = getRecensioniByUtente(idUtente);
        int count = 0;
        for (Recensione recensione : recensioniUtente) {
            if (eliminaRecensione(recensione.getId())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Elimina tutte le recensioni associate a un ristorante specifico.
     * Questo metodo è utile quando si elimina un ristorante e si vogliono rimuovere
     * anche tutte le recensioni associate.
     *
     * @param idRistorante ID del ristorante di cui eliminare le recensioni
     * @return Il numero di recensioni eliminate
     */
    public int eliminaRecensioniByRistorante(String idRistorante) {
        List<Recensione> recensioniRistorante = getRecensioniByRistorante(idRistorante);
        int count = 0;
        for (Recensione recensione : recensioniRistorante) {
            if (eliminaRecensione(recensione.getId())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Modifica una recensione esistente dato il suo ID.
     * Aggiorna titolo, testo e voto della recensione, e imposta la data all'istante corrente.
     *
     * @param recensioneId ID della recensione da modificare
     * @param titolo Nuovo titolo della recensione
     * @param testo Nuovo contenuto testuale della recensione
     * @param voto Nuova valutazione numerica
     * @return Un Optional contenente la recensione modificata se trovata, altrimenti vuoto
     */
    public Optional<Recensione> modificaRecensione(String recensioneId, String titolo, String testo, int voto) {
        Optional<Recensione> recensioneOpt = recensioneRepository.findById(recensioneId);
        if (recensioneOpt.isPresent()) {
            Recensione recensione = recensioneOpt.get();
            recensione.setTitle(titolo);
            recensione.setText(testo);
            recensione.setRate(voto);
            recensione.setDate(LocalDateTime.now());
            return Optional.of(salvaRecensione(recensione));
        }
        return Optional.empty();
    }

    /**
     * Recupera tutte le recensioni presenti nel sistema.
     *
     * @return Una lista contenente tutte le recensioni
     */
    public List<Recensione> getAllRecensioni() {
        return recensioneRepository.findAll();
    }

    /**
     * Recupera tutte le recensioni scritte da un utente specifico.
     *
     * @param idUtente ID dell'utente di cui recuperare le recensioni
     * @return Una lista contenente tutte le recensioni dell'utente
     */
    public List<Recensione> getRecensioniByUtente(String idUtente) {
        return recensioneRepository.findAll().stream()
                .filter(r -> r.getKey_user().equals(idUtente))
                .collect(Collectors.toList());
    }

    /**
     * Recupera tutte le recensioni relative a un ristorante specifico.
     *
     * @param idRistorante ID del ristorante di cui recuperare le recensioni
     * @return Una lista contenente tutte le recensioni del ristorante
     */
    public List<Recensione> getRecensioniByRistorante(String idRistorante) {
        return recensioneRepository.findAll().stream()
                .filter(r -> r.getKey_r().equals(idRistorante))
                .collect(Collectors.toList());
    }

    /**
     * Recupera tutte le recensioni di un ristorante con un voto specifico.
     *
     * @param idRistorante ID del ristorante di cui recuperare le recensioni
     * @param voto Voto specifico da filtrare
     * @return Una lista contenente le recensioni che soddisfano i criteri
     */
    public List<Recensione> getRecensioniByRistoranteAndVoto(String idRistorante, int voto) {
        return recensioneRepository.findAll().stream()
                .filter(r -> r.getKey_r().equals(idRistorante) && r.getRate() == voto)
                .collect(Collectors.toList());
    }

    /**
     * Recupera tutte le recensioni di un ristorante con un voto maggiore o uguale a quello specificato.
     * Utile per filtrare le recensioni positive di un ristorante.
     *
     * @param idRistorante ID del ristorante di cui recuperare le recensioni
     * @param votoMinimo Voto minimo per il filtro
     * @return Una lista contenente le recensioni che soddisfano i criteri
     */
    public List<Recensione> getRecensioniByRistoranteAndVotoMinimo(String idRistorante, int votoMinimo) {
        return recensioneRepository.findAll().stream()
                .filter(r -> r.getKey_r().equals(idRistorante) && r.getRate() >= votoMinimo)
                .collect(Collectors.toList());
    }

    /**
     * Recupera tutte le recensioni di un ristorante con un voto minore o uguale a quello specificato.
     * Utile per filtrare le recensioni negative di un ristorante.
     *
     * @param idRistorante ID del ristorante di cui recuperare le recensioni
     * @param votoMassimo Voto massimo per il filtro
     * @return Una lista contenente le recensioni che soddisfano i criteri
     */
    public List<Recensione> getRecensioniByRistoranteAndVotoMassimo(String idRistorante, int votoMassimo) {
        return recensioneRepository.findAll().stream()
                .filter(r -> r.getKey_r().equals(idRistorante) && r.getRate() <= votoMassimo)
                .collect(Collectors.toList());
    }

    /**
     * Determina se una recensione è considerata positiva in base al voto.
     * Una recensione è considerata positiva se ha un voto maggiore o uguale a 3.
     *
     * @param voto Il voto da valutare
     * @return true se il voto è considerato positivo, false altrimenti
     */
    public boolean isRecensionePositiva(int voto) {
        return voto >= 3;
    }

    /**
     * Converte un voto numerico in una descrizione testuale comprensibile.
     * Associa a ciascun valore numerico una descrizione qualitativa.
     *
     * @param voto Il voto numerico da convertire
     * @return La descrizione testuale corrispondente al voto
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
     * Calcola e formatta in modo user-friendly il tempo trascorso da una data specifica.
     * Restituisce una stringa che rappresenta il tempo trascorso in anni, mesi, giorni, ore o minuti,
     * scegliendo l'unità più appropriata e adattando il testo per singolare/plurale.
     *
     * @param data La data da cui calcolare il tempo trascorso
     * @return Una stringa che rappresenta il tempo trascorso (es. "2 giorni fa", "1 anno fa")
     */
    public String getTempoTrascorso(LocalDateTime data) {
        if (data == null) return "Data non disponibile";
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

    // --- RISPOSTE ---

    /**
     * Aggiunge una risposta a una recensione specifica.
     * Controlla prima che la recensione esista, poi delega la creazione della risposta al servizio dedicato.
     *
     * @param recensioneId ID della recensione a cui aggiungere la risposta
     * @param testo Contenuto testuale della risposta
     * @return Un Optional contenente la risposta creata se l'operazione ha successo, altrimenti vuoto
     */
    public Optional<Risposta> aggiungiRisposta(String recensioneId, String testo) {
        if (!recensioneRepository.findById(recensioneId).isPresent()) {
            return Optional.empty();
        }
        try {
            Risposta risposta = risposteService.creaRisposta(recensioneId, testo);
            return Optional.of(risposta);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Modifica una risposta esistente dato il suo ID.
     * Delega l'operazione al servizio dedicato alle risposte.
     *
     * @param rispostaId ID della risposta da modificare
     * @param testo Nuovo contenuto testuale della risposta
     * @return Un Optional contenente la risposta modificata se trovata, altrimenti vuoto
     */
    public Optional<Risposta> modificaRisposta(String rispostaId, String testo) {
        return risposteService.modificaRisposta(rispostaId, testo);
    }

    /**
     * Elimina una risposta dato il suo ID.
     * Delega l'operazione al servizio dedicato alle risposte.
     *
     * @param rispostaId ID della risposta da eliminare
     * @return true se la risposta è stata eliminata con successo, false se non esisteva
     */
    public boolean eliminaRisposta(String rispostaId) {
        return risposteService.eliminaRisposta(rispostaId);
    }

    /**
     * Recupera la risposta associata a una recensione specifica.
     * Delega l'operazione al servizio dedicato alle risposte.
     *
     * @param recensioneId ID della recensione di cui recuperare la risposta
     * @return Un Optional contenente la risposta se trovata, altrimenti vuoto
     */
    public Optional<Risposta> getRispostaByRecensione(String recensioneId) {
        return risposteService.getRispostaByRecensione(recensioneId);
    }

    /**
     * Verifica se una recensione ha una risposta associata.
     * Delega l'operazione al servizio dedicato alle risposte.
     *
     * @param recensioneId ID della recensione da controllare
     * @return true se la recensione ha una risposta, false altrimenti
     */
    public boolean hasRisposta(String recensioneId) {
        return risposteService.esisteRispostaPerRecensione(recensioneId);
    }
}