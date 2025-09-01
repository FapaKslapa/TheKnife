package example;

import example.models.Recensione;
import example.models.Risposta;
import example.models.Utente;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;
import services.AuthService;
import services.RecensioneService;
import services.RistoranteService;

import java.util.List;
import java.time.LocalDateTime;

/**
 * Controller per la gestione delle recensioni dei ristoranti.
 * Questa classe fornisce un'interfaccia utente per visualizzare, creare e filtrare
 * le recensioni dei ristoranti, con funzionalità interattive per la valutazione
 * tramite stelle e la visualizzazione di statistiche aggregate.
 *
 * <p>Funzionalità principali:
 * <ul>
 *   <li>Creazione di nuove recensioni con valutazione a stelle</li>
 *   <li>Visualizzazione di tutte le recensioni esistenti con possibilità di filtraggio</li>
 *   <li>Visualizzazione delle risposte dei ristoratori alle recensioni</li>
 *   <li>Calcolo e visualizzazione di statistiche aggregate sulle recensioni</li>
 *   <li>Interfaccia interattiva per la selezione del voto tramite stelle</li>
 * </ul>
 */
public class RecensioneController {
    /** Campi di testo per l'inserimento dei dati della recensione */
    @FXML private TextField idRistoranteField, idUtenteField, titoloField;

    /** Area di testo per l'inserimento del contenuto della recensione */
    @FXML private TextArea testoField;

    /** Etichetta per mostrare il risultato della creazione di una recensione */
    @FXML private Label creaRecResult;

    /** Contenitore per visualizzare le recensioni */
    @FXML private VBox recensioniContainer;

    /** Card per la creazione di una nuova recensione */
    @FXML private VBox creaRecensioneCard;

    /** Pulsanti per la navigazione e la gestione del form di recensione */
    @FXML private Button homeBtn, nuovaRecensioneBtn, chiudiFormBtn;

    /** Contenitore per le stelle selezionabili per il voto */
    @FXML private HBox stelleBox;

    /** ComboBox per il filtraggio delle recensioni */
    @FXML private ComboBox<String> filtroComboBox;

    /** Etichette per visualizzare le statistiche delle recensioni */
    @FXML private Label countLabel, mediaLabel, risposteLabel;

    /** Contenitore per le statistiche */
    @FXML private HBox statsBox;

    /** Servizio per la gestione delle recensioni */
    private RecensioneService recensioneService;

    /** Servizio per la gestione dei ristoranti */
    private RistoranteService ristoranteService;

    /** Servizio per l'autenticazione e gestione utenti */
    private AuthService authService;

    /** ID dell'utente corrente */
    private String currentUserId;

    /** Colori per gli stati delle stelle (vuota, piena, hover) */
    private final Color stellaColorVuota = Color.rgb(200, 200, 200, 0.7);
    private final Color stellaColorPiena = Color.rgb(255, 159, 67, 1.0);
    private final Color stellaColorHover = Color.rgb(255, 221, 87, 1.0);

    /** Voto selezionato tramite le stelle (da 0 a 5) */
    private int votoSelezionato = 0;

    /**
     * Imposta il servizio per le recensioni da utilizzare in questo controller.
     *
     * @param service L'istanza del servizio per le recensioni
     */
    public void setRecensioneService(RecensioneService service) {
        this.recensioneService = service;
    }

    /**
     * Imposta il servizio per i ristoranti da utilizzare in questo controller.
     *
     * @param service L'istanza del servizio per i ristoranti
     */
    public void setRistoranteService(RistoranteService service) {
        this.ristoranteService = service;
    }

    /**
     * Imposta il servizio di autenticazione da utilizzare in questo controller.
     *
     * @param service L'istanza del servizio di autenticazione
     */
    public void setAuthService(AuthService service) {
        this.authService = service;
    }

