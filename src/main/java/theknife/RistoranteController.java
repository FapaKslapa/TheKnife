// Marocco Stefano 762192 VA
// Marin Marco 760622 VA
// Gerti Alessia 762405 VA
// Sibilla Ginevra 761114 VA
package theknife;

import theknife.models.Ristorante;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.RistoranteService;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

/**
 * Controller per la gestione dei ristoranti nell'applicazione TheKnife.
 * Questa classe si occupa della creazione, visualizzazione e gestione dei ristoranti
 * attraverso l'interfaccia utente JavaFX. Fornisce funzionalità per aggiungere nuovi
 * ristoranti al sistema e visualizzare l'elenco di tutti i ristoranti disponibili.
 */
public class RistoranteController {
    /** Campi di testo per l'inserimento delle informazioni del ristorante */
    @FXML private TextField nomeField, tipoCucinaField, fasciaPrezzoField, orariAperturaField,
            latitudineField, longitudineField, idProprietarioField, numeroTelefonoField;

    /** Casella di controllo per indicare se il ristorante offre il servizio di consegna a domicilio */
    @FXML private CheckBox consegnaDomicilioBox;

    /** Etichetta per mostrare il risultato dell'operazione di creazione */
    @FXML private Label creaResult;

    /** Lista per visualizzare tutti i ristoranti disponibili */
    @FXML private ListView<String> ristorantiList;

    /** Servizio per la gestione delle operazioni sui ristoranti */
    private RistoranteService ristoranteService;

    /** Oggetto Gson per la conversione JSON, utilizzato per gestire gli orari di apertura */
    private final Gson gson = new Gson();

    /**
     * Imposta il servizio di gestione dei ristoranti.
     * Questo metodo viene chiamato per iniettare la dipendenza del servizio nel controller.
     *
     * @param service Il servizio di gestione dei ristoranti da utilizzare
     */
    public void setRistoranteService(RistoranteService service) {
        this.ristoranteService = service;
    }

    /**
     * Gestisce l'evento di creazione di un nuovo ristorante.
     * Raccoglie i dati inseriti nei campi dell'interfaccia utente, li valida
     * e crea un nuovo ristorante attraverso il servizio dedicato. Gli orari di apertura
     * vengono convertiti da formato JSON a una mappa di stringhe.
     * In caso di successo, mostra l'ID del ristorante creato; in caso di errore,
     * mostra un messaggio di errore appropriato.
     */
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

    /**
     * Visualizza tutti i ristoranti disponibili nel sistema.
     * Recupera l'elenco completo dei ristoranti tramite il servizio e
     * popola la lista nell'interfaccia utente con il nome e il tipo di cucina
     * di ciascun ristorante.
     */
    @FXML
    private void onShowAll() {
        List<Ristorante> ristoranti = ristoranteService.getAllRistoranti();
        ristorantiList.getItems().clear();
        for (Ristorante r : ristoranti) {
            ristorantiList.getItems().add(r.getNome() + " - " + r.getTipoCucina());
        }
    }
}