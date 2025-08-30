package example;

import example.models.Recensione;
import example.models.Ristorante;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.AuthService;
import services.RecensioneService;
import services.ReverseGeocodingService;
import services.RistoranteService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class MainViewController {
    @FXML
    private FlowPane ristorantiPane;
    @FXML
    private Button loginBtn, registerBtn;

    private final AuthService authService = new AuthService();
    private final RistoranteService ristoranteService = new RistoranteService();
    private final RecensioneService recensioneService = new RecensioneService();
    private final ReverseGeocodingService geocodingService = new ReverseGeocodingService();

    // Overlay root
    private StackPane mainRoot;

    @FXML
    public void initialize() {
        Scene scene = ristorantiPane.getScene();
        if (scene != null && scene.getRoot() instanceof StackPane) {
            mainRoot = (StackPane) scene.getRoot();
        }
        mostraCardRistoranti();
        loginBtn.setOnAction(e -> mostraDialogLogin());
        registerBtn.setOnAction(e -> mostraDialogRegistrazione());
    }

    private void mostraCardRistoranti() {
        ristorantiPane.getChildren().clear();
        ristorantiPane.setHgap(24);
        ristorantiPane.setVgap(24);
        ristorantiPane.getStyleClass().add("ristoranti-flow");

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

            String indirizzoStr = geocodingService.getAddress(r.getLatitudine(), r.getLongitudine());
            Label indirizzo = new Label("📍 " + indirizzoStr);
            indirizzo.getStyleClass().add("ristorante-indirizzo-modern");

            Label telefono = new Label("📞 " + r.getNumeroTelefono());
            telefono.getStyleClass().add("label-telefono-modern");

            Label consegna = new Label(r.isConsegnaDomicilio() ? "🚚 Consegna a domicilio: Sì" : "🚚 Consegna a domicilio: No");
            consegna.getStyleClass().add("label-consegna-modern");
            if (!r.isConsegnaDomicilio()) consegna.getStyleClass().add("no");

            Label orariTitle = new Label("🕒 Orari di apertura");
            orariTitle.getStyleClass().add("label-info-modern");
            Node orariScorrevoli = creaOrariScorrevoli(r.getOrariApertura());

            Button recensioniBtn = new Button("Leggi Recensioni");
            recensioniBtn.getStyleClass().add("button-recensioni");
            recensioniBtn.setOnAction(e -> mostraOverlayRecensioni(r));

            card.getChildren().addAll(nome, tipo, prezzo, indirizzo, telefono, consegna, orariTitle, orariScorrevoli, recensioniBtn);
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

    private Node creaOrariScorrevoli(Map<String, String> orariApertura) {
        String[] giorni = {"lunedi", "martedi", "mercoledi", "giovedi", "venerdi", "sabato", "domenica"};
        String[] labels = {"Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom"};
        int oggiIdx = LocalDate.now().getDayOfWeek().getValue() - 1;

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
        scroll.setPrefHeight(140);
        scroll.setMinHeight(120);
        scroll.setMaxHeight(160);
        scroll.setPannable(true);

        return scroll;
    }

    // Overlay recensioni con card scorrevoli
    private void mostraOverlayRecensioni(Ristorante ristorante) {
        if (mainRoot == null) {
            mostraDialogRecensioni(ristorante);
            return;
        }

        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("overlay-recensioni");
        overlay.setPrefSize(mainRoot.getWidth(), mainRoot.getHeight());

        VBox dialog = new VBox(18);
        dialog.getStyleClass().add("dialog-recensioni");

        HBox header = new HBox();
        header.setSpacing(8);

        Label title = new Label("Recensioni di " + ristorante.getNome());
        title.getStyleClass().add("label-dialog-title");

        Button closeBtn = new Button("✕");
        closeBtn.getStyleClass().add("button-close-recensioni");
        closeBtn.setOnAction(e -> {
            FadeTransition fade = new FadeTransition(Duration.millis(180), overlay);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setOnFinished(ev -> mainRoot.getChildren().remove(overlay));
            fade.play();
        });

        header.getChildren().addAll(title, closeBtn);

        // Card scorrevoli
        HBox recensioniCards = new HBox();
        recensioniCards.getStyleClass().add("hbox-recensioni-cards");

        List<Recensione> recensioni = recensioneService.getRecensioniByRistorante(ristorante.getId());
        if (recensioni.isEmpty()) {
            VBox card = new VBox(8);
            card.getStyleClass().add("card-recensione");
            Label noRec = new Label("Nessuna recensione disponibile.");
            noRec.getStyleClass().add("label-testo");
            card.getChildren().add(noRec);
            recensioniCards.getChildren().add(card);
        } else {
            for (Recensione rec : recensioni) {
                VBox card = new VBox(8);
                card.getStyleClass().add("card-recensione");

                Label voto = new Label("⭐ " + rec.getRate());
                voto.getStyleClass().add("label-voto");

                Label titolo = new Label(rec.getTitle());
                titolo.getStyleClass().add("label-titolo");

                Label testo = new Label(rec.getText());
                testo.getStyleClass().add("label-testo");

                Label tempo = new Label(recensioneService.getTempoTrascorso(rec.getDate()));
                tempo.getStyleClass().add("label-tempo");

                card.getChildren().addAll(voto, titolo, testo, tempo);
                recensioniCards.getChildren().add(card);
            }
        }

        ScrollPane scroll = new ScrollPane(recensioniCards);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setFitToHeight(true);
        scroll.setFitToWidth(false);
        scroll.setPrefHeight(260);
        scroll.setMinHeight(180);
        scroll.setMaxHeight(320);
        scroll.setStyle("-fx-background-color: transparent;");

        dialog.getChildren().addAll(header, scroll);
        overlay.getChildren().add(dialog);

        overlay.setOpacity(0.0);
        mainRoot.getChildren().add(overlay);

        FadeTransition fade = new FadeTransition(Duration.millis(220), overlay);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }

    // Fallback modale classico
    private void mostraDialogRecensioni(Ristorante ristorante) {
        Stage dialog = new Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.setTitle("Recensioni di " + ristorante.getNome());

        VBox root = new VBox(18);
        root.getStyleClass().add("dialog-recensioni");

        Label title = new Label("Recensioni di " + ristorante.getNome());
        title.getStyleClass().add("label-dialog-title");

        HBox recensioniCards = new HBox();
        recensioniCards.getStyleClass().add("hbox-recensioni-cards");

        List<Recensione> recensioni = recensioneService.getRecensioniByRistorante(ristorante.getId());
        if (recensioni.isEmpty()) {
            VBox card = new VBox(8);
            card.getStyleClass().add("card-recensione");
            Label noRec = new Label("Nessuna recensione disponibile.");
            noRec.getStyleClass().add("label-testo");
            card.getChildren().add(noRec);
            recensioniCards.getChildren().add(card);
        } else {
            for (Recensione rec : recensioni) {
                VBox card = new VBox(8);
                card.getStyleClass().add("card-recensione");

                Label voto = new Label("⭐ " + rec.getRate());
                voto.getStyleClass().add("label-voto");

                Label titolo = new Label(rec.getTitle());
                titolo.getStyleClass().add("label-titolo");

                Label testo = new Label(rec.getText());
                testo.getStyleClass().add("label-testo");

                Label tempo = new Label(recensioneService.getTempoTrascorso(rec.getDate()));
                tempo.getStyleClass().add("label-tempo");

                card.getChildren().addAll(voto, titolo, testo, tempo);
                recensioniCards.getChildren().add(card);
            }
        }

        ScrollPane scroll = new ScrollPane(recensioniCards);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setFitToHeight(true);
        scroll.setFitToWidth(false);
        scroll.setPrefHeight(260);
        scroll.setMinHeight(180);
        scroll.setMaxHeight(320);
        scroll.setStyle("-fx-background-color: transparent;");

        root.getChildren().addAll(title, scroll);

        Scene scene = new Scene(root, 540, 340);
        scene.getStylesheets().add(getClass().getResource("/example/style.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void mostraDialogLogin() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/example/AuthView.fxml"));
            Parent authRoot = loader.load(); // fix: cast a Parent, non StackPane
            Stage stage = (Stage) ristorantiPane.getScene().getWindow();
            stage.getScene().setRoot(authRoot);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void mostraDialogRegistrazione() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/example/RegisterView.fxml"));
            javafx.scene.Parent registerRoot = loader.load();
            Stage stage = (Stage) ristorantiPane.getScene().getWindow();
            stage.getScene().setRoot(registerRoot);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}