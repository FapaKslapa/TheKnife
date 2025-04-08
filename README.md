# TheKnife

Laboratorio A - Insubria 2024/2025
Per far runnare il codice va inserita questa VM option:

```bash
--module-path
"PathToProject/TheKnife/lib/PathToCorrectJavaFx/lib"
--add-modules
javafx.controls,javafx.fxml,javafx.web
--add-exports=javafx.web/com.sun.webkit=ALL-UNNAMED
--add-exports=javafx.web/com.sun.javafx.webkit=ALL-UNNAMED
--enable-native-access=javafx.graphics,javafx.web
```

## 1. Introduzione al Bridge

Questo progetto utilizza un bridge personalizzato che permette la comunicazione bidirezionale tra JavaScript (lato
frontend) e Java (lato backend) all'interno di una WebView JavaFX.

## 2. Cos'è JSON

JSON (JavaScript Object Notation) è un formato di scambio dati leggero e indipendente dal linguaggio. È basato sulla
sintassi degli oggetti JavaScript ed è facile da leggere e scrivere per gli umani, ma anche semplice da generare e
analizzare per le macchine.

Esempio di documento JSON:

```json
{
  "nome": "Mario",
  "età": 30,
  "indirizzo": {
    "via": "Via Roma 123",
    "città": "Milano"
  },
  "numeriTelefono": [
    "+39 123456789",
    "+39 987654321"
  ]
}
```

## 3. Come funziona la libreria GSON

GSON è una libreria Java sviluppata da Google che consente di convertire oggetti Java in formato JSON e viceversa. Nel
nostro progetto, utilizziamo GSON per:

- **Serializzazione**: convertire oggetti Java in stringhe JSON
- **Deserializzazione**: convertire stringhe JSON in oggetti Java

Esempio di serializzazione:

```java
Gson gson = new Gson();
Map<String, Object> dati = Map.of("nome", "Mario", "età", 30);
String jsonString = gson.toJson(dati);  // Risultato: {"nome":"Mario","età":30}
```

Esempio di deserializzazione:

```java
Gson gson = new Gson();
String jsonString = "{\"nome\":\"Mario\",\"età\":30}";
Map<String, Object> dati = gson.fromJson(jsonString, Map.class);
// dati contiene ora: {nome=Mario, età=30}
```

## 4. Come registrare metodi nel Bridge

Il nostro `JavaScriptBridge` permette di registrare metodi Java che possono essere chiamati da JavaScript. Per
registrare un nuovo metodo:

```java
// Nel Main.java, dopo aver creato il bridge
bridge.registerMethod("nomeFunzione",args ->{
        // Elaborazione dei parametri ricevuti
        // args è una stringa JSON con i parametri

        // Restituzione del risultato come Map
        return Map.

of("chiave1","valore1","chiave2",42);
});
```

## 5. Come gestire parametri e risultati

### Ricevere parametri da JavaScript

Quando JavaScript chiama un metodo Java, i parametri vengono passati come JSON. In Java:

```java
bridge.registerMethod("calcolaArea",args ->{
// args è una stringa JSON come: {"base": 10, "altezza": 5}

// Deserializza i parametri in una Map
Map<String, Object> parametri = gson.fromJson(args, Map.class);

// Estrai i valori dalla Map
double base = ((Number) parametri.get("base")).doubleValue();
double altezza = ((Number) parametri.get("altezza")).doubleValue();

// Calcola il risultato
double area = base * altezza;

// Restituisci il risultato come Map
    return Map.

of("area",area);
});
```

### Chiamare il metodo da JavaScript

Nel codice HTML/JavaScript, puoi chiamare la funzione così:

```javascript
// Chiama il metodo Java e passa i parametri come oggetto JS
const risultato = window.javaConnector.calcolaArea({base: 10, altezza: 5});

// Il risultato è un oggetto JavaScript: {area: 50}
console.log("L'area è: " + risultato.area);
```

### Gestire tipi di dato complessi

Per strutture dati più complesse:

```java
bridge.registerMethod("elaboraDati",args ->{
Map<String, Object> parametri = gson.fromJson(args, Map.class);

// Accesso a array
List<Object> elenco = (List<Object>) parametri.get("elementi");

// Accesso a oggetti annidati
Map<String, Object> persona = (Map<String, Object>) parametri.get("persona");
String nome = (String) persona.get("nome");

// Restituisci risultati complessi
Map<String, Object> risultato = new HashMap<>();
    risultato.

put("stato","completato");
    risultato.

put("totaleElementi",elenco.size());
        risultato.

put("dettagli",Map.of("processoCompleto", true,"tempoElaborazione",0.5));

        return risultato;
});
```

## 6. Best practices e gestione della memoria

