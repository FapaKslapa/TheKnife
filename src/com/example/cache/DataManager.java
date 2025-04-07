package com.example.cache;

import java.io.*;
import java.util.*;

public class DataManager {
    private static DataManager instance;
    private final Map<Class<?>, JsonRepository<?>> repositories = new HashMap<>();
    private final Map<String, RelationRepository> relationRepositories = new HashMap<>();

    private DataManager() {
        // Assicurati che la directory dati esista
        new File("data").mkdirs();

        // Inizializza i repository standard
        //registerEntityRepository(Utente.class, "data/utenti.json");
        //registerEntityRepository(Ristorante.class, "data/ristoranti.json");
        //registerEntityRepository(Recensione.class, "data/recensioni.json");
        // Aggiungi altre entità...

        // Inizializza i repository di relazioni
        registerRelationRepository("utenti_ristoranti", "data/utenti_ristoranti.json");
        registerRelationRepository("utenti_preferiti", "data/utenti_preferiti.json");
        // Aggiungi altre relazioni...
    }

    // Registra un nuovo repository di entità
    public <T extends BaseEntity> void registerEntityRepository(Class<T> entityClass, String filePath) {
        repositories.put(entityClass, new JsonRepository<>(filePath, entityClass));
    }

    // Registra un nuovo repository di relazioni
    public void registerRelationRepository(String name, String filePath) {
        relationRepositories.put(name, new RelationRepository(filePath));
    }

    // Ottieni un repository per una classe specifica
    @SuppressWarnings("unchecked")
    public <T extends BaseEntity> JsonRepository<T> getRepository(Class<T> entityClass) {
        return (JsonRepository<T>) repositories.get(entityClass);
    }

    // Ottieni un repository di relazioni per nome
    public RelationRepository getRelationRepository(String name) {
        return relationRepositories.get(name);
    }

    // Singleton getter
    public static synchronized DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }
}