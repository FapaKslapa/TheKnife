package services;

import com.example.cache.DataManager;
import com.example.cache.JsonRepository;
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
}