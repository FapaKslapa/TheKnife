package com.example.cache;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class JsonRepository<T extends BaseEntity> {
    private final Gson gson;
    private final String filePath;
    private final Class<T> entityClass;
    private final Type listType;
    private List<T> cache;

    public JsonRepository(String filePath, Class<T> entityClass) {
        this.filePath = filePath;
        this.entityClass = entityClass;
        this.listType = TypeToken.getParameterized(List.class, entityClass).getType();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.cache = loadData();
    }

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

    private void saveData() {
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(cache, writer);
        } catch (IOException e) {
            System.err.println("Errore durante il salvataggio dei dati: " + e.getMessage());
        }
    }

    // CRUD Operations
    public List<T> findAll() {
        return new ArrayList<>(cache);
    }

    public Optional<T> findById(String id) {
        return cache.stream()
                .filter(entity -> entity.getId().equals(id))
                .findFirst();
    }

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

    public void deleteById(String id) {
        cache.removeIf(entity -> entity.getId().equals(id));
        saveData();
    }
}