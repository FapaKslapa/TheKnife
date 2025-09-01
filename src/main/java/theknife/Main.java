// Marocco Stefano 762192 VA
// Marin Marco 760622 VA
// Gerti Alessia 762405 VA
package theknife;

import atlantafx.base.theme.PrimerDark;
import theknife.models.Ristorante;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import services.CSVImportService;
import services.RistoranteService;

import java.util.List;

/**
 * Classe principale dell'applicazione TheKnife.
 *
 * Questa classe è il punto di ingresso dell'applicazione e gestisce
 * l'inizializzazione dell'interfaccia utente principale. TheKnife è una
 * piattaforma per la ricerca e la prenotazione di ristoranti, con funzionalità
 * sia per i clienti che per i ristoratori.
 *
 * L'applicazione utilizza JavaFX come framework di interfaccia utente
 * e implementa il tema PrimerDark di AtlantaFX per lo stile grafico.
 */
public class Main extends Application {

    /**
     * Costruttore di default richiesto da JavaFX.
     * Necessario per il caricamento e l'inizializzazione dell'applicazione.
     */
    public Main() {
        // Nessuna inizializzazione specifica richiesta
    }

    /**
     * Inizializza e configura la finestra principale dell'applicazione.
     *
     * Questo metodo è chiamato automaticamente dal framework JavaFX
     * all'avvio dell'applicazione. Si occupa di:
     * <ul>
     *   <li>Importare i dati iniziali dal CSV se necessario</li>
     *   <li>Impostare il tema grafico dell'applicazione (PrimerDark)</li>
     *   <li>Caricare l'interfaccia utente principale definita nel file FXML</li>
     *   <li>Configurare e visualizzare la finestra principale</li>
     *   <li>Impostare l'icona dell'applicazione</li>
     * </ul>
     *
     * @param primaryStage Lo stage principale fornito dal framework JavaFX
     * @throws Exception Se si verifica un errore durante il caricamento dell'interfaccia utente
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("Avvio dell'applicazione TheKnife...");

        try {
            System.out.println("Verifica del database ristoranti...");
            importUnicoCSV();
            System.out.println("Verifica database completata.");

            System.out.println("Impostazione tema grafico...");
            Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

            System.out.println("Caricamento interfaccia utente principale...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/theknife/MainView.fxml"));
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
            throw e;
        }
    }

    /**
     * Importa utenti e ristoranti dai file CSV separati se il database è vuoto.
     * Questo metodo è più semplice e affidabile del vecchio approccio con file unico.
     */
    private void importUnicoCSV() {
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

                // Usa il nuovo metodo per importare da CSV separati
                System.out.println("Inizio importazione da CSV separati...");
                int[] counts = csvImportService.importFromSeparateCSVs();
                System.out.println("Importati " + counts[0] + " utenti e " + counts[1] + " ristoranti dai file CSV separati.");
            } else {
                System.out.println("Database già popolato con " + ristoranti.size() + " ristoranti. Proseguo con l'avvio...");
            }
        } catch (Exception e) {
            System.err.println("Errore durante l'importazione dei dati dai CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Punto di ingresso principale dell'applicazione.
     *
     * Questo metodo avvia l'applicazione JavaFX chiamando il metodo
     * {@code launch} che inizializza l'ambiente JavaFX e chiama il metodo
     * {@code start} di questa classe.
     *
     * @param args Argomenti della linea di comando (non utilizzati)
     */
    public static void main(String[] args) {
        launch(args);
    }
}