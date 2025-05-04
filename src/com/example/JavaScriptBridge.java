package com.example;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class JavaScriptBridge {
    private final WebEngine webEngine;
    private final Gson gson = new Gson();
    private final Map<String, Function<String, Map<String, Object>>> methods = new HashMap<>();

    public JavaScriptBridge(WebEngine webEngine) {
        this.webEngine = webEngine;

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                setupJavaScriptBridge();
            }
        });
    }

    // Metodo reso pubblico per consentire la reinizializzazione
    public void setupJavaScriptBridge() {
        try {
            System.out.println("Configurazione del bridge JavaScript...");

            // Verifica che il WebEngine sia pronto
            if (webEngine.getLoadWorker().getState() != Worker.State.SUCCEEDED) {
                System.err.println("WebEngine non pronto, rimando configurazione...");
                return;
            }

            // Inizializza strutture JavaScript con controllo errori
            webEngine.executeScript(
                    "try {\n" +
                            "    if (typeof window.javaConnectorCallbacks === 'undefined') {\n" +
                            "        window.javaConnectorCallbacks = {};\n" +
                            "    }\n" +
                            "    if (typeof window.javaConnectorCallbackId === 'undefined') {\n" +
                            "        window.javaConnectorCallbackId = 0;\n" +
                            "    }\n" +
                            "    if (typeof window.javaConnector === 'undefined') {\n" +
                            "        window.javaConnector = {};\n" +
                            "    }\n" +
                            "    console.log('Strutture JavaScript inizializzate');\n" +
                            "    true;\n" +
                            "} catch (e) {\n" +
                            "    console.error('Errore nella inizializzazione JS: ' + e);\n" +
                            "    false;\n" +
                            "}"
            );

            // Esponi oggetto Java a JavaScript
            JSObject window = (JSObject) webEngine.executeScript("window");
            window.setMember("javaBridge", this);

            // Crea metodi nel javaConnector
            for (String methodName : methods.keySet()) {
                try {
                    registerMethodToJS(methodName);
                } catch (Exception e) {
                    System.err.println("Errore nella registrazione del metodo " + methodName + ": " + e.getMessage());
                }
            }

            webEngine.executeScript("console.log('Java bridge inizializzato con successo');");
        } catch (Exception e) {
            System.err.println("Errore nell'inizializzazione del bridge: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void registerMethodToJS(String methodName) {
        webEngine.executeScript(
                "window.javaConnector." + methodName + " = function(args) {\n" +
                        "   return new Promise(function(resolve, reject) {\n" +
                        "       try {\n" +
                        "           var callbackId = 'cb_' + (window.javaConnectorCallbackId++);\n" +
                        "           var jsonArgs = JSON.stringify(args || {});\n" +
                        "           \n" +
                        "           // Registra callback per questa chiamata\n" +
                        "           window.javaConnectorCallbacks[callbackId] = function(jsonResult) {\n" +
                        "               try {\n" +
                        "                   resolve(JSON.parse(jsonResult));\n" +
                        "               } catch(e) {\n" +
                        "                   reject('Errore parsing JSON: ' + e);\n" +
                        "               }\n" +
                        "           };\n" +
                        "           \n" +
                        "           // Chiama direttamente il metodo Java\n" +
                        "           window.javaBridge.callMethod('" + methodName + "', callbackId, jsonArgs);\n" +
                        "           \n" +
                        "           // Timeout per la risposta\n" +
                        "           setTimeout(function() {\n" +
                        "               if (window.javaConnectorCallbacks[callbackId]) {\n" +
                        "                   delete window.javaConnectorCallbacks[callbackId];\n" +
                        "                   reject('Timeout attendendo risposta da Java');\n" +
                        "               }\n" +
                        "           }, 5000);\n" +
                        "       } catch(e) {\n" +
                        "           reject('Errore in chiamata: ' + e);\n" +
                        "       }\n" +
                        "   });\n" +
                        "};\n"
        );
    }

    // Metodo chiamato direttamente da JavaScript
    public void callMethod(String methodName, String callbackId, String jsonArgs) {
        try {
            // Verifica che jsonArgs sia un oggetto JSON valido
            if (jsonArgs == null || jsonArgs.trim().isEmpty() ||
                    (!jsonArgs.trim().startsWith("{") && !jsonArgs.trim().startsWith("["))) {
                // Se non è un oggetto JSON valido, crea un oggetto vuoto
                jsonArgs = "{}";
            }

            if (methods.containsKey(methodName)) {
                Map<String, Object> result;
                try {
                    // Tenta la deserializzazione con controllo d'errore
                    result = methods.get(methodName).apply(jsonArgs);
                } catch (com.google.gson.JsonSyntaxException e) {
                    System.err.println("Errore di sintassi JSON: " + e.getMessage() + " per i dati: " + jsonArgs);
                    result = Map.of("error", "Formato JSON non valido", "details", e.getMessage());
                }

                String jsonResult = gson.toJson(result);
                sendCallbackToJS(callbackId, jsonResult);
            } else {
                String error = gson.toJson(Map.of("error", "Metodo non trovato: " + methodName));
                sendCallbackToJS(callbackId, error);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String error = gson.toJson(Map.of("error", "Errore interno: " + e.getMessage()));
            sendCallbackToJS(callbackId, error);
        }
    }

    private void sendCallbackToJS(String callbackId, String jsonResult) {
        Platform.runLater(() -> {
            try {
                // Controlla che WebEngine sia ancora valido e non sia stato ricaricato
                if (webEngine.getLoadWorker().getState() != Worker.State.SUCCEEDED) {
                    System.out.println("Callback ignorato perché WebEngine non è pronto: " + callbackId);
                    return;
                }

                JSObject window = (JSObject) webEngine.executeScript("window");
                if (window == null) {
                    System.out.println("Oggetto window non disponibile");
                    return;
                }

                Object callbacksObj = window.getMember("javaConnectorCallbacks");
                if (callbacksObj == null || !(callbacksObj instanceof JSObject)) {
                    System.out.println("Callbacks non disponibili o non validi");
                    return;
                }

                JSObject callbacks = (JSObject) callbacksObj;
                Object callbackObj = callbacks.getMember(callbackId);
                if (callbackObj == null || !(callbackObj instanceof JSObject)) {
                    System.out.println("Callback " + callbackId + " non trovato");
                    return;
                }

                JSObject callback = (JSObject) callbackObj;
                callback.call("call", null, jsonResult);

                // Rimuovi il callback dopo l'uso per evitare perdite di memoria
                webEngine.executeScript("delete window.javaConnectorCallbacks['" + callbackId + "']");
            } catch (Exception e) {
                System.err.println("Errore nel callback: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public void registerMethod(String methodName, Function<String, Map<String, Object>> handler) {
        methods.put(methodName, handler);

        // Se WebEngine è già caricato, aggiorna il bridge
        if (webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
            Platform.runLater(() -> registerMethodToJS(methodName));
        }
    }
}