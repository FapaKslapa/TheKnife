package example;

import example.models.Ristorante;
import example.models.Utente;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.RistoranteService;
import javafx.scene.shape.SVGPath;

import java.util.List;

/**
 * Controller per la gestione della schermata principale del ristoratore nell'applicazione TheKnife.
 * Questa classe si occupa di visualizzare e gestire tutti i ristoranti di proprietà di un ristoratore,
 * permettendo di visualizzare le loro informazioni dettagliate, accedere alle recensioni e
 * modificare le informazioni del ristorante. Offre anche funzionalità per aggiungere nuovi ristoranti
 * e per effettuare il logout dall'applicazione.
 */
public class RistoratoreHomeController {
    /** Pannello per la visualizzazione dei ristoranti del ristoratore */
    @FXML
    private FlowPane ristorantiPane;
    /** Pulsante per effettuare il logout */
    @FXML
    private Button logoutBtn;
    /** Pulsante per creare un nuovo ristorante */
    @FXML
    private Button nuovoRistoranteBtn;

    /** Utente corrente con ruolo di ristoratore */
    private Utente ristoratore;
    /** Servizio per la gestione delle operazioni sui ristoranti */
    private RistoranteService ristoranteService;

    /**
     * Imposta l'utente ristoratore attuale e inizializza il servizio ristoranti.
     * Dopo aver impostato i valori, visualizza automaticamente i ristoranti di proprietà.
     *
     * @param ristoratore L'utente con ruolo di ristoratore da impostare
     */
    public void setRistoratore(Utente ristoratore) {
        this.ristoratore = ristoratore;
        this.ristoranteService = new RistoranteService();
        mostraRistorantiPropri();
    }

    /**
     * Inizializza la schermata principale del ristoratore.
     * Questo metodo viene chiamato automaticamente dopo che il file FXML è stato caricato.
     * Configura i listener per i pulsanti di logout e creazione nuovo ristorante.
     */
    @FXML
    public void initialize() {
        logoutBtn.setOnAction(this::onLogout);
        nuovoRistoranteBtn.setOnAction(this::onNuovoRistorante);
    }

    /**
     * Visualizza tutti i ristoranti di proprietà del ristoratore corrente.
     * Per ogni ristorante crea una card visuale contenente tutte le informazioni principali,
     * come nome, tipo di cucina, fascia di prezzo, indirizzo, numero di telefono,
     * informazioni sulla consegna a domicilio e orari di apertura. Aggiunge inoltre
     * pulsanti per visualizzare le recensioni e modificare il ristorante.
     */
    private void mostraRistorantiPropri() {
        ristorantiPane.getChildren().clear();
        if (ristoratore == null) return;
        List<Ristorante> mieiRistoranti = ristoranteService.getRistorantiByProprietario(ristoratore.getId());
        for (Ristorante r : mieiRistoranti) {
            VBox card = new VBox(14);
            card.getStyleClass().add("card-ristorante-modern");

            Label nome = new Label(r.getNome());
            nome.getStyleClass().add("label-nome-modern");

            Label tipo = new Label("🍽 " + r.getTipoCucina());
            tipo.getStyleClass().add("label-info-modern");

            Label prezzo = new Label("💶 " + renderPrezzi(r.getFasciaPrezzo()));
            prezzo.getStyleClass().add("price-tag-modern");

            String indirizzo = "Indirizzo non disponibile";
            Label indirizzoLabel = new Label("📍 " + indirizzo);
            indirizzoLabel.getStyleClass().add("ristorante-indirizzo-modern");

            Label telefono = new Label("📞 " + r.getNumeroTelefono());
            telefono.getStyleClass().add("label-telefono-modern");

            Label consegna = new Label(r.isConsegnaDomicilio() ? "🚚 Consegna a domicilio: Sì" : "🚚 Consegna a domicilio: No");
            consegna.getStyleClass().add("label-consegna-modern");
            if (!r.isConsegnaDomicilio()) consegna.getStyleClass().add("no");

            Label orariTitle = new Label("🕒 Orari di apertura");
            orariTitle.getStyleClass().add("label-info-modern");
            javafx.scene.Node orariScorrevoli = creaOrariScorrevoli(r.getOrariApertura());

            javafx.scene.layout.HBox actions = new javafx.scene.layout.HBox(16);
            actions.setPadding(new javafx.geometry.Insets(8, 0, 0, 0));
            actions.setStyle("-fx-alignment: center-left;");

            // Pulsante recensioni: icona chat rounded, light mode
            SVGPath recensioniIcon = new SVGPath();
            recensioniIcon.setContent("M20 2H4c-1.1 0-2 .9-2 2v16l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z"); // chat bubble rounded
            recensioniIcon.getStyleClass().add("icon-navbar");
            Button recensioniBtn = new Button();
            recensioniBtn.getStyleClass().add("icon-btn");
            recensioniBtn.setTooltip(new Tooltip("Recensioni"));
            recensioniBtn.setGraphic(recensioniIcon);
            recensioniBtn.setOnAction(e -> mostraRecensioni(r));

            // Pulsante modifica: icona edit rounded, light mode
            SVGPath modificaIcon = new SVGPath();
            modificaIcon.setContent("M3 17.25V21h3.75l11.06-11.06-3.75-3.75L3 17.25zm14.71-10.04a1.003 1.003 0 0 0 0-1.42l-2.54-2.54a1.003 1.003 0 0 0-1.42 0l-1.83 1.83 3.75 3.75 1.83-1.83z"); // edit rounded
            modificaIcon.getStyleClass().add("icon-navbar");
            Button modificaBtn = new Button();
            modificaBtn.getStyleClass().add("icon-btn");
            modificaBtn.setTooltip(new Tooltip("Modifica ristorante"));
            modificaBtn.setGraphic(modificaIcon);
            modificaBtn.setOnAction(e -> mostraModificaRistorante(r));

            actions.getChildren().addAll(recensioniBtn, modificaBtn);

            card.getChildren().addAll(
                nome, tipo, prezzo, indirizzoLabel, telefono, consegna, orariTitle, orariScorrevoli, actions
            );
            ristorantiPane.getChildren().add(card);
        }
    }

