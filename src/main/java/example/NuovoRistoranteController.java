package example;

import example.models.Utente;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import services.RistoranteService;
import services.ReverseGeocodingService;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controller per la creazione di un nuovo ristorante.
 * Questa classe gestisce l'interfaccia utente che permette ai ristoratori
 * di inserire tutte le informazioni necessarie per creare un nuovo ristorante.
 *
 * <p>Funzionalità principali:
 * <ul>
 *   <li>Inserimento delle informazioni base del ristorante (nome, tipo cucina, ecc.)</li>
 *   <li>Configurazione degli orari di apertura per ogni giorno della settimana</li>
 *   <li>Validazione dei dati inseriti prima della creazione</li>
 *   <li>Georeferenziazione dell'indirizzo in coordinate geografiche</li>
 *   <li>Registrazione del nuovo ristorante nel sistema</li>
 * </ul>
 */
public class NuovoRistoranteController {
    /** Campo di testo per il nome del ristorante */
    @FXML private TextField nomeField;

    /** Campo di testo per il tipo di cucina */
    @FXML private TextField tipoCucinaField;

    /** Selezione per la fascia di prezzo (1-3) */
    @FXML private ComboBox<String> fasciaPrezzoBox;

    /** Campo di testo per il numero di telefono */
    @FXML private TextField telefonoField;

    /** Campo di testo per l'indirizzo del ristorante */
    @FXML private TextField indirizzoField;

    /** Checkbox per indicare se il ristorante offre servizio di consegna a domicilio */
    @FXML private CheckBox consegnaCheck;

    /** Contenitore per le card degli orari di apertura */
    @FXML private HBox orariBox;

    /** Pulsanti per la conferma o l'annullamento della creazione */
    @FXML private Button confermaBtn, annullaBtn;

    /** Etichetta per mostrare il risultato delle operazioni */
    @FXML private Label resultLabel;

    /** Utente ristoratore proprietario del nuovo ristorante */
    private Utente ristoratore;

    /** Servizio per le operazioni sui ristoranti */
    private final RistoranteService ristoranteService = new RistoranteService();

    /** Servizio per la conversione tra indirizzi e coordinate geografiche */
    private final ReverseGeocodingService geocodingService = new ReverseGeocodingService();

    /**
     * Imposta l'utente ristoratore proprietario del nuovo ristorante.
     * Questo metodo deve essere chiamato prima di utilizzare il controller.
     *
     * @param ristoratore L'utente ristoratore che sta creando il nuovo ristorante
     */
    public void setRistoratore(Utente ristoratore) {
        this.ristoratore = ristoratore;
    }

    /**
     * Inizializza la vista dopo che gli elementi FXML sono stati caricati.
     * Configura i controlli dell'interfaccia, crea le card per gli orari
     * e imposta i listener per i pulsanti.
     */
    @FXML
    public void initialize() {
        fasciaPrezzoBox.getItems().addAll("1", "2", "3");
        creaCardOrari();
        confermaBtn.setOnAction(e -> onConferma());
        annullaBtn.setOnAction(e -> tornaHome());
    }

    /**
     * Crea le card per la gestione degli orari di apertura per ogni giorno della settimana.
     * Ogni card contiene:
     * <ul>
     *   <li>Campi per la prima fascia oraria (apertura e chiusura)</li>
     *   <li>Campi per la seconda fascia oraria (apertura e chiusura)</li>
     *   <li>Checkbox per indicare se il ristorante è chiuso quel giorno</li>
     * </ul>
     * I dati inseriti vengono salvati in UserData per essere recuperati in seguito.
     */
    private void creaCardOrari() {
        String[] giorni = {"lunedi", "martedi", "mercoledi", "giovedi", "venerdi", "sabato", "domenica"};
        String[] labels = {"Lunedì", "Martedì", "Mercoledì", "Giovedì", "Venerdì", "Sabato", "Domenica"};
        orariBox.getChildren().clear();
        for (int i = 0; i < giorni.length; i++) {
            VBox card = new VBox(14);
            card.getStyleClass().add("giorno-card-wide");
            card.setStyle("-fx-padding: 24 28 24 28; -fx-alignment: top_center;");
            Label giornoLabel = new Label(labels[i]);
            giornoLabel.getStyleClass().add("giorno-label-large");

            // Prima fascia
            HBox fascia1Row = new HBox(8);
            fascia1Row.setStyle("-fx-alignment: center-left;");
            TextField apertura1 = new TextField();
            apertura1.setPromptText("Apertura 1 (HH:mm)");
            apertura1.getStyleClass().add("textfield-orario-wide");
            apertura1.setTooltip(new Tooltip("Formato: HH:mm"));
            TextField chiusura1 = new TextField();
            chiusura1.setPromptText("Chiusura 1 (HH:mm)");
            chiusura1.getStyleClass().add("textfield-orario-wide");
            chiusura1.setTooltip(new Tooltip("Formato: HH:mm"));
            fascia1Row.getChildren().addAll(apertura1, chiusura1);

            // Seconda fascia
            HBox fascia2Row = new HBox(8);
            fascia2Row.setStyle("-fx-alignment: center-left;");
            TextField apertura2 = new TextField();
            apertura2.setPromptText("Apertura 2 (HH:mm)");
            apertura2.getStyleClass().add("textfield-orario-wide");
            apertura2.setTooltip(new Tooltip("Formato: HH:mm"));
            TextField chiusura2 = new TextField();
            chiusura2.setPromptText("Chiusura 2 (HH:mm)");
            chiusura2.getStyleClass().add("textfield-orario-wide");
            chiusura2.setTooltip(new Tooltip("Formato: HH:mm"));
            fascia2Row.getChildren().addAll(apertura2, chiusura2);

            // Checkbox chiuso
            HBox chiusoRow = new HBox(8);
            chiusoRow.setStyle("-fx-alignment: center-left;");
            CheckBox chiuso = new CheckBox();
            chiuso.getStyleClass().add("label-info-modern");
            Label chiusoLabel = new Label("Chiuso");
            chiusoLabel.getStyleClass().add("label-info-modern");
            chiusoRow.getChildren().addAll(chiuso, chiusoLabel);

            // UserData: [apertura1, chiusura1, apertura2, chiusura2, chiuso, giorno]
            card.setUserData(new Object[]{apertura1, chiusura1, apertura2, chiusura2, chiuso, giorni[i]});
            card.getChildren().addAll(giornoLabel, fascia1Row, fascia2Row, chiusoRow);
            orariBox.getChildren().add(card);
        }
    }

