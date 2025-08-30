package example.models;

import example.cache.BaseEntity;

import java.time.LocalDateTime;

/**
 * Classe che rappresenta una risposta a una recensione.
 * Contiene le informazioni relative alla risposta di un ristoratore.
 */
public class Risposta extends BaseEntity {
    private String recensioneId;
    private String testo;
    private LocalDateTime dataCreazione;
    private LocalDateTime dataModifica;

    /**
     * Costruttore vuoto necessario per la deserializzazione.
     */
    public Risposta() {
        // Costruttore vuoto per serializzazione/deserializzazione
    }

    /**
     * Costruttore per una nuova risposta.
     *
     * @param recensioneId L'ID della recensione a cui si risponde
     * @param testo Il testo della risposta
     */
    public Risposta(String recensioneId, String testo) {
        this.recensioneId = recensioneId;
        this.testo = testo;
        this.dataCreazione = LocalDateTime.now();
        this.dataModifica = this.dataCreazione;
    }

    /**
     * Modifica il testo della risposta.
     *
     * @param testo Il nuovo testo della risposta
     */
    public void modificaTesto(String testo) {
        this.testo = testo;
        this.dataModifica = LocalDateTime.now();
    }

    /**
     * Restituisce l'ID della recensione associata.
     *
     * @return L'ID della recensione
     */
    public String getRecensioneId() {
        return recensioneId;
    }

    /**
     * Imposta l'ID della recensione associata.
     *
     * @param recensioneId L'ID della recensione
     */
    public void setRecensioneId(String recensioneId) {
        this.recensioneId = recensioneId;
    }

    /**
     * Restituisce il testo della risposta.
     *
     * @return Il testo della risposta
     */
    public String getTesto() {
        return testo;
    }

    /**
     * Imposta il testo della risposta.
     *
     * @param testo Il testo della risposta
     */
    public void setTesto(String testo) {
        this.testo = testo;
    }

    /**
     * Restituisce la data di creazione della risposta.
     *
     * @return La data di creazione
     */
    public LocalDateTime getDataCreazione() {
        return dataCreazione;
    }

    /**
     * Imposta la data di creazione della risposta.
     *
     * @param dataCreazione La data di creazione
     */
    public void setDataCreazione(LocalDateTime dataCreazione) {
        this.dataCreazione = dataCreazione;
    }

    /**
     * Restituisce la data dell'ultima modifica della risposta.
     *
     * @return La data dell'ultima modifica
     */
    public LocalDateTime getDataModifica() {
        return dataModifica;
    }

    /**
     * Imposta la data dell'ultima modifica della risposta.
     *
     * @param dataModifica La data dell'ultima modifica
     */
    public void setDataModifica(LocalDateTime dataModifica) {
        this.dataModifica = dataModifica;
    }
}