Usiamo il package Cache per definire un sistema di memoria ottimizzato e persistente:

L'accesso ai file è gestito attraverso DataManager e JsonRepository, come funziona:

```java
public class Ristorante extends BaseEntity {
    private String nome;
    private String indirizzo;
    // altri campi, getter e setter
}
```

Estendendo Base Identity garantiamo un id unico e personale

```java
DataManager dm = DataManager.getInstance();
dm.

registerEntityRepository(Ristorante .class, "data/ristoranti.json");
dm.

registerRelationRepository("utenti_preferiti","data/utenti_preferiti.json");
```

Registro la nuova entità e il nuovo oggetto nel dataManager, oltre che la relazione che deve avere

```java
// Ottieni repository
JsonRepository<Ristorante> ristorantiRepo = dm.getRepository(Ristorante.class);
RelationRepository preferitiRepo = dm.getRelationRepository("utenti_preferiti");

// Crea e salva entità
Ristorante ristorante = new Ristorante();
ristorante.

setNome("Trattoria da Luigi");
ristorante.

setIndirizzo("Via Roma 123");
ristorantiRepo.

save(ristorante); // Genera ID e salva su JSON automaticamente

// Crea relazioni
preferitiRepo.

addRelation("utente123",ristorante.getId()); // Salva automaticamente
```

Così uso i repository tramite DataManager (da usare in un controller dedicato)

Ecco un esempio completo su come usarlo su recuperare i dati di un utente e i suoi ristoranti preferiti:

Definisco le entità necessarie:

```java
public class Utente extends BaseEntity {
    private String nome;
    private String email;
    private String password;

    // getter e setter
}

public class Ristorante extends BaseEntity {
    private String nome;
    private String indirizzo;
    private String categoria;

    // getter e setter
}
```

Creo un controller dedicato:

```java
public class UtentiController {
    private final JsonRepository<Utente> utentiRepo;
    private final JsonRepository<Ristorante> ristorantiRepo;
    private final RelationRepository preferiti;

    public UtentiController() {
        DataManager dm = DataManager.getInstance();
        this.utentiRepo = dm.getRepository(Utente.class);
        this.ristorantiRepo = dm.getRepository(Ristorante.class);
        this.preferiti = dm.getRelationRepository("utenti_preferiti");
    }

    // Ottieni un utente per ID
    public Optional<Utente> getUtenteById(String id) {
        return utentiRepo.findById(id);
    }

    // Ottieni tutti i ristoranti preferiti di un utente
    public List<Ristorante> getRistorantiPreferiti(String utenteId) {
        List<String> preferitiIds = preferiti.findRelatedIds(utenteId);
        return preferitiIds.stream()
                .map(ristorantiRepo::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
```

Registro nel main i nuovi servizi oltre che definire il main:

```java

@Override
public void start(Stage primaryStage) {
    try {
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();

        // Crea e configura il bridge
        JavaScriptBridge bridge = new JavaScriptBridge(webEngine);

        // Inizializzazione dei repository
        DataManager dm = DataManager.getInstance();
        dm.registerEntityRepository(Utente.class, "data/utenti.json");
        dm.registerEntityRepository(Ristorante.class, "data/ristoranti.json");
        dm.registerRelationRepository("utenti_preferiti", "data/utenti_preferiti.json");

        // Inizializza il controller
        UtentiController utentiController = new UtentiController();

        // Registra metodo per ottenere i dati di un utente
        bridge.registerMethod("getUtente", args -> {
            Map<String, Object> params = gson.fromJson(args, Map.class);
            String utenteId = (String) params.get("id");

            Optional<Utente> utente = utentiController.getUtenteById(utenteId);

            if (utente.isPresent()) {
                return Map.of("success", true, "utente", utente.get());
            } else {
                return Map.of("success", false, "message", "Utente non trovato");
            }
        });

        // Metodo per ottenere i ristoranti preferiti di un utente
        bridge.registerMethod("getRistorantiPreferiti", args -> {
            Map<String, Object> params = gson.fromJson(args, Map.class);
            String utenteId = (String) params.get("utenteId");

            List<Ristorante> preferiti = utentiController.getRistorantiPreferiti(utenteId);
            return Map.of("success", true, "preferiti", preferiti);
        });

        // Carica HTML e resto del codice
        // ...
    } catch (Exception e) {
        // Gestione errori
    }
}
```

## 7. Fasi del lavoro

- Utenti, Ristoranti, utenti - ristoranti, utenti - ristorantiLike: Alessia
- recensioni, ristoranti - recensioni, utenti - recensione: Marco
- risposte, risposte - recensione, utente - recensione (like): Stefano
- Filtri di ricerca: Ginevra
