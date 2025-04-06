# TheKnife

Laboratorio A - Insubria 2024/2025
Per far runnare il codice va inserita questa VM option:

```bash
--module-path
"PathToProject/TheKnife/lib/javafx/lib"
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

Dopo lo faccio ora non ho voglia

## 7. Fasi del lavoro

- Utenti, Ristoranti, utenti - ristoranti, utenti - ristorantiLike: Alessia
- recensioni, ristoranti - recensioni, utenti - recensione: Marco
- risposte, risposte - recensione, utente - recensione (like): Stefano
- Filtri di ricerca: Ginevra
