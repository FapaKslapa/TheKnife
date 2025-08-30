package example;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.RistoranteService;

import java.util.List;

public class PreferitiController {
    @FXML private TextField userIdField, ristoranteIdField;
    @FXML private ListView<String> preferitiList;
    @FXML private Label preferitiResult;

    private RistoranteService ristoranteService;

    public void setRistoranteService(RistoranteService service) {
        this.ristoranteService = service;
    }

    @FXML
    private void onAggiungiPreferito() {
        ristoranteService.aggiungiRistoranteAiPreferiti(userIdField.getText(), ristoranteIdField.getText());
        preferitiResult.setText("Aggiunto ai preferiti!");
    }

    @FXML
    private void onRimuoviPreferito() {
        ristoranteService.rimuoviRistoranteDaiPreferiti(userIdField.getText(), ristoranteIdField.getText());
        preferitiResult.setText("Rimosso dai preferiti!");
    }

    @FXML
    private void onShowPreferiti() {
        List<String> ids = ristoranteService.getRistorantiPreferitiByUtente(userIdField.getText());
        preferitiList.getItems().setAll(ids);
    }
}