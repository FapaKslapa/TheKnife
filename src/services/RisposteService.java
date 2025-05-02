package services;

import com.example.cache.DataManager;
import com.example.cache.JsonRepository;
import com.example.cache.RelationRepository;
import com.example.classes.Recensione;
import com.example.models.Risposta;
import com.example.models.Ristorante;

import java.util.Optional;

public class RisposteService {
    private final JsonRepository<Risposta> risposteRepo;
    private final JsonRepository<Recensione> recensioniRepo;
    private final JsonRepository<Ristorante> ristorantiRepo;
    private final RelationRepository likesRepo;

    public RisposteService() {
        DataManager dm = DataManager.getInstance();
        this.risposteRepo = dm.getRepository(Risposta.class);
        this.recensioniRepo = dm.getRepository(Recensione.class);
        this.ristorantiRepo = dm.getRepository(Ristorante.class);
        this.likesRepo = dm.getRelationRepository("utenti_recensioni_like");
    }

    // Crea risposta alla recensione (solo per il ristoratore proprietario)
    public Risposta creaRisposta(String ristoratoreId, String recensioneId, String testo) {
        // Recupera la recensione
        Recensione recensione = recensioniRepo.findById(recensioneId)
                .orElseThrow(() -> new IllegalArgumentException("Recensione non trovata"));

        // Verifica che il ristoratore sia proprietario del ristorante recensito
        Ristorante ristorante = ristorantiRepo.findById(recensione.getRistoranteId())
                .orElseThrow(() -> new IllegalArgumentException("Ristorante non trovato"));

        if (!ristorante.getProprietarioId().equals(ristoratoreId)) {
            throw new IllegalArgumentException("Solo il proprietario può rispondere");
        }

        // Controlla che non abbia già risposto
        boolean haGiaRisposto = risposteRepo.findAll().stream()
                .anyMatch(r -> r.getRecensioneId().equals(recensioneId));

        if (haGiaRisposto) {
            throw new IllegalArgumentException("Hai già risposto a questa recensione");
        }

        // Crea la risposta
        Risposta risposta = new Risposta();
        risposta.setRecensioneId(recensioneId);
        risposta.setRistoratoreId(ristoratoreId);
        risposta.setTesto(testo);
        risposta.setDataCreazione(new Date());

        return risposteRepo.save(risposta);
    }

    // Modifica risposta
    public Risposta modificaRisposta(String rispostaId, String ristoratoreId, String nuovoTesto) {
        Risposta risposta = risposteRepo.findById(rispostaId)
                .orElseThrow(() -> new IllegalArgumentException("Risposta non trovata"));

        if (!risposta.getRistoratoreId().equals(ristoratoreId)) {
            throw new IllegalArgumentException("Non puoi modificare risposte di altri");
        }

        risposta.setTesto(nuovoTesto);
        return risposteRepo.save(risposta);
    }

    // Elimina risposta
    public void eliminaRisposta(String rispostaId, String ristoratoreId) {
        Risposta risposta = risposteRepo.findById(rispostaId)
                .orElseThrow(() -> new IllegalArgumentException("Risposta non trovata"));

        if (!risposta.getRistoratoreId().equals(ristoratoreId)) {
            throw new IllegalArgumentException("Non puoi eliminare risposte di altri");
        }

        risposteRepo.deleteById(rispostaId);
    }

    // Aggiunge like ad una recensione
    public void aggiungiLike(String utenteId, String recensioneId) {
        // Verifica che la recensione esista
        if (!recensioniRepo.findById(recensioneId).isPresent()) {
            throw new IllegalArgumentException("Recensione non trovata");
        }

        // Aggiungi relazione utente-recensione (like)
        likesRepo.addRelation(utenteId, recensioneId);
    }

    // Rimuove like da una recensione
    public void rimuoviLike(String utenteId, String recensioneId) {
        likesRepo.removeRelation(utenteId, recensioneId);
    }

    // Ottieni numero di like per una recensione
    public int getNumeroLike(String recensioneId) {
        // Trova tutti gli utenti che hanno messo like a questa recensione
        List<String> utentiIds = likesRepo.findRelatedIds(recensioneId);
        return utentiIds.size();
    }

    // Verifica se un utente ha messo like a una recensione
    public boolean haMessoLike(String utenteId, String recensioneId) {
        List<String> recensioniConLike = likesRepo.findRelatedIds(utenteId);
        return recensioniConLike.contains(recensioneId);
    }

    // Ottieni risposta per una recensione
    public Optional<Risposta> getRispostaPerRecensione(String recensioneId) {
        return risposteRepo.findAll().stream()
                .filter(r -> r.getRecensioneId().equals(recensioneId))
                .findFirst();
    }
}