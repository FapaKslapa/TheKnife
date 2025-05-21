package com.example.models;

import com.example.cache.BaseEntity;
import com.example.cache.DataManager;
import com.example.cache.JsonRepository;
import com.example.models.FiltriDiRicerca;
import com.example.models.Ristorante;
import com.google.gson.JsonArray;

import javax.xml.crypto.Data;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class RistoranteService {
    private final JsonRepository<Ristorante> ristoranteJsonRepository;
    private final DateTimeFormatter orarioFormatter = DateTimeFormatter.ofPattern("HH:mm");


    public RistoranteService() {
        DataManager dataManager = DataManager.getInstance();

        // Regisistra il repository se non è già fatta
        dataManager.registerEntityRepository(Ristorante.class, "data/ristornati.jsno");
        this.ristoranteJsonRepository = dataManager.getRepository(Ristorante.class);

    }

    // crea un nuovo ristornate
    public Ristorante createRistorante(String nome, String tipoCucina, int fasciaPrezzo,
                                       Map<String, String> orarioApertura, double latitudine, double longitude, String idProprietario, String numeroTelefono, boolean consegnaDomicilio) {
        Ristorante nuovoRistorante = new Ristorante(nome, tipoCucina, orarioApertura, latitudine, longitude, idProprietario, numeroTelefono, consegnaDomicilio);
        return salvaRistornate(nuovoRistorante);
    }

    //Salva ristonate
    public Ristorante salvaRistornate(Ristorante ristorante) {
        return ristoranteJsonRepository.save(ristorante);
    }

    //Modifica un ristornate esistente
    public Optional<Ristorante> modificaRistorante(String id, String nome, String tipoCucina, int facsiaPrezzo, Map<String, String> orarioApertura, double latitudine, double longitude, String numeroTelefono, boolean consegnaDomicilio) {
        Optional<Ristorante> ristorante = ristoranteJsonRepository.findById(id);
        if (ristorante.isPresent()) {
            Ristorante ristorante;
            ristorante = ristorante.get();
            ristorante.setNome(nome);
            ristorante.setTipoCucina(tipoCucina);
            ristorante.setFasciaPrezzo(facsiaPrezzo);
            ristorante.setOrariApertura(orarioApertura);
            ristorante.setLatitudine(latitudine);
            ristorante.setLongitudine(longitude);
            ristorante.setNumeroTelefono(numeroTelefono);
            ristorante.setConsegnaDomicilio(consegnaDomicilio);
            return Optional.of(salvaRistornate(ristorante));
        }
        return Optional.empty();
    }

    //Elimina un Ristonate tramide ID
    public boolean eliminaRistorante(String id) {
        if (ristoranteJsonRepository.findById(id).isPresent()) {
            ristoranteJsonRepository.deleteById(id);
            return true;
        }
        return false;
    }

    //Trova una ristornate per ID

    public Optional<Ristorante> getRistornateById(String id) {
        return ristoranteJsonRepository.findById(id);
    }

    // Recupera tutti i Ristornati
    public List<Ristorante> getAllRistoranti() {
        return ristoranteJsonRepository.findAll();
    }


    //Recupera i ristorrnati di un proprietario
    public List<Ristorante> getRistorantiByProprietario(String idUtente) {
        return ristoranteJsonRepository.findAll().stream().filter(ristorante -> ristorante.
                getIdProprietario().equals(idUtente)).collect(Collectors.toList());

    }

    // Crea Ristornti per tipo di cucina
    public List<Ristorante> getRistorantiByTipoCucina(String tipoCucina) {
        return ristoranteJsonRepository.findAll().stream().filter(ristorante -> ristorante.getTipoCucina().
                equals(tipoCucina)).collect(Collectors.toList());
    }

    // Filtra i ristornti in base ai criterti specifici
    public List<Ristorante> filtriRicerca(FiltriDiRicerca filtri) {
        List<Ristorante> risultati = ristoranteJsonRepository.findAll();


        // Filta per tipo di cucina se specificato
        if (filtri.getTipoCucina() != null) {
            risultati = risultati.stream().filter(ristorante -> ristorante.getTipoCucina().
                    equalsIgnoreCase(filtri.getTipoCucina())).collect(Collectors.toList());

        }

        //Filtra per fascia prezzo
        if (filtri.getFasciaPrezzo() != null) {
            risultati = risultati.stream().filter(ristorante -> ristorante.getFasciaPrezzo() == filtri.getFasciaPrezzo()).collect(Collectors.toList());

        }

        //Filtra per consegna a domicilio
        if (filtri.getConsegnaDomicilio() != null && filtri.getConsegnaDomicilio()) {
            risultati = risultati.stream().filter(Ristorante::isConsegnaDomicilio).collect(Collectors.toList());
        }

        //Filtra per distanza se le coordinate e la distanza massina sono specificate
        if (filtri.getLatitudineUtente() != null && filtri.getLongitudineUtente() != null && filtri.getDistanzaMassima() != null) {
            double latitudineUtente = filtri.getLatitudineUtente();
            double longitudineUtente = filtri.getLongitudineUtente();
            int distanzaMassima = filtri.getDistanzaMassima();


            risultati = risultati.stream().filter(Ristorante -> calcolaDistanza(latitudineUtente, longitudineUtente,
                    Ristorante.getLatitudine()) <= distanzaMassima).collect(Collectors.toList());

        }

        //Filtro per "ApertoOra"
        if (filtri.getApertoOra() != null && filtri.getApertoOra()) {
            LocalDate oraCorrente = LocalDate.now();
            DayOfWeek giornoCorrente = LocalDate.now().getDayOfWeek();
            risultati = risultati.stream().filter(ristorante -> isRistoranteAperto(ristorante, oraCorrente, giornoCorrente).collect(Collectors.toList()));
        }
        return risultati;
    }

    // Verifica se un ristorante è aperto in un determinato momento 179

    private boolean isRistornate(Ristorante ristorante, DayOfWeek giorno, LocalDate ora) {
        Map<String, String> orarioApertura = ristorante.getOrariApertura();
        if (orarioApertura == null || orarioApertura.isEmpty()) {
            return false;
        }

        //Converti il giorno della settima in italiano minuscolo
        String giornoItaliano = convertiGiornoInItaliano(giorno);

        //Contorlla se c'è un orario per questo giorno
        String orarioGiorno = orarioGiorno.get(giornoItaliano);
        if (orarioGiorno == null || orarioGiorno.isEmpty() || orarioGiorno.equals("Chiuso")) {
            return false;
        }

        // Formato "09:00-23:00" 0 comunque simile
        String[] parti = orarioGiorno.split("-");
        if (parti.length != 2) {
            return false;
        }
        try {
            LocalTime apertura = LocalTime.parse(parti[0].trim(), orarioFormatter);
            LocalTime chiusura = LocalTime.parse(parti[1].trim(), orarioFormatter);


            //Gertisci anche il caso in cui il locale chiude dopo la mezzanotte
            if (chiusura.isBefore(apertura)) {
                return ora.isAfter(apertura) || ora.equals(apertura) || ora.isBefore(chiusura);
            } else {
                return (ora.isAfter(apertura) || ora.equals(apertura)) && (ora.isBefore(chiusura));
            }

        } catch (Exception e) {
            return false;
        }
    }

    // Convertire il giorno della settimana da DayOfWeek a Stringa in italiano
    private String convertiGiornoInItaliano(DayOfWeek giorno) {
        switch (giorno) {
            case MONDAY:
                return "lunedì";
            case TUESDAY:
                return "martedì";
            case WEDNESDAY:
                return "mercoledì";
            case THURSDAY:
                return "giovedi";
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


    // Calcola la distanza in km tra due punti geografici
    private double calcolaDistanza(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6378.137;// Raggio della Terra in km

        double latitudineDistance = Math.toRadians(lat2 - lat1);
        double longitudineDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latitudineDistance / 2) * Math.sin(latitudineDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(Math.toRadians(lon1))
                        * Math.cos(Math.toRadians(lon2)) * Math.sin(longitudineDistance / 2) * Math.cos(latitudineDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;// DIstanza in km
    }


}

