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
 * Servizio per la gestione delle recensioni.
 * Si occupa di alcune operazioni come salvataggio e creazione, modifica ed eliminazione.
 */
public class RecensioneService {
    private final JsonRepository<Recensione> recensioneRepository;
    private final RelationRepository likeRepository;
    private final RelationRepository dislikeRepository;
    private RisposteService risposteService;

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

    public Recensione salvaRecensione(Recensione recensione) {
        if (recensione.getDate() == null) {
            recensione.setDate(LocalDateTime.now());
        }
        return recensioneRepository.save(recensione);
    }

    public Recensione creaRecensione(String idRistorante, String idUtente, int voto, String titolo, String testo) {
        Recensione recensione = new Recensione(idRistorante, idUtente, LocalDateTime.now(), voto, titolo, testo);
        return salvaRecensione(recensione);
    }

    public boolean eliminaRecensione(String recensioneId) {
        if (recensioneRepository.findById(recensioneId).isPresent()) {
            risposteService.eliminaRispostaByRecensione(recensioneId);
            recensioneRepository.deleteById(recensioneId);
            return true;
        }
        return false;
    }

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

    public List<Recensione> getAllRecensioni() {
        return recensioneRepository.findAll();
    }

    public List<Recensione> getRecensioniByUtente(String idUtente) {
        return recensioneRepository.findAll().stream()
                .filter(r -> r.getKey_user().equals(idUtente))
                .collect(Collectors.toList());
    }

    public List<Recensione> getRecensioniByRistorante(String idRistorante) {
        return recensioneRepository.findAll().stream()
                .filter(r -> r.getKey_r().equals(idRistorante))
                .collect(Collectors.toList());
    }

    public List<Recensione> getRecensioniByRistoranteAndVoto(String idRistorante, int voto) {
        return recensioneRepository.findAll().stream()
                .filter(r -> r.getKey_r().equals(idRistorante) && r.getRate() == voto)
                .collect(Collectors.toList());
    }

    public List<Recensione> getRecensioniByRistoranteAndVotoMinimo(String idRistorante, int votoMinimo) {
        return recensioneRepository.findAll().stream()
                .filter(r -> r.getKey_r().equals(idRistorante) && r.getRate() >= votoMinimo)
                .collect(Collectors.toList());
    }

    public List<Recensione> getRecensioniByRistoranteAndVotoMassimo(String idRistorante, int votoMassimo) {
        return recensioneRepository.findAll().stream()
                .filter(r -> r.getKey_r().equals(idRistorante) && r.getRate() <= votoMassimo)
                .collect(Collectors.toList());
    }

    public boolean isRecensionePositiva(int voto) {
        return voto >= 3;
    }

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

    public Optional<Risposta> modificaRisposta(String rispostaId, String testo) {
        return risposteService.modificaRisposta(rispostaId, testo);
    }

    public boolean eliminaRisposta(String rispostaId) {
        return risposteService.eliminaRisposta(rispostaId);
    }

    public Optional<Risposta> getRispostaByRecensione(String recensioneId) {
        return risposteService.getRispostaByRecensione(recensioneId);
    }

    public boolean hasRisposta(String recensioneId) {
        return risposteService.esisteRispostaPerRecensione(recensioneId);
    }
}