# TheKnife

Laboratorio A - Insubria 2024/2025

## Introduzione

TheKnife è un progetto Java sviluppato per la gestione di utenti, ristoranti, recensioni e relazioni tra questi, con interfaccia grafica moderna basata su JavaFX e AtlantaFX. Il progetto è stato migrato dalla vecchia architettura WebView/Bridge a una soluzione nativa JavaFX, sfruttando AtlantaFX per uno stile visivo avanzato e componenti UI evolute.

## Distribuzione: JAR e EXE

Il progetto viene distribuito in due modalità:

- **JAR Maven**: generato tramite Maven, necessita di JavaFX installato sul sistema per essere eseguito correttamente. Assicurarsi che le librerie JavaFX siano disponibili nel classpath o installate localmente.
- **EXE con jpackage**: è stato creato un eseguibile Windows tramite jpackage, che include tutte le dipendenze necessarie (JavaFX compreso) e non richiede configurazioni aggiuntive. L'EXE si trova nella cartella `TheKnife/` e può essere avviato direttamente.

## Dipendenze e configurazione Maven

Il progetto utilizza Maven per la gestione delle dipendenze. Le principali librerie sono:

- **JavaFX**: framework per la creazione di interfacce grafiche moderne in Java.
- **AtlantaFX**: tema e componenti aggiuntivi per JavaFX, per un look professionale.
- **Gson**: serializzazione/deserializzazione JSON.
- **Bcrypt**: hashing sicuro delle password.

Le dipendenze sono dichiarate nel file `pom.xml`. Esempio:

```xml
<dependencies>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>21.0.0</version>
    </dependency>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>21.0.0</version>
    </dependency>
    <dependency>
        <groupId>io.github.palexdev</groupId>
        <artifactId>atlantafx-base</artifactId>
        <version>2.0.1</version>
    </dependency>
    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
        <version>2.10.1</version>
    </dependency>
    <dependency>
        <groupId>at.favre.lib</groupId>
        <artifactId>bcrypt</artifactId>
        <version>0.9.0</version>
    </dependency>
</dependencies>
```

Per avviare il progetto, Maven gestisce automaticamente il classpath e le dipendenze. Non è più necessario configurare manualmente le VM options per JavaFX, ma per il JAR è necessario che JavaFX sia installato.

## Architettura e moduli

Il progetto è strutturato in moduli principali:

- **Utenti**: gestione registrazione, login, preferenze.
- **Ristoranti**: CRUD ristoranti, filtri di ricerca.
- **Recensioni**: creazione e visualizzazione recensioni.
- **Risposte**: gestione risposte alle recensioni.
- **Relazioni**: utenti-ristoranti, utenti-preferiti, like, ecc.

La persistenza dei dati avviene tramite file JSON, gestiti da repository dedicati (JsonRepository, RelationRepository) e orchestrati dal DataManager.

## Esempio di utilizzo delle repository

```java
public class Ristorante extends BaseEntity {
    private String nome;
    private String indirizzo;
    // altri campi, getter e setter
}

DataManager dm = DataManager.getInstance();
dm.registerEntityRepository(Ristorante.class, "data/ristoranti.json");
dm.registerRelationRepository("utenti_preferiti", "data/utenti_preferiti.json");

JsonRepository<Ristorante> ristorantiRepo = dm.getRepository(Ristorante.class);
RelationRepository preferitiRepo = dm.getRelationRepository("utenti_preferiti");

Ristorante ristorante = new Ristorante();
ristorante.setNome("Trattoria da Luigi");
ristorante.setIndirizzo("Via Roma 123");
ristorantiRepo.save(ristorante);

preferitiRepo.addRelation("utente123", ristorante.getId());
```

## Esempio di controller

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

    public Optional<Utente> getUtenteById(String id) {
        return utentiRepo.findById(id);
    }

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

## UI con JavaFX e AtlantaFX

L’interfaccia è sviluppata con FXML e controller JavaFX. AtlantaFX viene importato per applicare temi e componenti avanzati:

```java
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.atlantafx.base.theme.PrimerDark;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(new PrimerDark().getUserAgentStylesheet());
        primaryStage.setScene(scene);
        primaryStage.setTitle("TheKnife");
        primaryStage.show();
    }
}
```

## Pattern progettuali utilizzati

### 1. Pattern Builder per i filtri di ricerca

Per la gestione dei filtri di ricerca sui ristoranti e sulle recensioni, è stato adottato il **pattern Builder**. Questo pattern consente di costruire oggetti filtro in modo flessibile e leggibile, permettendo di combinare diversi criteri (es. tipo cucina, valutazione, località) senza dover gestire costruttori complessi o molteplici parametri opzionali. Il builder facilita l'estensione futura dei filtri e migliora la manutenibilità del codice.

Esempio:
```java
RistoranteFilter filter = RistoranteFilter.builder()
    .tipoCucina("Italiana")
    .valutazioneMinima(4)
    .localita("Varese")
    .build();
```

### 2. Pattern MVC con FXML in JavaFX

Per la realizzazione dell'interfaccia grafica, è stato utilizzato il **pattern Model-View-Controller (MVC)** tramite FXML. In questo approccio:
- La **View** è definita nei file FXML, che descrivono la struttura e i componenti dell'interfaccia.
- Il **Controller** è una classe Java che gestisce la logica e gli eventi della view, collegata tramite l'attributo `fx:controller`.
- Il **Model** è rappresentato dalle classi Java standard che gestiscono i dati e la logica di business.

Questa separazione consente di mantenere il codice ordinato, favorisce la riusabilità e semplifica la manutenzione. I controller interagiscono con i modelli e aggiornano la view in modo reattivo.

Esempio di collegamento FXML:
```xml
<AnchorPane fx:controller="theknife.MainViewController" ...>
    <!-- componenti UI -->
</AnchorPane>
```

Esempio di controller:
```java
public class MainViewController {
    @FXML private Button cercaButton;
    // ...
    @FXML
    private void onCercaClicked() {
        // logica di ricerca
    }
}
```

## Best practices

- Utilizza Maven per la gestione delle dipendenze e build.
- Organizza i controller per responsabilità (Utenti, Ristoranti, Recensioni, ecc.).
- Gestisci la persistenza tramite repository e DataManager.
- Applica AtlantaFX per una UI moderna e accessibile.
- Proteggi le password con Bcrypt.

## Fasi del lavoro e suddivisione

- Utenti, Ristoranti, utenti-ristoranti, utenti-ristorantiLike: Alessia
- Recensioni, ristoranti-recensioni, utenti-recensione: Marco
- Risposte, risposte-recensione, utente-recensioneLike: Stefano
- Filtri di ricerca: Ginevra

## Note finali

Per domande o problemi tecnici, consulta la documentazione JavaFX, AtlantaFX e le guide Maven. Tutte le dipendenze sono gestite automaticamente tramite Maven.
