package com.example.models;

import com.example.cache.BaseEntity;

import java.util.Date;

public class Risposta extends BaseEntity {
    private String recensioneId;
    private String ristoratoreId;
    private String testo;
    private Date dataCreazione;

    // getter e setter
    public String getRecensioneId() {
        return recensioneId;
    }

    public void setRecensioneId(String recensioneId) {
        this.recensioneId = recensioneId;
    }

    public String getRistoratoreId() {
        return ristoratoreId;
    }

    public void setRistoratoreId(String ristoratoreId) {
        this.ristoratoreId = ristoratoreId;
    }

    public String getTesto() {
        return testo;
    }

    public void setTesto(String testo) {
        this.testo = testo;
    }

    public Date getDataCreazione() {
        return dataCreazione;
    }

    public void setDataCreazione(Date dataCreazione) {
        this.dataCreazione = dataCreazione;
    }
}