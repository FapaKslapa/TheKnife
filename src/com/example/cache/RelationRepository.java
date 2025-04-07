package com.example.cache;


import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.*;
import java.lang.reflect.*;
import java.util.*;

public class RelationRepository {
    private final Gson gson;
    private final String filePath;
    private Map<String, List<String>> relations;

    public RelationRepository(String filePath) {
        this.filePath = filePath;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.relations = loadRelations();
    }

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

    private void saveRelations() {
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(relations, writer);
        } catch (IOException e) {
            System.err.println("Errore durante il salvataggio delle relazioni: " + e.getMessage());
        }
    }

    // Aggiungi relazione
    public void addRelation(String entityId, String relatedEntityId) {
        relations.computeIfAbsent(entityId, k -> new ArrayList<>()).add(relatedEntityId);
        saveRelations();
    }

    // Rimuovi relazione
    public void removeRelation(String entityId, String relatedEntityId) {
        if (relations.containsKey(entityId)) {
            relations.get(entityId).remove(relatedEntityId);
            saveRelations();
        }
    }

    // Trova tutte le relazioni
    public List<String> findRelatedIds(String entityId) {
        return relations.getOrDefault(entityId, new ArrayList<>());
    }
}