    /**
     * Imposta l'ID dell'utente corrente e precompila il campo di testo
     * dell'utente nel form di creazione recensione.
     *
     * @param userId L'ID dell'utente corrente
     */
    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
        if (userId != null && !userId.isEmpty()) {
            idUtenteField.setText(userId);
            idUtenteField.setEditable(false);
        }
    }

    /**
     * Inizializza la vista dopo che gli elementi FXML sono stati caricati.
     * Configura l'interfaccia per le stelle, il filtro delle recensioni,
     * nasconde le statistiche e il form di creazione, e carica le recensioni.
     */
    @FXML
    public void initialize() {
        setupStelleBox();
        setupFiltroComboBox();
        // Nascondere le statistiche fino a quando non vengono caricate le recensioni
        statsBox.setVisible(false);
        // Nascondi il form all'inizio
        creaRecensioneCard.setVisible(false);
        creaRecensioneCard.setManaged(false);
        // Carica le recensioni all'avvio
        onShowAllRecensioni();
    }

    /**
     * Mostra o nasconde il form per la creazione di una nuova recensione.
     * Modifica lo stile e il testo del pulsante in base allo stato
     * e pulisce i campi quando il form viene chiuso.
     */
    @FXML
    public void toggleNuovaRecensione() {
        boolean nuovoStato = !creaRecensioneCard.isVisible();
        creaRecensioneCard.setVisible(nuovoStato);
        creaRecensioneCard.setManaged(nuovoStato);
        nuovaRecensioneBtn.setText(nuovoStato ? "- Chiudi form" : "+ Nuova recensione");
        nuovaRecensioneBtn.getStyleClass().clear();
        nuovaRecensioneBtn.getStyleClass().add(nuovoStato ? "button-auth-warning" : "button-auth-accent");

        // Reset del form quando viene chiuso
        if (!nuovoStato) {
            pulisciCampi();
        }
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
     * Configura il ComboBox per il filtraggio delle recensioni.
     * Aggiunge le opzioni di filtro e imposta il listener per applicare
     * i filtri quando viene selezionata un'opzione.
     */
    private void setupFiltroComboBox() {
        filtroComboBox.getItems().clear();
        filtroComboBox.getItems().addAll(
            "Tutte le recensioni",
            "Solo con risposta",
            "Senza risposta",
            "Voto alto (4-5)",
            "Voto medio (3)",
            "Voto basso (1-2)"
        );
        filtroComboBox.getSelectionModel().select(0);
        filtroComboBox.setOnAction(e -> filtraRecensioni());
    }

    /**
     * Gestisce la creazione di una nuova recensione.
     * Valida i dati inseriti, controlla che sia stato selezionato un voto,
     * crea la recensione attraverso il servizio e aggiorna l'interfaccia.
     */
    @FXML
    private void onCreaRecensione() {
        try {
            // Validazione input
            if (idRistoranteField.getText().isEmpty() || idUtenteField.getText().isEmpty() ||
                titoloField.getText().isEmpty() || testoField.getText().isEmpty()) {
                creaRecResult.setText("⚠️ Tutti i campi sono obbligatori");
                return;
            }

            // Ottiene il voto dalle stelle selezionate
            if (votoSelezionato == 0) {
                creaRecResult.setText("⚠️ Seleziona un voto (da 1 a 5 stelle)");
                return;
            }

            Recensione r = recensioneService.creaRecensione(
                    idRistoranteField.getText(),
                    idUtenteField.getText(),
                    votoSelezionato,
                    titoloField.getText(),
                    testoField.getText()
            );

            creaRecResult.setText("✅ Recensione pubblicata!");

            // Pulisce i campi dopo la creazione
            pulisciCampi();

            // Aggiorna la lista delle recensioni
            onShowAllRecensioni();

            // Chiudi automaticamente il form dopo il successo
            toggleNuovaRecensione();
        } catch (Exception e) {
            creaRecResult.setText("❌ Errore: " + e.getMessage());
        }
    }

    /**
     * Pulisce tutti i campi del form di creazione recensione.
     * Cancella il titolo, il testo, azzera il voto selezionato e
     * ripristina lo stato visivo delle stelle.
     */
    private void pulisciCampi() {
        titoloField.clear();
        testoField.clear();
        votoSelezionato = 0;
        ripristinaStelleSelezionate();
        creaRecResult.setText("");
    }

    /**
     * Carica e visualizza tutte le recensioni disponibili.
     * Utilizza il servizio per recuperare tutte le recensioni e le mostra nell'interfaccia.
     */
    @FXML
    private void onShowAllRecensioni() {
        List<Recensione> recensioni = recensioneService.getAllRecensioni();
        mostraRecensioni(recensioni);
    }

    /**
     * Filtra le recensioni in base all'opzione selezionata nel ComboBox.
     * Applica diversi filtri come: recensioni con o senza risposta,
     * recensioni con voto alto, medio o basso.
     */
    private void filtraRecensioni() {
        String filtro = filtroComboBox.getValue();
        List<Recensione> recensioni = recensioneService.getAllRecensioni();

        List<Recensione> recensioniFiltrate = switch (filtro) {
            case "Solo con risposta" -> recensioni.stream()
                .filter(r -> recensioneService.hasRisposta(r.getId()))
                .toList();
            case "Senza risposta" -> recensioni.stream()
                .filter(r -> !recensioneService.hasRisposta(r.getId()))
                .toList();
            case "Voto alto (4-5)" -> recensioni.stream()
                .filter(r -> r.getRate() >= 4)
                .toList();
            case "Voto medio (3)" -> recensioni.stream()
                .filter(r -> r.getRate() == 3)
                .toList();
            case "Voto basso (1-2)" -> recensioni.stream()
                .filter(r -> r.getRate() <= 2)
                .toList();
            default -> recensioni;
        };

        mostraRecensioni(recensioniFiltrate);
    }

    /**
     * Visualizza le recensioni nell'interfaccia utente.
     * Pulisce il contenitore, aggiorna le statistiche e crea una card
     * per ogni recensione. Se non ci sono recensioni, mostra un messaggio appropriato.
     *
     * @param recensioni Lista delle recensioni da visualizzare
     */
    private void mostraRecensioni(List<Recensione> recensioni) {
        recensioniContainer.getChildren().clear();

        // Aggiorna le statistiche
        updateStatistiche(recensioni);

        if (recensioni.isEmpty()) {
            Label noRecensioni = new Label("Nessuna recensione disponibile");
            noRecensioni.getStyleClass().add("label-titolo-grande");
            recensioniContainer.getChildren().add(noRecensioni);
        } else {
            for (Recensione r : recensioni) {
                // Crea una card per ogni recensione
                VBox card = creaRecensioneCard(r);

                // Aggiungi direttamente la card al FlowPane (senza spacer)
                recensioniContainer.getChildren().add(card);
            }
        }
    }

    /**
     * Aggiorna le statistiche visualizzate in base alle recensioni filtrate.
     * Calcola e mostra il numero totale di recensioni, il voto medio e
     * il numero di recensioni che hanno ricevuto risposta.
     *
     * @param recensioni Lista delle recensioni su cui calcolare le statistiche
     */
    private void updateStatistiche(List<Recensione> recensioni) {
        if (recensioni.isEmpty()) {
            statsBox.setVisible(false);
            return;
        }

        statsBox.setVisible(true);

        // Conteggio recensioni
        countLabel.setText("Recensioni totali: " + recensioni.size());

        // Voto medio
        double votomedio = recensioni.stream()
            .mapToDouble(Recensione::getRate)
            .average()
            .orElse(0.0);
        mediaLabel.setText(String.format("Voto medio: %.1f ⭐", votomedio));

        // Conteggio risposte
        long risposteCount = recensioni.stream()
            .filter(r -> recensioneService.getRispostaByRecensione(r.getId()).isPresent())
            .count();
        risposteLabel.setText("Risposte: " + risposteCount + "/" + recensioni.size());
    }

    /**
     * Crea una card visuale per una recensione.
     * Include il voto con stelle, il titolo, il testo, informazioni sul ristorante
     * e sull'utente, e la data di pubblicazione. Se presente, include anche
     * la risposta del ristoratore.
     *
     * @param recensione La recensione da visualizzare
     * @return Un componente VBox configurato come card della recensione
     */
    private VBox creaRecensioneCard(Recensione recensione) {
        VBox card = new VBox(16);
        card.getStyleClass().add("card-recensione-grande");

        HBox header = new HBox(18);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Visualizzazione voto con stelle
        HBox stelleVoto = new HBox(2);
        stelleVoto.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        stelleVoto.getStyleClass().add("stelle-recensione");

        // Crea stelle visive in base al voto
        for (int i = 1; i <= 5; i++) {
            Label stella = new Label(i <= recensione.getRate() ? "★" : "☆");
            stella.setStyle("-fx-text-fill: " + (i <= recensione.getRate() ? "#FF9F43" : "#CCCCCC") + "; -fx-font-size: 18px;");
            stelleVoto.getChildren().add(stella);
        }

        // Titolo recensione
        Label titolo = new Label(recensione.getTitle());
        titolo.getStyleClass().add("label-titolo-grande");

        header.getChildren().addAll(stelleVoto, titolo);

        // Testo della recensione
        Label testo = new Label(recensione.getText());
        testo.getStyleClass().add("label-testo-grande");
        testo.setWrapText(true);

        // Informazioni aggiuntive
        HBox infoBox = new HBox(16);
        infoBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Nome ristorante invece che ID
        String nomeRistorante = "Ristorante";
        if (ristoranteService != null) {
            var ristorante = ristoranteService.getRistoranteById(recensione.getKey_r());
            if (ristorante.isPresent()) {
                nomeRistorante = ristorante.get().getNome();
            }
        }
        Label idRistorante = new Label("Ristorante: " + nomeRistorante);
        idRistorante.getStyleClass().add("label-info-modern");

        // Nome utente invece che ID
        String nomeUtente = "Utente";
        if (authService != null) {
            var utente = authService.getUtenteById(recensione.getKey_user());
            if (utente.isPresent()) {
                nomeUtente = utente.get().getUsername();
            }
        }
        Label idUtente = new Label("Utente: " + nomeUtente);
        idUtente.getStyleClass().add("label-info-modern");

        // Data della recensione formattata
        Label data = new Label("Pubblicata: " + formatData(recensione.getDate()));
        data.getStyleClass().add("label-tempo-grande");

        infoBox.getChildren().addAll(idRistorante, idUtente, data);

        // Aggiungi tutti gli elementi alla card
        card.getChildren().addAll(header, testo, infoBox);

        // Aggiungi risposta se presente
        var rispostaOpt = recensioneService.getRispostaByRecensione(recensione.getId());
        if (rispostaOpt.isPresent()) {
            Risposta risposta = rispostaOpt.get();
            VBox rispostaBox = new VBox(12);
            rispostaBox.getStyleClass().add("risposta-box-light");

            Label rispostaTitle = new Label("Risposta del ristoratore:");
            rispostaTitle.getStyleClass().add("risposta-title-light");

            Label rispostaLabel = new Label(risposta.getTesto());
            rispostaLabel.getStyleClass().add("risposta-testo-light");
            rispostaLabel.setWrapText(true);

            Label dataRisposta = new Label("Risposta pubblicata " +
                recensioneService.getTempoTrascorso(risposta.getDataCreazione()));
            dataRisposta.getStyleClass().add("label-tempo-grande");
            dataRisposta.setStyle("-fx-padding: 8 0 0 0;");

            rispostaBox.getChildren().addAll(rispostaTitle, rispostaLabel, dataRisposta);
            card.getChildren().add(rispostaBox);
        }

        return card;
    }

    /**
     * Naviga alla home page principale dell'applicazione.
     * Carica la vista della home e la imposta come radice della scena corrente.
     */
    @FXML
    private void onHome() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/example/MainView.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) homeBtn.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Formatta una data in un formato leggibile.
     * Utilizza il metodo getTempoTrascorso del RecensioneService se disponibile,
     * altrimenti utilizza una formattazione base.
     *
     * @param dataTime La data da formattare
     * @return Una stringa che rappresenta la data in formato leggibile
     */
    private String formatData(LocalDateTime dataTime) {
        // Semplice formattazione della data
        if (dataTime == null) {
            return "data non disponibile";
        }

        // Utilizziamo il metodo getTempoTrascorso dal RecensioneService se disponibile
        if (recensioneService != null) {
            return recensioneService.getTempoTrascorso(dataTime);
        } else {
            // Formattazione base se il service non è disponibile
            return dataTime.toString();
        }
    }
}

