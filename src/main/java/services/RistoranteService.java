// Marocco Stefano 762192 VA
// Marin Marco 760622 VA
// Gerti Alessia 762405 VA
// Sibilla Ginevra 761114 VA
package services;

import theknife.cache.DataManager;
import theknife.cache.JsonRepository;
import theknife.cache.RelationRepository;
import theknife.models.FiltriDiRicerca;
import theknife.models.Ristorante;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servizio per la gestione dei ristoranti nell'applicazione TheKnife.
 * Questa classe fornisce un'interfaccia completa per manipolare i ristoranti,
 * incluse operazioni di CRUD (creazione, lettura, aggiornamento, eliminazione),
 * ricerca avanzata con vari filtri (tipo di cucina, fascia di prezzo, distanza, ecc.),
 * e gestione dei ristoranti preferiti dagli utenti.
 * Si occupa inoltre di gestire la verifica degli orari di apertura dei ristoranti e
 * del calcolo delle distanze geografiche tra utenti e ristoranti.
 */

public class RistoranteService {
    /** Repository per la gestione della persistenza dei ristoranti */
    private final JsonRepository<Ristorante> ristoranteRepository;
    /** Formattatore per la conversione degli orari in formato stringa (HH:mm) */
    private final DateTimeFormatter orarioFormatter = DateTimeFormatter.ofPattern("HH:mm");
    /** Repository per la gestione delle relazioni tra utenti e ristoranti preferiti */
    private final RelationRepository userLikesRepo;
    /** Servizio per la gestione delle recensioni, usato per operazioni correlate */
    private RecensioneService recensioneService;

    /**
     * Costruttore predefinito che inizializza i repository e registra le entità necessarie nel DataManager.
     * Configura le dipendenze per gestire ristoranti e le relazioni di preferenze degli utenti,
     * preparando tutti i repository necessari per il funzionamento del servizio.
     */
    public RistoranteService() {
        DataManager dataManager = DataManager.getInstance();
        dataManager.registerEntityRepository(Ristorante.class, "data/ristoranti.json");
        // Fix: registra il relation repository se non già fatto
        dataManager.registerRelationRepository("userLikesRistorante", "data/userLikesRistorante.json");
        this.ristoranteRepository = dataManager.getRepository(Ristorante.class);
        this.userLikesRepo = dataManager.getRelationRepository("userLikesRistorante");
    }

    /**
     * Imposta il servizio di gestione delle recensioni utilizzato da questo servizio.
     * Questo metodo è necessario per operazioni che richiedono interazioni con le recensioni,
     * come l'eliminazione di un ristorante che deve anche eliminare tutte le recensioni associate.
     *
     * @param recensioneService Il servizio di recensioni da utilizzare
     */
    public void setServices(RecensioneService recensioneService) {
        this.recensioneService = recensioneService;
    }

    /**
     * Crea un nuovo ristorante con i dati specificati.
     * Questo metodo semplifica la creazione di ristoranti fornendo un'interfaccia più chiara
     * rispetto alla creazione diretta dell'oggetto Ristorante.
     *
     * @param nome Nome del ristorante
     * @param tipoCucina Tipo di cucina offerta dal ristorante
     * @param fasciaPrezzo Fascia di prezzo del ristorante (da 1 a 3)
     * @param orariApertura Mappa contenente gli orari di apertura per ogni giorno della settimana
     * @param latitudine Coordinata geografica della latitudine del ristorante
     * @param longitudine Coordinata geografica della longitudine del ristorante
     * @param idProprietario ID dell'utente proprietario del ristorante
     * @param numeroTelefono Numero di telefono del ristorante
     * @param consegnaDomicilio Indica se il ristorante offre servizio di consegna a domicilio
     * @return Il ristorante creato e salvato, con ID assegnato
     */
    public Ristorante creaRistorante(String nome, String tipoCucina, int fasciaPrezzo,
                                     Map<String, String> orariApertura, double latitudine,
                                     double longitudine, String idProprietario, String numeroTelefono,
                                     boolean consegnaDomicilio) {
        Ristorante nuovoRistorante = new Ristorante(nome, tipoCucina, fasciaPrezzo,
                orariApertura, latitudine, longitudine, idProprietario, numeroTelefono,
                consegnaDomicilio);
        return salvaRistorante(nuovoRistorante);
    }

