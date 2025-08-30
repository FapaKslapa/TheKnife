package example;

import example.models.Ristorante;
import example.models.Recensione;
import example.models.Risposta;
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
        List<Recensione> recensioni = recensioneService.getRecensioniByRistorante(ristorante.getId());
        if (recensioni.isEmpty()) {
            Label noRec = new Label("Nessuna recensione disponibile.");
            noRec.getStyleClass().add("label-testo");
            recensioniBox.getChildren().add(noRec);
        } else {
            for (Recensione rec : recensioni) {
                VBox card = new VBox(16); // Maggiore spaziatura
                card.getStyleClass().add("card-recensione-grande"); // Solo stile CSS

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

                Label tempo = new Label(recensioneService.getTempoTrascorso(rec.getDate()));
                tempo.getStyleClass().add("label-tempo-grande");

                card.getChildren().addAll(header, testo, tempo);

                // Risposta
                Risposta risposta = recensioneService.getRispostaByRecensione(rec.getId()).orElse(null);
                if (risposta != null) {
                    VBox rispostaBox = new VBox(8);
                    rispostaBox.getStyleClass().add("risposta-box-light");
                    Label rispostaTitle = new Label("Risposta del ristoratore:");
                    rispostaTitle.getStyleClass().add("risposta-title-light");
                    Label rispostaLabel = new Label(risposta.getTesto());
                    rispostaLabel.getStyleClass().add("risposta-testo-light");
                    rispostaLabel.setWrapText(true);
                    rispostaBox.getChildren().addAll(rispostaTitle, rispostaLabel);
                    card.getChildren().add(rispostaBox);
                } else {
                    VBox rispostaBox = new VBox(10);
                    rispostaBox.getStyleClass().add("risposta-box-light");
                    Label rispostaTitle = new Label("Rispondi come ristoratore:");
                    rispostaTitle.getStyleClass().add("risposta-title-light");
                    TextArea rispostaArea = new TextArea();
                    rispostaArea.setPromptText("Scrivi una risposta...");
                    rispostaArea.setPrefRowCount(2);
                    rispostaArea.getStyleClass().add("textfield-auth");
                    Button inviaBtn = new Button("Rispondi");
                    inviaBtn.getStyleClass().add("button-auth");
                    Label result = new Label("");
                    result.getStyleClass().add("label-auth-result");
                    inviaBtn.setOnAction(e -> {
                        String testoRisposta = rispostaArea.getText();
                        if (testoRisposta.isEmpty()) {
                            result.setText("Inserisci un testo per la risposta.");
                            return;
                        }
                        var opt = recensioneService.aggiungiRisposta(rec.getId(), testoRisposta);
                        if (opt.isPresent()) {
                            result.setText("Risposta inviata!");
                            mostraRecensioni();
                        } else {
                            result.setText("Errore nell'invio della risposta.");
                        }
                    });
                    rispostaBox.getChildren().addAll(rispostaTitle, rispostaArea, inviaBtn, result);
                    card.getChildren().add(rispostaBox);
                }
                recensioniBox.getChildren().add(card);
            }
        }
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
