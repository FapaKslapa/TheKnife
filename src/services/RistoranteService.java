package services;

import com.example.cache.DataManager;
import com.example.cache.JsonRepository;
import com.example.models.FiltriDiRicerca;
import com.example.models.Ristorante;
import com.example.models.Utente;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RistoranteService {
    private final JsonRepository<Ristorante> ristoranteRepository;

    public RistoranteService() {
        DataManager dataManager = DataManager.getInstance();
        // Registra il repository se non è già fatto
        dataManager.registerEntityRepository(Ristorante.class, "data/ristoranti.json");
        this.ristoranteRepository = dataManager.getRepository(Ristorante.class);
    }

    /**
     * Crea un nuovo ristorante
     */
    public Ristorante creaRistorante(String nome, String tipoCucina, int fasciaPrezzo,
                                     java.util.Map<String, String> orariApertura, double latitudine,
                                     double longitudine, String idProprietario, String numeroTelefono,
                                     boolean consegnaDomicilio) {
        Ristorante nuovoRistorante = new Ristorante(nome, tipoCucina, fasciaPrezzo,
                orariApertura, latitudine, longitudine, idProprietario, numeroTelefono,
                consegnaDomicilio);
        return salvaRistorante(nuovoRistorante);
    }

    /**
     * Salva un ristorante nuovo o esistente
     */
    public Ristorante salvaRistorante(Ristorante ristorante) {
        return ristoranteRepository.save(ristorante);
    }

    /**
     * Modifica un ristorante esistente
     *
     * @return Il ristorante modificato o Optional vuoto se non trovato
     */
    public Optional<Ristorante> modificaRistorante(String id, String nome, String tipoCucina,
                                                   int fasciaPrezzo, java.util.Map<String, String> orariApertura,
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
     * Elimina un ristorante dato il suo ID
     */
    public boolean eliminaRistorante(String id) {
        if (ristoranteRepository.findById(id).isPresent()) {
            ristoranteRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Trova un ristorante per ID
     */
    public Optional<Ristorante> getRistoranteById(String id) {
        System.out.println("Ristorante id: " + id);
        return ristoranteRepository.findById(id);
    }

    /**
     * Recupera tutti i ristoranti
     */
    public List<Ristorante> getAllRistoranti() {
        return ristoranteRepository.findAll();
    }

    /**
     * Recupera i ristoranti di un proprietario
     */
    public List<Ristorante> getRistorantiByProprietario(String idUtente) {
        System.out.println("Ristorante id: " + idUtente);
        return ristoranteRepository.findAll().stream()
                .filter(r -> r.getIdProprietario().equals(idUtente))
                .collect(Collectors.toList());
    }

    /**
     * Cerca ristoranti per tipo di cucina
     */
    public List<Ristorante> getRistorantiByTipoCucina(String tipoCucina) {
        return ristoranteRepository.findAll().stream()
                .filter(r -> r.getTipoCucina().equalsIgnoreCase(tipoCucina))
                .collect(Collectors.toList());
    }

    public List<Ristorante> filtriRicerca(FiltriDiRicerca filtri) {
        List<Ristorante> risultati = ristoranteRepository.findAll();

        // Filtra per tipo cucina se specificato
        if (filtri.getTipoCucina() != null) {
            risultati = risultati.stream()
                    .filter(r -> r.getTipoCucina().equalsIgnoreCase(filtri.getTipoCucina()))
                    .collect(Collectors.toList());
        }

        // Filtra per fascia prezzo
        if (filtri.getFasciaPrezzo() != null) {
            risultati = risultati.stream()
                    .filter(r -> r.getFasciaPrezzo() == filtri.getFasciaPrezzo())
                    .collect(Collectors.toList());
        }

        // Filtra per consegna a domicilio
        if (filtri.getConsegnaDomicilio() != null && filtri.getConsegnaDomicilio()) {
            risultati = risultati.stream()
                    .filter(Ristorante::isConsegnaDomicilio)
                    .collect(Collectors.toList());
        }

        // Filtra per distanza se le coordinate e la distanza massima sono specificate
        if (filtri.getLatitudineUtente() != null && filtri.getLongitudineUtente() != null &&
                filtri.getDistanzaMassima() != null) {
            double latUtente = filtri.getLatitudineUtente();
            double lonUtente = filtri.getLongitudineUtente();
            int distanzaMassima = filtri.getDistanzaMassima();

            risultati = risultati.stream()
                    .filter(r -> calcolaDistanza(latUtente, lonUtente, r.getLatitudine(), r.getLongitudine()) <= distanzaMassima)
                    .collect(Collectors.toList());
        }

        // Filtro per "aperto ora" (richiede implementazione della logica oraria)
        if (filtri.getApertoOra() != null && filtri.getApertoOra()) {
            // Implementazione per verificare se il ristorante è aperto nell'ora corrente
            // Qui andrebbe inserita la logica che controlla gli orari di apertura
        }

        return risultati;
    }

    // Metodo di supporto per calcolare la distanza in km tra due punti
    private double calcolaDistanza(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Raggio della Terra in km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Distanza in km
    }
}