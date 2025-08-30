package example;

import example.models.Recensione;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.RecensioneService;

import java.util.List;

public class RecensioneController {
    @FXML private TextField idRistoranteField, idUtenteField, votoField, titoloField, testoField;
    @FXML private Label creaRecResult;
    @FXML private ListView<String> recensioniList;

    private RecensioneService recensioneService;

    public void setRecensioneService(RecensioneService service) {
        this.recensioneService = service;
    }

    @FXML
    private void onCreaRecensione() {
        try {
            Recensione r = recensioneService.creaRecensione(
                    idRistoranteField.getText(),
                    idUtenteField.getText(),
                    Integer.parseInt(votoField.getText()),
                    titoloField.getText(),
                    testoField.getText()
            );
            creaRecResult.setText("Recensione creata: " + r.getId());
        } catch (Exception e) {
            creaRecResult.setText("Errore: " + e.getMessage());
        }
    }

    @FXML
    private void onShowAllRecensioni() {
        List<Recensione> recensioni = recensioneService.getAllRecensioni();
        recensioniList.getItems().clear();
        for (Recensione r : recensioni) {
            recensioniList.getItems().add(r.getTitle() + " - " + r.getRate());
        }
    }
}