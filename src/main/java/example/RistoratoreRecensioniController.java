package example;

import example.models.Ristorante;
import example.models.Recensione;
import example.models.Risposta;
import example.models.Utente;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import services.RecensioneService;
import services.RistoranteService;
import services.AuthService;

import java.util.List;

/**
 * Controller per la gestione delle recensioni di un ristorante nell'applicazione TheKnife.
 * Questa classe si occupa della visualizzazione e della gestione delle recensioni
 * relative a un ristorante specifico, permettendo al ristoratore di rispondere a ciascuna
 * recensione o modificare le risposte già pubblicate. Fornisce anche statistiche
 * relative alle recensioni ricevute, come il voto medio e il numero di risposte date.
 */
public class RistoratoreRecensioniController {
    /** Etichetta per visualizzare il titolo della pagina con il nome del ristorante */
    @FXML private Label ristoranteTitle;
    /** Pulsante per tornare alla home del ristoratore */
    @FXML private Button homeBtn;
    /** Contenitore per la visualizzazione delle recensioni e delle relative risposte */
    @FXML private VBox recensioniBox;

    /** ID dell'utente ristoratore corrente */
    private String ristoratoreId;
    /** Ristorante di cui si stanno visualizzando le recensioni */
    private Ristorante ristorante;
    /** Servizio per la gestione delle operazioni sulle recensioni */
    private final RecensioneService recensioneService = new RecensioneService();
    /** Servizio per la gestione delle operazioni sui ristoranti */
    private final RistoranteService ristoranteService = new RistoranteService();
    /** Servizio per la gestione dell'autenticazione e delle operazioni sugli utenti */
    private final AuthService authService = new AuthService();

    /**
     * Imposta il contesto per la visualizzazione delle recensioni.
     * Inizializza il controller con l'ID del ristoratore e il ristorante specifico,
     * imposta il titolo della pagina e carica le recensioni relative al ristorante.
     *
     * @param ristoratoreId L'ID dell'utente ristoratore
     * @param ristorante Il ristorante di cui visualizzare le recensioni
     */
    public void setContext(String ristoratoreId, Ristorante ristorante) {
        this.ristoratoreId = ristoratoreId;
        this.ristorante = ristorante;
        ristoranteTitle.setText("Recensioni di " + ristorante.getNome());
        mostraRecensioni();
    }

    /**
     * Inizializza la schermata delle recensioni.
     * Questo metodo viene chiamato automaticamente dopo che il file FXML è stato caricato.
     * Configura i listener per i pulsanti di navigazione.
     */
    @FXML
    public void initialize() {
        homeBtn.setOnAction(e -> vaiHome());
    }

