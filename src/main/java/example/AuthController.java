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
        boolean ok = authService.login(usernameField.getText(), passwordField.getText()).isPresent();
        loginResult.setText(ok ? "Login effettuato!" : "Credenziali non valide.");
        // Puoi aggiungere qui la logica per tornare alla home dopo login
    }

    private void mostraRegistrazione() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/example/RegisterView.fxml"));
            javafx.scene.Parent registerRoot = loader.load();
            VBox registerCard = (VBox) registerRoot.lookup("#registerCard");
            Label loginLink = (Label) registerRoot.lookup("#loginLink");
            int idx = rootVBox.getChildren().indexOf(authRoot);
            if (idx != -1 && registerCard != null) {
                rootVBox.getChildren().set(idx, registerCard);
            }
            // Sostituisci il link sotto la card
            for (int i = 0; i < rootVBox.getChildren().size(); i++) {
                if (rootVBox.getChildren().get(i) instanceof Label && ((Label) rootVBox.getChildren().get(i)).getId() != null && ((Label) rootVBox.getChildren().get(i)).getId().equals("registerLink")) {
                    rootVBox.getChildren().set(i, loginLink);
                    break;
                }
            }
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
