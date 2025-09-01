package example;

import example.models.Ristorante;
import example.models.Recensione;
import example.models.Risposta;
import example.models.Utente;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import services.RecensioneService;
import services.RistoranteService;
import services.AuthService;

import java.util.List;

public class RistoratoreRecensioniController {
    @FXML private Label ristoranteTitle;
    @FXML private Button homeBtn;
    @FXML private VBox recensioniBox;

    private String ristoratoreId;
    private Ristorante ristorante;
    private final RecensioneService recensioneService = new RecensioneService();
    private final RistoranteService ristoranteService = new RistoranteService();
    private final AuthService authService = new AuthService();

    public void setContext(String ristoratoreId, Ristorante ristorante) {
        this.ristoratoreId = ristoratoreId;
        this.ristorante = ristorante;
        ristoranteTitle.setText("Recensioni di " + ristorante.getNome());
        mostraRecensioni();
    }

    @FXML
    public void initialize() {
        homeBtn.setOnAction(e -> vaiHome());
    }

    private void mostraRecensioni() {
        recensioniBox.getChildren().clear();

        // Intestazione con info ristorante
        HBox infoHeader = new HBox(18);
        infoHeader.setAlignment(javafx.geometry.Pos.CENTER);
        infoHeader.getStyleClass().add("info-header");

        Label infoLabel = new Label("Visualizzi le recensioni lasciate dai clienti per il tuo ristorante");
        infoLabel.getStyleClass().add("label-info-modern");
        infoLabel.setStyle("-fx-font-size: 18px;");

        Separator sep = new Separator();

        infoHeader.getChildren().addAll(infoLabel);
        recensioniBox.getChildren().addAll(infoHeader, sep);

        List<Recensione> recensioni = recensioneService.getRecensioniByRistorante(ristorante.getId());
        if (recensioni.isEmpty()) {
            VBox emptyState = new VBox(16);
            emptyState.setAlignment(javafx.geometry.Pos.CENTER);
            emptyState.setPadding(new javafx.geometry.Insets(60, 0, 40, 0));

            Label noRec = new Label("Nessuna recensione disponibile");
            noRec.getStyleClass().addAll("label-titolo-grande");

            Label subInfo = new Label("Quando i clienti lasceranno recensioni per il tuo ristorante, potrai vederle e rispondere qui.");
            subInfo.getStyleClass().add("label-info-modern");
            subInfo.setStyle("-fx-font-size: 16px;");

            emptyState.getChildren().addAll(noRec, subInfo);
            recensioniBox.getChildren().add(emptyState);
        } else {
            // Statistiche recensioni
            HBox statsBox = new HBox(24);
            statsBox.setAlignment(javafx.geometry.Pos.CENTER);
            statsBox.setPadding(new javafx.geometry.Insets(20, 0, 20, 0));

            Label countLabel = new Label("Recensioni totali: " + recensioni.size());
            countLabel.getStyleClass().add("label-info-modern");
            countLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

            // Calcolo voto medio
            double votomedio = recensioni.stream()
                .mapToDouble(Recensione::getRate)
                .average()
                .orElse(0.0);

            Label mediaLabel = new Label(String.format("Voto medio: %.1f ⭐", votomedio));
            mediaLabel.getStyleClass().add("label-voto-grande");

            // Risposte date
            long risposteCount = recensioni.stream()
                .filter(r -> recensioneService.getRispostaByRecensione(r.getId()).isPresent())
                .count();

            Label risposteLabel = new Label("Risposte date: " + risposteCount + "/" + recensioni.size());
            risposteLabel.getStyleClass().add("label-info-modern");
            risposteLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

            statsBox.getChildren().addAll(countLabel, mediaLabel, risposteLabel);
            recensioniBox.getChildren().add(statsBox);

            // Lista recensioni
            for (Recensione rec : recensioni) {
                VBox card = new VBox(16);
                card.getStyleClass().add("card-recensione-grande");

                HBox header = new HBox(18);
                header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                Label voto = new Label("⭐ " + rec.getRate());
                voto.getStyleClass().add("label-voto-grande");
                Label titolo = new Label(rec.getTitle());
                titolo.getStyleClass().add("label-titolo-grande");
                header.getChildren().addAll(voto, titolo);

                Label testo = new Label(rec.getText());
                testo.getStyleClass().add("label-testo-grande");
                testo.setWrapText(true);

                // Informazioni cliente e data
                HBox infoCliente = new HBox(12);
                infoCliente.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                // Ottieni il nome utente dal suo ID usando AuthService invece di UtenteService
                String nomeUtente = "Cliente";
                Utente cliente = authService.getUtenteById(rec.getKey_user()).orElse(null);
                if (cliente != null) {
                    nomeUtente = cliente.getUsername();
                }

                Label clienteLabel = new Label("Cliente: " + nomeUtente);
                clienteLabel.getStyleClass().add("label-info-modern");
                clienteLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

                Label tempo = new Label("Pubblicata " + recensioneService.getTempoTrascorso(rec.getDate()));
                tempo.getStyleClass().add("label-tempo-grande");

                infoCliente.getChildren().addAll(clienteLabel, tempo);

                card.getChildren().addAll(header, testo, infoCliente);

                // Risposta
                var rispostaOpt = recensioneService.getRispostaByRecensione(rec.getId());
                if (rispostaOpt.isPresent()) {
                    Risposta risposta = rispostaOpt.get();
                    VBox rispostaBox = new VBox(12);
                    rispostaBox.getStyleClass().add("risposta-box-light");
                    Label rispostaTitle = new Label("La tua risposta:");
                    rispostaTitle.getStyleClass().add("risposta-title-light");

                    Label rispostaLabel = new Label(risposta.getTesto());
                    rispostaLabel.getStyleClass().add("risposta-testo-light");
                    rispostaLabel.setWrapText(true);

                    Label dataRisposta = new Label("Risposta pubblicata " +
                        recensioneService.getTempoTrascorso(risposta.getDataCreazione()));
                    dataRisposta.getStyleClass().add("label-tempo-grande");
                    dataRisposta.setStyle("-fx-padding: 8 0 0 0;");

                    HBox rispostaActions = new HBox(16);
                    rispostaActions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

                    Button modificaBtn = new Button("Modifica risposta");
                    modificaBtn.getStyleClass().add("button-auth");
                    modificaBtn.setOnAction(e -> mostraModificaRisposta(rec, risposta));

                    rispostaActions.getChildren().add(modificaBtn);
                    rispostaBox.getChildren().addAll(rispostaTitle, rispostaLabel, dataRisposta, rispostaActions);
                    card.getChildren().add(rispostaBox);
                } else {
                    VBox rispostaBox = new VBox(14);
                    rispostaBox.getStyleClass().add("risposta-box-light");
                    Label rispostaTitle = new Label("Rispondi alla recensione:");
                    rispostaTitle.getStyleClass().add("risposta-title-light");

                    TextArea rispostaArea = new TextArea();
                    rispostaArea.setPromptText("Scrivi una risposta professionale a questa recensione...");
                    rispostaArea.setPrefRowCount(3);
                    rispostaArea.setStyle("-fx-font-size: 16px;");
                    rispostaArea.getStyleClass().add("textfield-auth");

                    HBox actionBox = new HBox(16);
                    actionBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

                    Button inviaBtn = new Button("Pubblica risposta");
                    inviaBtn.getStyleClass().add("button-auth");

                    Label result = new Label("");
                    result.getStyleClass().add("label-auth-result");

                    actionBox.getChildren().addAll(result, inviaBtn);

                    inviaBtn.setOnAction(e -> {
                        String testoRisposta = rispostaArea.getText();
                        if (testoRisposta.isEmpty()) {
                            result.setText("⚠️ Inserisci un testo per la risposta");
                            return;
                        }
                        var opt = recensioneService.aggiungiRisposta(rec.getId(), testoRisposta);
                        if (opt.isPresent()) {
                            result.setText("✅ Risposta pubblicata con successo!");
                            mostraRecensioni();
                        } else {
                            result.setText("❌ Errore nell'invio della risposta");
                        }
                    });

                    rispostaBox.getChildren().addAll(rispostaTitle, rispostaArea, actionBox);
                    card.getChildren().add(rispostaBox);
                }

                // Aggiungi spazio tra le recensioni
                VBox spacer = new VBox();
                spacer.setPrefHeight(24);

                recensioniBox.getChildren().addAll(card, spacer);
            }
        }
    }

