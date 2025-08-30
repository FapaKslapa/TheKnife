package example;

import example.models.Ristorante;
import example.models.Utente;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.RistoranteService;

import java.util.List;

public class RistoratoreHomeController {
    @FXML
    private FlowPane ristorantiPane;
    @FXML
    private Button logoutBtn;
    @FXML
    private Button nuovoRistoranteBtn;

    private Utente ristoratore;
    private RistoranteService ristoranteService;

    public void setRistoratore(Utente ristoratore) {
        this.ristoratore = ristoratore;
        this.ristoranteService = new RistoranteService();
        mostraRistorantiPropri();
    }

    @FXML
    public void initialize() {
        logoutBtn.setOnAction(this::onLogout);
        nuovoRistoranteBtn.setOnAction(this::onNuovoRistorante);
    }

    private void mostraRistorantiPropri() {
        ristorantiPane.getChildren().clear();
        if (ristoratore == null) return;
        List<Ristorante> mieiRistoranti = ristoranteService.getRistorantiByProprietario(ristoratore.getId());
        for (Ristorante r : mieiRistoranti) {
            VBox card = new VBox(14);
            card.getStyleClass().add("card-ristorante-modern");

            Label nome = new Label(r.getNome());
            nome.getStyleClass().add("label-nome-modern");

            Label tipo = new Label("🍽 " + r.getTipoCucina());
            tipo.getStyleClass().add("label-info-modern");

            Label prezzo = new Label("💶 " + renderPrezzi(r.getFasciaPrezzo()));
            prezzo.getStyleClass().add("price-tag-modern");

            String indirizzo = "Indirizzo non disponibile";
            Label indirizzoLabel = new Label("📍 " + indirizzo);
            indirizzoLabel.getStyleClass().add("ristorante-indirizzo-modern");

            Label telefono = new Label("📞 " + r.getNumeroTelefono());
            telefono.getStyleClass().add("label-telefono-modern");

            Label consegna = new Label(r.isConsegnaDomicilio() ? "🚚 Consegna a domicilio: Sì" : "🚚 Consegna a domicilio: No");
            consegna.getStyleClass().add("label-consegna-modern");
            if (!r.isConsegnaDomicilio()) consegna.getStyleClass().add("no");

            Label orariTitle = new Label("🕒 Orari di apertura");
            orariTitle.getStyleClass().add("label-info-modern");
            javafx.scene.Node orariScorrevoli = creaOrariScorrevoli(r.getOrariApertura());

            javafx.scene.layout.HBox actions = new javafx.scene.layout.HBox(16);
            actions.setPadding(new javafx.geometry.Insets(8, 0, 0, 0));
            actions.setStyle("-fx-alignment: center-left;");

            // Solo pulsante recensioni (fumetto viola)
            javafx.scene.text.Text recensioniIcon = new javafx.scene.text.Text("\uD83D\uDCAC");
            recensioniIcon.setFont(javafx.scene.text.Font.font(28));
            recensioniIcon.getStyleClass().add("icon-recensioni");
            Button recensioniBtn = new Button();
            recensioniBtn.getStyleClass().add("icon-btn");
            recensioniBtn.setTooltip(new Tooltip("Recensioni"));
            recensioniBtn.setGraphic(recensioniIcon);
            recensioniBtn.setOnAction(e -> mostraRecensioni(r));

            actions.getChildren().addAll(recensioniBtn);

            card.getChildren().addAll(
                nome, tipo, prezzo, indirizzoLabel, telefono, consegna, orariTitle, orariScorrevoli, actions
            );
            ristorantiPane.getChildren().add(card);
        }
    }

    private String renderPrezzi(int fascia) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            sb.append(i < fascia ? "€" : "€");
        }
        return sb.toString();
    }

    private javafx.scene.Node creaOrariScorrevoli(java.util.Map<String, String> orariApertura) {
        String[] giorni = {"lunedi", "martedi", "mercoledi", "giovedi", "venerdi", "sabato", "domenica"};
        String[] labels = {"Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom"};
        int oggiIdx = java.time.LocalDate.now().getDayOfWeek().getValue() - 1;

        javafx.scene.layout.HBox giorniBox = new javafx.scene.layout.HBox(18);
        giorniBox.getStyleClass().add("orari-scroll-inner-modern");

        for (int i = 0; i < giorni.length; i++) {
            VBox card = new VBox(6);
            card.getStyleClass().add("giorno-card-modern");
            if (i == oggiIdx) card.getStyleClass().add("oggi");

            Label giornoLabel = new Label(labels[i]);
            giornoLabel.getStyleClass().add("giorno-label-modern");

            VBox orariList = new VBox(3);
            orariList.getStyleClass().add("orari-list-modern");
            String orario = orariApertura != null ? orariApertura.getOrDefault(giorni[i], "Chiuso") : "Chiuso";
            if ("Chiuso".equalsIgnoreCase(orario)) {
                Label chiuso = new Label("Chiuso");
                chiuso.getStyleClass().add("orario-item-modern");
                chiuso.getStyleClass().add("text-danger-modern");
                orariList.getChildren().add(chiuso);
            } else {
                for (String fascia : orario.split(",")) {
                    Label fasciaLabel = new Label(fascia.trim());
                    fasciaLabel.getStyleClass().add("orario-item-modern");
                    orariList.getChildren().add(fasciaLabel);
                }
            }
            card.getChildren().addAll(giornoLabel, orariList);
            giorniBox.getChildren().add(card);
        }

        javafx.scene.control.ScrollPane scroll = new javafx.scene.control.ScrollPane(giorniBox);
        scroll.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setFitToHeight(true);
        scroll.setFitToWidth(false);
        scroll.getStyleClass().add("orari-scroll-wrapper-modern");
        scroll.setPrefHeight(340);
        scroll.setMinHeight(300);
        scroll.setMaxHeight(400);
        scroll.setPannable(true);

        return scroll;
    }

    private void mostraRecensioni(Ristorante ristorante) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/example/RistoratoreRecensioniView.fxml"));
            javafx.scene.Parent root = loader.load();
            RistoratoreRecensioniController ctrl = loader.getController();
            ctrl.setContext(ristoratore.getId(), ristorante);
            javafx.stage.Stage stage = (javafx.stage.Stage) ristorantiPane.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void onLogout(ActionEvent event) {
        try {
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/example/AuthView.fxml")));
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onNuovoRistorante(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/example/NuovoRistoranteView.fxml"));
            javafx.scene.Parent root = loader.load();
            NuovoRistoranteController ctrl = loader.getController();
            ctrl.setRistoratore(ristoratore);
            javafx.stage.Stage stage = (javafx.stage.Stage) nuovoRistoranteBtn.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}