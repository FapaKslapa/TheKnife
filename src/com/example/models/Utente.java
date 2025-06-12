package com.example.models;

import com.example.cache.BaseEntity;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Classe utente che rappresenta un utente del sistema.
 *Estende la classe BaseEntity; Presubibilmente per ereditare attributi comuni.
 * Contiene la informazioni principali dell'utente:udername,password(criptata),email e ruolo.
 */
public class Utente extends BaseEntity {
    private String username;    // Nome utente
    private String password;    // password criptata
    private String email;       // Indirizzo email dell'utente
    private Ruolo ruolo;        // Ruolo dell'utente nel sistema

    /**
     * Enum che definisce i ruoli possibili per un utente all'interno del sistema.
     * - UTENTE: ruolo standard, con accessi e funzionalità limitate.
     * -RISTORANTE: ruolo con privilegi specifici, come ad esempio gestire un ristorante.
     */

    public enum Ruolo {
        UTENTE,
        RISTORATORE
    }

    /**
     * Costruttore vuoto della classe Utente.
     * Nessessario per la deserializzazione aitomatica da parte di librerie come Gson.
     *
     * Gson richiede un costruttore senza argomenti per poter creare oggetti tramite riflessione.
     */

    public Utente() {
        // Costruttore vuoto necessario per Gson
    }

    /**
     * Costruttore della calsse Utente.
     * Inizialiazza un nuovo ogetto Utente con i dati forniti.
     * La password viene cripitata prima di essere salvata.
     *
     * @param username         Nome utente scelto dall'utente.
     * @param passwordChiara   Password in chiaro fornita dall'utente, che verrà criptata.
     * @param email            Indirizo email associato all'utente.
     * @param ruolo            Ruolo asseganto all'utente.
     */
    public Utente(String username, String passwordChiara, String email, Ruolo ruolo) {
        this.username = username;  // Assegna l'username all'utente
        this.password = criptaPassword(passwordChiara);  // Cripta la password in chiara e la salva
        this.email = email; // Assegna l'email dell'utente
        this.ruolo = ruolo; // Imposta il ruolo dell'utente
    }

    /**
     * Restityuisce l'username dell'utente.
     *
     * @return  L'username attualemnte assegnato all'utente.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Imposta o aggiorna l'username dell'utente
     * @param username
     */

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Restituisci la password dell'utente
     * la password è criptata , non in chiaro.
     * @return      La password è criptata dell'utente
     */

    public String getPassword() {
        return password;
    }

    /**
     * Imposta o aggiorna la password dell'utente
     * La password viene automaticamente criptata prima di essere slavata.
     * @param passwordChiara
     */
    public void setPassword(String passwordChiara) {
        this.password = criptaPassword(passwordChiara);
    }

    /**
     * Restituisce l'indirizzo email associato all'utente
     *
     * @return      l'email dell'utente.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Imposta o aggiorna l'indirizzo email dell'utente
     *
     * @param email     Il nuovo indirizzo email da assegnare all'utente.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Restituisce il ruolo associato all'utente
     *
     * @return  Il ruolo dell'utente
     */
    public Ruolo getRuolo() {
        return ruolo;
    }

    /**
     * Imposta o aggiorna il ruolo dell'utente
     *
     * @param ruolo     Il nuovo ruolo da assegnare dell'utente.
     */

    public void setRuolo(Ruolo ruolo) {
        this.ruolo = ruolo;
    }

    /**
     * criptauna password in chaira utilizzando l'algoritmo BCrypt.
     * IL metodo genera automaticamnete un salt siciro e applica il costo specificato.
     *
     * @param passwordChiara La password in chiaro da cripatre.
     * @return  La password criptata, pronta per essere salvata in un medo sicuro.
     */

    private String criptaPassword(String passwordChiara) {
        return BCrypt.hashpw(passwordChiara, BCrypt.gensalt(12));
    }

    /**
     * Verifica se una password inserita corrisponde alla password cripatata dell'utente.
     *
     * @param passwordInserita  La password in chiaro inserita dall'utente.
     * @return  true se la password corrisponde, false altrimenti.
     */

    public boolean verificaPassword(String passwordInserita) {
        return BCrypt.checkpw(passwordInserita, this.password);
    }
}