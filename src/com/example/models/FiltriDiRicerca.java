package com.example.models;

public class FiltriDiRicerca {
    private String tipoCucina;
    private Integer fasciaPrezzo;
    private Double latitudineUtente;
    private Double longitudineUtente;
    private Integer distanzaMassima;
    private Boolean consegnaDomicilio;
    private Boolean apertoOra;


    private FiltriDiRicerca() {

    }

    public String getTipoCucina() {
        return tipoCucina;
    }

    public Integer getFasciaPrezzo() {
        return fasciaPrezzo;
    }

    public Double getLatitudineUtente() {
        return latitudineUtente;
    }

    public Double getLongitudineUtente() {
        return longitudineUtente;
    }

    public Integer getDistanzaMassima() {
        return distanzaMassima;
    }

    public Boolean getConsegnaDomicilio() {
        return consegnaDomicilio;
    }

    public Boolean getApertoOra() {
        return apertoOra;
    }

    public static class Builder {
        private final FiltriDiRicerca filtriDiRicerca;

        public Builder() {
            filtriDiRicerca = new FiltriDiRicerca();
        }

        public Builder tipoCucina(String tipoCucina) {
            filtriDiRicerca.tipoCucina = tipoCucina;
            return this;
        }

        public Builder fasciaPrezzo(Integer fasciaPrezzo) {
            filtriDiRicerca.fasciaPrezzo = fasciaPrezzo;
            return this;
        }

        public Builder posizione(Double latitudine, Double longitudine, Integer distanzaMassima) {
            filtriDiRicerca.latitudineUtente = latitudine;
            filtriDiRicerca.longitudineUtente = longitudine;
            filtriDiRicerca.distanzaMassima = distanzaMassima;
            return this;
        }

        public Builder consegnaDomicilio(Boolean consegnaDomicilio) {
            filtriDiRicerca.consegnaDomicilio = consegnaDomicilio;
            return this;
        }

        public Builder apertoOra(Boolean apertoOra) {
            filtriDiRicerca.apertoOra = apertoOra;
            return this;
        }

        public FiltriDiRicerca build() {
            return filtriDiRicerca;
        }
    }

}
