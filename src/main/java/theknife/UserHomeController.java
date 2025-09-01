// Marocco Stefano 762192 VA
// Marin Marco 760622 VA
// Gerti Alessia 762405 VA
package theknife;

import theknife.models.FiltriDiRicerca;
import theknife.models.Ristorante;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.AuthService;
import services.RistoranteService;
import services.RecensioneService;
import services.ReverseGeocodingService;

import java.util.Arrays;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

/**
 * Controller per la gestione della schermata principale dell'utente nell'applicazione TheKnife.
 * Questa classe si occupa di visualizzare e gestire tutti i ristoranti disponibili nel sistema,
 * permettendo all'utente di filtrarli in base a diversi criteri (tipo di cucina, fascia di prezzo,
 * distanza, ecc.), visualizzare le loro recensioni e aggiungere o rimuovere ristoranti dai preferiti.
 * Fornisce inoltre funzionalità per la navigazione ad altre schermate e per il logout.
 */
public class UserHomeController {
    /** Pannello per la visualizzazione dei ristoranti */
    @FXML private FlowPane ristorantiPane;
    /** Pulsanti per il logout, visualizzazione preferiti, reset filtri e ricerca */
    @FXML private Button logoutBtn, preferitiBtn, resetFiltriBtn, cercaBtn;
    /** Menu a tendina per la selezione dei filtri di tipo cucina, fascia prezzo e distanza */
    @FXML private ComboBox<String> tipoCucinaCombo, fasciaPrezzoCombo, distanzaCombo;
    /** Caselle di controllo per filtrare ristoranti con consegna a domicilio o aperti in questo momento */
    @FXML private CheckBox consegnaCheckbox, apertoOraCheckbox;
    /** Campo di testo per inserire una posizione da utilizzare per filtrare per distanza */
    @FXML private TextField posizioneField;
    @FXML private TextField nomeRistoranteField;
    @FXML private Button prevPageBtn, nextPageBtn;
    @FXML private Label pageLabel;

    /** Servizio per la gestione dell'autenticazione e delle operazioni sugli utenti */
    private final AuthService authService = new AuthService();
    /** Servizio per la gestione delle operazioni sui ristoranti */
    private final RistoranteService ristoranteService = new RistoranteService();
    /** Servizio per la gestione delle operazioni sulle recensioni */
    private final RecensioneService recensioneService = new RecensioneService();
    /** Servizio per la conversione di coordinate geografiche in indirizzi e viceversa */
    private final ReverseGeocodingService geocodingService = new ReverseGeocodingService();
    /** ID dell'utente corrente */
    private String userId;

    /** Lista completa dei ristoranti (cache per non dover ricaricare dopo il filtraggio) */
    private List<Ristorante> tuttiRistoranti;

    private int currentPage = 1;
    private final int pageSize = 20;
    private int totalPages = 1;
    private List<Ristorante> ristorantiCorrenti;

    /**
     * Imposta l'ID dell'utente corrente e aggiorna la visualizzazione dei ristoranti.
     * Questo metodo viene chiamato dopo l'autenticazione per inizializzare la schermata
     * con i dati specifici dell'utente.
     *
     * @param userId L'ID dell'utente corrente
     */
    public void setUserId(String userId) {
        this.userId = userId;
        // Aggiorna la visualizzazione quando viene impostato l'userId
        mostraCardRistoranti(1);
    }

    /**
     * Inizializza la schermata principale dell'utente.
     * Questo metodo viene chiamato automaticamente dopo che il file FXML è stato caricato.
     * Configura i listener per i pulsanti e inizializza i filtri di ricerca.
     */
    @FXML
    public void initialize() {
        // Non mostrare i ristoranti qui, aspetta che venga impostato l'userId
        logoutBtn.setOnAction(e -> logout());
        preferitiBtn.setOnAction(e -> mostraPreferiti());

        // Inizializza i filtri
        initFiltri();

        prevPageBtn.setOnAction(e -> vaiPagina(currentPage - 1));
        nextPageBtn.setOnAction(e -> vaiPagina(currentPage + 1));
    }

