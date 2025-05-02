package com.example;

import com.example.models.Recensione;
import com.example.models.Ristorante;
import com.example.models.Utente;
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

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class Main extends Application {
    private AuthService authService;
    private RistoranteService ristoranteService;
    private RecensioneService recensioneService;
    private final Gson gson = new Gson();

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

            // Registra i metodi per i ristoranti
            registerRistoranteMethods(bridge);

            // Registra i metodi per le recensioni
            registerRecensioneMethods(bridge);

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
                                    "        alert('LOG: ' + message);" +
                                    "        oldLog.apply(console, arguments);" +
                                    "    };" +
                                    "    console.error = function() {" +
                                    "        var message = Array.from(arguments).map(String).join(' ');" +
                                    "        alert('ERROR: ' + message);" +
                                    "        oldError.apply(console, arguments);" +
                                    "    };" +
                                    "    console.warn = function() {" +
                                    "        var message = Array.from(arguments).map(String).join(' ');" +
                                    "        alert('WARN: ' + message);" +
                                    "        oldWarn.apply(console, arguments);" +
                                    "    };" +
                                    "    console.info = function() {" +
                                    "        var message = Array.from(arguments).map(String).join(' ');" +
                                    "        alert('INFO: ' + message);" +
                                    "        oldInfo.apply(console, arguments);" +
                                    "    };" +
                                    "    window.onerror = function(message, source, lineno, colno, error) {" +
                                    "        alert('JS ERROR: ' + message + ' at ' + source + ':' + lineno + ':' + colno);" +
                                    "        return false;" +
                                    "    };" +
                                    "})();";
                    webEngine.executeScript(script);
                }
            });

            URL url = getClass().getResource("/web/index.html");
            if (url != null) {
                webEngine.load(url.toExternalForm());
                System.out.println("File HTML caricato correttamente da: " + url);
            } else {
                throw new IllegalArgumentException("File HTML non trovato! Verifica il percorso.");
            }

            // Gestisce gli alert (inclusi i log reindirizzati)
            webEngine.setOnAlert(event -> {
                String message = event.getData();
                if (message.startsWith("LOG: ")) {
                    System.out.println("JS Log: " + message.substring(5));
                } else if (message.startsWith("ERROR: ")) {
                    System.err.println("JS Error: " + message.substring(7));
                } else if (message.startsWith("WARN: ")) {
                    System.out.println("JS Warning: " + message.substring(6));
                } else if (message.startsWith("INFO: ")) {
                    System.out.println("JS Info: " + message.substring(6));
                } else if (message.startsWith("JS ERROR: ")) {
                    System.err.println(message);
                } else {
                    System.out.println("JS Alert: " + message);
                }
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

    private void registerAuthMethods(JavaScriptBridge bridge) {
        // Registrazione utente
        bridge.registerMethod("registraUtente", args -> {
            Map<String, Object> params = gson.fromJson(args, Map.class);
            String username = (String) params.get("username");
            String password = (String) params.get("password");
            String email = (String) params.get("email");
            String ruoloStr = (String) params.get("ruolo");

            Utente.Ruolo ruolo = Utente.Ruolo.valueOf(ruoloStr);

            boolean success = authService.registraUtente(username, password, email, ruolo);

            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            if (!success) {
                result.put("error", "Username o email già in uso");
            }
            return result;
        });

        // Login
        bridge.registerMethod("login", args -> {
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

            Ristorante ristorante = ristoranteService.creaRistorante(
                    nome, tipoCucina, fasciaPrezzo, orariApertura,
                    latitudine, longitudine, idProprietario,
                    numeroTelefono, consegnaDomicilio);

            return Map.of(
                    "success", true,
                    "ristoranteId", ristorante.getId()
            );
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

            Optional<Ristorante> ristoranteModificato = ristoranteService.modificaRistorante(
                    id, nome, tipoCucina, fasciaPrezzo, orariApertura,
                    latitudine, longitudine, numeroTelefono, consegnaDomicilio);

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

            return Map.of(
                    "success", eliminato,
                    "error", eliminato ? "" : "Ristorante non trovato"
            );
        });

        // Recupera tutti i ristoranti
        bridge.registerMethod("getAllRistoranti", args -> {
            List<Ristorante> ristoranti = ristoranteService.getAllRistoranti();
            return Map.of(
                    "success", true,
                    "ristoranti", ristoranti
            );
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
            Map<String, Object> params = gson.fromJson(args, Map.class);
            String idProprietario = (String) params.get("idProprietario");

            List<Ristorante> ristoranti = ristoranteService.getRistorantiByProprietario(idProprietario);
            return Map.of(
                    "success", true,
                    "ristoranti", ristoranti
            );
        });

        // Recupera ristoranti per tipo di cucina
        bridge.registerMethod("getRistorantiByTipoCucina", args -> {
            Map<String, Object> params = gson.fromJson(args, Map.class);
            String tipoCucina = (String) params.get("tipoCucina");

            List<Ristorante> ristoranti = ristoranteService.getRistorantiByTipoCucina(tipoCucina);
            return Map.of(
                    "success", true,
                    "ristoranti", ristoranti
            );
        });
    }

    private void registerRecensioneMethods(JavaScriptBridge bridge) {
        // Crea una nuova recensione
        bridge.registerMethod("creaRecensione", args -> {
            Map<String, Object> params = gson.fromJson(args, Map.class);

            String idRistorante = (String) params.get("idRistorante");
            String idUtente = (String) params.get("idUtente");
            int voto = ((Double) params.get("voto")).intValue();
            String titolo = (String) params.get("titolo");
            String testo = (String) params.get("testo");

            Recensione recensione = recensioneService.creaRecensione(
                    idRistorante, idUtente, voto, titolo, testo);

            return Map.of(
                    "success", true,
                    "recensioneId", recensione.getId()
            );
        });

        // Modifica una recensione esistente
        bridge.registerMethod("modificaRecensione", args -> {
            Map<String, Object> params = gson.fromJson(args, Map.class);

            String recensioneId = (String) params.get("recensioneId");
            String titolo = (String) params.get("titolo");
            String testo = (String) params.get("testo");
            int voto = ((Double) params.get("voto")).intValue();

            Optional<Recensione> recensioneModificata = recensioneService.modificaRecensione(
                    recensioneId, titolo, testo, voto);

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
            return Map.of(
                    "success", true,
                    "recensioni", recensioni
            );
        });

        // Recupera recensioni di un utente
        bridge.registerMethod("getRecensioniByUtente", args -> {
            Map<String, Object> params = gson.fromJson(args, Map.class);
            String idUtente = (String) params.get("idUtente");

            List<Recensione> recensioni = recensioneService.getRecensioniByUtente(idUtente);
            return Map.of(
                    "success", true,
                    "recensioni", recensioni
            );
        });

        // Recupera recensioni di un ristorante
        bridge.registerMethod("getRecensioniByRistorante", args -> {
            Map<String, Object> params = gson.fromJson(args, Map.class);
            String idRistorante = (String) params.get("idRistorante");

            List<Recensione> recensioni = recensioneService.getRecensioniByRistorante(idRistorante);
            return Map.of(
                    "success", true,
                    "recensioni", recensioni
            );
        });

        // Recupera recensioni filtrate per ristorante e voto
        bridge.registerMethod("getRecensioniByRistoranteAndVoto", args -> {
            Map<String, Object> params = gson.fromJson(args, Map.class);
            String idRistorante = (String) params.get("idRistorante");
            int voto = ((Double) params.get("voto")).intValue();

            List<Recensione> recensioni = recensioneService.getRecensioniByRistoranteAndVoto(
                    idRistorante, voto);
            return Map.of(
                    "success", true,
                    "recensioni", recensioni
            );
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}