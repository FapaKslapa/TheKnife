package example;

import example.models.Ristorante;
import example.models.Recensione;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import services.RecensioneService;
import services.RistoranteService;
import services.ReverseGeocodingService;

import java.util.List;

public class RecensioniRistoranteController {
    @FXML private Label ristoranteTitle;
    @FXML private Button homeBtn, confermaBtn;
    @FXML private VBox nuovaRecensioneCard;
    @FXML private GridPane recensioniCards;
    @FXML private HBox stelleBox;
    @FXML private TextField titoloField;
    @FXML private TextArea testoField;
    @FXML private Label resultLabel;

    private String userId;
    private Ristorante ristorante;
    private final RecensioneService recensioneService = new RecensioneService();
    private final RistoranteService ristoranteService = new RistoranteService();
    private final ReverseGeocodingService geocodingService = new ReverseGeocodingService();

    public void setContext(String userId, Ristorante ristorante) {
        this.userId = userId;
        this.ristorante = ristorante;
        ristoranteTitle.setText(ristorante.getNome());
        mostraRecensioni();
        setupStelleBox();
    }

    @FXML
    public void initialize() {
        homeBtn.setOnAction(e -> vaiHome());
        confermaBtn.setOnAction(e -> onConfermaRecensione());
    }

    private void mostraRecensioni() {
        recensioniCards.getChildren().clear();
        List<Recensione> recensioni = recensioneService.getRecensioniByRistorante(ristorante.getId());
        if (recensioni.isEmpty()) {
            VBox card = new VBox(8);
            card.getStyleClass().add("card-recensione");
            Label noRec = new Label("Nessuna recensione disponibile.");
            noRec.getStyleClass().add("label-testo");
            card.getChildren().add(noRec);
            recensioniCards.add(card, 0, 0);
        } else {
            int col = 0, row = 0, maxCol = 3;
            for (int i = 0; i < recensioni.size(); i++) {
                Recensione rec = recensioni.get(i);
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
                recensioniCards.add(card, col, row);
                col++;
                if (col >= maxCol) { col = 0; row++; }
            }
        }
    }

    private void setupStelleBox() {
        stelleBox.getChildren().clear();
        ToggleGroup stelleGroup = new ToggleGroup();
        for (int i = 1; i <= 5; i++) {
            ToggleButton stella = new ToggleButton("⭐");
            stella.setUserData(i);
            stella.setStyle("-fx-font-size: 22px; -fx-text-fill: #FF9F43;");
            stella.setToggleGroup(stelleGroup);
            stelleBox.getChildren().add(stella);
        }
        stelleBox.setUserData(stelleGroup);
    }

    private void onConfermaRecensione() {
        ToggleGroup stelleGroup = (ToggleGroup) stelleBox.getUserData();
        Toggle selected = stelleGroup.getSelectedToggle();
        String titolo = titoloField.getText();
        String testo = testoField.getText();
        if (selected == null || titolo.isEmpty() || testo.isEmpty()) {
            resultLabel.setText("Compila tutti i campi e seleziona il voto!");
            return;
        }
        int voto = (int) selected.getUserData();
        recensioneService.creaRecensione(ristorante.getId(), userId, voto, titolo, testo);
        resultLabel.setText("Recensione inserita!");
        titoloField.clear();
        testoField.clear();
        stelleGroup.selectToggle(null);
        mostraRecensioni();
    }

    private void vaiHome() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/example/UserHomeView.fxml"));
            javafx.scene.Parent root = loader.load();
            UserHomeController ctrl = loader.getController();
            ctrl.setUserId(userId);
            javafx.stage.Stage stage = (javafx.stage.Stage) homeBtn.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
