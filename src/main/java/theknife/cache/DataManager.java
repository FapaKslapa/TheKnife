package theknife.cache;
// Marocco Stefano 762192 VA
// Marin Marco 760622 VA
// Gerti Alessia 762405 VA
import java.io.*;
import java.util.*;

/**
 * Gestore centralizzato dei dati dell'applicazione implementato come singleton.
 * Questa classe mantiene e coordina tutti i repository di entità e relazioni,
 * fornendo un punto di accesso unico per le operazioni di persistenza.
 */
public class DataManager {
    /**
     * Istanza singleton del DataManager.
     */
    private static DataManager instance;

    /**
     * Mappa che associa ogni classe di entità al suo repository JSON.
     */
    private final Map<Class<?>, JsonRepository<?>> repositories = new HashMap<>();

    /**
     * Mappa che associa ogni nome di relazione al suo repository di relazioni.
     */
    private final Map<String, RelationRepository> relationRepositories = new HashMap<>();

    /**
     * Costruttore privato che inizializza la struttura dei dati.
     * Crea la directory dei dati se non esiste e registra i repository
     * di relazioni standard utilizzati dall'applicazione.
     */
    private DataManager() {
        // Assicurati che la directory dati esista
        new File("data").mkdirs();

        // Inizializza i repository di relazioni
        registerRelationRepository("utenti_ristoranti", "data/utenti_ristoranti.json");
        registerRelationRepository("utenti_preferiti", "data/utenti_preferiti.json");
    }

    /**
     * Registra un nuovo repository di entità nel sistema.
     * Crea un JsonRepository per la classe di entità specificata e lo associa
     * al percorso del file di archiviazione.
     *
     * @param <T>         tipo dell'entità che estende BaseEntity
     * @param entityClass classe dell'entità da registrare
     * @param filePath    percorso del file JSON dove verranno archiviate le entità
     */
    public <T extends BaseEntity> void registerEntityRepository(Class<T> entityClass, String filePath) {
        repositories.put(entityClass, new JsonRepository<>(filePath, entityClass));
    }

    /**
     * Registra un nuovo repository di relazioni nel sistema.
     * Crea un RelationRepository con il nome specificato e lo associa
     * al percorso del file di archiviazione.
     *
     * @param name     nome identificativo del repository di relazioni
     * @param filePath percorso del file JSON dove verranno archiviate le relazioni
     */
    public void registerRelationRepository(String name, String filePath) {
        relationRepositories.put(name, new RelationRepository(filePath));
    }

    /**
     * Ottiene il repository associato alla classe di entità specificata.
     *
     * @param <T>         tipo dell'entità che estende BaseEntity
     * @param entityClass classe dell'entità di cui ottenere il repository
     * @return repository JSON per la classe di entità specificata
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseEntity> JsonRepository<T> getRepository(Class<T> entityClass) {
        return (JsonRepository<T>) repositories.get(entityClass);
    }

    /**
     * Ottiene il repository di relazioni con il nome specificato.
     *
     * @param name nome identificativo del repository di relazioni
     * @return repository di relazioni richiesto
     */
    public RelationRepository getRelationRepository(String name) {
        return relationRepositories.get(name);
    }

    /**
     * Ottiene l'istanza singleton del DataManager.
     * Se l'istanza non esiste, viene creata.
     *
     * @return l'istanza singleton del DataManager
     */
    public static synchronized DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }
}