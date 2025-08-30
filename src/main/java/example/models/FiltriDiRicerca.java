package example.models;

    /** Classe che costituisce i filtri di ricerca per i ristoranti.
     * Tra questi: tipo di cucina, fascia di prezzo, posizione dell'utente, distanza del locale o
     * disponibilità del servizio.
     * @author Ginevra
     * @version 1.0
     */

public class FiltriDiRicerca {
    private String tipoCucina;          //Tipo di cucina desiderato (es. Cinese, Italiano...)
    private Integer fasciaPrezzo;       //Fascia di prezzo (Economico, Medio, Costoso)
    private Double latitudineUtente;    //Latitudine dell'utente
    private Double longitudineUtente;   //Longitudine dell'utente
    private Integer distanzaMassima;    //Massima distanza tra l'utente e il locale
    private Boolean consegnaDomicilio;  //Disponibilità del servizio di consegna a domicilio
    private Boolean apertoOra;          //Indica se il locale è aperto o meno


    private FiltriDiRicerca() {

    }

    //Metodi get per ogni campo

    /** Metodo get che restituisce il tipo di cucina
    * @return Tipo di cucina scelto dall'utente
    */
    public String getTipoCucina() {
        return tipoCucina;
    }

    /** Metodo get che restituisce la fascia di prezzo desiderata
    * @return   La fascia di prezzo selezionata
    */
    public Integer getFasciaPrezzo() {
        return fasciaPrezzo;
    }

    /** Metodo get che restituisce la latitudine dell'utente
    * @return   La latitudine geografica dell'utente
    */
    public Double getLatitudineUtente() {
        return latitudineUtente;
    }

    /** Metodo get che restituisce la longitudine del'utente
    * @return   La longitudine geografica dell'utente
    */
    public Double getLongitudineUtente() {
        return longitudineUtente;
    }

    /** Metodo get che restituisce la distanza massima del locale dall'utente
    * @return   La massima distanza dall'utente
    */
    public Integer getDistanzaMassima() {
        return distanzaMassima;
    }

    /** Metodo get che restituisce la disponibilità della consegna a domicilio
    * @return   True se la consegna a domicilio è disponibile, false altrimenti
    */
    public Boolean getConsegnaDomicilio() {
        return consegnaDomicilio;
    }

    /** Metodo get che restituisce lo stato del locale
    * @return   True se è aperto, false altrimenti
    */
    public Boolean getApertoOra() {
        return apertoOra;
    }

    /** Classe annidata che implementa il pattern Builder,
     * serve per costruire oggetti della classe FiltriDiRicerca attraverso
     * costruttori controllati
     *
     * @author Ginevra
     * @version 1.0
     */

    public static class Builder {
        private final FiltriDiRicerca filtriDiRicerca;

        public Builder() {
            filtriDiRicerca = new FiltriDiRicerca();
        }


        /**
         * Imposta il tipo di cucina desiderato.
         * @param tipoCucina    Tipo di cucina selezionato
         * @return  this
         */
        public Builder tipoCucina(String tipoCucina) {
            filtriDiRicerca.tipoCucina = tipoCucina;
            return this;
        }


        /**
         * Imposta la fascia di prezzo desiderata.
         * @param fasciaPrezzo      Fascia di prezzo del ristorante
         * @return  this
         */
        public Builder fasciaPrezzo(Integer fasciaPrezzo) {
            filtriDiRicerca.fasciaPrezzo = fasciaPrezzo;
            return this;
        }


        /**
         * Imposta la locazione geografica dell'utente e la distanza massima dalla sua posizione.
         * @param latitudine        Latitudine geografica dell'utente
         * @param longitudine       Longitudine geografica dell'utente
         * @param distanzaMassima   Massima distanza tra l'utente e il ristorante
         * @return this
         */
        public Builder posizione(Double latitudine, Double longitudine, Integer distanzaMassima) {
            filtriDiRicerca.latitudineUtente = latitudine;
            filtriDiRicerca.longitudineUtente = longitudine;
            filtriDiRicerca.distanzaMassima = distanzaMassima;
            return this;
        }


        /**
         * Specifica se si vuole la consegna a domicilio.
         * @param consegnaDomicilio     Disponibilità del servizio
         * @return  this
         */
        public Builder consegnaDomicilio(Boolean consegnaDomicilio) {
            filtriDiRicerca.consegnaDomicilio = consegnaDomicilio;
            return this;
        }


        /**
         * Determina se il locale dev'essere aperto al momento della ricerca.
         * @param apertoOra     Stato del locale
         * @return  this
         */
        public Builder apertoOra(Boolean apertoOra) {
            filtriDiRicerca.apertoOra = apertoOra;
            return this;
        }


        /**
         * Metodo che builda l'istanza di FiltriDiRicerca
         * @return  L'istanza stessa di filtriDiRicerca
         */
        public FiltriDiRicerca build() {
            return filtriDiRicerca;
        }
    }

}
