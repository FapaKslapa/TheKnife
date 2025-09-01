package example;

import atlantafx.base.theme.PrimerDark;
import example.models.Ristorante;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import services.CSVImportService;
import services.RistoranteService;

import java.io.InputStream;
import java.util.List;

/**
 * Classe principale dell'applicazione TheKnife.
 *
 * <p>Questa classe è il punto di ingresso dell'applicazione e gestisce
 * l'inizializzazione dell'interfaccia utente principale. TheKnife è una
 * piattaforma per la ricerca e la prenotazione di ristoranti, con funzionalità
 * sia per i clienti che per i ristoratori.</p>
 *
 * <p>L'applicazione utilizza JavaFX come framework di interfaccia utente
 * e implementa il tema PrimerDark di AtlantaFX per lo stile grafico.</p>
 */
public class Main extends Application {

    /**
     * Inizializza e configura la finestra principale dell'applicazione.
     *
     * <p>Questo metodo è chiamato automaticamente dal framework JavaFX
     * all'avvio dell'applicazione. Si occupa di:
     * <ul>
     *   <li>Importare i dati iniziali dal CSV se necessario</li>
     *   <li>Impostare il tema grafico dell'applicazione (PrimerDark)</li>
     *   <li>Caricare l'interfaccia utente principale definita nel file FXML</li>
     *   <li>Configurare e visualizzare la finestra principale</li>
     *   <li>Impostare l'icona dell'applicazione</li>
     * </ul>
     * </p>
     *
     * @param primaryStage Lo stage principale fornito dal framework JavaFX
     * @throws Exception Se si verifica un errore durante il caricamento dell'interfaccia utente
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("Avvio dell'applicazione TheKnife...");

        try {
            // Importa i dati dal CSV se necessario
            System.out.println("Verifica del database ristoranti...");
            //importRistorantiFromCSV();
            System.out.println("Verifica database completata.");

            System.out.println("Impostazione tema grafico...");
            Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

            System.out.println("Caricamento interfaccia utente principale...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/example/MainView.fxml"));
            Scene scene = new Scene(loader.load(), 900, 700);
            System.out.println("Interfaccia utente caricata con successo.");

            // Imposta l'icona dell'applicazione
            try {
                System.out.println("Caricamento icona applicazione...");
                Image appIcon = new Image(getClass().getResourceAsStream("/icons/Logo.png"));
                primaryStage.getIcons().add(appIcon);
                System.out.println("Icona caricata con successo.");
            } catch (Exception e) {
                System.err.println("Errore nel caricamento dell'icona dell'applicazione: " + e.getMessage());
            }

            System.out.println("Configurazione finestra principale...");
            primaryStage.setTitle("TheKnife");
            primaryStage.setScene(scene);
            System.out.println("Visualizzazione interfaccia utente...");
            primaryStage.show();
            System.out.println("Applicazione avviata con successo!");
        } catch (Exception e) {
            System.err.println("Errore durante l'avvio dell'applicazione: " + e.getMessage());
            e.printStackTrace();
            throw e; // Rilancia l'eccezione per una corretta gestione dell'errore
        }
    }

    /**
     * Importa i ristoranti dal file CSV se il database è vuoto.
     * Questo metodo controlla se ci sono già ristoranti nel sistema e,
     * in caso contrario, importa i dati dal file CSV incluso nelle risorse.
     */
    private void importRistorantiFromCSV() {
        try {
            System.out.println("Inizializzazione RistoranteService...");
            RistoranteService ristoranteService = new RistoranteService();

            System.out.println("Recupero lista ristoranti...");
            List<Ristorante> ristoranti = ristoranteService.getAllRistoranti();
            System.out.println("Recuperati " + ristoranti.size() + " ristoranti dal database.");

            // Importa dati solo se non ci sono già ristoranti nel sistema
            if (ristoranti.isEmpty()) {
                System.out.println("Nessun ristorante trovato nel database. Importazione dati iniziali...");

                System.out.println("Inizializzazione CSVImportService...");
                CSVImportService csvImportService = new CSVImportService(ristoranteService);

                // Cerca il file CSV nelle risorse
                System.out.println("Ricerca file CSV nelle risorse...");
                InputStream inputStream = getClass().getResourceAsStream("/data/ristoranti_import.csv");
                if (inputStream != null) {
                    System.out.println("File CSV trovato, inizio importazione...");
                    int count = csvImportService.importRistorantiFromCSV(inputStream);
                    System.out.println("Importati " + count + " ristoranti dal file CSV.");
                } else {
                    System.err.println("File CSV non trovato nelle risorse!");
                }
            } else {
                System.out.println("Database già popolato con " + ristoranti.size() + " ristoranti. Proseguo con l'avvio...");
            }
        } catch (Exception e) {
            System.err.println("Errore durante l'importazione dei dati dal CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Punto di ingresso principale dell'applicazione.
     *
     * <p>Questo metodo avvia l'applicazione JavaFX chiamando il metodo
     * {@code launch} che inizializza l'ambiente JavaFX e chiama il metodo
     * {@code start} di questa classe.</p>
     *
     * @param args Argomenti della linea di comando (non utilizzati)
     */
    public static void main(String[] args) {
        launch(args);
    }
}