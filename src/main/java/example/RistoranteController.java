package example;

import example.models.Ristorante;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.RistoranteService;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

public class RistoranteController {
    @FXML private TextField nomeField, tipoCucinaField, fasciaPrezzoField, orariAperturaField,
            latitudineField, longitudineField, idProprietarioField, numeroTelefonoField;
    @FXML private CheckBox consegnaDomicilioBox;
    @FXML private Label creaResult;
    @FXML private ListView<String> ristorantiList;

    private RistoranteService ristoranteService;
    private final Gson gson = new Gson();

    public void setRistoranteService(RistoranteService service) {
        this.ristoranteService = service;
    }

    @FXML
    private void onCreaRistorante() {
        try {
            Map<String, String> orari = gson.fromJson(orariAperturaField.getText(), Map.class);
            Ristorante r = ristoranteService.creaRistorante(
                    nomeField.getText(),
                    tipoCucinaField.getText(),
                    Integer.parseInt(fasciaPrezzoField.getText()),
                    orari,
                    Double.parseDouble(latitudineField.getText()),
                    Double.parseDouble(longitudineField.getText()),
                    idProprietarioField.getText(),
                    numeroTelefonoField.getText(),
                    consegnaDomicilioBox.isSelected()
            );
            creaResult.setText("Creato: " + r.getId());
        } catch (Exception e) {
            creaResult.setText("Errore: " + e.getMessage());
        }
    }

    @FXML
    private void onShowAll() {
        List<Ristorante> ristoranti = ristoranteService.getAllRistoranti();
        ristorantiList.getItems().clear();
        for (Ristorante r : ristoranti) {
            ristorantiList.getItems().add(r.getNome() + " - " + r.getTipoCucina());
        }
    }
}