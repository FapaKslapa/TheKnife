package services;

import example.models.Ristorante;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Servizio per l'importazione di dati da file CSV.
 * Questa classe fornisce metodi per leggere file CSV contenenti informazioni sui ristoranti
 * e caricarli nel sistema utilizzando i servizi esistenti.
 */
public class CSVImportService {

    private final RistoranteService ristoranteService;

    /**
     * Costruttore che inizializza il servizio con le dipendenze necessarie.
     *
     * @param ristoranteService Il servizio per la gestione dei ristoranti
     */
    public CSVImportService(RistoranteService ristoranteService) {
        this.ristoranteService = ristoranteService;
    }

    /**
     * Importa ristoranti da un file CSV nel sistema.
     * Il file deve contenere intestazioni corrispondenti ai campi del ristorante.
     *
     * @param csvFilePath Il percorso del file CSV da importare
     * @return Il numero di ristoranti importati con successo
     * @throws IOException Se si verifica un errore durante la lettura del file
     */
    public int importRistorantiFromCSV(String csvFilePath) throws IOException {
        Path path = Paths.get(csvFilePath);
        return importRistorantiFromCSV(Files.newInputStream(path));
    }

    /**
     * Importa ristoranti da un CSV fornito come InputStream.
     * Utile per leggere file dalle risorse dell'applicazione.
     *
     * @param inputStream Lo stream del file CSV
     * @return Il numero di ristoranti importati con successo
     * @throws IOException Se si verifica un errore durante la lettura del file
     */
    public int importRistorantiFromCSV(InputStream inputStream) throws IOException {
        int importedCount = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            // Leggi l'intestazione per determinare l'indice delle colonne
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return 0;
            }

            Map<String, Integer> columnIndices = parseHeader(headerLine);