    /**
     * Salva o aggiorna un ristorante nel repository.
     * Se il ristorante è nuovo, gli viene assegnato un ID univoco.
     * Se il ristorante esiste già, i suoi dati vengono aggiornati.
     *
     * @param ristorante Il ristorante da salvare o aggiornare
     * @return Il ristorante salvato, con ID assegnato se nuovo
     */
    public Ristorante salvaRistorante(Ristorante ristorante) {
        return ristoranteRepository.save(ristorante);
    }

    /**
     * Modifica un ristorante esistente dato il suo ID.
     * Aggiorna tutti i campi del ristorante con i nuovi valori specificati.
     *
     * @param id ID del ristorante da modificare
     * @param nome Nuovo nome del ristorante
     * @param tipoCucina Nuovo tipo di cucina
     * @param fasciaPrezzo Nuova fascia di prezzo
     * @param orariApertura Nuovi orari di apertura
     * @param latitudine Nuova latitudine
     * @param longitudine Nuova longitudine
     * @param numeroTelefono Nuovo numero di telefono
     * @param consegnaDomicilio Nuovo stato del servizio di consegna a domicilio
     * @return Un Optional contenente il ristorante modificato se trovato, altrimenti vuoto
     */
    public Optional<Ristorante> modificaRistorante(String id, String nome, String tipoCucina,
                                                   int fasciaPrezzo, Map<String, String> orariApertura,
                                                   double latitudine, double longitudine,
                                                   String numeroTelefono, boolean consegnaDomicilio) {
        Optional<Ristorante> ristoranteOpt = ristoranteRepository.findById(id);
        if (ristoranteOpt.isPresent()) {
            Ristorante ristorante = ristoranteOpt.get();
            ristorante.setNome(nome);
            ristorante.setTipoCucina(tipoCucina);
            ristorante.setFasciaPrezzo(fasciaPrezzo);
            ristorante.setOrariApertura(orariApertura);
            ristorante.setLatitudine(latitudine);
            ristorante.setLongitudine(longitudine);
            ristorante.setNumeroTelefono(numeroTelefono);
            ristorante.setConsegnaDomicilio(consegnaDomicilio);
            return Optional.of(salvaRistorante(ristorante));
        }
        return Optional.empty();
    }

    /**
     * Elimina un ristorante dato il suo ID.
     * Se il ristorante esiste, elimina anche tutte le recensioni associate ad esso.
     * Richiede che il RecensioneService sia stato correttamente inizializzato.
     *
     * @param id ID del ristorante da eliminare
     * @return true se il ristorante è stato eliminato con successo, false se non esisteva
     * @throws IllegalStateException se il RecensioneService non è stato inizializzato
     */
    public boolean eliminaRistorante(String id) {
        if (recensioneService == null) {
            throw new IllegalStateException("RecensioneService non è stato inizializzato");
        }

        Optional<Ristorante> ristoranteOpt = ristoranteRepository.findById(id);
        if (!ristoranteOpt.isPresent()) {
            return false;
        }

        recensioneService.eliminaRecensioniByRistorante(id);
        ristoranteRepository.deleteById(id);

        return true;
    }

    /**
     * Elimina tutti i ristoranti associati a un proprietario specifico.
     * Questo metodo è utile quando si elimina un account utente ristoratore e si vogliono rimuovere
     * anche tutti i ristoranti di sua proprietà.
     *
     * @param idProprietario ID dell'utente proprietario di cui eliminare i ristoranti
     * @return Il numero di ristoranti eliminati
     */
    public int eliminaRistorantiByProprietario(String idProprietario) {
        List<Ristorante> ristoranti = getRistorantiByProprietario(idProprietario);

        int count = 0;
        for (Ristorante ristorante : ristoranti) {
            if (eliminaRistorante(ristorante.getId())) {
                count++;
            }
        }

        return count;
    }

    /**
     * Recupera un ristorante specifico dato il suo ID.
     *
     * @param id ID del ristorante da recuperare
     * @return Un Optional contenente il ristorante se trovato, altrimenti vuoto
     */
    public Optional<Ristorante> getRistoranteById(String id) {
        System.out.println("Ristorante id: " + id);
        return ristoranteRepository.findById(id);
    }

    /**
     * Recupera tutti i ristoranti presenti nel sistema.
     *
     * @return Una lista contenente tutti i ristoranti
     */
    public List<Ristorante> getAllRistoranti() {
        return ristoranteRepository.findAll();
    }

