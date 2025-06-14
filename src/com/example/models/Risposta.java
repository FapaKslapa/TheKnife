package com.example.models;

import com.example.cache.BaseEntity;

import java.util.Date;

/**
 * Classe che rappresenta una risposta del ristoratore a una recensione.
 * Estende BaseEntity per ereditare le funzionalità di base dell'entità.
 *
 * @author Stefano Marocco
 * @version 1.0
 */
public class Risposta extends BaseEntity {
    /**
     * Identificativo della recensione a cui si riferisce questa risposta
     */
    private String recensioneId;

    /**
     * Identificativo del ristoratore che ha scritto la risposta
     */
    private String ristoratoreId;

    /**
     * Contenuto testuale della risposta
     */
    private String testo;

    /**
     * Data di creazione della risposta
     */
    private Date dataCreazione;

    /**
     * Data dell'ultima modifica della risposta
     */
    private Date dataModifica;

    /**
     * Costruttore vuoto necessario per la deserializzazione tramite GSON.
     */
    public Risposta() {
        // Costruttore vuoto per GSON
    }

    /**
     * Restituisce l'ID della recensione a cui questa risposta è associata.
     *
     * @return l'identificativo della recensione
     */
    public String getRecensioneId() {
        return recensioneId;
    }

    /**
     * Imposta l'ID della recensione a cui questa risposta è associata.
     *
     * @param recensioneId l'identificativo della recensione
     */
    public void setRecensioneId(String recensioneId) {
        this.recensioneId = recensioneId;
    }

    /**
     * Restituisce l'ID del ristoratore che ha scritto questa risposta.
     *
     * @return l'identificativo del ristoratore
     */
    public String getRistoratoreId() {
        return ristoratoreId;
    }

    /**
     * Imposta l'ID del ristoratore che ha scritto questa risposta.
     *
     * @param ristoratoreId l'identificativo del ristoratore
     */
    public void setRistoratoreId(String ristoratoreId) {
        this.ristoratoreId = ristoratoreId;
    }

    /**
     * Restituisce il testo della risposta.
     *
     * @return il contenuto testuale della risposta
     */
    public String getTesto() {
        return testo;
    }

    /**
     * Imposta il testo della risposta.
     *
     * @param testo il contenuto testuale della risposta
     */
    public void setTesto(String testo) {
        this.testo = testo;
    }

    /**
     * Restituisce la data di creazione della risposta.
     *
     * @return la data di creazione
     */
    public Date getDataCreazione() {
        return dataCreazione;
    }

    /**
     * Imposta la data di creazione della risposta.
     *
     * @param dataCreazione la data di creazione
     */
    public void setDataCreazione(Date dataCreazione) {
        this.dataCreazione = dataCreazione;
    }

    /**
     * Restituisce la data dell'ultima modifica della risposta.
     *
     * @return la data di modifica
     */
    public Date getDataModifica() {
        return dataModifica;
    }

    /**
     * Imposta la data dell'ultima modifica della risposta.
     *
     * @param dataModifica la data di modifica
     */
    public void setDataModifica(Date dataModifica) {
        this.dataModifica = dataModifica;
    }
}