    /**
     * Inizializza i componenti dell'interfaccia utente per i filtri di ricerca.
     * Configura i menu a tendina con i valori predefiniti e imposta i listener
     * per i pulsanti di reset e ricerca.
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

        // Distanze (in km)
        List<String> distanze = Arrays.asList("Tutte", "5 km", "10 km", "20 km", "50 km");
        distanzaCombo.setItems(FXCollections.observableArrayList(distanze));
        distanzaCombo.getSelectionModel().selectFirst();

        // Reset dei filtri
        resetFiltriBtn.setOnAction(e -> resetFiltri());

        // Bottone cerca
        cercaBtn.setOnAction(e -> applicaFiltri());
    }

    /**
     * Reimposta tutti i filtri di ricerca ai valori predefiniti e aggiorna
     * la visualizzazione dei ristoranti mostrando l'elenco completo.
     */
    private void resetFiltri() {
        tipoCucinaCombo.getSelectionModel().selectFirst();
        fasciaPrezzoCombo.getSelectionModel().selectFirst();
        distanzaCombo.getSelectionModel().selectFirst();
        consegnaCheckbox.setSelected(false);
        apertoOraCheckbox.setSelected(false);
        posizioneField.clear();
        nomeRistoranteField.clear();

        // Mostra tutti i ristoranti
        mostraCardRistoranti(1);
    }

    /**
     * Visualizza tutti i ristoranti disponibili nel sistema.
     * Questo metodo recupera la lista di ristoranti dal servizio, crea dinamicamente
     * le card per ciascun ristorante e le aggiunge all'interfaccia utente.
     * Se non ci sono ristoranti disponibili, visualizza un messaggio appropriato.
     */
    @FXML
    private void mostraCardRistoranti(int pagina) {
        ristorantiPane.getChildren().clear();
        List<Ristorante> ristoranti = ristoranteService.getAllRistoranti();
        tuttiRistoranti = ristoranti; // Salva la lista completa per i filtri
        ristorantiCorrenti = ristoranti;
        mostraRistorantiFiltrati(ristoranti, pagina);
    }

