// Marocco Stefano 762192 VA
// Marin Marco 760622 VA
// Gerti Alessia 762405 VA
package services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import theknife.models.Ristorante;
import theknife.models.Utente;
import theknife.models.Utente.Ruolo;

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
        // Controlla e crea i file di persistenza se non esistono
        createFileIfNotExists("data/ristoranti.json", "[]");
        createFileIfNotExists("data/utenti.json", "[]");
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
        // Controlla e crea i file di persistenza se non esistono
        createFileIfNotExists("data/ristoranti.json", "[]");
        createFileIfNotExists("data/utenti.json", "[]");
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
        String[] headers = splitCSVLine(headerLine);  // Usa lo stesso metodo di split
        Map<String, Integer> columnIndices = new HashMap<>();

        for (int i = 0; i < headers.length; i++) {
            String header = headers[i].trim();
            // Rimuovi le virgolette se presenti
            if (header.startsWith("\"") && header.endsWith("\"")) {
                header = header.substring(1, header.length() - 1);
            }
            columnIndices.put(header, i);
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
            // Usa i nomi delle colonne esatte come sono nel JSON
            String nome = getValueOrDefault(values, columnIndices, "nome", "");
            String tipoCucina = getValueOrDefault(values, columnIndices, "tipoCucina", "");
            String prezzoStr = getValueOrDefault(values, columnIndices, "fasciaPrezzo", "2");
            int fasciaPrezzo = parseInteger(prezzoStr);
            double latitudine = parseDouble(getValueOrDefault(values, columnIndices, "latitudine", "0"));
            double longitudine = parseDouble(getValueOrDefault(values, columnIndices, "longitudine", "0"));
            String telefono = getValueOrDefault(values, columnIndices, "numeroTelefono", "");
            boolean consegnaDomicilio = getValueOrDefault(values, columnIndices, "consegnaDomicilio", "false").equalsIgnoreCase("true");
            String idProprietario = getValueOrDefault(values, columnIndices, "idProprietario", "r1-1111-1111-1111-111111111111");
            String id = getValueOrDefault(values, columnIndices, "id", "");

            Map<String, String> orariApertura = createDefaultOrari();

            // Controlla duplicati su ID o nome e coordinate
            boolean ristoranteEsistente = ristoranteService.getAllRistoranti().stream()
                    .anyMatch(r -> (id != null && !id.isEmpty() && r.getId().equals(id)) ||
                            (r.getNome().equalsIgnoreCase(nome) &&
                                    r.getLatitudine() == latitudine && r.getLongitudine() == longitudine));

            if (!ristoranteEsistente && !nome.isEmpty()) {
                Ristorante ristorante = ristoranteService.creaRistorante(
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

                // Imposta l'ID specifico se fornito
                if (id != null && !id.isEmpty()) {
                    ristorante.setId(id);
                }

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
     * Divide correttamente una riga CSV gestendo le virgolette, le virgole e le colonne vuote.
     */
    private String[] splitCSVLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Due virgolette consecutive = una virgoletta escaped
                    sb.append('"');
                    i++; // Salta la prossima virgoletta
                } else {
                    // Cambia stato delle virgolette
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        result.add(sb.toString());
        return result.toArray(new String[0]);
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

        // Se è già un numero, restituiscilo
        try {
            return Integer.parseInt(price.trim());
        } catch (NumberFormatException e) {
            // Se non è un numero, conta i simboli € o $
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
     * Converte una stringa in un valore integer.
     *
     * @param value La stringa da convertire
     * @return Il valore integer o 1 in caso di errore
     */
    private int parseInteger(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    /**
     * Converte una stringa in un valore double.
     *
     * @param value La stringa da convertire
     * @return Il valore double o 0 in caso di errore
     */
    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value.trim());
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

    /**
     * Crea un file con contenuto di default se non esiste già.
     * @param filePath percorso del file
     * @param defaultContent contenuto da scrivere se il file non esiste
     */
    private void createFileIfNotExists(String filePath, String defaultContent) {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                // Crea le directory se non esistono
                file.getParentFile().mkdirs();
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(defaultContent.getBytes(StandardCharsets.UTF_8));
                }
            } catch (IOException e) {
                System.err.println("Impossibile creare il file: " + filePath);
                e.printStackTrace();
            }
        }
    }

    /**
     * Importa sia utenti che ristoranti da un unico file CSV.
     * Il file deve avere la colonna 'Tipo' (UTENTE o RISTORANTE) e tutte le colonne necessarie.
     * @param csvFilePath percorso del file CSV
     * @return array: [utentiImportati, ristorantiImportati]
     * @throws IOException in caso di errore
     */
    public int[] importUnicoCSV(String csvFilePath) throws IOException {
        createFileIfNotExists("data/utenti.json", "[]");
        createFileIfNotExists("data/ristoranti.json", "[]");

        Path path = Paths.get(csvFilePath);
        List<Utente> utenti = new ArrayList<>();
        List<Ristorante> ristoranti = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) return new int[]{0, 0};

            Map<String, Integer> columnIndices = parseHeader(headerLine);
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] values = splitCSVLine(line);
                String tipo = getValueOrDefault(values, columnIndices, "Tipo", "");

                if (tipo.equalsIgnoreCase("UTENTE")) {
                    try {
                        String id = getValueOrDefault(values, columnIndices, "Id", "");
                        String username = getValueOrDefault(values, columnIndices, "Username", "");
                        String password = getValueOrDefault(values, columnIndices, "Password", "");
                        String email = getValueOrDefault(values, columnIndices, "Email", "");
                        String ruoloStr = getValueOrDefault(values, columnIndices, "Ruolo", "UTENTE");
                        Ruolo ruolo = ruoloStr.equalsIgnoreCase("RISTORATORE") ? Ruolo.RISTORATORE : Ruolo.UTENTE;

                        if (!id.isEmpty() && !username.isEmpty()) {
                            Utente utente = new Utente(username, password, email, ruolo);
                            utente.setId(id);

                            // Evita duplicati
                            boolean exists = utenti.stream().anyMatch(u -> u.getId().equals(id));
                            if (!exists) {
                                utenti.add(utente);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Errore importando utente dalla riga: " + line);
                        e.printStackTrace();
                    }
                } else if (tipo.equalsIgnoreCase("RISTORANTE")) {
                    try {
                        String id = getValueOrDefault(values, columnIndices, "Id", "");
                        String nome = getValueOrDefault(values, columnIndices, "Nome", "");
                        String tipoCucina = getValueOrDefault(values, columnIndices, "Cucina", "");
                        String prezzoStr = getValueOrDefault(values, columnIndices, "Prezzo", "€€");
                        int fasciaPrezzo = convertPriceToFasciaPrezzo(prezzoStr);
                        double latitudine = parseDouble(getValueOrDefault(values, columnIndices, "Latitudine", "0"));
                        double longitudine = parseDouble(getValueOrDefault(values, columnIndices, "Longitudine", "0"));
                        String numeroTelefono = getValueOrDefault(values, columnIndices, "Telefono", "");
                        boolean consegnaDomicilio = getValueOrDefault(values, columnIndices, "ConsegnaDomicilio", "false").equalsIgnoreCase("true");
                        String idProprietario = getValueOrDefault(values, columnIndices, "IdProprietario", "");

                        // Se non c'è un proprietario specifico, assegna casualmente uno dei ristoratori disponibili
                        if (idProprietario.isEmpty()) {
                            List<Utente> ristoratori = utenti.stream()
                                    .filter(u -> u.getRuolo() == Ruolo.RISTORATORE)
                                    .toList();

                            if (!ristoratori.isEmpty()) {
                                // Assegna casualmente uno dei ristoratori disponibili
                                idProprietario = ristoratori.get(new Random().nextInt(ristoratori.size())).getId();
                            } else {
                                // Se non ci sono ristoratori, usa il valore predefinito
                                idProprietario = "r1-1111-1111-1111-111111111111";
                            }
                        }

                        if (!id.isEmpty() && !nome.isEmpty()) {
                            Map<String, String> orariApertura = createDefaultOrari();
                            Ristorante ristorante = new Ristorante(nome, tipoCucina, fasciaPrezzo, orariApertura,
                                    latitudine, longitudine, idProprietario,
                                    numeroTelefono, consegnaDomicilio);
                            ristorante.setId(id);

                            // Evita duplicati
                            boolean exists = ristoranti.stream().anyMatch(r -> r.getId().equals(id));
                            if (!exists) {
                                ristoranti.add(ristorante);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Errore importando ristorante dalla riga: " + line);
                        e.printStackTrace();
                    }
                }
            }
        }

        // Serializza con Gson
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Files.write(Paths.get("data/utenti.json"), gson.toJson(utenti).getBytes(StandardCharsets.UTF_8));
        Files.write(Paths.get("data/ristoranti.json"), gson.toJson(ristoranti).getBytes(StandardCharsets.UTF_8));

        return new int[]{utenti.size(), ristoranti.size()};
    }

    /**
     * Importa utenti e ristoranti dai rispettivi file CSV separati.
     * Questo metodo è più semplice e meno soggetto a errori rispetto all'importazione da un file unico.
     * @return array: [utentiImportati, ristorantiImportati]
     * @throws IOException in caso di errore
     */
    public int[] importFromSeparateCSVs() throws IOException {
        createFileIfNotExists("data/utenti.json", "[]");
        createFileIfNotExists("data/ristoranti.json", "[]");

        List<Utente> utenti = new ArrayList<>();
        List<Ristorante> ristoranti = new ArrayList<>();

        // Importa utenti
        try (InputStream utentiStream = getClass().getResourceAsStream("/data/utenti.csv")) {
            if (utentiStream != null) {
                System.out.println("Importazione utenti dal CSV...");
                utenti = importUtentiFromCSV(utentiStream);
                System.out.println("Importati " + utenti.size() + " utenti.");
            } else {
                System.err.println("File utenti.csv non trovato nelle risorse!");
            }
        }

        // Importa ristoranti
        try (InputStream ristorantiStream = getClass().getResourceAsStream("/data/ristoranti.csv")) {
            if (ristorantiStream != null) {
                System.out.println("Importazione ristoranti dal CSV...");
                ristoranti = importRistorantiFromCSV(ristorantiStream, utenti);
                System.out.println("Importati " + ristoranti.size() + " ristoranti.");
            } else {
                System.err.println("File ristoranti.csv non trovato nelle risorse!");
            }
        }

        // Serializza con Gson
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Files.write(Paths.get("data/utenti.json"), gson.toJson(utenti).getBytes(StandardCharsets.UTF_8));
        Files.write(Paths.get("data/ristoranti.json"), gson.toJson(ristoranti).getBytes(StandardCharsets.UTF_8));

        return new int[]{utenti.size(), ristoranti.size()};
    }

    /**
     * Importa utenti da un file CSV specifico
     */
    private List<Utente> importUtentiFromCSV(InputStream inputStream) throws IOException {
        List<Utente> utenti = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) return utenti;

            Map<String, Integer> columnIndices = parseHeader(headerLine);
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                try {
                    String[] values = splitCSVLine(line);
                    String id = getValueOrDefault(values, columnIndices, "Id", "");
                    String username = getValueOrDefault(values, columnIndices, "Username", "");
                    String password = getValueOrDefault(values, columnIndices, "Password", "");
                    String email = getValueOrDefault(values, columnIndices, "Email", "");
                    String ruoloStr = getValueOrDefault(values, columnIndices, "Ruolo", "UTENTE");
                    Ruolo ruolo = ruoloStr.equalsIgnoreCase("RISTORATORE") ? Ruolo.RISTORATORE : Ruolo.UTENTE;

                    if (!id.isEmpty() && !username.isEmpty()) {
                        Utente utente = new Utente(username, password, email, ruolo);
                        utente.setId(id);
                        utenti.add(utente);
                    }
                } catch (Exception e) {
                    System.err.println("Errore importando utente dalla riga: " + line);
                    e.printStackTrace();
                }
            }
        }

        return utenti;
    }

    /**
     * Importa ristoranti da un file CSV specifico con assegnazione casuale dei proprietari
     */
    private List<Ristorante> importRistorantiFromCSV(InputStream inputStream, List<Utente> utenti) throws IOException {
        List<Ristorante> ristoranti = new ArrayList<>();
        List<Utente> ristoratori = utenti.stream()
                .filter(u -> u.getRuolo() == Ruolo.RISTORATORE)
                .toList();

        Random random = new Random();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) return ristoranti;

            Map<String, Integer> columnIndices = parseHeader(headerLine);
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                try {
                    String[] values = splitCSVLine(line);
                    String id = getValueOrDefault(values, columnIndices, "Id", "");
                    String nome = getValueOrDefault(values, columnIndices, "Nome", "");
                    String tipoCucina = getValueOrDefault(values, columnIndices, "TipoCucina", "");
                    int fasciaPrezzo = parseInteger(getValueOrDefault(values, columnIndices, "FasciaPrezzo", "2"));
                    double latitudine = parseDouble(getValueOrDefault(values, columnIndices, "Latitudine", "0"));
                    double longitudine = parseDouble(getValueOrDefault(values, columnIndices, "Longitudine", "0"));
                    String numeroTelefono = getValueOrDefault(values, columnIndices, "Telefono", "");
                    boolean consegnaDomicilio = getValueOrDefault(values, columnIndices, "ConsegnaDomicilio", "false").equalsIgnoreCase("true");

                    // Assegna casualmente uno dei ristoratori disponibili
                    String idProprietario = "r1-1111-1111-1111-111111111111"; // default
                    if (!ristoratori.isEmpty()) {
                        idProprietario = ristoratori.get(random.nextInt(ristoratori.size())).getId();
                    }

                    if (!id.isEmpty() && !nome.isEmpty()) {
                        Map<String, String> orariApertura = createDefaultOrari();
                        Ristorante ristorante = new Ristorante(nome, tipoCucina, fasciaPrezzo, orariApertura,
                                latitudine, longitudine, idProprietario,
                                numeroTelefono, consegnaDomicilio);
                        ristorante.setId(id);
                        ristoranti.add(ristorante);
                    }
                } catch (Exception e) {
                    System.err.println("Errore importando ristorante dalla riga: " + line);
                    e.printStackTrace();
                }
            }
        }

        return ristoranti;
    }

    // Metodi di utilità per compatibilità (rimossi per brevità ma possono essere mantenuti se necessari)
}