    /**
     * Recupera tutti i ristoranti associati a un proprietario specifico.
     *
     * @param idUtente ID dell'utente proprietario di cui recuperare i ristoranti
     * @return Una lista contenente tutti i ristoranti del proprietario
     */
    public List<Ristorante> getRistorantiByProprietario(String idUtente) {
        System.out.println("Ristorante id: " + idUtente);
        return ristoranteRepository.findAll().stream()
                .filter(r -> r.getIdProprietario().equals(idUtente))
                .collect(Collectors.toList());
    }

    /**
     * Recupera tutti i ristoranti che offrono un determinato tipo di cucina.
     *
     * @param tipoCucina Il tipo di cucina da filtrare (confronto case-insensitive)
     * @return Una lista contenente i ristoranti che offrono il tipo di cucina specificato
     */
    public List<Ristorante> getRistorantiByTipoCucina(String tipoCucina) {
        return ristoranteRepository.findAll().stream()
                .filter(r -> r.getTipoCucina().equalsIgnoreCase(tipoCucina))
                .collect(Collectors.toList());
    }

    /**
     * Filtra i ristoranti in base ai criteri specificati nell'oggetto FiltriDiRicerca.
     * Supporta il filtraggio per tipo di cucina, fascia di prezzo, servizio di consegna a domicilio,
     * distanza geografica dall'utente e stato di apertura corrente.
     *
     * @param filtri Oggetto contenente i criteri di filtro da applicare
     * @return Una lista contenente i ristoranti che soddisfano tutti i criteri di filtro
     */
    public List<Ristorante> filtriRicerca(FiltriDiRicerca filtri) {
        List<Ristorante> risultati = ristoranteRepository.findAll();

        if (filtri.getTipoCucina() != null) {
            risultati = risultati.stream()
                    .filter(r -> r.getTipoCucina().equalsIgnoreCase(filtri.getTipoCucina()))
                    .collect(Collectors.toList());
        }

        if (filtri.getFasciaPrezzo() != null) {
            risultati = risultati.stream()
                    .filter(r -> r.getFasciaPrezzo() == filtri.getFasciaPrezzo())
                    .collect(Collectors.toList());
        }

        if (filtri.getConsegnaDomicilio() != null && filtri.getConsegnaDomicilio()) {
            risultati = risultati.stream()
                    .filter(Ristorante::isConsegnaDomicilio)
                    .collect(Collectors.toList());
        }

        if (filtri.getLatitudineUtente() != null && filtri.getLongitudineUtente() != null &&
                filtri.getDistanzaMassima() != null) {
            double latUtente = filtri.getLatitudineUtente();
            double lonUtente = filtri.getLongitudineUtente();
            int distanzaMassima = filtri.getDistanzaMassima();

            risultati = risultati.stream()
                    .filter(r -> calcolaDistanza(latUtente, lonUtente, r.getLatitudine(), r.getLongitudine()) <= distanzaMassima)
                    .collect(Collectors.toList());
        }

        if (filtri.getApertoOra() != null && filtri.getApertoOra()) {
            LocalTime oraCorrente = LocalTime.now();
            DayOfWeek giornoCorrente = LocalDate.now().getDayOfWeek();

            risultati = risultati.stream()
                    .filter(r -> isRistoranteAperto(r, giornoCorrente, oraCorrente))
                    .collect(Collectors.toList());
        }

        if (filtri.getNomeParziale() != null && !filtri.getNomeParziale().trim().isEmpty()) {
            String nomeParzialeLower = filtri.getNomeParziale().toLowerCase();
            risultati = risultati.stream()
                    .filter(r -> r.getNome() != null && r.getNome().toLowerCase().contains(nomeParzialeLower))
                    .collect(Collectors.toList());
        }

        return risultati;
    }

