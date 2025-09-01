// Marocco Stefano 762192 VA
// Marin Marco 760622 VA
// Gerti Alessia 762405 VA
package theknife;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import services.AuthService;
import theknife.models.Utente;

/**
 * Controller per la gestione della schermata di registrazione.
 * Questa classe gestisce tutte le interazioni dell'utente con il form di registrazione,
 * inclusa la validazione dei dati inseriti e l'invio delle informazioni al servizio di autenticazione.
 */
public class RegisterController {
    /** Campo di testo per l'inserimento del nome utente */
    @FXML private TextField usernameField, emailField;
    /** Campo password per l'inserimento della password e della sua conferma */
    @FXML private PasswordField passwordField, confirmPasswordField;
    /** Menu a tendina per la selezione del ruolo utente */
    @FXML private ComboBox<String> ruoloBox;
    /** Etichetta per mostrare il risultato dell'operazione di registrazione */
    @FXML private Label registerResult;
    /** Pulsanti per navigare alla home e alla schermata di login */
    @FXML private Button homeBtn, loginBtn;
    /** Collegamento per passare alla schermata di login */
    @FXML private Label loginLink;
    /** Contenitore principale per la schermata di registrazione */
    @FXML private VBox registerRootVBox;
    /** Contenitore per il form di registrazione */
    @FXML private VBox registerCard;

    /** Servizio per la gestione dell'autenticazione */
    private final AuthService authService = new AuthService();

    /**
     * Inizializza la schermata di registrazione.
     * Questo metodo viene chiamato automaticamente dopo che il file FXML è stato caricato.
     * Configura i listener per i pulsanti e popola il menu a tendina dei ruoli.
     */
    @FXML
    public void initialize() {
        homeBtn.setOnAction(e -> vaiHome());
        loginBtn.setOnAction(e -> vaiLogin());
        ruoloBox.getItems().addAll("UTENTE", "RISTORATORE");
        ruoloBox.getSelectionModel().selectFirst();
        loginLink.setOnMouseClicked(e -> mostraLoginCard());
    }

    /**
     * Gestisce l'evento di registrazione quando l'utente conferma l'inserimento dei dati.
     * Esegue la validazione dei campi inseriti e, se validi, procede con la registrazione
     * dell'utente tramite il servizio di autenticazione.
     */
    @FXML
    private void onRegister() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();
        String ruoloStr = ruoloBox.getValue();
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty() || ruoloStr == null) {
            registerResult.setText("Compila tutti i campi!");
            return;
        }
        if (!password.equals(confirm)) {
            registerResult.setText("Le password non coincidono!");
            return;
        }
        Utente.Ruolo ruolo = ruoloStr.equals("RISTORATORE") ? Utente.Ruolo.RISTORATORE : Utente.Ruolo.UTENTE;
        boolean ok = authService.registraUtente(username, password, email, ruolo);
        registerResult.setText(ok ? "Registrazione avvenuta!" : "Username o email già esistenti.");
    }

    /**
     * Naviga alla schermata principale dell'applicazione.
     * Carica il file FXML della vista principale e sostituisce il contenuto
     * della finestra corrente con la nuova vista.
     */
    private void vaiHome() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/theknife/MainView.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) usernameField.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Naviga alla schermata di login.
     * Carica il file FXML della vista di autenticazione e sostituisce il contenuto
     * della finestra corrente con la nuova vista.
     */
    private void vaiLogin() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/theknife/AuthView.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) usernameField.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Sostituisce il form di registrazione con quello di login senza cambiare pagina.
     * Carica il componente di login dal file FXML corrispondente e lo inserisce
     * all'interno del layout attuale, mantenendo la stessa finestra.
     */
    private void mostraLoginCard() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/theknife/AuthView.fxml"));
            javafx.scene.Parent authRootLoaded = loader.load();
            VBox authCard = (VBox) authRootLoaded.lookup("#authRoot");
            Label registerLink = (Label) authRootLoaded.lookup("#registerLink");
            int idx = registerRootVBox.getChildren().indexOf(registerCard);
            if (idx != -1 && authCard != null) {
                registerRootVBox.getChildren().set(idx, authCard);
            }
            // Sostituisci il link sotto la card
            for (int i = 0; i < registerRootVBox.getChildren().size(); i++) {
                if (registerRootVBox.getChildren().get(i) instanceof Label && ((Label) registerRootVBox.getChildren().get(i)).getId() != null && ((Label) registerRootVBox.getChildren().get(i)).getId().equals("loginLink")) {
                    registerRootVBox.getChildren().set(i, registerLink);
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