    /**
     * Visualizza la lista filtrata di ristoranti nell'interfaccia utente.
     * Crea dinamicamente le card per ciascun ristorante e le aggiunge all'interfaccia.
     * Se la lista filtrata è vuota, mostra un messaggio appropriato.
     *
     * @param ristoranti La lista filtrata di ristoranti da visualizzare
     */
    private void mostraRistorantiFiltrati(List<Ristorante> ristoranti, int pagina) {
        ristorantiPane.getChildren().clear();

        if (ristoranti.isEmpty()) {
            Label noResults = new Label("Nessun ristorante trovato con i filtri selezionati");
            noResults.getStyleClass().add("no-results-label");
            ristorantiPane.getChildren().add(noResults);
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

            String indirizzo = null;
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
            recensioniIcon.setContent("M20 2H4c-1.1 0-2 .9-2 2v16l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z"); // chat bubble rounded
            recensioniIcon.getStyleClass().add("icon-navbar");
            Button recensioniBtn = new Button();
            recensioniBtn.getStyleClass().add("icon-btn");
            recensioniBtn.setTooltip(new Tooltip("Recensioni"));
            recensioniBtn.setGraphic(recensioniIcon);
            recensioniBtn.setOnAction(e -> mostraRecensioni(r));

            // Icona preferiti (cuore)
            boolean isPreferito = ristoranteService.isRistorantePreferito(userId, r.getId());
            SVGPath cuoreIcon = new SVGPath();
            if (isPreferito) {
                // Cuore pieno
                cuoreIcon.setContent("M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z");
            } else {
                // Cuore vuoto (solo bordo)
                cuoreIcon.setContent("M16.5 3c-1.74 0-3.41.81-4.5 2.09C10.91 3.81 9.24 3 7.5 3 4.42 3 2 5.42 2 8.5c0 3.78 3.4 6.86 8.55 11.54L12 21.35l1.45-1.32C18.6 15.36 22 12.28 22 8.5 22 5.42 19.58 3 16.5 3zm-4.4 15.55l-.1.1-.1-.1C7.14 14.24 4 11.39 4 8.5 4 6.5 5.5 5 7.5 5c1.54 0 3.04.99 3.57 2.36h1.87C13.46 5.99 14.96 5 16.5 5 19.58 5 22 7.42 22 10.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z");
            }
            cuoreIcon.getStyleClass().add("icon-navbar");
            if (isPreferito) {
                cuoreIcon.setFill(Color.web("#FF9F43"));
            }
            Button preferitoBtn = new Button();
            preferitoBtn.getStyleClass().add("icon-btn");
            preferitoBtn.setTooltip(new Tooltip("Preferiti"));
            preferitoBtn.setGraphic(cuoreIcon);
            if (isPreferito) {
                preferitoBtn.setStyle("-fx-background-color: #FFF7ED; -fx-effect: dropshadow(gaussian, #FF9F43AA, 8, 0.18, 0, 2);");
            } else {
                preferitoBtn.setStyle("");
            }
            preferitoBtn.setOnAction(e -> {
                boolean nuovoStato = !ristoranteService.isRistorantePreferito(userId, r.getId());
                if (nuovoStato) {
                    ristoranteService.aggiungiRistoranteAiPreferiti(userId, r.getId());
                    cuoreIcon.setContent("M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z");
                    cuoreIcon.setFill(Color.web("#FF9F43"));
                    preferitoBtn.setStyle("-fx-background-color: #FFF7ED; -fx-effect: dropshadow(gaussian, #FF9F43AA, 8, 0.18, 0, 2);");
                } else {
                    ristoranteService.rimuoviRistoranteDaiPreferiti(userId, r.getId());
                    cuoreIcon.setContent("M16.5 3c-1.74 0-3.41.81-4.5 2.09C10.91 3.81 9.24 3 7.5 3 4.42 3 2 5.42 2 8.5c0 3.78 3.4 6.86 8.55 11.54L12 21.35l1.45-1.32C18.6 15.36 22 12.28 22 8.5 22 5.42 19.58 3 16.5 3zm-4.4 15.55l-.1.1-.1-.1C7.14 14.24 4 11.39 4 8.5 4 6.5 5.5 5 7.5 5c1.54 0 3.04.99 3.57 2.36h1.87C13.46 5.99 14.96 5 16.5 5c2 0 3.5 1.5 3.5 3.5 0 2.89-3.14 5.74-7.9 10.05z");
                    cuoreIcon.setFill(Color.web("#8256D0"));
                    preferitoBtn.setStyle("");
                }
            });

            actions.getChildren().addAll(recensioniBtn, preferitoBtn);

            card.getChildren().addAll(
                nome, tipo, prezzo, indirizzoLabel, telefono, consegna, orariTitle, orariScorrevoli, actions
            );
            ristorantiPane.getChildren().add(card);
        }
        aggiornaBarraPaginazione(currentPage, totalPages);
    }

    private void aggiornaBarraPaginazione(int pagina, int totPagine) {
        pageLabel.setText("Pagina " + pagina + " di " + totPagine);
        prevPageBtn.setDisable(pagina <= 1);
        nextPageBtn.setDisable(pagina >= totPagine);
    }

    private void vaiPagina(int nuovaPagina) {
        mostraRistorantiFiltrati(ristorantiCorrenti, nuovaPagina);
    }

