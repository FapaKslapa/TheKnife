package example;

import example.models.Ristorante;
import example.models.Recensione;
import example.models.Risposta;
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
import java.time.LocalDateTime;

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
     * Crea una card visuale per una recensione.
     * Include il voto con stelle, il titolo, il testo e la data di pubblicazione.
     * Se presente, include anche la risposta del ristoratore.
     *
     * @param recensione La recensione da visualizzare
     * @return Un componente VBox configurato come card della recensione
     */
    private VBox creaRecensioneCard(Recensione recensione) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card-recensione");

        // Voto con stelle
        HBox stelleVoto = new HBox(2);
        stelleVoto.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        stelleVoto.getStyleClass().add("stelle-recensione");

        for (int i = 1; i <= 5; i++) {
            Label stella = new Label(i <= recensione.getRate() ? "★" : "☆");
            stella.setStyle("-fx-text-fill: " + (i <= recensione.getRate() ? "#FF9F43" : "#CCCCCC") + "; -fx-font-size: 16px;");
            stelleVoto.getChildren().add(stella);
        }

        // Titolo recensione
        Label titolo = new Label(recensione.getTitle());
        titolo.getStyleClass().add("label-titolo");

        // Testo recensione
        Label testo = new Label(recensione.getText());
        testo.getStyleClass().add("label-testo");
        testo.setWrapText(true);

        // Data recensione
        Label tempo = new Label(recensioneService.getTempoTrascorso(recensione.getDate()));
        tempo.getStyleClass().add("label-tempo");

        card.getChildren().addAll(stelleVoto, titolo, testo, tempo);

        // Aggiungi risposta se presente
        Optional<Risposta> rispostaOpt = recensioneService.getRispostaByRecensione(recensione.getId());
        if (rispostaOpt.isPresent()) {
            Risposta risposta = rispostaOpt.get();

            // Separatore tra recensione e risposta
            Separator sep = new Separator();
            sep.setPadding(new javafx.geometry.Insets(4, 0, 4, 0));

            // Contenitore per la risposta
            VBox rispostaBox = new VBox(4);
            rispostaBox.getStyleClass().add("risposta-box-light-mini");

            // Titolo della risposta
            Label rispostaTitle = new Label("Risposta del ristoratore:");
            rispostaTitle.getStyleClass().add("risposta-title-light");
            rispostaTitle.setStyle("-fx-font-size: 14px;");

            // Testo della risposta
            Label rispostaTesto = new Label(risposta.getTesto());
            rispostaTesto.getStyleClass().add("risposta-testo-light");
            rispostaTesto.setStyle("-fx-font-size: 14px;");
            rispostaTesto.setWrapText(true);

            // Data della risposta
            Label dataRisposta = new Label(recensioneService.getTempoTrascorso(risposta.getDataCreazione()));
            dataRisposta.getStyleClass().add("label-tempo");

            rispostaBox.getChildren().addAll(rispostaTitle, rispostaTesto, dataRisposta);
            card.getChildren().addAll(sep, rispostaBox);
        }

        return card;
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
