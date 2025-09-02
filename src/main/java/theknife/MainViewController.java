// Marocco Stefano 762192 VA
// Marin Marco 760622 VA
// Gerti Alessia 762405 VA
// Sibilla Ginevra 761114 VA
package theknife;

import theknife.models.FiltriDiRicerca;
import theknife.models.Recensione;
import theknife.models.Ristorante;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.AuthService;
import services.RecensioneService;
import services.ReverseGeocodingService;
import services.RistoranteService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javafx.collections.FXCollections;

/**
 * Controller per la vista principale dell'applicazione TheKnife.
 * Questa classe gestisce la visualizzazione e l'interazione con la lista dei ristoranti
 * disponibili per tutti gli utenti, anche quelli non autenticati.
 * 
 * <p>Funzionalità principali:
 * <ul>
 *   <li>Visualizzazione dei ristoranti in formato card</li>
 *   <li>Filtraggio dei ristoranti per tipo di cucina, fascia di prezzo, distanza, ecc.</li>
 *   <li>Visualizzazione degli orari di apertura dei ristoranti</li>
 *   <li>Consultazione delle recensioni dei ristoranti</li>
 *   <li>Navigazione verso le pagine di login e registrazione</li>
 * </ul>
 */
public class MainViewController {
    /** Pannello che contiene le card dei ristoranti */
    @FXML private FlowPane ristorantiPane;
    
    /** Pulsanti per login, registrazione e gestione filtri */
    @FXML private Button loginBtn, registerBtn, resetFiltriBtn, cercaBtn;
    @FXML private Button prevPageBtn, nextPageBtn;
    @FXML private Label pageLabel;

    /** ComboBox per i filtri: tipo di cucina, fascia di prezzo e distanza */
    @FXML private ComboBox<String> tipoCucinaCombo, fasciaPrezzoCombo, distanzaCombo;
    
    /** Checkbox per filtri aggiuntivi: consegna a domicilio e ristoranti aperti ora */
    @FXML private CheckBox consegnaCheckbox, apertoOraCheckbox;
    
    /** Campo di testo per inserire la posizione di riferimento per il filtro di distanza */
    @FXML private TextField posizioneField;

    /** Campo di testo per inserire il nome del ristorante */
    @FXML private TextField nomeRistoranteField;

    /** Servizio per la gestione dell'autenticazione degli utenti */
    private final AuthService authService = new AuthService();
    
    /** Servizio per la gestione delle operazioni sui ristoranti */
    private final RistoranteService ristoranteService = new RistoranteService();
    
    /** Servizio per la gestione delle recensioni */
    private final RecensioneService recensioneService = new RecensioneService();
    
    /** Servizio per convertire coordinate in indirizzi e viceversa */
    private final ReverseGeocodingService geocodingService = new ReverseGeocodingService();

    /** Riferimento al pannello principale per mostrare overlay */
    private StackPane mainRoot;

    private int currentPage = 1;
    private final int pageSize = 20;
    private int totalPages = 1;
    private List<Ristorante> ristorantiCorrenti;

    /**
     * Costruttore di default richiesto da JavaFX.
     * Necessario per il caricamento FXML e l'iniezione dei campi annotati con @FXML.
     */
    public MainViewController() {
        // Nessuna inizializzazione specifica richiesta
    }

    /**
     * Inizializza la vista dopo che gli elementi FXML sono stati caricati.
     * Configura i listener per i pulsanti, inizializza i filtri e carica i ristoranti.
     */
    @FXML
    public void initialize() {
        Scene scene = ristorantiPane.getScene();
        if (scene != null && scene.getRoot() instanceof StackPane) {
            mainRoot = (StackPane) scene.getRoot();
        }

        // Inizializzazione filtri
        initFiltri();

        // Carica tutti i ristoranti
        mostraCardRistoranti(1);

        // Gestione login e registrazione
        loginBtn.setOnAction(e -> mostraDialogLogin());
        registerBtn.setOnAction(e -> mostraDialogRegistrazione());
        prevPageBtn.setOnAction(e -> vaiPagina(currentPage - 1));
        nextPageBtn.setOnAction(e -> vaiPagina(currentPage + 1));
    }

