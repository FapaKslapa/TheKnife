// Marocco Stefano 762192 VA
// Marin Marco 760622 VA
// Gerti Alessia 762405 VA
// Sibilla Ginevra 761114 VA
package theknife;

import theknife.models.Ristorante;
import theknife.models.Recensione;
import theknife.models.Risposta;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import services.RecensioneService;
import services.RistoranteService;
import services.ReverseGeocodingService;

import java.util.List;
import java.util.Optional;

/**
 * Controller per la gestione delle recensioni di un ristorante specifico.
 * Questa classe gestisce sia la visualizzazione delle recensioni esistenti di un ristorante
 * che la creazione di nuove recensioni da parte dell'utente corrente.
 *
 * <p>Funzionalità principali:
 * <ul>
 *   <li>Visualizzazione delle recensioni di un ristorante specifico</li>
 *   <li>Creazione di nuove recensioni con valutazione a stelle</li>
 *   <li>Visualizzazione delle risposte del ristoratore alle recensioni</li>
 *   <li>Interfaccia interattiva per la selezione del voto tramite stelle</li>
 *   <li>Navigazione alla home page dell'utente</li>
 * </ul>
 */
public class RecensioniRistoranteController {
    /** Etichetta per visualizzare il nome del ristorante */
    @FXML private Label ristoranteTitle;

    /** Pulsanti per la navigazione e la conferma della recensione */
    @FXML private Button homeBtn, confermaBtn;

    /** Contenitore per il form di inserimento della nuova recensione */
    @FXML private VBox nuovaRecensioneCard;

    /** Contenitore flessibile per le card delle recensioni */
    @FXML private FlowPane recensioniCards;

    /** Contenitore per le stelle selezionabili per il voto */
    @FXML private HBox stelleBox;

    /** Campo di testo per il titolo della recensione */
    @FXML private TextField titoloField;

    /** Area di testo per il contenuto della recensione */
    @FXML private TextArea testoField;

    /** Etichetta per mostrare il risultato dell'operazione */
    @FXML private Label resultLabel;

    /** Form di modifica recensione */
    @FXML private VBox modificaRecensioneCard;
    @FXML private TextField modTitoloField;
    @FXML private TextArea modTestoField;
    @FXML private HBox modStelleBox;
    @FXML private Label modRecResult;
    @FXML private Button salvaModificaBtn, annullaModificaBtn;

    /** ID dell'utente corrente */
    private String userId;

    /** Ristorante oggetto delle recensioni */
    private Ristorante ristorante;

    /** Servizio per la gestione delle recensioni */
    private final RecensioneService recensioneService = new RecensioneService();

    /** Servizio per la gestione dei ristoranti */
    private final RistoranteService ristoranteService = new RistoranteService();

    /** Servizio per la conversione di coordinate geografiche */
    private final ReverseGeocodingService geocodingService = new ReverseGeocodingService();

    /** Colori per gli stati delle stelle (vuota, piena, hover) */
    private final Color stellaColorVuota = Color.rgb(200, 200, 200, 0.7);
    private final Color stellaColorPiena = Color.rgb(255, 159, 67, 1.0);
    private final Color stellaColorHover = Color.rgb(255, 221, 87, 1.0);

    /** Voto selezionato tramite le stelle (da 0 a 5) */
    private int votoSelezionato = 0;

    /** Recensione attualmente in modifica */
    private Recensione recensioneInModifica;
    private int votoModifica = 0;

    /**
     * Imposta il contesto per il controller, specificando l'utente corrente e il ristorante.
     * Questo metodo deve essere chiamato prima di utilizzare il controller.
     * Imposta il titolo della pagina, configura le stelle per il voto e carica le recensioni.
     *
     * @param userId ID dell'utente corrente
     * @param ristorante Il ristorante di cui visualizzare e gestire le recensioni
     */
    public void setContext(String userId, Ristorante ristorante) {
        this.userId = userId;
        this.ristorante = ristorante;
        ristoranteTitle.setText(ristorante.getNome());
        setupStelleBox();
        mostraRecensioni();
    }

    /**
     * Inizializza la vista dopo che gli elementi FXML sono stati caricati.
     * Configura i listener per i pulsanti e inizializza l'etichetta del risultato.
     */
    @FXML
    public void initialize() {
        homeBtn.setOnAction(e -> vaiHome());
        confermaBtn.setOnAction(e -> onConfermaRecensione());
        resultLabel.setText("");
    }

