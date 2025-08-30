package example.cache;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * Repository per la gestione delle relazioni tra entità, utilizzando un file JSON come storage.
 * Permette di memorizzare, recuperare e gestire relazioni many-to-many tra le entità
 * del sistema, come per esempio preferiti, likes o associazioni tra utenti e risorse.
 *
 * @author Stefano Marocco
 * @version 1.0
 */
public class RelationRepository {
    /**
     * Istanza di Gson utilizzata per la serializzazione e deserializzazione JSON.
     */
    private final Gson gson;

    /**
     * Percorso del file JSON utilizzato per archiviare le relazioni.
     */
    private final String filePath;

    /**
     * Mappa in memoria che associa l'ID di un'entità con una lista di ID di entità correlate.
     */
    private Map<String, List<String>> relations;

    /**
     * Costruttore che inizializza il repository di relazioni.
     * Configura Gson e carica le relazioni esistenti dal file JSON specificato.
     *
     * @param filePath percorso del file JSON dove verranno archiviate le relazioni
     */
    public RelationRepository(String filePath) {
        this.filePath = filePath;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.relations = loadRelations();
    }

    /**
     * Carica le relazioni dal file JSON nella mappa in memoria.
     * Se il file non esiste o si verificano errori durante la lettura,
     * viene restituita una mappa vuota.
     *
     * @return mappa contenente le relazioni caricate dal file JSON, o mappa vuota in caso di errore
     */
    private Map<String, List<String>> loadRelations() {
        File file = new File(filePath);
        if (!file.exists()) {
            return new HashMap<>();
        }

        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, List<String>>>() {
            }.getType();
            Map<String, List<String>> data = gson.fromJson(reader, type);
            return data != null ? data : new HashMap<>();
        } catch (IOException e) {
            System.err.println("Errore durante il caricamento delle relazioni: " + e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Salva la mappa delle relazioni nel file JSON.
     * Questo metodo viene chiamato automaticamente dopo ogni operazione
     * che modifica lo stato della mappa delle relazioni.
     */
    private void saveRelations() {
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(relations, writer);
        } catch (IOException e) {
            System.err.println("Errore durante il salvataggio delle relazioni: " + e.getMessage());
        }
    }

    /**
     * Aggiunge una relazione tra due entità.
     * Associa l'ID dell'entità correlata all'ID dell'entità principale.
     *
     * @param entityId        ID dell'entità principale
     * @param relatedEntityId ID dell'entità correlata da associare
     */
    public void addRelation(String entityId, String relatedEntityId) {
        relations.computeIfAbsent(entityId, k -> new ArrayList<>()).add(relatedEntityId);
        saveRelations();
    }

    /**
     * Rimuove una relazione tra due entità.
     * Elimina l'associazione tra l'ID dell'entità principale e l'ID dell'entità correlata.
     *
     * @param entityId        ID dell'entità principale
     * @param relatedEntityId ID dell'entità correlata da dissociare
     */
    public void removeRelation(String entityId, String relatedEntityId) {
        if (relations.containsKey(entityId)) {
            relations.get(entityId).remove(relatedEntityId);
            saveRelations();
        }
    }

    /**
     * Recupera tutti gli ID delle entità correlate ad un'entità specifica.
     *
     * @param entityId ID dell'entità principale di cui trovare le relazioni
     * @return lista degli ID delle entità correlate, o lista vuota se non ci sono relazioni
     */
    public List<String> findRelatedIds(String entityId) {
        return relations.getOrDefault(entityId, new ArrayList<>());
    }

    /**
     * Conta quante entità sono collegate a un certo relatedEntityId.
     *
     * @param relatedEntityId ID dell'entità correlata
     * @return numero di entità che hanno una relazione con relatedEntityId
     */
    public int countRelationsTo(String relatedEntityId) {
        int count = 0;
        for (List<String> relatedIds : relations.values()) {
            if (relatedIds.contains(relatedEntityId)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Trova tutti gli entityId che sono collegati a relatedEntityId.
     *
     * @param relatedEntityId ID dell'entità correlata
     * @return lista degli entityId che hanno una relazione con relatedEntityId
     */
    public List<String> findEntitiesByRelatedId(String relatedEntityId) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : relations.entrySet()) {
            if (entry.getValue().contains(relatedEntityId)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /**
     * Salva le relazioni su file (implementazione tipica)
     */
    public void save() {
        // Salva le relazioni su file (implementazione tipica)
        try (java.io.FileWriter writer = new java.io.FileWriter(this.filePath)) {
            new com.google.gson.Gson().toJson(this.relations, writer);
        } catch (Exception e) {
            System.err.println("Errore nel salvataggio delle relazioni: " + e.getMessage());
        }
    }
}