    /**
     * Inizializza i componenti dell'interfaccia per i filtri di ricerca.
     * Configura le ComboBox per tipo di cucina, fasce di prezzo e distanza,
     * e imposta i listener per i pulsanti di reset e ricerca.
     */
    private void initFiltri() {
        // Tipi di cucina
        List<String> tipiCucina = Arrays.asList("Tutti", "Italiana", "Cinese", "Messicana", "Indiana", "Giapponese");
        tipoCucinaCombo.setItems(FXCollections.observableArrayList(tipiCucina));
        tipoCucinaCombo.getSelectionModel().selectFirst();

        // Fasce di prezzo
        List<String> fascePrezzo = Arrays.asList("Tutte", "€", "€€", "€€€");
        fasciaPrezzoCombo.setItems(FXCollections.observableArrayList(fascePrezzo));
        fasciaPrezzoCombo.getSelectionModel().selectFirst();

        // Distanze (in km)
        List<String> distanze = Arrays.asList("Tutte", "5 km", "10 km", "20 km", "50 km");
        distanzaCombo.setItems(FXCollections.observableArrayList(distanze));
        distanzaCombo.getSelectionModel().selectFirst();

        // Gestione eventi
        resetFiltriBtn.setOnAction(e -> resetFiltri());
        cercaBtn.setOnAction(e -> applicaFiltri());
    }

    /**
     * Carica tutti i ristoranti dal servizio e li mostra nell'interfaccia.
     * Configura lo stile del pannello che contiene le card e memorizza
     * la lista completa dei ristoranti per un uso successivo.
     */
    private void mostraCardRistoranti(int pagina) {
        List<Ristorante> ristoranti = ristoranteService.getAllRistoranti();
        ristorantiCorrenti = ristoranti;
        mostraRistorantiFiltrati(ristoranti, pagina);
    }

    /**
     * Visualizza i ristoranti filtrati nell'interfaccia utente.
     * Se la lista è vuota, mostra un messaggio appropriato.
     * Altrimenti, crea una card per ogni ristorante con tutte le informazioni rilevanti.
     * 
     * @param ristoranti Lista dei ristoranti da visualizzare
     */
    private void mostraRistorantiFiltrati(List<Ristorante> ristoranti, int pagina) {
        ristorantiPane.getChildren().clear();

        if (ristoranti.isEmpty()) {
            // Mostra messaggio quando non ci sono risultati
            VBox emptyState = new VBox(16);
            emptyState.setStyle("-fx-alignment: center; -fx-padding: 80px 0;");

            Label iconLabel = new Label("🔍");
            iconLabel.setStyle("-fx-font-size: 60px; -fx-text-fill: #CCCCCC;");

            Label titleLabel = new Label("Nessun ristorante corrisponde ai filtri");
            titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

            Label subtitleLabel = new Label("Prova a modificare i filtri di ricerca");
            subtitleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #777777;");

            Button resetBtn = new Button("Reset filtri");
            resetBtn.getStyleClass().add("button-reset");
            resetBtn.setOnAction(e -> resetFiltri());

            emptyState.getChildren().addAll(iconLabel, titleLabel, subtitleLabel, resetBtn);
            ristorantiPane.getChildren().add(emptyState);
            aggiornaBarraPaginazione(1, 1);
            return;
        }

        int total = ristoranti.size();
        totalPages = (int) Math.ceil((double) total / pageSize);
        currentPage = Math.max(1, Math.min(pagina, totalPages));
        int fromIdx = (currentPage - 1) * pageSize;
        int toIdx = Math.min(fromIdx + pageSize, total);
        List<Ristorante> pageList = ristoranti.subList(fromIdx, toIdx);
        for (Ristorante r : pageList) {
            VBox card = new VBox(14);
            card.getStyleClass().add("card-ristorante-modern");

            Label nome = new Label(r.getNome());
            nome.getStyleClass().add("label-nome-modern");

            Label tipo = new Label("🍽 " + r.getTipoCucina());
            tipo.getStyleClass().add("label-info-modern");

            Label prezzo = new Label("💶 " + renderPrezzi(r.getFasciaPrezzo()));
            prezzo.getStyleClass().add("price-tag-modern");

            String indirizzoStr = geocodingService.getAddress(r.getLatitudine(), r.getLongitudine());
            Label indirizzo = new Label("📍 " + indirizzoStr);
            indirizzo.getStyleClass().add("ristorante-indirizzo-modern");

            Label telefono = new Label("📞 " + r.getNumeroTelefono());
            telefono.getStyleClass().add("label-telefono-modern");

            Label consegna = new Label(r.isConsegnaDomicilio() ? "🚚 Consegna a domicilio: Sì" : "🚚 Consegna a domicilio: No");
            consegna.getStyleClass().add("label-consegna-modern");
            if (!r.isConsegnaDomicilio()) consegna.getStyleClass().add("no");

            Label orariTitle = new Label("🕒 Orari di apertura");
            orariTitle.getStyleClass().add("label-info-modern");
            Node orariScorrevoli = creaOrariScorrevoli(r.getOrariApertura());

            Button recensioniBtn = new Button("Leggi Recensioni");
            recensioniBtn.getStyleClass().add("button-recensioni");
            recensioniBtn.setOnAction(e -> mostraOverlayRecensioni(r));

            card.getChildren().addAll(nome, tipo, prezzo, indirizzo, telefono, consegna, orariTitle, orariScorrevoli, recensioniBtn);
            ristorantiPane.getChildren().add(card);
        }
        aggiornaBarraPaginazione(currentPage, totalPages);
    }