    /**
     * Configura il contenitore delle stelle per la valutazione.
     * Crea cinque stelle selezionabili con effetti di hover e clic,
     * utilizzando poligoni personalizzati a forma di stella.
     */
    private void setupStelleBox() {
        stelleBox.getChildren().clear();

        for (int i = 1; i <= 5; i++) {
            Polygon stella = creaStellaPoligono();
            stella.setFill(stellaColorVuota);
            stella.setUserData(i);

            // Animazione e interattività
            stella.setOnMouseEntered(e -> {
                Polygon s = (Polygon) e.getSource();
                int val = (int) s.getUserData();
                evidenziaStelleFinoA(val);
            });

            stella.setOnMouseExited(e -> {
                ripristinaStelleSelezionate();
            });

            stella.setOnMouseClicked(e -> {
                Polygon s = (Polygon) e.getSource();
                int val = (int) s.getUserData();
                selezionaStelle(val);

                // Animazione di selezione
                ScaleTransition st = new ScaleTransition(Duration.millis(200), s);
                st.setFromX(1.0);
                st.setFromY(1.0);
                st.setToX(1.3);
                st.setToY(1.3);
                st.setCycleCount(2);
                st.setAutoReverse(true);
                st.play();
            });

            stelleBox.getChildren().add(stella);
        }
    }

    /**
     * Crea un poligono a forma di stella a cinque punte.
     * Configura la geometria, lo stile e il cursore del poligono.
     *
     * @return Un oggetto Polygon configurato come una stella a cinque punte
     */
    private Polygon creaStellaPoligono() {
        Polygon stella = new Polygon();

        // Punti della stella a 5 punte
        double size = 12.0;
        double centerX = size;
        double centerY = size;

        for (int i = 0; i < 5; i++) {
            double outerAngle = 2.0 * Math.PI * i / 5.0 - Math.PI / 2.0;
            double innerAngle = outerAngle + Math.PI / 5.0;

            // Punto esterno
            stella.getPoints().add(centerX + size * Math.cos(outerAngle));
            stella.getPoints().add(centerY + size * Math.sin(outerAngle));

            // Punto interno
            stella.getPoints().add(centerX + size * 0.4 * Math.cos(innerAngle));
            stella.getPoints().add(centerY + size * 0.4 * Math.sin(innerAngle));
        }

        stella.setStroke(Color.rgb(230, 126, 34, 0.7));
        stella.setStrokeWidth(1.0);
        stella.setCursor(javafx.scene.Cursor.HAND);

        return stella;
    }

    /**
     * Evidenzia le stelle fino al valore specificato quando l'utente passa il mouse.
     * Le stelle fino al valore indicato vengono colorate con il colore di hover,
     * mentre le stelle successive rimangono vuote.
     *
     * @param valore Il valore (1-5) fino al quale evidenziare le stelle
     */
    private void evidenziaStelleFinoA(int valore) {
        for (int i = 0; i < stelleBox.getChildren().size(); i++) {
            Polygon stella = (Polygon) stelleBox.getChildren().get(i);
            int stellaValore = (int) stella.getUserData();

            if (stellaValore <= valore) {
                stella.setFill(stellaColorHover);
            } else {
                stella.setFill(stellaColorVuota);
            }
        }
    }

    /**
     * Ripristina lo stato delle stelle in base al voto attualmente selezionato.
     * Le stelle fino al voto selezionato vengono colorate come piene,
     * mentre le stelle successive rimangono vuote.
     */
    private void ripristinaStelleSelezionate() {
        for (int i = 0; i < stelleBox.getChildren().size(); i++) {
            Polygon stella = (Polygon) stelleBox.getChildren().get(i);
            int stellaValore = (int) stella.getUserData();

            if (stellaValore <= votoSelezionato) {
                stella.setFill(stellaColorPiena);
            } else {
                stella.setFill(stellaColorVuota);
            }
        }
    }

    /**
     * Imposta il voto selezionato in base al numero di stelle cliccate.
     * Aggiorna lo stato visivo delle stelle per riflettere la selezione.
     *
     * @param valore Il valore del voto selezionato (1-5)
     */
    private void selezionaStelle(int valore) {
        votoSelezionato = valore;
        ripristinaStelleSelezionate();
    }

