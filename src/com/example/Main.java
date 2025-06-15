package com.example;

import com.example.models.FiltriDiRicerca;
import com.example.models.Recensione;
import com.example.models.Ristorante;
import com.example.models.Utente;
import com.example.utils.LocalDateTimeAdapter;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import services.AuthService;
import services.RecensioneService;
import services.RistoranteService;
import com.google.gson.Gson;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Classe principale dell'applicazione TheKnife che gestisce l'interfaccia utente
 * e la comunicazione tra il backend Java e il frontend JavaScript.
 * <p>
 * Questa applicazione consente la gestione di ristoranti, recensioni e utenti
 * attraverso un'interfaccia web integrata in JavaFX.
 * </p>
 *
 * @author Stefano Marocco
 * @version 1.0
 */
public class Main extends Application {
    /**
     *  Servizio per la gestione dell'autenticazione e degli utenti
     */
    private AuthService authService;

    /**
     * Servizio per la gestione dei ristoranti
     */
    private RistoranteService ristoranteService;

    /**
     * Servizio per la gestione delle recensioni
     */
    private RecensioneService recensioneService;

    /**
     * Istanza di Gson configurata per la serializzazione/deserializzazione di oggetti
     */
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    /**
     * Inizializza e avvia l'applicazione JavaFX.
     * Configura il WebView, i servizi, il bridge JavaScript-Java e carica la pagina HTML iniziale.
     *
     * @param primaryStage Lo stage principale dell'applicazione
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            // Inizializza i servizi
            authService = new AuthService();
            ristoranteService = new RistoranteService();
            recensioneService = new RecensioneService();

            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();

            // Crea e configura il bridge
            JavaScriptBridge bridge = new JavaScriptBridge(webEngine);

            // Registra i metodi base
            bridge.registerMethod("getName", args -> Map.of("name", "TheKnife"));

            // Registra i metodi di autenticazione
            registerAuthMethods(bridge);
            System.out.println("Metodi di autenticazione registrati");

            // Registra i metodi per i ristoranti
            registerRistoranteMethods(bridge);
            System.out.println("Metodi ristorante registrati");

            // Registra i metodi per le recensioni
            registerRecensioneMethods(bridge);
            System.out.println("Metodi recensione registrati");

            // Carica l'icona dell'applicazione
            try {
                Image appIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/Logo.png")));
                primaryStage.getIcons().add(appIcon);
            } catch (Exception e) {
                System.err.println("Errore nel caricamento dell'icona: " + e.getMessage());
            }

            // Reindirizza console.log, console.error e altri metodi della console JS a Java
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    String script =
                            "(function() {" +
                                    "    var oldLog = console.log;" +
                                    "    var oldError = console.error;" +
                                    "    var oldWarn = console.warn;" +
                                    "    var oldInfo = console.info;" +
                                    "    console.log = function() {" +
                                    "        var message = Array.from(arguments).map(String).join(' ');" +
                                    "        window.javaConnector.consoleLog({ message: message, type: 'log' });" + // Corretto qui
                                    "        oldLog.apply(console, arguments);" +
                                    "    };" +
                                    "    console.error = function() {" +
                                    "        var message = Array.from(arguments).map(String).join(' ');" +
                                    "        window.javaConnector.consoleLog({ message: message, type: 'error' });" + // Corretto qui
                                    "        oldError.apply(console, arguments);" +
                                    "    };" +
                                    "    console.warn = function() {" +
                                    "        var message = Array.from(arguments).map(String).join(' ');" +
                                    "        window.javaConnector.consoleLog({ message: message, type: 'warn' });" + // Corretto qui
                                    "        oldWarn.apply(console, arguments);" +
                                    "    };" +
                                    "    console.info = function() {" +
                                    "        var message = Array.from(arguments).map(String).join(' ');" +
                                    "        window.javaConnector.consoleLog({ message: message, type: 'info' });" + // Corretto qui
                                    "        oldInfo.apply(console, arguments);" +
                                    "    };" +
                                    "    window.onerror = function(message, source, lineno, colno, error) {" +
                                    "        window.javaConnector.consoleLog({ message: 'Errore: ' + message + ' a ' + source + ':' + lineno + ':' + colno, type: 'error' });" + // Corretto qui
                                    "        return false;" +
                                    "    };" +
                                    "})();";
                    webEngine.executeScript(script);
                }
            });
            URL url = getClass().getResource("/web/homeutente.html");
            // Registra il metodo per i log della console JavaScript
            bridge.registerMethod("consoleLog", args -> {
                try {
                    Map<String, Object> params = gson.fromJson(args, Map.class);

                    // Controllo per valori null
                    String message = params != null && params.get("message") != null ? (String) params.get("message") : "(messaggio vuoto)";
                    String type = params != null && params.get("type") != null ? (String) params.get("type") : "log";

                    switch (type) {
                        case "error":
                            System.err.println("JS Error: " + message);
                            break;
                        case "warn":
                            System.out.println("JS Warning: " + message);
                            break;
                        case "info":
                            System.out.println("JS Info: " + message);
                            break;
                        case "log":
                        default:
                            System.out.println("JS Log: " + message);
                            break;
                    }

                    return Map.of("success", true);
                } catch (Exception e) {
                    System.err.println("Errore nella gestione del log della console: " + e.getMessage());
                    e.printStackTrace();
                    return Map.of("success", false, "error", e.getMessage());
                }
            });
            if (url != null) {
                webEngine.load(url.toExternalForm());
                System.out.println("File HTML caricato correttamente da: " + url);
            } else {
                throw new IllegalArgumentException("File HTML non trovato! Verifica il percorso.");
            }

            // Aggiungi qui il codice per gestire la navigazione tra pagine e reinizializzare il bridge
            webEngine.locationProperty().addListener((observable, oldValue, newValue) -> {
                System.out.println("Navigazione a: " + newValue);

                // Attendi il completamento del caricamento della nuova pagina
                webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
                    @Override
                    public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldState, Worker.State newState) {
                        if (newState == Worker.State.SUCCEEDED) {
                            // Rimuovi questo listener per evitare chiamate multiple
                            webEngine.getLoadWorker().stateProperty().removeListener(this);

                            // Attendi un momento per garantire che il DOM sia completamente caricato
                            Platform.runLater(() -> {
                                System.out.println("Reinizializzazione bridge dopo cambio pagina");
                                bridge.setupJavaScriptBridge();
                            });
                        }
                    }
                });
            });

            Scene scene = new Scene(webView, 800, 600);
            primaryStage.setTitle("TheKnife");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            System.err.println("Errore durante l'inizializzazione dell'applicazione: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Registra i metodi di autenticazione nel bridge JavaScript.
     * <p>
     * Include metodi per registrazione, login e recupero informazioni utente.
     * </p>
     *
     * @param bridge Il bridge JavaScript-Java in cui registrare i metodi
     */
    private void registerAuthMethods(JavaScriptBridge bridge) {
        // Registrazione utente
        bridge.registerMethod("registraUtente", args -> {
            try {
                Map<String, Object> params = gson.fromJson(args, Map.class);
                String username = (String) params.get("username");
                String password = (String) params.get("password");
                String email = (String) params.get("email");
                String ruoloStr = (String) params.get("ruolo");

                // Validazione dei parametri
                if (username == null || password == null || email == null || ruoloStr == null) {
                    return Map.of("success", false, "error", "Parametri mancanti");
                }

                Utente.Ruolo ruolo;
                try {
                    ruolo = Utente.Ruolo.valueOf(ruoloStr);
                } catch (IllegalArgumentException e) {
                    return Map.of("success", false, "error", "Ruolo non valido");
                }

                boolean success = authService.registraUtente(username, password, email, ruolo);

                Map<String, Object> result = new HashMap<>();
                result.put("success", success);
                if (!success) {
                    result.put("error", "Username o email già in uso");
                }
                return result;
            } catch (Exception e) {
                System.err.println("Errore durante la registrazione: " + e.getMessage());
                return Map.of("success", false, "error", "Errore durante la registrazione");
            }
        });


        // Login
        bridge.registerMethod("login", args -> {
            System.out.println("login");
            Map<String, Object> params = gson.fromJson(args, Map.class);
            String usernameOrEmail = (String) params.get("usernameOrEmail");
            String password = (String) params.get("password");
            Optional<Utente> utenteOpt = authService.login(usernameOrEmail, password);

            Map<String, Object> result = new HashMap<>();
            if (utenteOpt.isPresent()) {
                Utente utente = utenteOpt.get();
                result.put("success", true);
                result.put("userId", utente.getId());
                result.put("username", utente.getUsername());
                result.put("email", utente.getEmail());
                result.put("ruolo", utente.getRuolo().toString());
            } else {
                result.put("success", false);
                result.put("error", "Credenziali non valide");
            }
            return result;
        });

        // Recupera informazioni utente
        bridge.registerMethod("getUtenteInfo", args -> {
            System.out.println("getUtenteInfo");
            Map<String, Object> params = gson.fromJson(args, Map.class);
            String userId = (String) params.get("userId");

            Optional<Utente> utenteOpt = authService.getUtenteById(userId);

            Map<String, Object> result = new HashMap<>();
            if (utenteOpt.isPresent()) {
                Utente utente = utenteOpt.get();
                result.put("success", true);
                result.put("userId", utente.getId());
                result.put("username", utente.getUsername());
                result.put("email", utente.getEmail());
                result.put("ruolo", utente.getRuolo().toString());
            } else {
                result.put("success", false);
                result.put("error", "Utente non trovato");
            }
            return result;
        });
    }

