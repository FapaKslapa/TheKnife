package com.example;


import com.example.cache.BaseEntity;

public class Ristornate extends BaseEntity {
    private String nome;
    private String idProprietario;
    private String indirizzo;
    private int numeroTelefono;
    private String categoria;
    private double latitudine;
    private double longitudine;
    private String fasciaPrezzo;
    private String orarioApertura;
    private String orarioChiusura;
    private boolean consegnaDomicilio;
    private boolean prenotazioneTavolo;

    public Ristornate(String nome, String indirizzo) {
        this.nome = nome;
        this.idProprietario = idProprietario;
        this.indirizzo = indirizzo;
        this.numeroTelefono = numeroTelefono;
        this.categoria = categoria;
        this.latitudine = latitudine;
        this.longitudine = longitudine;
        this.fasciaPrezzo = fasciaPrezzo;
        this.orarioApertura = orarioApertura;
        this.orarioChiusura = orarioChiusura;
        this.consegnaDomicilio = consegnaDomicilio;
        this.prenotazioneTavolo = prenotazioneTavolo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getIdProprietario() {
        return idProprietario;
    }

    public void setIdProprietario(String idProprietario) {
        this.idProprietario = idProprietario;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public void setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
    }

    public int getNumeroTelefono() {
        return numeroTelefono;
    }

    public void setNumeroTelefono(int numeroTelefono) {
        this.numeroTelefono = numeroTelefono;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
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

    public String getFasciaPrezzo() {
        return fasciaPrezzo;
    }

    public void setFasciaPrezzo(String fasciaPrezzo) {
        this.fasciaPrezzo = fasciaPrezzo;
    }

    public String getOrarioApertura() {
        return orarioApertura;
    }

    public void setOrarioApertura(String orarioApertura) {
        this.orarioApertura = orarioApertura;
    }

    public String getOrarioChiusura() {
        return orarioChiusura;
    }

    public void setOrarioChiusura(String orarioChiusura) {
        this.orarioChiusura = orarioChiusura;
    }

    public boolean isConsegnaDomicilio() {
        return consegnaDomicilio;
    }

    public void setConsegnaDomicilio(boolean consegnaDomicilio) {
        this.consegnaDomicilio = consegnaDomicilio;
    }

    public boolean isPrenotazioneTavolo() {
        return prenotazioneTavolo;
    }

    public void setPrenotazioneTavolo(boolean prenotazioneTavolo) {
        this.prenotazioneTavolo = prenotazioneTavolo;
    }

}