    /**
     * Genera una rappresentazione visuale della fascia di prezzo del ristorante.
     * Converte il valore numerico della fascia prezzo in una stringa di simboli €,
     * dove i simboli attivi indicano la fascia di prezzo e quelli vuoti (○) completano
     * il numero totale di simboli.
     *
     * @param fascia Il valore numerico della fascia di prezzo (da 1 a 3)
     * @return Una stringa che rappresenta visivamente la fascia di prezzo
     */
    private String renderPrezzi(int fascia) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            sb.append(i < fascia ? "€" : "○");
        }
        return sb.toString();
    }

    /**
     * Crea un componente scorrevole per visualizzare gli orari di apertura del ristorante.
     * Il componente mostra gli orari per tutti i giorni della settimana, evidenziando
     * il giorno corrente. Per ogni giorno, visualizza le fasce orarie di apertura o
     * indica se il ristorante è chiuso.
     *
     * @param orariApertura Mappa contenente gli orari di apertura per ogni giorno della settimana
     * @return Un componente JavaFX che visualizza gli orari di apertura in modo scorrevole
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

        javafx.scene.control.ScrollPane scroll = new javafx.scene.control.ScrollPane(giorniBox);
        scroll.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setFitToHeight(true);
        scroll.setFitToWidth(false);
        scroll.getStyleClass().add("orari-scroll-wrapper-modern");
        scroll.setPrefHeight(340); // allungato
        scroll.setMinHeight(300);
        scroll.setMaxHeight(400);
        scroll.setPannable(true);

        return scroll;
    }

    /**
     * Naviga alla schermata di visualizzazione delle recensioni per un ristorante specifico.
     * Delega il compito di navigazione al metodo mostraPaginaRecensioni.
     *
     * @param ristorante Il ristorante di cui visualizzare le recensioni
     */
    private void mostraRecensioni(Ristorante ristorante) {
        mostraPaginaRecensioni(ristorante);
    }

    /**
     * Naviga alla schermata di visualizzazione delle recensioni per un ristorante specifico.
     * Carica la vista delle recensioni e passa il contesto (utente e ristorante)
     * al controller corrispondente.
     *
     * @param ristorante Il ristorante di cui visualizzare le recensioni
     */
    private void mostraPaginaRecensioni(Ristorante ristorante) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/theknife/RecensioniRistoranteView.fxml"));
            javafx.scene.Parent root = loader.load();
            RecensioniRistoranteController ctrl = loader.getController();
            ctrl.setContext(userId, ristorante);
            javafx.stage.Stage stage = (javafx.stage.Stage) ristorantiPane.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception ex) {
            System.err.println("Errore durante il caricamento della pagina recensioni: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Naviga alla schermata di visualizzazione dei ristoranti preferiti dall'utente.
     * Carica la vista dei preferiti e passa l'ID dell'utente corrente al controller corrispondente.
     */
    private void mostraPreferiti() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/theknife/PreferitiView.fxml"));
            javafx.scene.Parent preferitiRoot = loader.load();
            PreferitiViewController ctrl = loader.getController();
            ctrl.setUserId(userId);
            javafx.stage.Stage stage = (javafx.stage.Stage) ristorantiPane.getScene().getWindow();
            stage.getScene().setRoot(preferitiRoot);
        } catch (Exception ex) {
            System.err.println("Errore durante il caricamento della pagina preferiti: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Effettua il logout dell'utente corrente e torna alla schermata principale dell'applicazione.
     * Carica la vista principale senza passare alcun contesto utente.
     */
    private void logout() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/theknife/MainView.fxml"));
            javafx.scene.Parent root = loader.load();
            Stage stage = (Stage) ristorantiPane.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception ex) {
            System.err.println("Errore durante il logout: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Applica i filtri di ricerca selezionati dall'utente per filtrare l'elenco dei ristoranti.
     * Raccoglie i valori dai vari componenti dell'interfaccia utente, costruisce un oggetto
     * FiltriDiRicerca e lo utilizza per ottenere la lista filtrata di ristoranti.
     * Se la posizione è specificata, utilizza il servizio di geocoding per convertirla in coordinate.
     */
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

        // Nome ristorante (corrispondenza parziale)
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
