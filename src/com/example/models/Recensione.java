package com.example.models;

import com.example.cache.BaseEntity;

import java.time.LocalDateTime;

/**
 * Classe Recensione rappresenta una recensione lasciata da un utente ad un ristorante
 *
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
    public Recensione() {
        // costruttore vuoto
    }

    public Recensione(String key_r, String key_user, LocalDateTime date, int rate, String title, String text) {
        this.key_r = key_r;
        this.key_user = key_user;
        this.date = date != null ? date : LocalDateTime.now();
        this.rate = rate;
        this.title = title;
        this.text = text;
    }

    // METODI (getter e setter)
    public String getKey_r() {
        return key_r;
    }

    public void setKey_r(String key_r) {
        this.key_r = key_r;
    }

    public String getKey_user() {
        return key_user;
    }

    public void setKey_user(String key_user) {
        this.key_user = key_user;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}