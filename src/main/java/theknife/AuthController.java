// Marocco Stefano 762192 VA
// Marin Marco 760622 VA
// Gerti Alessia 762405 VA
// Sibilla Ginevra 761114 VA
package theknife;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import services.AuthService;
import javafx.scene.layout.VBox;

/**
 * Controller per la gestione dell'autenticazione degli utenti nell'applicazione TheKnife.
 * Questa classe gestisce il processo di login e la navigazione verso le schermate
 * di registrazione o della home page. Si occupa di validare le credenziali inserite
 * dall'utente e di reindirizzarlo alla schermata appropriata in base al suo ruolo.
 *
 * <p>Funzionalità principali:
 * <ul>
 *   <li>Gestione del login degli utenti con verifica delle credenziali</li>
 *   <li>Reindirizzamento verso la home appropriata in base al ruolo dell'utente (RISTORATORE o UTENTE)</li>
 *   <li>Navigazione verso la pagina di registrazione per nuovi utenti</li>
 *   <li>Navigazione verso la home page principale dell'applicazione</li>
 * </ul>
 *
 * <p>Questo controller è associato al file FXML "AuthView.fxml" che definisce
 * l'interfaccia utente per la schermata di login.
 */
public class AuthController {
    /** Campo di testo per l'inserimento del nome utente o email */
    @FXML private TextField usernameField;

    /** Campo di testo per l'inserimento della password (con caratteri nascosti) */
    @FXML private PasswordField passwordField;

    /** Etichetta per mostrare il risultato del tentativo di login (successo o errore) */
    @FXML private Label loginResult;

    /** Pulsante per navigare alla home page principale dell'applicazione */
    @FXML private Button homeBtn;

    /** Pulsante per navigare alla schermata di registrazione */
    @FXML private Button registerBtn;

    /** Etichetta cliccabile per passare alla schermata di registrazione */
    @FXML private Label registerLink;

    /** Contenitore principale per l'interfaccia di autenticazione */
    @FXML private VBox authRoot;

    /** Contenitore radice che include tutti gli elementi dell'interfaccia */
    @FXML private VBox rootVBox;

    /** Servizio per la gestione dell'autenticazione degli utenti */
    private final AuthService authService = new AuthService();

    /**
     * Costruttore di default richiesto da JavaFX.
     * Necessario per il caricamento FXML e l'iniezione dei campi annotati con @FXML.
     */
    public AuthController() {
        // Nessuna inizializzazione specifica richiesta
    }

    /**
     * Inizializza la vista dopo che gli elementi FXML sono stati caricati.
     * Questo metodo viene chiamato automaticamente dal loader FXML dopo
     * la creazione dell'oggetto controller e l'iniezione dei campi annotati con @FXML.
     * Configura i listener per i pulsanti e l'etichetta di registrazione per
     * gestire gli eventi dell'interfaccia utente.
     */
    @FXML
    public void initialize() {
        homeBtn.setOnAction(e -> vaiHome());
        registerBtn.setOnAction(e -> mostraRegistrazione());
        registerLink.setOnMouseClicked(e -> mostraRegistrazione());
    }

    /**
     * Gestisce il processo di login quando l'utente preme il pulsante di accesso.
     * Questo metodo recupera le credenziali inserite dall'utente, le verifica tramite
     * il servizio di autenticazione e, in caso di successo, reindirizza l'utente
     * alla home page appropriata in base al suo ruolo (ristoratore o cliente).
     * In caso di credenziali non valide, mostra un messaggio di errore.
     * <p>
     * Il metodo è annotato con @FXML e viene collegato al pulsante di login
     * tramite l'attributo onAction nel file FXML.
     */
    @FXML
    private void onLogin() {
        var utenteOpt = authService.login(usernameField.getText(), passwordField.getText());
        boolean ok = utenteOpt.isPresent();
        loginResult.setText(ok ? "Login effettuato!" : "Credenziali non valide.");
        if (ok) {
            try {
                var utente = utenteOpt.get();
                javafx.fxml.FXMLLoader loader;
                javafx.scene.Parent root;
                javafx.stage.Stage stage = (javafx.stage.Stage) homeBtn.getScene().getWindow();
                if (utente.getRuolo().name().equals("RISTORATORE")) {
                    loader = new javafx.fxml.FXMLLoader(getClass().getResource("/theknife/RistoratoreHomeView.fxml"));
                    root = loader.load();
                    theknife.RistoratoreHomeController ctrl = loader.getController();
                    ctrl.setRistoratore(utente);
                    stage.getScene().setRoot(root);
                } else {
                    loader = new javafx.fxml.FXMLLoader(getClass().getResource("/theknife/UserHomeView.fxml"));
                    root = loader.load();
                    UserHomeController ctrl = loader.getController();
                    ctrl.setUserId(utente.getId());
                    stage.getScene().setRoot(root);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Naviga alla pagina di registrazione.
     * Questo metodo carica la vista della registrazione (RegisterView.fxml) e
     * la imposta come radice della scena corrente, sostituendo la vista di login.
     * In caso di errori durante il caricamento della vista, stampa lo stack trace dell'eccezione.
     */
    private void mostraRegistrazione() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/theknife/RegisterView.fxml"));
            javafx.scene.Parent registerRoot = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) homeBtn.getScene().getWindow();
            stage.getScene().setRoot(registerRoot);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Naviga alla home page principale dell'applicazione.
     * Questo metodo carica la vista principale (MainView.fxml) e
     * la imposta come radice della scena corrente, sostituendo la vista di login.
     * La home page principale è accessibile a tutti gli utenti, anche non autenticati.
     * In caso di errori durante il caricamento della vista, stampa lo stack trace dell'eccezione.
     */
    private void vaiHome() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/theknife/MainView.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) homeBtn.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
