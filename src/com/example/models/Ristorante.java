package com.example.models;

import com.example.cache.BaseEntity;
import com.example.cache.BaseEntity;

import java.util.Map;

public class Ristorante extends BaseEntity {
    private String nome;
    private String tipoCucina;
    private int fasciaPrezzo; // da 1 a 5
    private Map<String, String> orariApertura; // es. "lunedì": "09:00-23:00"
    private double latitudine;
    private double longitudine;
    private String idProprietario; // ID dell'utente ristoratore

    public Ristorante() {
        // Costruttore vuoto necessario per Gson
    }

    public Ristorante(String nome, String tipoCucina, int fasciaPrezzo,
                      Map<String, String> orariApertura, double latitudine,
                      double longitudine, String idProprietario) {
        this.nome = nome;
        this.tipoCucina = tipoCucina;
        setFasciaPrezzo(fasciaPrezzo); // Usa il setter per la validazione
        this.orariApertura = orariApertura;
        this.latitudine = latitudine;
        this.longitudine = longitudine;
        this.idProprietario = idProprietario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipoCucina() {
        return tipoCucina;
    }

    public void setTipoCucina(String tipoCucina) {
        this.tipoCucina = tipoCucina;
    }

    public int getFasciaPrezzo() {
        return fasciaPrezzo;
    }

    public void setFasciaPrezzo(int fasciaPrezzo) {
        if (fasciaPrezzo < 1 || fasciaPrezzo > 5) {
            throw new IllegalArgumentException("La fascia di prezzo deve essere compresa tra 1 e 5");
        }
        this.fasciaPrezzo = fasciaPrezzo;
    }

    public Map<String, String> getOrariApertura() {
        return orariApertura;
    }

    public void setOrariApertura(Map<String, String> orariApertura) {
        this.orariApertura = orariApertura;
    }

    public double getLatitudine() {
        return latitudine;
    }

    public void setLatitudine(double latitudine) {
        this.latitudine = latitudine;
    }

    public double getLongitudine() {
        return longitudine;
    }

    public void setLongitudine(double longitudine) {
        this.longitudine = longitudine;
    }

    public String getIdProprietario() {
        return idProprietario;
    }

    public void setIdProprietario(String idProprietario) {
        this.idProprietario = idProprietario;
    }
}