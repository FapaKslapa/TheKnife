// Marocco Stefano 762192 VA
// Marin Marco 760622 VA
// Gerti Alessia 762405 VA
// Sibilla Ginevra 761114 VA
package theknife;

import theknife.models.FiltriDiRicerca;
import theknife.models.Ristorante;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import services.RistoranteService;
import services.RecensioneService;
import services.ReverseGeocodingService;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

/**
 * Controller per la vista dei ristoranti preferiti dell'utente.
 * Questa classe gestisce la visualizzazione, il filtraggio e l'interazione con i ristoranti
 * che l'utente ha salvato come preferiti.
 *
 * <p>Funzionalità principali:
 * <ul>
 *   <li>Visualizzazione dei ristoranti preferiti in una griglia</li>
 *   <li>Filtraggio dei ristoranti preferiti per tipo di cucina, fascia di prezzo, ecc.</li>
 *   <li>Rimozione dei ristoranti dalla lista dei preferiti</li>
 *   <li>Navigazione alle recensioni di un ristorante</li>
 * </ul>
 */
public class PreferitiViewController {
    /** Pannello che contiene le card dei ristoranti preferiti */
    @FXML private FlowPane preferitiPane;

    /** Pulsanti per la navigazione e gestione dei filtri */
    @FXML private Button homeBtn, resetFiltriBtn, cercaBtn;

    /** ComboBox per la selezione dei filtri di tipo cucina e fascia di prezzo */
    @FXML private ComboBox<String> tipoCucinaCombo, fasciaPrezzoCombo;

    /** Checkbox per filtri aggiuntivi (consegna a domicilio, aperto ora) */
    @FXML private CheckBox consegnaCheckbox, apertoOraCheckbox;

    /** Campo di testo per la ricerca per nome del ristorante */
    @FXML private TextField nomeRistoranteField;

    /** ID dell'utente corrente */
    private String userId;

    /** Servizio per le operazioni sui ristoranti */
    private final RistoranteService ristoranteService = new RistoranteService();

    /** Servizio per le operazioni sulle recensioni */
    private final RecensioneService recensioneService = new RecensioneService();

    /** Servizio per ottenere gli indirizzi dalle coordinate geografiche */
    private final ReverseGeocodingService geocodingService = new ReverseGeocodingService();

    /**
     * Lista completa dei ristoranti preferiti (cache per non dover ricaricare dopo il filtraggio)
     */
    private List<Ristorante> tuttiRistorantiPreferiti;

    /**
     * Imposta l'ID dell'utente corrente e carica i suoi ristoranti preferiti.
     *
     * @param userId ID dell'utente corrente
     */
    public void setUserId(String userId) {
        this.userId = userId;
        mostraPreferiti();
    }

    /**
     * Inizializza la vista dopo che gli elementi FXML sono stati caricati.
     * Configura i listener per i pulsanti e inizializza i filtri.
     */
    @FXML
    public void initialize() {
        homeBtn.setOnAction(e -> vaiHome());

        // Inizializza i filtri
        initFiltri();
    }

    /**
     * Inizializza i componenti dell'interfaccia per i filtri di ricerca.
     * Configura le ComboBox per tipo di cucina e fasce di prezzo e imposta i listener per i pulsanti.
     */
    private void initFiltri() {
        // Tipi di cucina (esempio statico, da sostituire con valori dinamici se necessario)
        List<String> tipiCucina = Arrays.asList("Tutti", "Italiana", "Cinese", "Messicana", "Indiana", "Giapponese");
        tipoCucinaCombo.setItems(FXCollections.observableArrayList(tipiCucina));
        tipoCucinaCombo.getSelectionModel().selectFirst();

        // Fasce di prezzo
        List<String> fascePrezzo = Arrays.asList("Tutte", "€", "€€", "€€€");
        fasciaPrezzoCombo.setItems(FXCollections.observableArrayList(fascePrezzo));
        fasciaPrezzoCombo.getSelectionModel().selectFirst();

        // Reset dei filtri
        resetFiltriBtn.setOnAction(e -> resetFiltri());

        // Bottone cerca
        cercaBtn.setOnAction(e -> applicaFiltri());
    }

    /**
     * Reimposta tutti i filtri ai valori predefiniti e mostra nuovamente tutti i ristoranti preferiti.
     */
    private void resetFiltri() {
        tipoCucinaCombo.getSelectionModel().selectFirst();
        fasciaPrezzoCombo.getSelectionModel().selectFirst();
        consegnaCheckbox.setSelected(false);
        apertoOraCheckbox.setSelected(false);
        nomeRistoranteField.clear();

        // Mostra tutti i ristoranti preferiti
        mostraPreferiti();
    }

