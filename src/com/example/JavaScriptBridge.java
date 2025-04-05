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

    private void setupJavaScriptBridge() {
        try {
            // Inizializza strutture JavaScript
            webEngine.executeScript(
                    "window.javaConnectorCallbacks = {};\n" +
                            "window.javaConnectorCallbackId = 0;\n" +
                            "window.javaConnector = {};\n"
            );

            // Esponi oggetto Java a JavaScript
            JSObject window = (JSObject) webEngine.executeScript("window");
            window.setMember("javaBridge", this);

            // Crea metodi nel javaConnector
            for (String methodName : methods.keySet()) {
                registerMethodToJS(methodName);
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
            if (methods.containsKey(methodName)) {
                Map<String, Object> result = methods.get(methodName).apply(jsonArgs);
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
                JSObject window = (JSObject) webEngine.executeScript("window");
                JSObject callbacks = (JSObject) window.getMember("javaConnectorCallbacks");

                if (callbacks.getMember(callbackId) != null) {
                    JSObject callback = (JSObject) callbacks.getMember(callbackId);
                    callback.call("call", null, jsonResult);
                }
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