    /**
     * Registra i metodi per la gestione dei ristoranti nel bridge JavaScript.
     * <p>
     * Include metodi per creare, modificare, eliminare e cercare ristoranti
     * con vari criteri di filtro.
     * </p>
     *
     * @param bridge Il bridge JavaScript-Java in cui registrare i metodi
     */
    private void registerRistoranteMethods(JavaScriptBridge bridge) {
        // Crea nuovo ristorante
        bridge.registerMethod("creaRistorante", args -> {
            Map<String, Object> params = gson.fromJson(args, Map.class);

            String nome = (String) params.get("nome");
            String tipoCucina = (String) params.get("tipoCucina");
            int fasciaPrezzo = ((Double) params.get("fasciaPrezzo")).intValue();
            Map<String, String> orariApertura = (Map<String, String>) params.get("orariApertura");
            double latitudine = (Double) params.get("latitudine");
            double longitudine = (Double) params.get("longitudine");
            String idProprietario = (String) params.get("idProprietario");
            String numeroTelefono = (String) params.get("numeroTelefono");
            boolean consegnaDomicilio = (Boolean) params.get("consegnaDomicilio");

            Ristorante ristorante = ristoranteService.creaRistorante(nome, tipoCucina, fasciaPrezzo, orariApertura, latitudine, longitudine, idProprietario, numeroTelefono, consegnaDomicilio);

            return Map.of("success", true, "ristoranteId", ristorante.getId());
        });

        // Modifica ristorante
        bridge.registerMethod("modificaRistorante", args -> {
            Map<String, Object> params = gson.fromJson(args, Map.class);

            String id = (String) params.get("id");
            String nome = (String) params.get("nome");
            String tipoCucina = (String) params.get("tipoCucina");
            int fasciaPrezzo = ((Double) params.get("fasciaPrezzo")).intValue();
            Map<String, String> orariApertura = (Map<String, String>) params.get("orariApertura");
            double latitudine = (Double) params.get("latitudine");
            double longitudine = (Double) params.get("longitudine");
            String numeroTelefono = (String) params.get("numeroTelefono");
            boolean consegnaDomicilio = (Boolean) params.get("consegnaDomicilio");

            Optional<Ristorante> ristoranteModificato = ristoranteService.modificaRistorante(id, nome, tipoCucina, fasciaPrezzo, orariApertura, latitudine, longitudine, numeroTelefono, consegnaDomicilio);

            Map<String, Object> result = new HashMap<>();
            if (ristoranteModificato.isPresent()) {
                result.put("success", true);
                result.put("ristorante", ristoranteModificato.get());
            } else {
                result.put("success", false);
                result.put("error", "Ristorante non trovato");
            }
            return result;
        });

        // Elimina ristorante
        bridge.registerMethod("eliminaRistorante", args -> {
            Map<String, Object> params = gson.fromJson(args, Map.class);
            String id = (String) params.get("id");

            boolean eliminato = ristoranteService.eliminaRistorante(id);

            return Map.of("success", eliminato, "error", eliminato ? "" : "Ristorante non trovato");
        });

        // Recupera tutti i ristoranti
        bridge.registerMethod("getAllRistoranti", args -> {

            List<Ristorante> ristoranti = ristoranteService.getAllRistoranti();
            return Map.of("success", true, "ristoranti", ristoranti);
        });

        // Recupera ristorante per ID
        bridge.registerMethod("getRistoranteById", args -> {
            Map<String, Object> params = gson.fromJson(args, Map.class);
            String id = (String) params.get("id");
            Optional<Ristorante> ristoranteOpt = ristoranteService.getRistoranteById(id);
            Map<String, Object> result = new HashMap<>();
            if (ristoranteOpt.isPresent()) {
                result.put("success", true);
                result.put("ristorante", ristoranteOpt.get());
            } else {
                result.put("success", false);
                result.put("error", "Ristorante non trovato");
            }
            return result;
        });

        // Recupera ristoranti di un proprietario
        bridge.registerMethod("getRistorantiByProprietario", args -> {
            try {
                System.out.println("Chiamata a getRistorantiByProprietario ricevuta");
                Map<String, Object> params = gson.fromJson(args, Map.class);
                String idProprietario = (String) params.get("idProprietario");

                System.out.println("Recupero ristoranti per proprietario ID: " + idProprietario);

                List<Ristorante> ristoranti = ristoranteService.getRistorantiByProprietario(idProprietario);

                System.out.println("Trovati " + ristoranti.size() + " ristoranti");

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("ristoranti", ristoranti);
                return result;
            } catch (Exception e) {
                System.err.println("Errore in getRistorantiByProprietario: " + e.getMessage());
                e.printStackTrace();
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", e.getMessage());
                return result;
            }
        });
        // Recupera ristoranti per tipo di cucina
        bridge.registerMethod("getRistorantiByTipoCucina", args -> {
            Map<String, Object> params = gson.fromJson(args, Map.class);
            String tipoCucina = (String) params.get("tipoCucina");

            List<Ristorante> ristoranti = ristoranteService.getRistorantiByTipoCucina(tipoCucina);
            return Map.of("success", true, "ristoranti", ristoranti);
        });

        // Registra il metodo di ricerca con filtri
        bridge.registerMethod("filtriRicerca", args -> {
            Map<String, Object> params = gson.fromJson(args, Map.class);

            // Crea il builder per FiltriDiRicerca
            FiltriDiRicerca.Builder builder = new FiltriDiRicerca.Builder();

            // Aggiungi i filtri in base ai parametri ricevuti
            if (params.containsKey("tipoCucina") && params.get("tipoCucina") != null) {
                builder.tipoCucina((String) params.get("tipoCucina"));
            }

            if (params.containsKey("fasciaPrezzo") && params.get("fasciaPrezzo") != null) {
                builder.fasciaPrezzo(((Double) params.get("fasciaPrezzo")).intValue());
            }

            if (params.containsKey("consegnaDomicilio") && params.get("consegnaDomicilio") != null) {
                builder.consegnaDomicilio((Boolean) params.get("consegnaDomicilio"));
            }

            if (params.containsKey("apertoOra") && params.get("apertoOra") != null) {
                builder.apertoOra((Boolean) params.get("apertoOra"));
            }

            // Se sono presenti latitudine, longitudine e distanza massima
            if (params.containsKey("latitudine") && params.get("latitudine") != null &&
                    params.containsKey("longitudine") && params.get("longitudine") != null &&
                    params.containsKey("distanzaMassima") && params.get("distanzaMassima") != null) {

                Double latitudine = (Double) params.get("latitudine");
                Double longitudine = (Double) params.get("longitudine");
                Integer distanzaMassima = ((Double) params.get("distanzaMassima")).intValue();
                builder.posizione(latitudine, longitudine, distanzaMassima);
            }

            // Costruisci l'oggetto filtri
            FiltriDiRicerca filtri = builder.build();

            // Esegui la ricerca
            List<Ristorante> risultati = ristoranteService.filtriRicerca(filtri);

            // Restituisci i risultati
            return Map.of("success", true, "ristoranti", risultati);
        });
    }