    /**
     * Applica i filtri selezionati dall'utente e mostra i ristoranti preferiti filtrati.
     * Utilizza il pattern Builder per costruire l'oggetto FiltriDiRicerca.
     */
    private void applicaFiltri() {
        String nomeParziale = nomeRistoranteField.getText();
        String tipoCucina = tipoCucinaCombo.getValue();
        String fasciaPrezzo = fasciaPrezzoCombo.getValue();
        boolean consegna = consegnaCheckbox.isSelected();
        boolean apertoOra = apertoOraCheckbox.isSelected();

        FiltriDiRicerca.Builder builder = new FiltriDiRicerca.Builder();
        if (nomeParziale != null && !nomeParziale.trim().isEmpty()) {
            builder.nomeParziale(nomeParziale.trim());
        }

        // Tipo di cucina
        if (tipoCucina != null && !"Tutti".equals(tipoCucina)) {
            builder.tipoCucina(tipoCucina);
        }

        // Fascia di prezzo
        if (fasciaPrezzo != null && !"Tutte".equals(fasciaPrezzo)) {
            int fascia = 0;
            switch (fasciaPrezzo) {
                case "€": fascia = 1; break;
                case "€€": fascia = 2; break;
                case "€€€": fascia = 3; break;
            }
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

        // Costruisci l'oggetto filtri e applica al servizio
        FiltriDiRicerca filtri = builder.build();

        // Filtra solo i ristoranti preferiti
        List<Ristorante> ristorantiFiltrati = ristoranteService.filtriRicerca(filtri).stream()
                .filter(r -> ristoranteService.isRistorantePreferito(userId, r.getId()))
                .collect(Collectors.toList());

        mostraRistorantiFiltrati(ristorantiFiltrati);
    }

    /**
     * Visualizza i ristoranti filtrati nell'interfaccia utente.
     * Se la lista è vuota, mostra un messaggio appropriato.
     * Altrimenti, crea una card per ogni ristorante con tutte le informazioni rilevanti.
     *
     * @param ristoranti Lista dei ristoranti da visualizzare
     */
    private void mostraRistorantiFiltrati(List<Ristorante> ristoranti) {
        preferitiPane.getChildren().clear();

        if (ristoranti.isEmpty()) {
            // Mostra messaggio quando non ci sono preferiti che corrispondono ai filtri
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
            preferitiPane.getChildren().add(emptyState);
            return;
        }

        for (Ristorante r : ristoranti) {
            VBox card = new VBox(14);
            card.getStyleClass().add("card-ristorante-modern");

            Label nome = new Label(r.getNome());
            nome.getStyleClass().add("label-nome-modern");

            Label tipo = new Label("🍽 " + r.getTipoCucina());
            tipo.getStyleClass().add("label-info-modern");

            Label prezzo = new Label("💶 " + renderPrezzi(r.getFasciaPrezzo()));
            prezzo.getStyleClass().add("price-tag-modern");

            String indirizzo = "";
            try {
                indirizzo = geocodingService.getAddress(r.getLatitudine(), r.getLongitudine());
            } catch (Exception ex) { indirizzo = "Indirizzo non disponibile"; }

            Label indirizzoLabel = new Label("📍 " + indirizzo);
            indirizzoLabel.getStyleClass().add("ristorante-indirizzo-modern");

            Label telefono = new Label("📞 " + r.getNumeroTelefono());
            telefono.getStyleClass().add("label-telefono-modern");

            Label consegna = new Label(r.isConsegnaDomicilio() ? "🚚 Consegna a domicilio: Sì" : "🚚 Consegna a domicilio: No");
            consegna.getStyleClass().add("label-consegna-modern");
            if (!r.isConsegnaDomicilio()) consegna.getStyleClass().add("no");

            Label orariTitle = new Label("🕒 Orari di apertura");
            orariTitle.getStyleClass().add("label-info-modern");
            Node orariScorrevoli = creaOrariScorrevoli(r.getOrariApertura());

            HBox actions = new HBox(16);
            actions.setPadding(new Insets(8, 0, 0, 0));
            actions.setStyle("-fx-alignment: center-left;");

            // Icona recensioni (fumetto)
            SVGPath recensioniIcon = new SVGPath();
            recensioniIcon.setContent("M20 2H4c-1.1 0-2 .9-2 2v16l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z");
            recensioniIcon.getStyleClass().add("icon-navbar");
            Button recensioniBtn = new Button();
            recensioniBtn.getStyleClass().add("icon-btn");
            recensioniBtn.setTooltip(new Tooltip("Recensioni"));
            recensioniBtn.setGraphic(recensioniIcon);
            recensioniBtn.setOnAction(e -> mostraPaginaRecensioni(r));

            // Icona rimuovi dai preferiti (cuore pieno)
            SVGPath cuoreIcon = new SVGPath();
            cuoreIcon.setContent("M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z");
            cuoreIcon.getStyleClass().add("icon-navbar");
            cuoreIcon.setFill(Color.web("#FF9F43"));

            Button rimuoviBtn = new Button();
            rimuoviBtn.getStyleClass().add("icon-btn");
            rimuoviBtn.setTooltip(new Tooltip("Rimuovi dai preferiti"));
            rimuoviBtn.setGraphic(cuoreIcon);
            rimuoviBtn.setStyle("-fx-background-color: #FFF7ED; -fx-effect: dropshadow(gaussian, #FF9F43AA, 8, 0.18, 0, 2);");
            rimuoviBtn.setOnAction(e -> {
                ristoranteService.rimuoviRistoranteDaiPreferiti(userId, r.getId());
                mostraPreferiti(); // Aggiorna la lista dopo la rimozione
            });

            actions.getChildren().addAll(recensioniBtn, rimuoviBtn);

            card.getChildren().addAll(
                nome, tipo, prezzo, indirizzoLabel, telefono, consegna, orariTitle, orariScorrevoli, actions
            );
            preferitiPane.getChildren().add(card);
        }
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
     * Evidenzia il giorno corrente.
     *
     * @param orariApertura Mappa che contiene gli orari di apertura per ogni giorno della settimana
     * @return Componente Node che mostra gli orari in formato scrollabile
     */
    private Node creaOrariScorrevoli(java.util.Map<String, String> orariApertura) {
        String[] giorni = {"lunedi", "martedi", "mercoledi", "giovedi", "venerdi", "sabato", "domenica"};
        String[] labels = {"Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom"};
        int oggiIdx = java.time.LocalDate.now().getDayOfWeek().getValue() - 1;

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

        ScrollPane scroll = new ScrollPane(giorniBox);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setFitToHeight(true);
        scroll.setFitToWidth(false);
        scroll.getStyleClass().add("orari-scroll-wrapper-modern");
        scroll.setPrefHeight(340);
        scroll.setMinHeight(300);
        scroll.setMaxHeight(400);
        scroll.setPannable(true);

        return scroll;
    }

    /**
     * Naviga alla pagina delle recensioni per il ristorante specificato.
     * Carica la vista delle recensioni e passa l'ID dell'utente e il ristorante al controller.
     *
     * @param ristorante Il ristorante di cui visualizzare le recensioni
     */
    private void mostraPaginaRecensioni(Ristorante ristorante) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/theknife/RecensioniRistoranteView.fxml"));
            javafx.scene.Parent root = loader.load();
            RecensioniRistoranteController ctrl = loader.getController();
            ctrl.setContext(userId, ristorante);
            javafx.stage.Stage stage = (javafx.stage.Stage) preferitiPane.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Naviga alla home page dell'utente.
     * Carica la vista della home e passa l'ID dell'utente al controller.
     */
    private void vaiHome() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/theknife/UserHomeView.fxml"));
            javafx.scene.Parent root = loader.load();
            UserHomeController ctrl = loader.getController();
            ctrl.setUserId(userId);
            javafx.stage.Stage stage = (javafx.stage.Stage) preferitiPane.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Carica e visualizza i ristoranti preferiti dell'utente.
     * Se non ci sono preferiti, mostra un messaggio appropriato.
     * Altrimenti, recupera i ristoranti preferiti e li visualizza.
     */
    private void mostraPreferiti() {
        preferitiPane.getChildren().clear();
        List<String> ids = ristoranteService.getRistorantiPreferitiByUtente(userId);

        if (ids.isEmpty()) {
            // Mostra messaggio quando non ci sono preferiti
            VBox emptyState = new VBox(16);
            emptyState.setStyle("-fx-alignment: center; -fx-padding: 80px 0;");

            Label iconLabel = new Label("❤");
            iconLabel.setStyle("-fx-font-size: 60px; -fx-text-fill: #CCCCCC;");

            Label titleLabel = new Label("Nessun ristorante preferito");
            titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

            Label subtitleLabel = new Label("I ristoranti che aggiungi ai preferiti appariranno qui");
            subtitleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #777777;");

            Button goHomeBtn = new Button("Esplora ristoranti");
            goHomeBtn.getStyleClass().add("button-reset");
            goHomeBtn.setOnAction(e -> vaiHome());

            emptyState.getChildren().addAll(iconLabel, titleLabel, subtitleLabel, goHomeBtn);
            preferitiPane.getChildren().add(emptyState);
            return;
        }

        List<Ristorante> preferiti = ristoranteService.getAllRistoranti().stream()
                .filter(r -> ids.contains(r.getId())).collect(Collectors.toList());

        // Salva la lista completa per i filtri
        tuttiRistorantiPreferiti = preferiti;

        mostraRistorantiFiltrati(preferiti);
    }
}
