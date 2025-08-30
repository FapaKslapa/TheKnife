package example;

import example.models.Ristorante;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.RistoranteService;
import services.RecensioneService;

import java.util.List;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Toggle;

public class PreferitiViewController {
    @FXML private FlowPane preferitiPane;
    @FXML private Button homeBtn;
    private String userId;
    private final RistoranteService ristoranteService = new RistoranteService();
    private final RecensioneService recensioneService = new RecensioneService();

    public void setUserId(String userId) {
        this.userId = userId;
        mostraPreferiti();
    }

    @FXML
    public void initialize() {
        homeBtn.setOnAction(e -> vaiHome());
    }

    private void mostraPreferiti() {
        preferitiPane.getChildren().clear();
        List<String> ids = ristoranteService.getRistorantiPreferitiByUtente(userId);
        List<Ristorante> preferiti = ristoranteService.getAllRistoranti().stream()
                .filter(r -> ids.contains(r.getId())).toList();
        for (Ristorante r : preferiti) {
            VBox card = new VBox(14);
            card.getStyleClass().add("card-ristorante-modern");
            card.setOnMouseClicked(e -> mostraDettaglioRistorante(r));
            card.setStyle("-fx-cursor: hand;");
            // Indirizzo (se disponibile)
            String indirizzo = "";
            try {
                services.ReverseGeocodingService geo = new services.ReverseGeocodingService();
                indirizzo = geo.getAddress(r.getLatitudine(), r.getLongitudine());
            } catch (Exception ex) { indirizzo = "Indirizzo non disponibile"; }
            Label nome = new Label(r.getNome());
            nome.getStyleClass().add("label-nome-modern");
            Label tipo = new Label("\uD83C\uDF7D " + r.getTipoCucina());
            tipo.getStyleClass().add("label-info-modern");
            Label prezzo = new Label("\uD83D\uDCB6 " + r.getFasciaPrezzo());
            prezzo.getStyleClass().add("price-tag-modern");
            Label indirizzoLabel = new Label("\uD83D\uDCCD " + indirizzo);
            indirizzoLabel.getStyleClass().add("ristorante-indirizzo-modern");
            Label telefono = new Label("\uD83D\uDCDE " + r.getNumeroTelefono());
            telefono.getStyleClass().add("label-telefono-modern");
            // Orari di apertura
            Label orariTitle = new Label("\uD83D\uDD52 Orari di apertura");
            orariTitle.getStyleClass().add("label-info-modern");
            VBox orariBox = new VBox(4);
            orariBox.getStyleClass().add("orari-list-modern");
            if (r.getOrariApertura() != null) {
                r.getOrariApertura().forEach((giorno, orario) -> {
                    Label l = new Label(giorno + ": " + orario);
                    l.getStyleClass().add("orario-item-modern");
                    orariBox.getChildren().add(l);
                });
            }
            card.getChildren().addAll(nome, tipo, prezzo, indirizzoLabel, telefono, orariTitle, orariBox);
            preferitiPane.getChildren().add(card);
        }
    }

    private void mostraDettaglioRistorante(Ristorante ristorante) {
        mostraPaginaRecensioni(ristorante);
    }

    private void mostraPaginaRecensioni(Ristorante ristorante) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/example/RecensioniRistoranteView.fxml"));
            javafx.scene.Parent root = loader.load();
            RecensioniRistoranteController ctrl = loader.getController();
            ctrl.setContext(userId, ristorante);
            javafx.stage.Stage stage = (javafx.stage.Stage) preferitiPane.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void mostraModalRecensione(Ristorante ristorante, Dialog<?> parentDialog) {
        mostraPaginaRecensioni(ristorante);
    }

    private void vaiHome() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/example/UserHomeView.fxml"));
            javafx.scene.Parent root = loader.load();
            UserHomeController ctrl = loader.getController();
            ctrl.setUserId(userId);
            javafx.stage.Stage stage = (javafx.stage.Stage) preferitiPane.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