            // Leggi le righe dei dati
            String line;
            while ((line = reader.readLine()) != null) {
                if (importRistoranteFromCSVLine(line, columnIndices)) {
                    importedCount++;
                }
            }
        }

        return importedCount;
    }

    /**
     * Analizza l'intestazione del CSV per determinare l'indice di ogni colonna.
     *
     * @param headerLine La riga di intestazione del CSV
     * @return Una mappa che associa i nomi delle colonne ai rispettivi indici
     */
    private Map<String, Integer> parseHeader(String headerLine) {
        String[] headers = headerLine.split(",");
        Map<String, Integer> columnIndices = new HashMap<>();

        for (int i = 0; i < headers.length; i++) {
            columnIndices.put(headers[i].trim(), i);
        }

        return columnIndices;
    }

    /**
     * Importa un singolo ristorante da una riga CSV.
     *
     * @param line La riga CSV contenente i dati del ristorante
     * @param columnIndices La mappa degli indici delle colonne
     * @return true se il ristorante è stato importato con successo, false altrimenti
     */
    private boolean importRistoranteFromCSVLine(String line, Map<String, Integer> columnIndices) {
        String[] values = splitCSVLine(line);

        try {
            // Estrai i valori dalle colonne
            String nome = getValueOrDefault(values, columnIndices, "Name", "");
            String indirizzo = getValueOrDefault(values, columnIndices, "Address", "");
            String tipoCucina = getValueOrDefault(values, columnIndices, "Cuisine", "");
            String prezzoStr = getValueOrDefault(values, columnIndices, "Price", "");

            // Gestisci fascia prezzo (converti in 1-3)
            int fasciaPrezzo = convertPriceToFasciaPrezzo(prezzoStr);

            // Estrai latitudine e longitudine
            double latitudine = parseDouble(getValueOrDefault(values, columnIndices, "Latitude", "0"));
            double longitudine = parseDouble(getValueOrDefault(values, columnIndices, "Longitude", "0"));

            // Estrai telefono
            String telefono = getValueOrDefault(values, columnIndices, "PhoneNumber", "");

            // Estrai descrizione
            String descrizione = getValueOrDefault(values, columnIndices, "Description", "");

            // Crea una mappa per gli orari di apertura predefiniti
            Map<String, String> orariApertura = createDefaultOrari();

            // Controlla se il ristorante già esiste nel sistema per evitare duplicati
            // Per semplicità, consideriamo duplicati i ristoranti con lo stesso nome e indirizzo
            boolean ristoranteEsistente = ristoranteService.getAllRistoranti().stream()
                    .anyMatch(r -> r.getNome().equalsIgnoreCase(nome) &&
                                  (r.getLatitudine() == latitudine && r.getLongitudine() == longitudine));

            if (!ristoranteEsistente) {
                // Usiamo "admin" come ID proprietario predefinito
                String idProprietario = "admin";

                // La consegna a domicilio è attiva di default
                boolean consegnaDomicilio = true;

                // Crea il ristorante
                ristoranteService.creaRistorante(
                    nome,
                    tipoCucina,
                    fasciaPrezzo,
                    orariApertura,
                    latitudine,
                    longitudine,
                    idProprietario,
                    telefono,
                    consegnaDomicilio
                );

                return true;
            }

            return false;
        } catch (Exception e) {
            System.err.println("Errore durante l'importazione della riga: " + line);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Divide correttamente una riga CSV gestendo le virgolette e le virgole all'interno dei campi.
     *
     * @param line La riga CSV da dividere
     * @return Un array di valori estratti dalla riga
     */
    private String[] splitCSVLine(String line) {
        // Implementazione semplificata per la divisione di una riga CSV
        // In una implementazione reale, dovresti gestire le virgolette e le virgole nei campi
        return line.split(",", -1);
    }

    /**
     * Ottiene un valore da un array di valori CSV in base all'indice della colonna.
     *
     * @param values L'array di valori della riga CSV
     * @param columnIndices La mappa degli indici delle colonne
     * @param columnName Il nome della colonna di cui ottenere il valore
     * @param defaultValue Il valore predefinito se la colonna non esiste o è vuota
     * @return Il valore della colonna o il valore predefinito
     */
    private String getValueOrDefault(String[] values, Map<String, Integer> columnIndices, String columnName, String defaultValue) {
        Integer index = columnIndices.get(columnName);
        if (index == null || index >= values.length) {
            return defaultValue;
        }

        String value = values[index].trim();
        // Rimuovi le virgolette se presenti
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }

        return value.isEmpty() ? defaultValue : value;
    }

    /**
     * Converte una stringa di prezzo (es. "€€€") in un valore numerico di fascia prezzo (1-3).
     *
     * @param price La stringa di prezzo da convertire
     * @return Un valore numerico rappresentante la fascia di prezzo (1-3)
     */
    private int convertPriceToFasciaPrezzo(String price) {
        if (price == null || price.isEmpty()) {
            return 2; // Fascia media come default
        }

        // Conta i simboli € o $ per determinare la fascia
        int count = 0;
        for (char c : price.toCharArray()) {
            if (c == '€' || c == '$') {
                count++;
            }
        }

        // Limita a 1-3
        return Math.min(Math.max(count, 1), 3);
    }

    /**
     * Converte una stringa in un valore double.
     *
     * @param value La stringa da convertire
     * @return Il valore double o 0 in caso di errore
     */
    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Crea una mappa di orari di apertura predefiniti per tutti i giorni della settimana.
     *
     * @return Una mappa con gli orari di apertura predefiniti
     */
    private Map<String, String> createDefaultOrari() {
        Map<String, String> orari = new LinkedHashMap<>();
        orari.put("lunedì", "12:00-15:00, 19:00-23:00");
        orari.put("martedì", "12:00-15:00, 19:00-23:00");
        orari.put("mercoledì", "12:00-15:00, 19:00-23:00");
        orari.put("giovedì", "12:00-15:00, 19:00-23:00");
        orari.put("venerdì", "12:00-15:00, 19:00-23:00");
        orari.put("sabato", "12:00-15:00, 19:00-23:00");
        orari.put("domenica", "12:00-15:00, 19:00-23:00");
        return orari;
    }
}