    /**
     * Gestisce la conferma della creazione del nuovo ristorante.
     * Raccoglie tutti i dati inseriti dall'utente, li valida e,
     * se corretti, procede con la creazione del nuovo ristorante.
     * In caso di successo, ritorna alla home del ristoratore.
     */
    private void onConferma() {
        String nome = nomeField.getText();
        String tipoCucina = tipoCucinaField.getText();
        String fasciaPrezzoStr = fasciaPrezzoBox.getValue();
        String telefono = telefonoField.getText();
        String indirizzo = indirizzoField.getText();
        boolean consegna = consegnaCheck.isSelected();
        if (nome.isEmpty() || tipoCucina.isEmpty() || fasciaPrezzoStr == null || telefono.isEmpty() || indirizzo.isEmpty()) {
            resultLabel.setText("Compila tutti i campi obbligatori!");
            return;
        }
        int fasciaPrezzo = Integer.parseInt(fasciaPrezzoStr);
        Map<String, String> orariApertura = new LinkedHashMap<>();
        String[] giorniOrdinati = {"lunedi", "martedi", "mercoledi", "giovedi", "venerdi", "sabato", "domenica"};
        for (String giorno : giorniOrdinati) {
            for (javafx.scene.Node node : orariBox.getChildren()) {
                VBox card = (VBox) node;
                Object[] data = (Object[]) card.getUserData();
                String giornoCard = (String) data[5];
                if (giornoCard.equals(giorno)) {
                    TextField apertura1 = (TextField) data[0];
                    TextField chiusura1 = (TextField) data[1];
                    TextField apertura2 = (TextField) data[2];
                    TextField chiusura2 = (TextField) data[3];
                    CheckBox chiuso = (CheckBox) data[4];

                    boolean primaFasciaVuota = apertura1.getText().isEmpty() || chiusura1.getText().isEmpty();
                    boolean secondaFasciaVuota = apertura2.getText().isEmpty() || chiusura2.getText().isEmpty();

                    if (chiuso.isSelected() || (primaFasciaVuota && secondaFasciaVuota)) {
                        orariApertura.put(giorno, "chiuso");
                    } else {
                        StringBuilder fascia = new StringBuilder();
                        if (!primaFasciaVuota) {
                            if (!isOrarioValido(apertura1.getText()) || !isOrarioValido(chiusura1.getText())) {
                                resultLabel.setText("Orario non valido per " + giorno + " (fascia 1)");
                                return;
                            }
                            fascia.append(apertura1.getText().trim()).append("-").append(chiusura1.getText().trim());
                        }
                        if (!secondaFasciaVuota) {
                            if (!isOrarioValido(apertura2.getText()) || !isOrarioValido(chiusura2.getText())) {
                                resultLabel.setText("Orario non valido per " + giorno + " (fascia 2)");
                                return;
                            }
                            if (fascia.length() > 0) fascia.append(", ");
                            fascia.append(apertura2.getText().trim()).append("-").append(chiusura2.getText().trim());
                        }
                        orariApertura.put(giorno, fascia.toString());
                    }
                }
            }
        }
        double lat = 0.0, lon = 0.0;
        try {
            double[] coords = geocodingService.geocode(indirizzo);
            if (coords != null && coords.length == 2) {
                lat = coords[0];
                lon = coords[1];
            } else {
                resultLabel.setText("Indirizzo non valido o non trovato!");
                return;
            }
        } catch (Exception ex) {
            resultLabel.setText("Errore nella geolocalizzazione dell'indirizzo!");
            return;
        }
        var ristorante = ristoranteService.creaRistorante(nome, tipoCucina, fasciaPrezzo, orariApertura, lat, lon, ristoratore.getId(), telefono, consegna);
        resultLabel.setText("Ristorante creato!");
        tornaHome();
    }

    /**
     * Verifica che l'orario inserito sia in un formato valido (HH:mm).
     *
     * @param orario Stringa contenente l'orario da validare
     * @return true se l'orario è nel formato corretto, false altrimenti
     */
    private boolean isOrarioValido(String orario) {
        // Formato HH:mm
        if (orario == null || orario.length() != 5) return false;
        String[] parts = orario.split(":");
        if (parts.length != 2) return false;
        try {
            int h = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            return h >= 0 && h < 24 && m >= 0 && m < 60;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Naviga alla home page del ristoratore.
     * Carica la vista della home del ristoratore e passa l'oggetto ristoratore al controller.
     */
    private void tornaHome() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/example/RistoratoreHomeView.fxml"));
            javafx.scene.Parent root = loader.load();
            RistoratoreHomeController ctrl = loader.getController();
            ctrl.setRistoratore(ristoratore);
            javafx.stage.Stage stage = (javafx.stage.Stage) annullaBtn.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