    /**
     * Visualizza tutte le recensioni del ristorante corrente.
     * Questo metodo costruisce dinamicamente l'interfaccia utente per mostrare le recensioni,
     * le statistiche correlate (voto medio, numero di recensioni, risposte date) e
     * permette di rispondere alle recensioni o modificare le risposte esistenti.
     * Se non ci sono recensioni, mostra un messaggio appropriato.
     */
    private void mostraRecensioni() {
        recensioniBox.getChildren().clear();

        // Intestazione con info ristorante
        HBox infoHeader = new HBox(18);
        infoHeader.setAlignment(javafx.geometry.Pos.CENTER);
        infoHeader.getStyleClass().add("info-header");

        Label infoLabel = new Label("Visualizzi le recensioni lasciate dai clienti per il tuo ristorante");
        infoLabel.getStyleClass().add("label-info-modern");
        infoLabel.setStyle("-fx-font-size: 18px;");

        Separator sep = new Separator();

        infoHeader.getChildren().addAll(infoLabel);
        recensioniBox.getChildren().addAll(infoHeader, sep);

        List<Recensione> recensioni = recensioneService.getRecensioniByRistorante(ristorante.getId());
        if (recensioni.isEmpty()) {
            VBox emptyState = new VBox(16);
            emptyState.setAlignment(javafx.geometry.Pos.CENTER);
            emptyState.setPadding(new javafx.geometry.Insets(60, 0, 40, 0));

            Label noRec = new Label("Nessuna recensione disponibile");
            noRec.getStyleClass().addAll("label-titolo-grande");

            Label subInfo = new Label("Quando i clienti lasceranno recensioni per il tuo ristorante, potrai vederle e rispondere qui.");
            subInfo.getStyleClass().add("label-info-modern");
            subInfo.setStyle("-fx-font-size: 16px;");

            emptyState.getChildren().addAll(noRec, subInfo);
            recensioniBox.getChildren().add(emptyState);
        } else {
            // Statistiche recensioni
            HBox statsBox = new HBox(24);
            statsBox.setAlignment(javafx.geometry.Pos.CENTER);
            statsBox.setPadding(new javafx.geometry.Insets(20, 0, 20, 0));

            Label countLabel = new Label("Recensioni totali: " + recensioni.size());
            countLabel.getStyleClass().add("label-info-modern");
            countLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

            // Calcolo voto medio
            double votomedio = recensioni.stream()
                .mapToDouble(Recensione::getRate)
                .average()
                .orElse(0.0);

            Label mediaLabel = new Label(String.format("Voto medio: %.1f ⭐", votomedio));
            mediaLabel.getStyleClass().add("label-voto-grande");

            // Risposte date
            long risposteCount = recensioni.stream()
                .filter(r -> recensioneService.getRispostaByRecensione(r.getId()).isPresent())
                .count();

            Label risposteLabel = new Label("Risposte date: " + risposteCount + "/" + recensioni.size());
            risposteLabel.getStyleClass().add("label-info-modern");
            risposteLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

            statsBox.getChildren().addAll(countLabel, mediaLabel, risposteLabel);
            recensioniBox.getChildren().add(statsBox);

            // Lista recensioni
            for (Recensione rec : recensioni) {
                VBox card = new VBox(16);
                card.getStyleClass().add("card-recensione-grande");

                HBox header = new HBox(18);
                header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                Label voto = new Label("⭐ " + rec.getRate());
                voto.getStyleClass().add("label-voto-grande");
                Label titolo = new Label(rec.getTitle());
                titolo.getStyleClass().add("label-titolo-grande");
                header.getChildren().addAll(voto, titolo);

                Label testo = new Label(rec.getText());
                testo.getStyleClass().add("label-testo-grande");
                testo.setWrapText(true);

                // Informazioni cliente e data
                HBox infoCliente = new HBox(12);
                infoCliente.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                // Ottieni il nome utente dal suo ID usando AuthService invece di UtenteService
                String nomeUtente = "Cliente";
                Utente cliente = authService.getUtenteById(rec.getKey_user()).orElse(null);
                if (cliente != null) {
                    nomeUtente = cliente.getUsername();
                }

                Label clienteLabel = new Label("Cliente: " + nomeUtente);
                clienteLabel.getStyleClass().add("label-info-modern");
                clienteLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

                Label tempo = new Label("Pubblicata " + recensioneService.getTempoTrascorso(rec.getDate()));
                tempo.getStyleClass().add("label-tempo-grande");

                infoCliente.getChildren().addAll(clienteLabel, tempo);

                card.getChildren().addAll(header, testo, infoCliente);

                // Risposta
                var rispostaOpt = recensioneService.getRispostaByRecensione(rec.getId());
                if (rispostaOpt.isPresent()) {
                    Risposta risposta = rispostaOpt.get();
                    VBox rispostaBox = new VBox(12);
                    rispostaBox.getStyleClass().add("risposta-box-light");
                    Label rispostaTitle = new Label("La tua risposta:");
                    rispostaTitle.getStyleClass().add("risposta-title-light");

                    Label rispostaLabel = new Label(risposta.getTesto());
                    rispostaLabel.getStyleClass().add("risposta-testo-light");
                    rispostaLabel.setWrapText(true);

                    Label dataRisposta = new Label("Risposta pubblicata " +
                        recensioneService.getTempoTrascorso(risposta.getDataCreazione()));
                    dataRisposta.getStyleClass().add("label-tempo-grande");
                    dataRisposta.setStyle("-fx-padding: 8 0 0 0;");

                    HBox rispostaActions = new HBox(16);
                    rispostaActions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

                    Button modificaBtn = new Button("Modifica risposta");
                    modificaBtn.getStyleClass().add("button-auth");
                    modificaBtn.setOnAction(e -> mostraModificaRisposta(rec, risposta));

                    rispostaActions.getChildren().add(modificaBtn);
                    rispostaBox.getChildren().addAll(rispostaTitle, rispostaLabel, dataRisposta, rispostaActions);
                    card.getChildren().add(rispostaBox);
                } else {
                    VBox rispostaBox = new VBox(14);
                    rispostaBox.getStyleClass().add("risposta-box-light");
                    Label rispostaTitle = new Label("Rispondi alla recensione:");
                    rispostaTitle.getStyleClass().add("risposta-title-light");

                    TextArea rispostaArea = new TextArea();
                    rispostaArea.setPromptText("Scrivi una risposta professionale a questa recensione...");
                    rispostaArea.setPrefRowCount(3);
                    rispostaArea.setStyle("-fx-font-size: 16px;");
                    rispostaArea.getStyleClass().add("textfield-auth");

                    HBox actionBox = new HBox(16);
                    actionBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

                    Button inviaBtn = new Button("Pubblica risposta");
                    inviaBtn.getStyleClass().add("button-auth");

                    Label result = new Label("");
                    result.getStyleClass().add("label-auth-result");

                    actionBox.getChildren().addAll(result, inviaBtn);

                    inviaBtn.setOnAction(e -> {
                        String testoRisposta = rispostaArea.getText();
                        if (testoRisposta.isEmpty()) {
                            result.setText("⚠️ Inserisci un testo per la risposta");
                            return;
                        }
                        var opt = recensioneService.aggiungiRisposta(rec.getId(), testoRisposta);
                        if (opt.isPresent()) {
                            result.setText("✅ Risposta pubblicata con successo!");
                            mostraRecensioni();
                        } else {
                            result.setText("❌ Errore nell'invio della risposta");
                        }
                    });

                    rispostaBox.getChildren().addAll(rispostaTitle, rispostaArea, actionBox);
                    card.getChildren().add(rispostaBox);
                }

                // Aggiungi spazio tra le recensioni
                VBox spacer = new VBox();
                spacer.setPrefHeight(24);

                recensioniBox.getChildren().addAll(card, spacer);
            }
        }
    }

