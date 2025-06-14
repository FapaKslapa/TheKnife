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

/**
 * Servizio che gestisce le operazioni relative alle risposte alle recensioni.
 * Fornisce funzionalità per creare, modificare ed eliminare risposte, oltre a
 * gestire i like associati alle recensioni.
 *
 * @author Stefano Marocco
 * @version 1.0
 */
public class RisposteService {
    private final JsonRepository<Risposta> risposteRepo;
    private final JsonRepository<Recensione> recensioniRepo;
    private final JsonRepository<Ristorante> ristorantiRepo;
    private final RelationRepository likesRepo;

    /**
     * Costruttore che inizializza il servizio ottenendo le istanze dei repository necessari.
     * Registra il repository per le risposte se non è già stato fatto in precedenza.
     */
    public RisposteService() {
        DataManager dm = DataManager.getInstance();

        // Registra il repository per le risposte se non è già stato fatto
        dm.registerEntityRepository(Risposta.class, "data/risposte.json");

        this.risposteRepo = dm.getRepository(Risposta.class);
        this.recensioniRepo = dm.getRepository(Recensione.class);
        this.ristorantiRepo = dm.getRepository(Ristorante.class);
        this.likesRepo = dm.getRelationRepository("utenti_recensioni_like");
    }

    /**
     * Crea una nuova risposta ad una recensione.
     * Verifica che il ristoratore sia il proprietario del ristorante recensito
     * e che non abbia già risposto alla recensione.
     *
     * @param ristoratoreId ID del ristoratore che vuole rispondere
     * @param recensioneId  ID della recensione a cui rispondere
     * @param testo         Contenuto della risposta
     * @return Optional contenente la risposta creata, o Optional vuoto in caso di errore
     */
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

    /**
     * Modifica il testo di una risposta esistente.
     * Verifica che la risposta esista e che appartenga al ristoratore specificato.
     *
     * @param rispostaId    ID della risposta da modificare
     * @param ristoratoreId ID del ristoratore che vuole modificare la risposta
     * @param nuovoTesto    Nuovo testo della risposta
     * @return Optional contenente la risposta modificata, o Optional vuoto in caso di errore
     */
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

    /**
     * Elimina una risposta esistente.
     * Verifica che la risposta esista e che appartenga al ristoratore specificato.
     *
     * @param rispostaId    ID della risposta da eliminare
     * @param ristoratoreId ID del ristoratore che vuole eliminare la risposta
     * @return true se l'eliminazione ha avuto successo, false altrimenti
     */
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

    /**
     * Aggiunge un like di un utente ad una recensione.
     * Verifica che la recensione esista prima di aggiungere il like.
     *
     * @param utenteId     ID dell'utente che mette like
     * @param recensioneId ID della recensione a cui mettere like
     * @return true se l'operazione ha avuto successo, false altrimenti
     */
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

    /**
     * Rimuove il like di un utente da una recensione.
     *
     * @param utenteId     ID dell'utente che rimuove il like
     * @param recensioneId ID della recensione da cui rimuovere il like
     * @return true se l'operazione ha avuto successo, false altrimenti
     */
    public boolean rimuoviLike(String utenteId, String recensioneId) {
        try {
            likesRepo.removeRelation(utenteId, recensioneId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Ottiene il numero totale di like per una recensione.
     *
     * @param recensioneId ID della recensione di cui contare i like
     * @return il numero di like della recensione
     */
    public int getNumeroLike(String recensioneId) {
        try {
            // Trova tutti gli utenti che hanno messo like a questa recensione
            List<String> utentiIds = likesRepo.findRelatedIds(recensioneId);
            return utentiIds.size();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Verifica se un utente ha messo like a una specifica recensione.
     *
     * @param utenteId     ID dell'utente da verificare
     * @param recensioneId ID della recensione da verificare
     * @return true se l'utente ha messo like alla recensione, false altrimenti
     */
    public boolean haMessoLike(String utenteId, String recensioneId) {
        try {
            List<String> recensioniConLike = likesRepo.findRelatedIds(utenteId);
            return recensioniConLike.contains(recensioneId);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Ottiene la risposta associata a una specifica recensione.
     * Una recensione può avere al massimo una risposta.
     *
     * @param recensioneId ID della recensione di cui ottenere la risposta
     * @return Optional contenente la risposta, se presente, altrimenti Optional vuoto
     */
    public Optional<Risposta> getRispostaPerRecensione(String recensioneId) {
        return risposteRepo.findAll().stream()
                .filter(r -> r.getRecensioneId().equals(recensioneId))
                .findFirst();
    }
}