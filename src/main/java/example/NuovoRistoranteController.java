package example;

import example.models.Utente;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import services.RistoranteService;
import services.ReverseGeocodingService;

import java.util.HashMap;
import java.util.Map;

public class NuovoRistoranteController {
    @FXML private TextField nomeField;
    @FXML private TextField tipoCucinaField;
    @FXML private ComboBox<String> fasciaPrezzoBox;
    @FXML private TextField telefonoField;
    @FXML private TextField indirizzoField;
    @FXML private CheckBox consegnaCheck;
    @FXML private VBox orariBox;
    @FXML private Button confermaBtn, annullaBtn;
    @FXML private Label resultLabel;

    private Utente ristoratore;
    private final RistoranteService ristoranteService = new RistoranteService();
    private final ReverseGeocodingService geocodingService = new ReverseGeocodingService();

    public void setRistoratore(Utente ristoratore) {
        this.ristoratore = ristoratore;
    }

    @FXML
    public void initialize() {
        fasciaPrezzoBox.getItems().addAll("1", "2", "3");
        creaCardOrari();
        confermaBtn.setOnAction(e -> onConferma());
        annullaBtn.setOnAction(e -> tornaHome());
    }

    private void creaCardOrari() {
        String[] giorni = {"lunedì", "martedì", "mercoledì", "giovedì", "venerdì", "sabato", "domenica"};
        String[] labels = {"Lunedì", "Martedì", "Mercoledì", "Giovedì", "Venerdì", "Sabato", "Domenica"};
        for (int i = 0; i < giorni.length; i++) {
            VBox card = new VBox(8);
            card.getStyleClass().add("giorno-card-wide");
            card.setStyle("-fx-padding: 12 0 12 0;");
            Label giornoLabel = new Label(labels[i]);
            giornoLabel.getStyleClass().add("giorno-label-large");
            HBox orariRow = new HBox(16);
            orariRow.setStyle("-fx-alignment: center-left;");
            TextField apertura = new TextField();
            apertura.setPromptText("Apertura (HH:mm)");
            apertura.getStyleClass().add("textfield-orario-wide");
            apertura.setTooltip(new Tooltip("Formato: HH:mm"));
            TextField chiusura = new TextField();
            chiusura.setPromptText("Chiusura (HH:mm)");
            chiusura.getStyleClass().add("textfield-orario-wide");
            chiusura.setTooltip(new Tooltip("Formato: HH:mm"));
            CheckBox chiuso = new CheckBox("Chiuso");
            chiuso.getStyleClass().add("label-info-modern");
            orariRow.getChildren().addAll(apertura, chiusura, chiuso);
            card.getChildren().addAll(giornoLabel, orariRow);
            card.setUserData(new Object[]{apertura, chiusura, chiuso, giorni[i]});
            orariBox.getChildren().add(card);
        }
    }

    private void onConferma() {
        String nome = nomeField.getText();
        String tipoCucina = tipoCucinaField.getText();
        String fasciaPrezzoStr = fasciaPrezzoBox.getValue();
        String telefono = telefonoField.getText();
        String indirizzo = indirizzoField.getText();
        boolean consegna = consegnaCheck.isSelected();
        if (nome.isEmpty() || tipoCucina.isEmpty() || fasciaPrezzoStr == null || telefono.isEmpty() || indirizzo.isEmpty()) {
            resultLabel.setText("Compila tutti i campi obbligatori!");
            return;
        }
        int fasciaPrezzo = Integer.parseInt(fasciaPrezzoStr);
        Map<String, String> orariApertura = new HashMap<>();
        for (javafx.scene.Node node : orariBox.getChildren()) {
            VBox card = (VBox) node;
            Object[] data = (Object[]) card.getUserData();
            TextField apertura = (TextField) data[0];
            TextField chiusura = (TextField) data[1];
            CheckBox chiuso = (CheckBox) data[2];
            String giorno = (String) data[3];
            if (chiuso.isSelected()) {
                orariApertura.put(giorno, "Chiuso");
            } else {
                String ap = apertura.getText();
                String ch = chiusura.getText();
                if (ap.isEmpty() || ch.isEmpty()) {
                    resultLabel.setText("Compila tutti gli orari o seleziona 'Chiuso'!");
                    return;
                }
                orariApertura.put(giorno, ap + "-" + ch);
            }
        }
        // Latitudine/Longitudine placeholder (potresti aggiungere input o geolocalizzazione)
        double lat = 0.0, lon = 0.0;
        var ristorante = ristoranteService.creaRistorante(nome, tipoCucina, fasciaPrezzo, orariApertura, lat, lon, ristoratore.getId(), telefono, consegna);
        // Salva anche l'indirizzo (se il model lo supporta, altrimenti puoi aggiungerlo)
        // Puoi aggiungere qui la logica per geocoding se vuoi ottenere lat/lon dall'indirizzo
        resultLabel.setText("Ristorante creato!");
        tornaHome();
    }

    private void tornaHome() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/example/RistoratoreHomeView.fxml"));
            javafx.scene.Parent root = loader.load();
            RistoratoreHomeController ctrl = loader.getController();
            ctrl.setRistoratore(ristoratore);
            javafx.stage.Stage stage = (javafx.stage.Stage) annullaBtn.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
