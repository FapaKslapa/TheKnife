// Marocco Stefano 762192 VA
// Marin Marco 760622 VA
// Gerti Alessia 762405 VA
package theknife;

import theknife.models.Ristorante;
import theknife.models.Utente;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import services.RistoranteService;
import services.ReverseGeocodingService;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controller per la modifica delle informazioni di un ristorante.
 * Questa classe gestisce l'interfaccia utente che permette ai ristoratori
 * di modificare i dettagli del proprio ristorante.
 *
 * <p>Funzionalità principali:
 * <ul>
 *   <li>Modifica delle informazioni base del ristorante (nome, tipo cucina, ecc.)</li>
 *   <li>Gestione degli orari di apertura per ogni giorno della settimana</li>
 *   <li>Validazione dei dati inseriti</li>
 *   <li>Salvataggio delle modifiche nel sistema</li>
 * </ul>
 */
public class ModificaRistoranteController {
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

    /** Pulsanti per la conferma o l'annullamento delle modifiche */
    @FXML private Button confermaBtn, annullaBtn;

    /** Etichetta per mostrare il risultato delle operazioni */
    @FXML private Label resultLabel;

    /** Utente ristoratore proprietario del ristorante */
    private Utente ristoratore;

    /** Ristorante da modificare */
    private Ristorante ristorante;

    /** Servizio per le operazioni sui ristoranti */
    private final RistoranteService ristoranteService = new RistoranteService();

    /** Servizio per la conversione tra indirizzi e coordinate geografiche */
    private final ReverseGeocodingService geocodingService = new ReverseGeocodingService();

    /**
     * Imposta il ristorante da modificare e popola i campi dell'interfaccia.
     * Utilizzato quando l'utente ristoratore è già noto al controller.
     *
     * @param ristorante Il ristorante di cui modificare i dati
     */
    public void setRistorante(Ristorante ristorante) {
        this.ristorante = ristorante;
        popolaCampi();
        // Popola orari dopo aver creato le card
        popolaOrari(ristorante.getOrariApertura());
    }

    /**
     * Imposta sia il ristoratore che il ristorante da modificare.
     * Utilizzato quando si naviga da una vista che non ha informazioni
     * sul ristoratore corrente.
     *
     * @param ristoratore L'utente ristoratore proprietario del ristorante
     * @param ristorante Il ristorante di cui modificare i dati
     */
    public void setContext(Utente ristoratore, Ristorante ristorante) {
        this.ristoratore = ristoratore;
        this.ristorante = ristorante;
        popolaCampi();
        // Popola orari dopo aver creato le card
        popolaOrari(ristorante.getOrariApertura());
    }

    /**
     * Inizializza la vista dopo che gli elementi FXML sono stati caricati.
     * Configura i controlli dell'interfaccia e imposta i listener per i pulsanti.
     */
    @FXML
    public void initialize() {
        fasciaPrezzoBox.getItems().addAll("1", "2", "3");
        creaCardOrari();
        // Popola orari se il ristorante è già stato impostato prima dell'initialize
        if (ristorante != null) {
            popolaOrari(ristorante.getOrariApertura());
        }
        confermaBtn.setOnAction(e -> onConferma());
        annullaBtn.setOnAction(e -> tornaHome());
    }

    /**
     * Popola i campi dell'interfaccia con i dati del ristorante.
     * Converte le coordinate geografiche in un indirizzo leggibile.
     */
    private void popolaCampi() {
        if (ristorante == null) return;
        nomeField.setText(ristorante.getNome());
        tipoCucinaField.setText(ristorante.getTipoCucina());
        fasciaPrezzoBox.setValue(String.valueOf(ristorante.getFasciaPrezzo()));
        telefonoField.setText(ristorante.getNumeroTelefono());
        indirizzoField.setText(geocodingService.getAddress(ristorante.getLatitudine(), ristorante.getLongitudine()));
        consegnaCheck.setSelected(ristorante.isConsegnaDomicilio());
        // RIMUOVI questa chiamata, ora viene gestita in setContext/setRistorante
        // popolaOrari(ristorante.getOrariApertura());
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
     * Popola i campi degli orari di apertura con i dati esistenti del ristorante.
     * Gestisce anche il caso di giorni di chiusura e fasce orarie multiple.
     *
     * @param orariApertura Mappa contenente gli orari di apertura per ogni giorno della settimana
     */
    private void popolaOrari(Map<String, String> orariApertura) {
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
                    String orario = orariApertura != null ? orariApertura.getOrDefault(giorno, "chiuso") : "chiuso";
                    if (orario.equalsIgnoreCase("chiuso") || orario.equalsIgnoreCase("Chiuso")) {
                        chiuso.setSelected(true);
                        apertura1.setText("");
                        chiusura1.setText("");
                        apertura2.setText("");
                        chiusura2.setText("");
                    } else {
                        chiuso.setSelected(false);
                        String[] fasce = orario.split(",");
                        if (fasce.length > 0) {
                            String[] parts1 = fasce[0].trim().split("-");
                            if (parts1.length == 2) {
                                apertura1.setText(parts1[0].trim());
                                chiusura1.setText(parts1[1].trim());
                            } else {
                                apertura1.setText("");
                                chiusura1.setText("");
                            }
                        }
                        if (fasce.length > 1) {
                            String[] parts2 = fasce[1].trim().split("-");
                            if (parts2.length == 2) {
                                apertura2.setText(parts2[0].trim());
                                chiusura2.setText(parts2[1].trim());
                            } else {
                                apertura2.setText("");
                                chiusura2.setText("");
                            }
                        } else {
                            apertura2.setText("");
                            chiusura2.setText("");
                        }
                    }
                }
            }
        }
    }

    /**
     * Gestisce la conferma delle modifiche al ristorante.
     * Raccoglie tutti i dati inseriti, li valida e invia la richiesta di modifica.
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
        var opt = ristoranteService.modificaRistorante(
            ristorante.getId(), nome, tipoCucina, fasciaPrezzo, orariApertura, lat, lon, telefono, consegna
        );
        if (opt.isPresent()) {
            resultLabel.setText("Modifiche salvate!");
            tornaHome();
        } else {
            resultLabel.setText("Errore nella modifica del ristorante!");
        }
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
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/theknife/RistoratoreHomeView.fxml"));
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