    /**
     * Genera una rappresentazione visuale della fascia di prezzo del ristorante.
     * Converte il valore numerico della fascia prezzo in una stringa di simboli €,
     * dove il numero di simboli evidenziati corrisponde al valore della fascia.
     *
     * @param fascia Il valore numerico della fascia di prezzo
     * @return Una stringa che rappresenta visivamente la fascia di prezzo
     */
    private String renderPrezzi(int fascia) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            sb.append(i < fascia ? "€" : "€");
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
    private javafx.scene.Node creaOrariScorrevoli(java.util.Map<String, String> orariApertura) {
        String[] giorni = {"lunedi", "martedi", "mercoledi", "giovedi", "venerdi", "sabato", "domenica"};
        String[] labels = {"Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom"};
        int oggiIdx = java.time.LocalDate.now().getDayOfWeek().getValue() - 1;

        javafx.scene.layout.HBox giorniBox = new javafx.scene.layout.HBox(18);
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
        scroll.setPrefHeight(340);
        scroll.setMinHeight(300);
        scroll.setMaxHeight(400);
        scroll.setPannable(true);

        return scroll;
    }

    /**
     * Naviga alla schermata di visualizzazione delle recensioni per un ristorante specifico.
     * Carica la vista delle recensioni e passa il contesto (ristoratore e ristorante)
     * al controller corrispondente.
     *
     * @param ristorante Il ristorante di cui visualizzare le recensioni
     */
    private void mostraRecensioni(Ristorante ristorante) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/example/RistoratoreRecensioniView.fxml"));
            javafx.scene.Parent root = loader.load();
            RistoratoreRecensioniController ctrl = loader.getController();
            ctrl.setContext(ristoratore.getId(), ristorante);
            javafx.stage.Stage stage = (javafx.stage.Stage) ristorantiPane.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Naviga alla schermata di modifica delle informazioni di un ristorante specifico.
     * Carica la vista di modifica e passa il contesto (ristoratore e ristorante)
     * al controller corrispondente.
     *
     * @param ristorante Il ristorante da modificare
     */
    private void mostraModificaRistorante(Ristorante ristorante) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/example/ModificaRistoranteView.fxml"));
            javafx.scene.Parent root = loader.load();
            ModificaRistoranteController ctrl = loader.getController();
            ctrl.setContext(ristoratore, ristorante);
            Stage stage = (Stage) ristorantiPane.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Gestisce l'evento di logout dell'utente.
     * Naviga alla schermata di autenticazione, terminando la sessione corrente del ristoratore.
     *
     * @param event L'evento di azione che ha attivato il logout
     */
    private void onLogout(ActionEvent event) {
        try {
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/example/AuthView.fxml")));
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gestisce l'evento di creazione di un nuovo ristorante.
     * Naviga alla schermata di creazione di un nuovo ristorante e passa
     * l'utente ristoratore corrente al controller corrispondente.
     *
     * @param event L'evento di azione che ha attivato la creazione di un nuovo ristorante
     */
    private void onNuovoRistorante(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/example/NuovoRistoranteView.fxml"));
            javafx.scene.Parent root = loader.load();
            NuovoRistoranteController ctrl = loader.getController();
            ctrl.setRistoratore(ristoratore);
            javafx.stage.Stage stage = (javafx.stage.Stage) nuovoRistoranteBtn.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}