package example;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import services.AuthService;
import javafx.scene.layout.VBox;

public class AuthController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label loginResult;
    @FXML private Button homeBtn, registerBtn;
    @FXML private Label registerLink;
    @FXML private VBox authRoot;
    @FXML private VBox rootVBox; // aggiungi fx:id="rootVBox" al VBox principale in AuthView.fxml

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        homeBtn.setOnAction(e -> vaiHome());
        registerBtn.setOnAction(e -> mostraRegistrazione());
        registerLink.setOnMouseClicked(e -> mostraRegistrazione());
    }

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
                    loader = new javafx.fxml.FXMLLoader(getClass().getResource("/example/RistoratoreHomeView.fxml"));
                    root = loader.load();
                    example.RistoratoreHomeController ctrl = loader.getController();
                    ctrl.setRistoratore(utente);
                    stage.getScene().setRoot(root);
                } else {
                    loader = new javafx.fxml.FXMLLoader(getClass().getResource("/example/UserHomeView.fxml"));
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

    private void mostraRegistrazione() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/example/RegisterView.fxml"));
            javafx.scene.Parent registerRoot = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) homeBtn.getScene().getWindow();
            stage.getScene().setRoot(registerRoot);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void vaiHome() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/example/MainView.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) homeBtn.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