    /**
     * Mostra un dialog per modificare una risposta esistente a una recensione.
     * Crea e configura un dialog modale che permette al ristoratore di modificare
     * il testo della risposta. Se confermata, la modifica viene salvata e
     * l'interfaccia viene aggiornata per mostrare la risposta modificata.
     *
     * @param recensione La recensione a cui è associata la risposta da modificare
     * @param risposta La risposta da modificare
     */
    private void mostraModificaRisposta(Recensione recensione, Risposta risposta) {
        // Creo un dialog per modificare la risposta
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Modifica risposta");
        dialog.setHeaderText("Modifica la tua risposta alla recensione");

        // Configuro il contenuto del dialog
        VBox content = new VBox(18);
        content.setPadding(new javafx.geometry.Insets(20, 20, 10, 20));

        Label recTitolo = new Label("Recensione: " + recensione.getTitle());
        recTitolo.getStyleClass().add("label-titolo-grande");
        recTitolo.setStyle("-fx-font-size: 18px;");

        TextArea textArea = new TextArea(risposta.getTesto());
        textArea.setWrapText(true);
        textArea.setPrefRowCount(5);
        textArea.getStyleClass().add("textfield-auth");

        content.getChildren().addAll(recTitolo, textArea);
        dialog.getDialogPane().setContent(content);

        // Aggiungo i pulsanti
        ButtonType confermaButtonType = new ButtonType("Salva modifiche", ButtonBar.ButtonData.OK_DONE);
        ButtonType annullaButtonType = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(confermaButtonType, annullaButtonType);

        // Configuro la risposta del dialog
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confermaButtonType) {
                return textArea.getText();
            }
            return null;
        });

        // Mostro il dialog e processo il risultato
        dialog.showAndWait().ifPresent(nuovoTesto -> {
            if (!nuovoTesto.isEmpty()) {
                var rispostaModificataOpt = recensioneService.modificaRisposta(risposta.getId(), nuovoTesto);
                if (rispostaModificataOpt.isPresent()) {
                    mostraRecensioni();
                }
            }
        });
    }

    /**
     * Naviga alla schermata principale del ristoratore.
     * Carica la vista home del ristoratore e passa il contesto dell'utente
     * ristoratore corrente al controller corrispondente.
     */
    private void vaiHome() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/example/RistoratoreHomeView.fxml"));
            javafx.scene.Parent root = loader.load();
            RistoratoreHomeController ctrl = loader.getController();
            AuthService authService = new AuthService();
            ctrl.setRistoratore(authService.getUtenteById(ristoratoreId).orElse(null));
            javafx.stage.Stage stage = (javafx.stage.Stage) homeBtn.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
