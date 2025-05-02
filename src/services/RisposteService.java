package services;

import com.example.cache.DataManager;
import com.example.cache.JsonRepository;
import com.example.cache.RelationRepository;
import com.example.models.Recensione;
import com.example.models.Risposta;
import com.example.models.Ristorante;
import com.example.cache.BaseEntity;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public class RisposteService {
    private final JsonRepository<Risposta> risposteRepo;
    private final JsonRepository<Recensione> recensioniRepo;
    private final JsonRepository<Ristorante> ristorantiRepo;
    private final RelationRepository likesRepo;

    public RisposteService() {
        DataManager dm = DataManager.getInstance();

        // Registra il repository per le risposte se non è già stato fatto
        dm.registerEntityRepository(Risposta.class, "data/risposte.json");

        this.risposteRepo = dm.getRepository(Risposta.class);
        this.recensioniRepo = dm.getRepository(Recensione.class);
        this.ristorantiRepo = dm.getRepository(Ristorante.class);
        this.likesRepo = dm.getRelationRepository("utenti_recensioni_like");
    }

    // Crea risposta alla recensione (solo per il ristoratore proprietario)
    public Optional<Risposta> creaRisposta(String ristoratoreId, String recensioneId, String testo) {
        try {
            // Recupera la recensione
            Optional<Recensione> recensioneOpt = recensioniRepo.findById(recensioneId);
            if (!recensioneOpt.isPresent()) {
                return Optional.empty();
            }
            Recensione recensione = recensioneOpt.get();

            // Recupera il ristorante
            Optional<Ristorante> ristoranteOpt = ristorantiRepo.findById(recensione.getKey_r());
            if (!ristoranteOpt.isPresent()) {
                return Optional.empty();
            }
            Ristorante ristorante = ristoranteOpt.get();

            // Verifica che il ristoratore sia proprietario del ristorante recensito
            if (!ristorante.getIdProprietario().equals(ristoratoreId)) {
                return Optional.empty();
            }

            // Controlla che non abbia già risposto
            boolean haGiaRisposto = risposteRepo.findAll().stream()
                    .anyMatch(r -> r.getRecensioneId().equals(recensioneId));

            if (haGiaRisposto) {
                return Optional.empty();
            }

            // Crea la risposta
            Risposta risposta = new Risposta();
            risposta.setRecensioneId(recensioneId);
            risposta.setRistoratoreId(ristoratoreId);
            risposta.setTesto(testo);
            risposta.setDataCreazione(new Date());

            return Optional.of(risposteRepo.save(risposta));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // Modifica risposta
    public Optional<Risposta> modificaRisposta(String rispostaId, String ristoratoreId, String nuovoTesto) {
        try {
            Optional<Risposta> rispostaOpt = risposteRepo.findById(rispostaId);
            if (!rispostaOpt.isPresent()) {
                return Optional.empty();
            }

            Risposta risposta = rispostaOpt.get();
            if (!risposta.getRistoratoreId().equals(ristoratoreId)) {
                return Optional.empty();
            }

            risposta.setTesto(nuovoTesto);
            risposta.setDataModifica(new Date()); // Aggiorna la data di modifica
            return Optional.of(risposteRepo.save(risposta));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // Elimina risposta
    public boolean eliminaRisposta(String rispostaId, String ristoratoreId) {
        try {
            Optional<Risposta> rispostaOpt = risposteRepo.findById(rispostaId);
            if (!rispostaOpt.isPresent()) {
                return false;
            }

            Risposta risposta = rispostaOpt.get();
            if (!risposta.getRistoratoreId().equals(ristoratoreId)) {
                return false;
            }

            risposteRepo.deleteById(rispostaId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Aggiunge like ad una recensione
    public boolean aggiungiLike(String utenteId, String recensioneId) {
        try {
            // Verifica che la recensione esista
            if (!recensioniRepo.findById(recensioneId).isPresent()) {
                return false;
            }

            // Aggiungi relazione utente-recensione (like)
            likesRepo.addRelation(utenteId, recensioneId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Rimuove like da una recensione
    public boolean rimuoviLike(String utenteId, String recensioneId) {
        try {
            likesRepo.removeRelation(utenteId, recensioneId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Ottieni numero di like per una recensione
    public int getNumeroLike(String recensioneId) {
        try {
            // Trova tutti gli utenti che hanno messo like a questa recensione
            List<String> utentiIds = likesRepo.findRelatedIds(recensioneId);
            return utentiIds.size();
        } catch (Exception e) {
            return 0;
        }
    }

    // Verifica se un utente ha messo like a una recensione
    public boolean haMessoLike(String utenteId, String recensioneId) {
        try {
            List<String> recensioniConLike = likesRepo.findRelatedIds(utenteId);
            return recensioniConLike.contains(recensioneId);
        } catch (Exception e) {
            return false;
        }
    }

    // Ottieni risposta per una recensione
    public Optional<Risposta> getRispostaPerRecensione(String recensioneId) {
        return risposteRepo.findAll().stream()
                .filter(r -> r.getRecensioneId().equals(recensioneId))
                .findFirst();
    }
}