    /** Gestione stelle per la modifica */
    private void setupModStelleBox() {
        modStelleBox.getChildren().clear();
        for (int i = 1; i <= 5; i++) {
            Polygon stella = new Polygon();
            double size = 12.0;
            double centerX = size;
            double centerY = size;
            for (int j = 0; j < 5; j++) {
                double outerAngle = 2.0 * Math.PI * j / 5.0 - Math.PI / 2.0;
                double innerAngle = outerAngle + Math.PI / 5.0;
                stella.getPoints().add(centerX + size * Math.cos(outerAngle));
                stella.getPoints().add(centerY + size * Math.sin(outerAngle));
                stella.getPoints().add(centerX + size * 0.4 * Math.cos(innerAngle));
                stella.getPoints().add(centerY + size * 0.4 * Math.sin(innerAngle));
            }
            stella.setFill(i <= votoModifica ? stellaColorPiena : stellaColorVuota);
            stella.setUserData(i);
            stella.setOnMouseEntered(e -> {
                Polygon s = (Polygon) e.getSource();
                int val = (int) s.getUserData();
                for (int k = 0; k < modStelleBox.getChildren().size(); k++) {
                    Polygon st = (Polygon) modStelleBox.getChildren().get(k);
                    st.setFill(k < val ? stellaColorPiena : stellaColorVuota);
                }
            });
            stella.setOnMouseExited(e -> {
                for (int k = 0; k < modStelleBox.getChildren().size(); k++) {
                    Polygon st = (Polygon) modStelleBox.getChildren().get(k);
                    st.setFill(k < votoModifica ? stellaColorPiena : stellaColorVuota);
                }
            });
            stella.setOnMouseClicked(e -> {
                Polygon s = (Polygon) e.getSource();
                int val = (int) s.getUserData();
                votoModifica = val;
                for (int k = 0; k < modStelleBox.getChildren().size(); k++) {
                    Polygon st = (Polygon) modStelleBox.getChildren().get(k);
                    st.setFill(k < votoModifica ? stellaColorPiena : stellaColorVuota);
                }
            });
            modStelleBox.getChildren().add(stella);
        }
    }

