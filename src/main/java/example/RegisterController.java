package example;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import services.AuthService;
import example.models.Utente;

public class RegisterController {
    @FXML private TextField usernameField, emailField;
    @FXML private PasswordField passwordField, confirmPasswordField;
    @FXML private ComboBox<String> ruoloBox;
    @FXML private Label registerResult;
    @FXML private Button homeBtn, loginBtn;
    @FXML private Label loginLink;
    @FXML private VBox registerRootVBox;
    @FXML private VBox registerCard;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        homeBtn.setOnAction(e -> vaiHome());
        loginBtn.setOnAction(e -> vaiLogin());
        ruoloBox.getItems().addAll("UTENTE", "RISTORATORE");
        ruoloBox.getSelectionModel().selectFirst();
        loginLink.setOnMouseClicked(e -> mostraLoginCard());
    }

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

    private void vaiHome() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/example/MainView.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) usernameField.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void vaiLogin() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/example/AuthView.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) usernameField.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void mostraLoginCard() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/example/AuthView.fxml"));
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
