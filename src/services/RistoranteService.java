package services;

import com.example.cache.DataManager;
import com.example.cache.JsonRepository;
import com.example.models.Ristorante;
import com.example.models.Utente;

import java.util.List;
import java.util.stream.Collectors;

public class RistoranteService {
    private final JsonRepository<Ristorante> ristoranteRepository;

    public RistoranteService() {
        DataManager dataManager = DataManager.getInstance();
        // Registra il repository se non è già fatto
        dataManager.registerEntityRepository(Ristorante.class, "data/ristoranti.json");
        this.ristoranteRepository = dataManager.getRepository(Ristorante.class);
    }

    public Ristorante salvaRistorante(Ristorante ristorante) {
        return ristoranteRepository.save(ristorante);
    }

    public List<Ristorante> getAllRistoranti() {
        return ristoranteRepository.findAll();
    }

    public List<Ristorante> getRistorantiByProprietario(String idUtente) {
        return ristoranteRepository.findAll().stream()
                .filter(r -> r.getIdProprietario().equals(idUtente))
                .collect(Collectors.toList());
    }
}