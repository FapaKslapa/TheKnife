package com.example.cache;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.lang.reflect.*;

import com.example.utils.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * Repository per la gestione della persistenza delle entità su file JSON.
 * Fornisce operazioni CRUD (Create, Read, Update, Delete) per entità che
 * estendono BaseEntity, utilizzando un file JSON come storage.
 *
 * @param <T> tipo dell'entità che deve estendere BaseEntity
 * @author Stefano Marocco
 * @version 1.0
 */
public class JsonRepository<T extends BaseEntity> {
    /**
     * Istanza di Gson utilizzata per la serializzazione e deserializzazione JSON.
     */
    private final Gson gson;

    /**
     * Percorso del file JSON utilizzato per archiviare i dati.
     */
    private final String filePath;

    /**
     * Classe dell'entità gestita dal repository.
     */
    private final Class<T> entityClass;

    /**
     * Tipo parametrizzato utilizzato per la deserializzazione delle liste di entità.
     */
    private final Type listType;

    /**
     * Cache in memoria delle entità caricate dal file JSON.
     */
    private List<T> cache;

    /**
     * Costruttore che inizializza il repository per un tipo specifico di entità.
     * Configura Gson con gli adapter necessari e carica i dati dal file JSON specificato.
     *
     * @param filePath    percorso del file JSON dove verranno archiviate le entità
     * @param entityClass classe dell'entità da gestire nel repository
     */
    public JsonRepository(String filePath, Class<T> entityClass) {
        this.filePath = filePath;
        this.entityClass = entityClass;
        // Crea un tipo parametrizzato per la deserializzazione
        this.listType = com.google.gson.internal.$Gson$Types.newParameterizedTypeWithOwner(null, List.class, entityClass);
        // Configura Gson con l'adapter per LocalDateTime
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        // Carica i dati dal file JSON nella cache
        this.cache = loadData();
    }

    /**
     * Carica i dati dal file JSON nella cache in memoria.
     * Se il file non esiste o si verificano errori durante la lettura,
     * viene restituita una lista vuota.
     *
     * @return lista di entità caricate dal file JSON, o lista vuota in caso di errore
     */
    private List<T> loadData() {
        File file = new File(filePath);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (Reader reader = new FileReader(file)) {
            List<T> entities = gson.fromJson(reader, listType);
            return entities != null ? entities : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Errore durante il caricamento dei dati: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Salva la cache delle entità nel file JSON.
     * Questo metodo viene chiamato automaticamente dopo ogni operazione
     * che modifica lo stato della cache.
     */
    private void saveData() {
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(cache, writer);
        } catch (IOException e) {
            System.err.println("Errore durante il salvataggio dei dati: " + e.getMessage());
        }
    }

    /**
     * Recupera tutte le entità presenti nel repository.
     *
     * @return una nuova lista contenente tutte le entità
     */
    public List<T> findAll() {
        return new ArrayList<>(cache);
    }

    /**
     * Cerca un'entità per il suo identificatore univoco.
     *
     * @param id identificatore dell'entità da cercare
     * @return Optional contenente l'entità trovata, o Optional vuoto se non esiste
     */
    public Optional<T> findById(String id) {
        return cache.stream()
                .filter(entity -> entity.getId().equals(id))
                .findFirst();
    }

    /**
     * Salva un'entità nel repository. Se l'entità ha già un ID, viene aggiornata;
     * altrimenti viene creata una nuova entità con un ID generato.
     *
     * @param entity entità da salvare
     * @return l'entità salvata con ID assegnato (se nuovo)
     */
    public T save(T entity) {
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID().toString());
            cache.add(entity);
        } else {
            deleteById(entity.getId());
            cache.add(entity);
        }
        saveData();
        return entity;
    }

    /**
     * Elimina un'entità dal repository in base al suo ID.
     *
     * @param id identificatore dell'entità da eliminare
     */
    public void deleteById(String id) {
        cache.removeIf(entity -> entity.getId().equals(id));
        saveData();
    }
}