    /**
     * Genera una rappresentazione visiva della fascia di prezzo.
     * Converte il valore numerico in una stringa di simboli "€".
     * 
     * @param fascia Valore numerico della fascia di prezzo (1-3)
     * @return Stringa che rappresenta visivamente la fascia di prezzo (es. "€○○" per fascia 1)
     */
    private String renderPrezzi(int fascia) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            sb.append(i < fascia ? "€" : "○");
        }
        return sb.toString();
    }

    /**
     * Crea un componente scrollabile che mostra gli orari di apertura del ristorante per ogni giorno della settimana.
     * Evidenzia il giorno corrente e distingue visivamente i giorni di chiusura.
     * 
     * @param orariApertura Mappa che contiene gli orari di apertura per ogni giorno della settimana
     * @return Componente Node che mostra gli orari in formato scrollabile
     */
    private Node creaOrariScorrevoli(Map<String, String> orariApertura) {
        String[] giorni = {"lunedi", "martedi", "mercoledi", "giovedi", "venerdi", "sabato", "domenica"};
        String[] labels = {"Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom"};
        int oggiIdx = LocalDate.now().getDayOfWeek().getValue() - 1;

        HBox giorniBox = new HBox(18);
        giorniBox.getStyleClass().add("orari-scroll-inner-modern");

        for (int i = 0; i < giorni.length; i++) {
            VBox card = new VBox(6);
            card.getStyleClass().add("giorno-card-modern");
            if (i == oggiIdx) card.getStyleClass().add("oggi");

            Label giornoLabel = new Label(labels[i]);
            giornoLabel.getStyleClass().add("giorno-label-modern");

            VBox orariList = new VBox(3);
            orariList.getStyleClass().add("orari-list-modern");
            String orario = orariApertura != null ? orariApertura.getOrDefault(giorni[i], "Chiuso") : "Chiuso";
            if ("Chiuso".equalsIgnoreCase(orario)) {
                Label chiuso = new Label("Chiuso");
                chiuso.getStyleClass().add("orario-item-modern");
                chiuso.getStyleClass().add("text-danger-modern");
                orariList.getChildren().add(chiuso);
            } else {
                for (String fascia : orario.split(",")) {
                    Label fasciaLabel = new Label(fascia.trim());
                    fasciaLabel.getStyleClass().add("orario-item-modern");
                    orariList.getChildren().add(fasciaLabel);
                }
            }
            card.getChildren().addAll(giornoLabel, orariList);
            giorniBox.getChildren().add(card);
        }

        javafx.scene.control.ScrollPane scroll = new javafx.scene.control.ScrollPane(giorniBox);
        scroll.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setFitToHeight(true);
        scroll.setFitToWidth(false);
        scroll.getStyleClass().add("orari-scroll-wrapper-modern");
        scroll.setPrefHeight(140);
        scroll.setMinHeight(120);
        scroll.setMaxHeight(160);
        scroll.setPannable(true);

        return scroll;
    }

    /**
     * Mostra le recensioni di un ristorante in un overlay scorrevole.
     * Se la configurazione principale non lo consente, utilizza un modale classico
     * come fallback chiamando {@link #mostraDialogRecensioni}.
     * 
     * @param ristorante Il ristorante di cui visualizzare le recensioni
     */
    private void mostraOverlayRecensioni(Ristorante ristorante) {
        if (mainRoot == null) {
            mostraDialogRecensioni(ristorante);
            return;
        }

        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("overlay-recensioni");
        overlay.setPrefSize(mainRoot.getWidth(), mainRoot.getHeight());

        VBox dialog = new VBox(18);
        dialog.getStyleClass().add("dialog-recensioni");

        HBox header = new HBox();
        header.setSpacing(8);

        Label title = new Label("Recensioni di " + ristorante.getNome());
        title.getStyleClass().add("label-dialog-title");

        Button closeBtn = new Button("✕");
        closeBtn.getStyleClass().add("button-close-recensioni");
        closeBtn.setOnAction(e -> {
            FadeTransition fade = new FadeTransition(Duration.millis(180), overlay);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setOnFinished(ev -> mainRoot.getChildren().remove(overlay));
            fade.play();
        });

        header.getChildren().addAll(title, closeBtn);

        // Card scorrevoli
        HBox recensioniCards = new HBox();
        recensioniCards.getStyleClass().add("hbox-recensioni-cards");

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
                recensioniCards.getChildren().add(card);
            }
        }

        ScrollPane scroll = new ScrollPane(recensioniCards);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setFitToHeight(true);
        scroll.setFitToWidth(false);
        scroll.setPrefHeight(260);
        scroll.setMinHeight(180);
        scroll.setMaxHeight(320);
        scroll.setStyle("-fx-background-color: transparent;");

        dialog.getChildren().addAll(header, scroll);
        overlay.getChildren().add(dialog);

        overlay.setOpacity(0.0);
        mainRoot.getChildren().add(overlay);

        FadeTransition fade = new FadeTransition(Duration.millis(220), overlay);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }

    /**
     * Mostra le recensioni di un ristorante in una finestra modale classica.
     * Utilizzato come fallback quando l'overlay non può essere mostrato.
     * 
     * @param ristorante Il ristorante di cui visualizzare le recensioni
     */
    private void mostraDialogRecensioni(Ristorante ristorante) {
        Stage dialog = new Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.setTitle("Recensioni di " + ristorante.getNome());

        VBox root = new VBox(18);
        root.getStyleClass().add("dialog-recensioni");

        Label title = new Label("Recensioni di " + ristorante.getNome());
        title.getStyleClass().add("label-dialog-title");

        HBox recensioniCards = new HBox();
        recensioniCards.getStyleClass().add("hbox-recensioni-cards");

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
                recensioniCards.getChildren().add(card);
            }
        }

        ScrollPane scroll = new ScrollPane(recensioniCards);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setFitToHeight(true);
        scroll.setFitToWidth(false);
        scroll.setPrefHeight(260);
        scroll.setMinHeight(180);
        scroll.setMaxHeight(320);
        scroll.setStyle("-fx-background-color: transparent;");

        root.getChildren().addAll(title, scroll);

        Scene scene = new Scene(root, 540, 340);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/theknife/style.css")).toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    /**
     * Naviga alla pagina di login dell'applicazione.
     * Carica la vista di autenticazione e la imposta come radice della scena corrente.
     */
    private void mostraDialogLogin() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/theknife/AuthView.fxml"));
            Parent authRoot = loader.load(); // fix: cast a Parent, non StackPane
            Stage stage = (Stage) ristorantiPane.getScene().getWindow();
            stage.getScene().setRoot(authRoot);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Naviga alla pagina di registrazione dell'applicazione.
     * Carica la vista di registrazione e la imposta come radice della scena corrente.
     */
    private void mostraDialogRegistrazione() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/theknife/RegisterView.fxml"));
            javafx.scene.Parent registerRoot = loader.load();
            Stage stage = (Stage) ristorantiPane.getScene().getWindow();
            stage.getScene().setRoot(registerRoot);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void aggiornaBarraPaginazione(int pagina, int totPagine) {
        pageLabel.setText("Pagina " + pagina + " di " + totPagine);
        prevPageBtn.setDisable(pagina <= 1);
        nextPageBtn.setDisable(pagina >= totPagine);
    }

    private void vaiPagina(int nuovaPagina) {
        mostraRistorantiFiltrati(ristorantiCorrenti, nuovaPagina);
    }

    private void resetFiltri() {
        tipoCucinaCombo.getSelectionModel().selectFirst();
        fasciaPrezzoCombo.getSelectionModel().selectFirst();
        distanzaCombo.getSelectionModel().selectFirst();
        consegnaCheckbox.setSelected(false);
        apertoOraCheckbox.setSelected(false);
        posizioneField.clear();
        nomeRistoranteField.clear();
        mostraCardRistoranti(1);
    }

    private void applicaFiltri() {
        String nomeParziale = nomeRistoranteField.getText();
        String tipoCucina = tipoCucinaCombo.getValue();
        String fasciaPrezzo = fasciaPrezzoCombo.getValue();
        String distanza = distanzaCombo.getValue();
        boolean consegna = consegnaCheckbox.isSelected();
        boolean apertoOra = apertoOraCheckbox.isSelected();
        String posizione = posizioneField.getText();

        // Usa FiltriDiRicerca.Builder per creare l'oggetto filtri
        FiltriDiRicerca.Builder builder = new FiltriDiRicerca.Builder();

        // Nome ristorante (ricerca parziale)
        if (nomeParziale != null && !nomeParziale.trim().isEmpty()) {
            builder.nomeParziale(nomeParziale.trim());
        }

        // Tipo di cucina
        if (tipoCucina != null && !"Tutti".equals(tipoCucina)) {
            builder.tipoCucina(tipoCucina);
        }

        // Fascia di prezzo
        if (fasciaPrezzo != null && !"Tutte".equals(fasciaPrezzo)) {
            int fascia = switch (fasciaPrezzo) {
                case "€" -> 1;
                case "€€" -> 2;
                case "€€€" -> 3;
                default -> 0;
            };
            builder.fasciaPrezzo(fascia);
        }

        // Consegna a domicilio
        if (consegna) {
            builder.consegnaDomicilio(true);
        }

        // Aperto ora
        if (apertoOra) {
            builder.apertoOra(true);
        }

        // Posizione e distanza
        if (posizione != null && !posizione.trim().isEmpty() && distanza != null && !"Tutte".equals(distanza)) {
            try {
                // Ottieni le coordinate dalla posizione inserita
                double[] coordinate = geocodingService.geocode(posizione);
                if (coordinate != null) {
                    // Estrai il valore numerico dalla stringa di distanza (es. "5 km" -> 5)
                    int distanzaKm = Integer.parseInt(distanza.split(" ")[0]);
                    builder.posizione(coordinate[0], coordinate[1], distanzaKm);
                }
            } catch (Exception ex) {
                // Gestisci eventuali errori di geocoding
                System.err.println("Errore nel geocoding: " + ex.getMessage());
            }
        }

        // Costruisci l'oggetto filtri e applica al servizio
        FiltriDiRicerca filtri = builder.build();
        List<Ristorante> ristorantiFiltrati = ristoranteService.filtriRicerca(filtri);
        ristorantiCorrenti = ristorantiFiltrati;
        mostraRistorantiFiltrati(ristorantiFiltrati, 1);
    }
}
