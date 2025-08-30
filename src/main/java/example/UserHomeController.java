package example;

import example.models.Ristorante;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.AuthService;
import services.RistoranteService;
import services.RecensioneService;

import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class UserHomeController {
    @FXML private FlowPane ristorantiPane;
    @FXML private Button logoutBtn, preferitiBtn;
    private final AuthService authService = new AuthService();
    private final RistoranteService ristoranteService = new RistoranteService();
    private final RecensioneService recensioneService = new RecensioneService();
    private String userId;

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @FXML
    public void initialize() {
        mostraCardRistoranti();
        logoutBtn.setOnAction(e -> logout());
        preferitiBtn.setOnAction(e -> mostraPreferiti());
    }

    @FXML
    private void mostraCardRistoranti() {
        ristorantiPane.getChildren().clear();
        List<Ristorante> ristoranti = ristoranteService.getAllRistoranti();
        for (Ristorante r : ristoranti) {
            VBox card = new VBox(14);
            card.getStyleClass().add("card-ristorante-modern");

            Label nome = new Label(r.getNome());
            nome.getStyleClass().add("label-nome-modern");

            Label tipo = new Label("🍽 " + r.getTipoCucina());
            tipo.getStyleClass().add("label-info-modern");

            Label prezzo = new Label("💶 " + renderPrezzi(r.getFasciaPrezzo()));
            prezzo.getStyleClass().add("price-tag-modern");

            String indirizzo = "";
            try {
                services.ReverseGeocodingService geo = new services.ReverseGeocodingService();
                indirizzo = geo.getAddress(r.getLatitudine(), r.getLongitudine());
            } catch (Exception ex) { indirizzo = "Indirizzo non disponibile"; }
            Label indirizzoLabel = new Label("📍 " + indirizzo);
            indirizzoLabel.getStyleClass().add("ristorante-indirizzo-modern");

            Label telefono = new Label("📞 " + r.getNumeroTelefono());
            telefono.getStyleClass().add("label-telefono-modern");

            Label consegna = new Label(r.isConsegnaDomicilio() ? "🚚 Consegna a domicilio: Sì" : "🚚 Consegna a domicilio: No");
            consegna.getStyleClass().add("label-consegna-modern");
            if (!r.isConsegnaDomicilio()) consegna.getStyleClass().add("no");

            Label orariTitle = new Label("🕒 Orari di apertura");
            orariTitle.getStyleClass().add("label-info-modern");
            Node orariScorrevoli = creaOrariScorrevoli(r.getOrariApertura());

            HBox actions = new HBox(16);
            actions.setPadding(new Insets(8, 0, 0, 0));
            actions.setStyle("-fx-alignment: center-left;");

            // Icona recensioni (fumetto viola)
            Text recensioniIcon = new Text("\uD83D\uDCAC");
            recensioniIcon.setFont(Font.font(28));
            recensioniIcon.getStyleClass().add("icon-recensioni");
            Button recensioniBtn = new Button();
            recensioniBtn.getStyleClass().add("icon-btn");
            recensioniBtn.setTooltip(new Tooltip("Recensioni"));
            recensioniBtn.setGraphic(recensioniIcon);
            recensioniBtn.setOnAction(e -> mostraRecensioni(r));

            // Icona preferiti (cuore pieno arancione / vuoto grigio)
            boolean isPreferito = ristoranteService.isRistorantePreferito(userId, r.getId());
            Text cuoreIcon = new Text(isPreferito ? "❤" : "♡");
            cuoreIcon.setFont(Font.font(30));
            cuoreIcon.getStyleClass().clear();
            cuoreIcon.getStyleClass().add(isPreferito ? "icon-cuore-on" : "icon-cuore-off");
            Button preferitoBtn = new Button();
            preferitoBtn.getStyleClass().add("icon-btn");
            preferitoBtn.setTooltip(new Tooltip("Preferiti"));
            preferitoBtn.setGraphic(cuoreIcon);
            if (isPreferito) {
                preferitoBtn.setStyle("-fx-background-color: #FFF7ED; -fx-effect: dropshadow(gaussian, #FF9F43AA, 8, 0.18, 0, 2);");
            } else {
                preferitoBtn.setStyle("");
            }
            preferitoBtn.setOnAction(e -> {
                boolean nuovoStato = !ristoranteService.isRistorantePreferito(userId, r.getId());
                if (nuovoStato) {
                    ristoranteService.aggiungiRistoranteAiPreferiti(userId, r.getId());
                    cuoreIcon.setText("❤");
                    cuoreIcon.getStyleClass().clear();
                    cuoreIcon.getStyleClass().add("icon-cuore-on");
                    preferitoBtn.setStyle("-fx-background-color: #FFF7ED; -fx-effect: dropshadow(gaussian, #FF9F43AA, 8, 0.18, 0, 2);");
                } else {
                    ristoranteService.rimuoviRistoranteDaiPreferiti(userId, r.getId());
                    cuoreIcon.setText("♡");
                    cuoreIcon.getStyleClass().clear();
                    cuoreIcon.getStyleClass().add("icon-cuore-off");
                    preferitoBtn.setStyle("");
                }
            });

            actions.getChildren().addAll(recensioniBtn, preferitoBtn);

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

    private Node creaOrariScorrevoli(java.util.Map<String, String> orariApertura) {
        String[] giorni = {"lunedi", "martedi", "mercoledi", "giovedi", "venerdi", "sabato", "domenica"};
        String[] labels = {"Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom"};
        int oggiIdx = java.time.LocalDate.now().getDayOfWeek().getValue() - 1;

        HBox giorniBox = new HBox(18);
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
        scroll.setPrefHeight(340); // allungato
        scroll.setMinHeight(300);
        scroll.setMaxHeight(400);
        scroll.setPannable(true);

        return scroll;
    }

    private void mostraRecensioni(Ristorante ristorante) {
        mostraPaginaRecensioni(ristorante);
    }

    private void mostraPaginaRecensioni(Ristorante ristorante) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/example/RecensioniRistoranteView.fxml"));
            javafx.scene.Parent root = loader.load();
            RecensioniRistoranteController ctrl = loader.getController();
            ctrl.setContext(userId, ristorante);
            javafx.stage.Stage stage = (javafx.stage.Stage) ristorantiPane.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void mostraPreferiti() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/example/PreferitiView.fxml"));
            javafx.scene.Parent preferitiRoot = loader.load();
            PreferitiViewController ctrl = loader.getController();
            ctrl.setUserId(userId);
            javafx.stage.Stage stage = (javafx.stage.Stage) ristorantiPane.getScene().getWindow();
            stage.getScene().setRoot(preferitiRoot);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void logout() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/example/MainView.fxml"));
            javafx.scene.Parent root = loader.load();
            Stage stage = (Stage) ristorantiPane.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}