    /**
     * Carica e visualizza le recensioni del ristorante corrente.
     * Se non ci sono recensioni, mostra un messaggio appropriato.
     * Altrimenti, crea una card per ogni recensione e la aggiunge al contenitore.
     */
    private void mostraRecensioni() {
        recensioniCards.getChildren().clear();
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
                VBox card = creaRecensioneCard(rec);
                recensioniCards.getChildren().add(card);
            }
        }
    }

    /**
     * Crea una card visuale per una recensione, con pulsanti Modifica/Elimina
     * Include il voto con stelle, il titolo, il testo e la data di pubblicazione.
     * Se presente, include anche la risposta del ristoratore.
     *
     * @param recensione La recensione da visualizzare
     * @return Un componente VBox configurato come card della recensione
     */
    private VBox creaRecensioneCard(Recensione recensione) {
        VBox card = new VBox(16);
        card.getStyleClass().add("card-recensione-grande");
        HBox header = new HBox(18);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        // Voto stelle
        HBox stelleVoto = new HBox(2);
        stelleVoto.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        stelleVoto.getStyleClass().add("stelle-recensione");
        for (int i = 1; i <= 5; i++) {
            Label stella = new Label(i <= recensione.getRate() ? "★" : "☆");
            stella.setStyle("-fx-text-fill: " + (i <= recensione.getRate() ? "#FF9F43" : "#CCCCCC") + "; -fx-font-size: 18px;");
            stelleVoto.getChildren().add(stella);
        }
        Label titolo = new Label(recensione.getTitle());
        titolo.getStyleClass().add("label-titolo-grande");
        header.getChildren().addAll(stelleVoto, titolo);
        Label testo = new Label(recensione.getText());
        testo.getStyleClass().add("label-testo-grande");
        testo.setWrapText(true);
        HBox infoBox = new HBox(16);
        infoBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label utente = new Label("Utente: " + recensione.getKey_user());
        utente.getStyleClass().add("label-info-modern");
        Label data = new Label("Pubblicata: " + recensione.getDate());
        data.getStyleClass().add("label-tempo-grande");
        infoBox.getChildren().addAll(utente, data);
        card.getChildren().addAll(header, testo, infoBox);
        // Risposta del ristoratore
        Optional<Risposta> rispostaOpt = recensioneService.getRispostaByRecensione(recensione.getId());
        if (rispostaOpt.isPresent()) {
            Risposta risposta = rispostaOpt.get();
            VBox rispostaBox = new VBox(12);
            rispostaBox.getStyleClass().add("risposta-box-light");
            Label rispostaTitle = new Label("Risposta del ristoratore:");
            rispostaTitle.getStyleClass().add("risposta-title-light");
            Label rispostaLabel = new Label(risposta.getTesto());
            rispostaLabel.getStyleClass().add("risposta-testo-light");
            rispostaLabel.setWrapText(true);
            rispostaBox.getChildren().addAll(rispostaTitle, rispostaLabel);
            card.getChildren().add(rispostaBox);
        }
        // Pulsanti Modifica/Elimina
        HBox azioniBox = new HBox(10);
        azioniBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        Button modificaBtn = new Button("Modifica");
        modificaBtn.getStyleClass().add("button-modifica-rec");
        modificaBtn.setOnAction(e -> mostraFormModifica(recensione));
        Button eliminaBtn = new Button("Elimina");
        eliminaBtn.getStyleClass().add("button-elimina-rec");
        eliminaBtn.setOnAction(e -> confermaEliminazione(recensione));
        azioniBox.getChildren().addAll(modificaBtn, eliminaBtn);
        card.getChildren().add(azioniBox);
        return card;
    }

    /** Mostra il form di modifica recensione, precompilando i dati */
    private void mostraFormModifica(Recensione recensione) {
        recensioneInModifica = recensione;
        modificaRecensioneCard.setVisible(true);
        modificaRecensioneCard.setManaged(true);
        modTitoloField.setText(recensione.getTitle());
        modTestoField.setText(recensione.getText());
        votoModifica = recensione.getRate();
        setupModStelleBox();
        modRecResult.setText("");
    }

    /** Nasconde il form di modifica recensione */
    @FXML
    private void annullaModificaRecensione() {
        modificaRecensioneCard.setVisible(false);
        modificaRecensioneCard.setManaged(false);
        recensioneInModifica = null;
        votoModifica = 0;
        modRecResult.setText("");
    }

    /** Salva la modifica della recensione */
    @FXML
    private void salvaModificaRecensione() {
        if (recensioneInModifica == null) return;
        if (modTitoloField.getText().isEmpty() || modTestoField.getText().isEmpty() || votoModifica == 0) {
            modRecResult.setText("⚠️ Tutti i campi sono obbligatori e il voto deve essere selezionato");
            return;
        }
        var opt = recensioneService.modificaRecensione(
            recensioneInModifica.getId(),
            modTitoloField.getText(),
            modTestoField.getText(),
            votoModifica
        );
        if (opt.isPresent()) {
            modRecResult.setText("✅ Modifica salvata!");
            annullaModificaRecensione();
            mostraRecensioni();
        } else {
            modRecResult.setText("❌ Errore nella modifica");
        }
    }

    /** Conferma ed elimina la recensione */
    private void confermaEliminazione(Recensione recensione) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma eliminazione");
        alert.setHeaderText("Vuoi eliminare questa recensione?");
        alert.setContentText("Questa azione è irreversibile.");
        var result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean ok = recensioneService.eliminaRecensione(recensione.getId());
            if (ok) {
                mostraRecensioni();
            } else {
                Alert err = new Alert(Alert.AlertType.ERROR, "Errore nell'eliminazione.");
                err.showAndWait();
            }
        }
    }

    /**
     * Gestisce la conferma della creazione di una nuova recensione.
     * Valida i dati inseriti, controlla che sia stato selezionato un voto,
     * crea la recensione tramite il servizio e aggiorna l'interfaccia.
     * In caso di successo, pulisce i campi e aggiorna la lista delle recensioni.
     */
    @FXML
    private void onConfermaRecensione() {
        try {
            // Validazione input
            if (titoloField.getText().isEmpty() || testoField.getText().isEmpty()) {
                resultLabel.setText("⚠️ Titolo e testo sono obbligatori");
                return;
            }

            // Ottiene il voto dalle stelle selezionate
            if (votoSelezionato == 0) {
                resultLabel.setText("⚠️ Seleziona un voto (da 1 a 5 stelle)");
                return;
            }

            // Crea la recensione
            Recensione nuovaRecensione = recensioneService.creaRecensione(
                    ristorante.getId(),
                    userId,
                    votoSelezionato,
                    titoloField.getText(),
                    testoField.getText()
            );

            // Feedback positivo e pulizia
            resultLabel.setText("✅ Recensione pubblicata!");
            titoloField.clear();
            testoField.clear();
            votoSelezionato = 0;
            ripristinaStelleSelezionate();

            // Aggiorna la lista delle recensioni
            mostraRecensioni();
        } catch (Exception e) {
            resultLabel.setText("❌ Errore: " + e.getMessage());
        }
    }

    /**
     * Naviga alla home page dell'utente.
     * Carica la vista della home dell'utente e passa l'ID dell'utente al controller.
     */
    private void vaiHome() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/theknife/UserHomeView.fxml"));
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
