// Marocco Stefano 762192 VA
// Marin Marco 760622 VA
// Gerti Alessia 762405 VA
package theknife.models;

import theknife.cache.BaseEntity;

import java.time.LocalDateTime;

/**
 * Classe Recensione rappresenta una recensione lasciata da un utente ad un ristorante.
 * Estende BaseEntity, presubibilmente per ereditare attributi comuni.
 * Contiene le caratteristiche basi di un commento, come l'orario in cui viene scritto, il titolo del commento e il commento in sé.
 */

public class Recensione extends BaseEntity {
    // CAMPI
    private String key_r;     // identificativo del ristorante a cui si riferisce la recensione
    private String key_user;  // identificativo dell'utente che ha scritto la recensione
    private LocalDateTime date;  // data e ora in cui e' stata lasciata la recensione
    private int rate;         // valutazione data al ristorante (da 0 a 5 stelle)
    private String title;     // titolo della recensione (in grassetto nell'interfaccia)
    private String text;      // contenuto testuale completo della recensione

    // COSTRUTTORI

    /**
     * Costruttore vuoto necessario per la deserializzazione automatica di JSON.
     */
    public Recensione() {
        // costruttore vuoto
    }

    /**
     * Costruttore completo necessario per inizialozzare tutti i campi della recensione.
     * @param key_r     identificativo del ristorante
     * @param key_user  identificativo dell'utente
     * @param date      data e ora della recensione
     * @param rate      valutazione (da 0 a 5 stelle)
     * @param title     titolo della recensione
     * @param text      contenuto della recensione
     */
    public Recensione(String key_r, String key_user, LocalDateTime date, int rate, String title, String text) {
        this.key_r = key_r;
        this.key_user = key_user;
        this.date = date != null ? date : LocalDateTime.now();
        this.rate = rate;
        this.title = title;
        this.text = text;
    }

    // METODI (getter e setter)

    /**
     * Restituisce l'identificativo del ritorante.
     */
    public String getKey_r() {
        return key_r;
    }

    /**
     * Imposta l'identificativo del ritorante.
     */
    public void setKey_r(String key_r) {
        this.key_r = key_r;
    }

    /**
     * Restituisce l'identificativo dell'utente.
     */
    public String getKey_user() {
        return key_user;
    }

    /**
     * Imposta l'identificativo dell'utente.
     */
    public void setKey_user(String key_user) {
        this.key_user = key_user;
    }

    /**
     * Restituisce la data e l'ora della recensione.
     */
    public LocalDateTime getDate() {
        return date;
    }

    /**
     * Imposta la data e l'ora della recensione.
     */
    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    /**
     * Restituisce la valutazioone del ristorante.
     */
    public int getRate() {
        return rate;
    }

    /**
     * Imposta la valutazioone del ristorante.
     */
    public void setRate(int rate) {
        this.rate = rate;
    }

    /**
     * Restituisce il titolo della recensione.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Imposta il titolo della recensione.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Restituisce il testo della recensione.
     */
    public String getText() {
        return text;
    }

    /**
     * Imposta il testo della recensione.
     */
    public void setText(String text) {
        this.text = text;
    }
}