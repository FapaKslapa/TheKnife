package com.example.models;

import com.example.cache.BaseEntity;
import org.mindrot.jbcrypt.BCrypt;

public class Utente extends BaseEntity {
    private String username;
    private String password; // password criptata
    private String email;
    private Ruolo ruolo;

    public enum Ruolo {
        UTENTE,
        RISTORATORE
    }

    public Utente() {
        // Costruttore vuoto necessario per Gson
    }

    public Utente(String username, String passwordChiara, String email, Ruolo ruolo) {
        this.username = username;
        this.password = criptaPassword(passwordChiara);
        this.email = email;
        this.ruolo = ruolo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String passwordChiara) {
        this.password = criptaPassword(passwordChiara);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Ruolo getRuolo() {
        return ruolo;
    }

    public void setRuolo(Ruolo ruolo) {
        this.ruolo = ruolo;
    }

    // Metodi per gestire le password
    private String criptaPassword(String passwordChiara) {
        return BCrypt.hashpw(passwordChiara, BCrypt.gensalt(12));
    }

    public boolean verificaPassword(String passwordInserita) {
        return BCrypt.checkpw(passwordInserita, this.password);
    }
}