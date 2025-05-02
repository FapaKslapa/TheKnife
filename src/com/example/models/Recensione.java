package com.example.models;

import com.example.cache.BaseEntity;

import java.time.LocalDateTime;

public class Recensione extends BaseEntity {
    // CAMPI
    private String key_r;     // chiave del ristorante
    private String key_user;  // chiave dell'utente
    private LocalDateTime date;  // data e ora della recensione
    private int rate;         // valutazione (da 0 a 5 stelle) del ristorante
    private String title;     // titolo della recensione (scritto in grassetto)
    private String text;      // testo principale della recensione

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

    // METODI
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