    /**
     * Verifica se un ristorante è aperto in un determinato giorno e ora.
     * Controlla gli orari di apertura del ristorante per il giorno specificato
     * e determina se l'ora specificata rientra nell'intervallo di apertura.
     * Gestisce anche il caso particolare in cui l'orario di chiusura è dopo la mezzanotte.
     *
     * @param ristorante Il ristorante da verificare
     * @param giorno Il giorno della settimana da controllare
     * @param ora L'ora da verificare
     * @return true se il ristorante è aperto al giorno e all'ora specificati, false altrimenti
     */
    private boolean isRistoranteAperto(Ristorante ristorante, DayOfWeek giorno, LocalTime ora) {
        Map<String, String> orari = ristorante.getOrariApertura();
        if (orari == null || orari.isEmpty()) {
            return false;
        }

        String giornoItaliano = convertiGiornoInItaliano(giorno);

        String orarioGiorno = orari.get(giornoItaliano);
        if (orarioGiorno == null || orarioGiorno.trim().isEmpty() || orarioGiorno.equals("chiuso")) {
            return false;
        }

        String[] parti = orarioGiorno.split("-");
        if (parti.length != 2) {
            return false;
        }

        try {
            LocalTime apertura = LocalTime.parse(parti[0].trim(), orarioFormatter);
            LocalTime chiusura = LocalTime.parse(parti[1].trim(), orarioFormatter);

            if (chiusura.isBefore(apertura)) {
                return ora.isAfter(apertura) || ora.equals(apertura) || ora.isBefore(chiusura);
            } else {
                return (ora.isAfter(apertura) || ora.equals(apertura)) && (ora.isBefore(chiusura));
            }
        } catch (Exception e) {
            System.err.println("Errore nel formato dell'orario per il ristorante " + ristorante.getNome() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Converte un oggetto DayOfWeek in una stringa rappresentante il giorno in italiano.
     * Utilizzato per mappare i giorni della settimana del sistema con i giorni
     * memorizzati negli orari di apertura dei ristoranti.
     *
     * @param giorno Il giorno della settimana da convertire
     * @return La stringa rappresentante il giorno in italiano, o stringa vuota se non riconosciuto
     */
    private String convertiGiornoInItaliano(DayOfWeek giorno) {
        switch (giorno) {
            case MONDAY:
                return "lunedì";
            case TUESDAY:
                return "martedì";
            case WEDNESDAY:
                return "mercoledì";
            case THURSDAY:
                return "giovedì";
            case FRIDAY:
                return "venerdì";
            case SATURDAY:
                return "sabato";
            case SUNDAY:
                return "domenica";
            default:
                return "";
        }
    }

    /**
     * Calcola la distanza in chilometri tra due punti geografici utilizzando la formula di Haversine.
     * Questa formula considera la curvatura della Terra e fornisce una buona approssimazione
     * della distanza reale tra due punti identificati da coordinate geografiche.
     *
     * @param lat1 Latitudine del primo punto
     * @param lon1 Longitudine del primo punto
     * @param lat2 Latitudine del secondo punto
     * @param lon2 Longitudine del secondo punto
     * @return La distanza in chilometri tra i due punti
     */
    private double calcolaDistanza(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Raggio della Terra in km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    /**
     * Aggiunge un ristorante ai preferiti di un utente.
     * Crea una relazione tra l'utente e il ristorante nel repository delle relazioni.
     *
     * @param userId ID dell'utente
     * @param ristoranteId ID del ristorante da aggiungere ai preferiti
     */
    public void aggiungiRistoranteAiPreferiti(String userId, String ristoranteId) {
        userLikesRepo.addRelation(userId, ristoranteId);
    }

    /**
     * Rimuove un ristorante dai preferiti di un utente.
     * Elimina la relazione tra l'utente e il ristorante dal repository delle relazioni.
     *
     * @param userId ID dell'utente
     * @param ristoranteId ID del ristorante da rimuovere dai preferiti
     */
    public void rimuoviRistoranteDaiPreferiti(String userId, String ristoranteId) {
        userLikesRepo.removeRelation(userId, ristoranteId);
    }

    /**
     * Recupera tutti i ristoranti preferiti da un utente specifico.
     * Restituisce solo gli ID dei ristoranti, non gli oggetti completi.
     *
     * @param userId ID dell'utente di cui recuperare i ristoranti preferiti
     * @return Una lista contenente gli ID dei ristoranti preferiti dall'utente
     */
    public List<String> getRistorantiPreferitiByUtente(String userId) {
        return userLikesRepo.findRelatedIds(userId);
    }

    /**
     * Verifica se un ristorante è tra i preferiti di un utente.
     *
     * @param userId ID dell'utente
     * @param ristoranteId ID del ristorante da verificare
     * @return true se il ristorante è tra i preferiti dell'utente, false altrimenti
     */
    public boolean isRistorantePreferito(String userId, String ristoranteId) {
        return userLikesRepo.findRelatedIds(userId).contains(ristoranteId);
    }
}