    /**
     * Registra i metodi per la gestione delle recensioni nel bridge JavaScript.
     * <p>
     * Include metodi per creare, modificare, eliminare e cercare recensioni
     * con vari criteri di filtro.
     * </p>
     *
     * @param bridge Il bridge JavaScript-Java in cui registrare i metodi
     */
    private void registerRecensioneMethods(JavaScriptBridge bridge) {
        // Crea una nuova recensione
        bridge.registerMethod("creaRecensione", args -> {
            Map<String, Object> params = gson.fromJson(args, Map.class);

            String idRistorante = (String) params.get("idRistorante");
            String idUtente = (String) params.get("idUtente");
            int voto = ((Double) params.get("voto")).intValue();
            String titolo = (String) params.get("titolo");
            String testo = (String) params.get("testo");

            Recensione recensione = recensioneService.creaRecensione(idRistorante, idUtente, voto, titolo, testo);

            return Map.of("success", true, "recensioneId", recensione.getId());
        });

        // Modifica una recensione esistente
        bridge.registerMethod("modificaRecensione", args -> {
            Map<String, Object> params = gson.fromJson(args, Map.class);

            String recensioneId = (String) params.get("recensioneId");
            String titolo = (String) params.get("titolo");
            String testo = (String) params.get("testo");
            int voto = ((Double) params.get("voto")).intValue();

            Optional<Recensione> recensioneModificata = recensioneService.modificaRecensione(recensioneId, titolo, testo, voto);

            Map<String, Object> result = new HashMap<>();
            if (recensioneModificata.isPresent()) {
                result.put("success", true);
                result.put("recensione", recensioneModificata.get());
            } else {
                result.put("success", false);
                result.put("error", "Recensione non trovata");
            }
            return result;
        });

        // Elimina una recensione
        bridge.registerMethod("eliminaRecensione", args -> {
            Map<String, Object> params = gson.fromJson(args, Map.class);
            String recensioneId = (String) params.get("recensioneId");

            recensioneService.eliminaRecensione(recensioneId);

            return Map.of("success", true);
        });

        // Recupera tutte le recensioni
        bridge.registerMethod("getAllRecensioni", args -> {
            List<Recensione> recensioni = recensioneService.getAllRecensioni();
            return Map.of("success", true, "recensioni", recensioni);
        });

        // Recupera recensioni di un utente
        bridge.registerMethod("getRecensioniByUtente", args -> {
            Map<String, Object> params = gson.fromJson(args, Map.class);
            String idUtente = (String) params.get("idUtente");

            List<Recensione> recensioni = recensioneService.getRecensioniByUtente(idUtente);
            return Map.of("success", true, "recensioni", recensioni);
        });

        // Recupera recensioni di un ristorante
        bridge.registerMethod("getRecensioniByRistorante", args -> {
            Map<String, Object> params = gson.fromJson(args, Map.class);
            String idRistorante = (String) params.get("idRistorante");

            List<Recensione> recensioni = recensioneService.getRecensioniByRistorante(idRistorante);
            return Map.of("success", true, "recensioni", recensioni);
        });

        // Recupera recensioni filtrate per ristorante e voto
        bridge.registerMethod("getRecensioniByRistoranteAndVoto", args -> {
            Map<String, Object> params = gson.fromJson(args, Map.class);
            String idRistorante = (String) params.get("idRistorante");
            int voto = ((Double) params.get("voto")).intValue();

            List<Recensione> recensioni = recensioneService.getRecensioniByRistoranteAndVoto(idRistorante, voto);
            return Map.of("success", true, "recensioni", recensioni);
        });
    }

    /**
     * Punto di ingresso principale dell'applicazione.
     * Avvia l'applicazione JavaFX.
     *
     * @param args Argomenti da riga di comando (non utilizzati)
     */
    public static void main(String[] args) {
        launch(args);
    }
}