    private void mostraModificaRisposta(Recensione recensione, Risposta risposta) {
        // Creo un dialog per modificare la risposta
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Modifica risposta");
        dialog.setHeaderText("Modifica la tua risposta alla recensione");

        // Configuro il contenuto del dialog
        VBox content = new VBox(18);
        content.setPadding(new javafx.geometry.Insets(20, 20, 10, 20));

        Label recTitolo = new Label("Recensione: " + recensione.getTitle());
        recTitolo.getStyleClass().add("label-titolo-grande");
        recTitolo.setStyle("-fx-font-size: 18px;");

        TextArea textArea = new TextArea(risposta.getTesto());
        textArea.setWrapText(true);
        textArea.setPrefRowCount(5);
        textArea.getStyleClass().add("textfield-auth");

        content.getChildren().addAll(recTitolo, textArea);
        dialog.getDialogPane().setContent(content);

        // Aggiungo i pulsanti
        ButtonType confermaButtonType = new ButtonType("Salva modifiche", ButtonBar.ButtonData.OK_DONE);
        ButtonType annullaButtonType = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(confermaButtonType, annullaButtonType);

        // Configuro la risposta del dialog
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confermaButtonType) {
                return textArea.getText();
            }
            return null;
        });

        // Mostro il dialog e processo il risultato
        dialog.showAndWait().ifPresent(nuovoTesto -> {
            if (!nuovoTesto.isEmpty()) {
                var rispostaModificataOpt = recensioneService.modificaRisposta(risposta.getId(), nuovoTesto);
                if (rispostaModificataOpt.isPresent()) {
                    mostraRecensioni();
                }
            }
        });
    }

    private void vaiHome() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/example/RistoratoreHomeView.fxml"));
            javafx.scene.Parent root = loader.load();
            RistoratoreHomeController ctrl = loader.getController();
            AuthService authService = new AuthService();
            ctrl.setRistoratore(authService.getUtenteById(ristoratoreId).orElse(null));
            javafx.stage.Stage stage = (javafx.stage.Stage) homeBtn.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
