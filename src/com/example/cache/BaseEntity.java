package com.example.cache;

/**
 * Classe base per tutte le entità persistenti del sistema.
 * Fornisce un identificatore univoco e i relativi metodi di accesso.
 * Tutte le entità che necessitano di persistenza dovrebbero estendere questa classe.
 *
 * @author Stefano Marocco
 * @version 1.0
 */
public class BaseEntity {
    /**
     * Identificatore univoco dell'entità.
     */
    private String id;

    /**
     * Restituisce l'identificatore univoco dell'entità.
     *
     * @return l'identificatore dell'entità
     */
    public String getId() {
        return id;
    }

    /**
     * Imposta l'identificatore univoco dell'entità.
     *
     * @param id il nuovo identificatore da assegnare all'entità
     */
    public void setId(String id) {
        this.id = id;
    }
}