package com.example.models;

import com.example.cache.BaseEntity;

import java.util.Map;

/**
 * Classe Ristorante che rappresenta un ristorante all'interno del sistema.
 * Estende BaseEntity, ereditanto proprabilmente un ID univoco e altri dati.
 * Contiene informazioni essenziali per descrivere un ristorante , come nome, tipo di cucina, posizione, orari e servizi offerti.
 */

public class Ristorante extends BaseEntity {

    private String nome;    //Nome del ristornate
    private String tipoCucina;  //tipo di cucina(es.
    private int fasciaPrezzo; // Fascia di prezzo da 1 (economico) a 5 (costoso)
    private Map<String, String> orariApertura; // Mappa dei giorni con relativi orari di apertura (es. "lunedì": "09:00-23:00")
    private double latitudine;      // Latitudine della posizione del ristorante.
    private double longitudine;     //Longitudine della posizione del ristornate
    private String idProprietario; // ID dell'utente che gestisce il ristoratore
    private String numeroTelefono; // Numero di telefono  di contatto del ristorante
    private boolean consegnaDomicilio; // True se il risotrnate offre servizio a domicilio fasle altrimenti.

    /**
     * Costruttore del ristorante utilizzato peer creare un nuovo oggetto con i dati forniti.
     *
     * @param nome                  Nome del ristonate.
     * @param tipoCucina            Tipo di cucina offerta.
     * @param orarioApertura        Mappa conytente gli orari di apertura per ogni giorno.
     * @param latitudine            Latitudine della posizione geografica.
     * @param longitude             Longitudine della posizione geografica.
     * @param idProprietario        ID dell'utente ristornate proprietario.
     * @param numeroTelefono        Numero di telefono del ristorante.
     * @param consegnaDomicilio     True se è possibile la consegna a domicilio.
     */
    public Ristorante(String nome, String tipoCucina, Map<String, String> orarioApertura, double latitudine, double longitude, String idProprietario, String numeroTelefono, boolean consegnaDomicilio) {
        // Costruttore vuoto necessario per Gson
    }

    /**
     * Costruttore completo della classe Ristornate.
     * Utilizza il setter per la fascia di prezzo per garantire la validità del valore.
     *
     * @param nome              Nome del ristorante.
     * @param tipoCucina        Tipo di cucina offerta.
     * @param fasciaPrezzo      Fascia di prezzo(1-5).
     * @param orariApertura     Mappa degli orari di apertura.
     * @param latitudine        Latitudine geografica.
     * @param longitudine       Longitudine geografica.
     * @param idProprietario    ID del proprietario del ristorante.
     * @param numeroTelefono    Numero di telefono del ristorante.
     * @param consegnaDomicilio True se disponibile il servizio a consegna a domicilio.
     */

    public Ristorante(String nome, String tipoCucina, int fasciaPrezzo,
                      Map<String, String> orariApertura, double latitudine,
                      double longitudine, String idProprietario, String numeroTelefono,
                      boolean consegnaDomicilio) {
        this.nome = nome;
        this.tipoCucina = tipoCucina;
        setFasciaPrezzo(fasciaPrezzo); // Usa il setter per la validazione
        this.orariApertura = orariApertura;
        this.latitudine = latitudine;
        this.longitudine = longitudine;
        this.idProprietario = idProprietario;
        this.numeroTelefono = numeroTelefono;
        this.consegnaDomicilio = consegnaDomicilio;
    }

    // Getter e setter

    /** restituisce il nome del ristorante */
    public String getNome() {
        return nome;
    }

    /** Imposta il nome del ristorante. */
    public void setNome(String nome) {
        this.nome = nome;
    }
    /** Restituisce il tipo di cucina offerta del ristorante. */
    public String getTipoCucina() {
        return tipoCucina;
    }
    /** Imposta il tipo di cucina offerta dal ristorante. */
    public void setTipoCucina(String tipoCucina) {
        this.tipoCucina = tipoCucina;
    }

    /**
     * Restituisce la fascia di prezzo.
     * Valore compresto tra 1 (economico) e 5 (costoso). */

    public int getFasciaPrezzo() {
        return fasciaPrezzo;
    }

    /**
     * Imposta la fascia di prezzo.
     * Lancia IllegalArgumentException se il valore non è compreso tra 1 e 5.
     */
    public void setFasciaPrezzo(int fasciaPrezzo) {
        if (fasciaPrezzo < 1 || fasciaPrezzo > 5) {
            throw new IllegalArgumentException("La fascia di prezzo deve essere compresa tra 1 e 5");
        }
        this.fasciaPrezzo = fasciaPrezzo;
    }
    /** Restituisce la mappa degli orari di apertura.*/

    public Map<String, String> getOrariApertura() {
        return orariApertura;
    }
    /** Imposta la mappa degli orari di apertura. */

    public void setOrariApertura(String orariApertura) {
        this.orariApertura = orariApertura;
    }
    /** Restituisce la latitudine del ristorante. */
    public double getLatitudine() {
        return latitudine;
    }
    /** Imposta la latitudine del ristorante. */
    public void setLatitudine(double latitudine) {
        this.latitudine = latitudine;
    }
    /** Restituisce la longitudine del ristornate. */
    public double getLongitudine() {
        return longitudine;
    }
    /** Imposta la longitudien del ristorante. */
    public void setLongitudine(double longitudine) {
        this.longitudine = longitudine;
    }
    /** Restituisce l'ID del proprietario del ristorante. */
    public String getIdProprietario() {
        return idProprietario;
    }
    /** Imposta l'ID del proprietario del ristorante. */
    public void setIdProprietario(String idProprietario) {
        this.idProprietario = idProprietario;
    }
    /** Restitusce il numero di telefono del ristornate. */
    public String getNumeroTelefono() {
        return numeroTelefono;
    }
    /** Imposta il nunmero di telefono del ristornate. */
    public void setNumeroTelefono(String numeroTelefono) {
        this.numeroTelefono = numeroTelefono;
    }
    /** Restituisce true se il ristorante offre consegana a domicilio, flase altrimenti. */
    public boolean isConsegnaDomicilio() {
        return consegnaDomicilio;
    }
    /** Imposta se il ristornate offre la consegna a domicilio. */
    public void setConsegnaDomicilio(boolean consegnaDomicilio) {
        this.consegnaDomicilio = consegnaDomicilio;
    }
}