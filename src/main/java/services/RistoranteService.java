package services;

import example.cache.DataManager;
import example.cache.JsonRepository;
import example.cache.RelationRepository;
import example.models.FiltriDiRicerca;
import example.models.Ristorante;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servizio per la gestione dei ristoranti.
 * Si occupa delle operazioni CRUD, ricerca e filtri avanzati dei ristoranti.
 */

public class RistoranteService {
    private final JsonRepository<Ristorante> ristoranteRepository;
    private final DateTimeFormatter orarioFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final RelationRepository userLikesRepo;
    private RecensioneService recensioneService;

    public RistoranteService() {
        DataManager dataManager = DataManager.getInstance();
        dataManager.registerEntityRepository(Ristorante.class, "data/ristoranti.json");
        // Fix: registra il relation repository se non già fatto
        dataManager.registerRelationRepository("userLikesRistorante", "data/userLikesRistorante.json");
        this.ristoranteRepository = dataManager.getRepository(Ristorante.class);
        this.userLikesRepo = dataManager.getRelationRepository("userLikesRistorante");
    }

    public void setServices(RecensioneService recensioneService) {
        this.recensioneService = recensioneService;
    }

    public Ristorante creaRistorante(String nome, String tipoCucina, int fasciaPrezzo,
                                     Map<String, String> orariApertura, double latitudine,
                                     double longitudine, String idProprietario, String numeroTelefono,
                                     boolean consegnaDomicilio) {
        Ristorante nuovoRistorante = new Ristorante(nome, tipoCucina, fasciaPrezzo,
                orariApertura, latitudine, longitudine, idProprietario, numeroTelefono,
                consegnaDomicilio);
        return salvaRistorante(nuovoRistorante);
    }

    public Ristorante salvaRistorante(Ristorante ristorante) {
        return ristoranteRepository.save(ristorante);
    }

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

    public Optional<Ristorante> getRistoranteById(String id) {
        System.out.println("Ristorante id: " + id);
        return ristoranteRepository.findById(id);
    }

    public List<Ristorante> getAllRistoranti() {
        return ristoranteRepository.findAll();
    }

    public List<Ristorante> getRistorantiByProprietario(String idUtente) {
        System.out.println("Ristorante id: " + idUtente);
        return ristoranteRepository.findAll().stream()
                .filter(r -> r.getIdProprietario().equals(idUtente))
                .collect(Collectors.toList());
    }

    public List<Ristorante> getRistorantiByTipoCucina(String tipoCucina) {
        return ristoranteRepository.findAll().stream()
                .filter(r -> r.getTipoCucina().equalsIgnoreCase(tipoCucina))
                .collect(Collectors.toList());
    }

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

        return risultati;
    }

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

    private double calcolaDistanza(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    public void aggiungiRistoranteAiPreferiti(String userId, String ristoranteId) {
        userLikesRepo.addRelation(userId, ristoranteId);
    }

    public void rimuoviRistoranteDaiPreferiti(String userId, String ristoranteId) {
        userLikesRepo.removeRelation(userId, ristoranteId);
    }

    public List<String> getRistorantiPreferitiByUtente(String userId) {
        return userLikesRepo.findRelatedIds(userId);
    }

    public boolean isRistorantePreferito(String userId, String ristoranteId) {
        return userLikesRepo.findRelatedIds(userId).contains(ristoranteId);
    }
}
