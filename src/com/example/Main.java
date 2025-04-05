package com.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Map;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();

            // Crea e configura il bridge
            JavaScriptBridge bridge = new JavaScriptBridge(webEngine);

            // Registra i metodi che JavaScript può chiamare
            bridge.registerMethod("getName", args -> {
                // Ritorna un oggetto Map che verrà convertito in JSON
                return Map.of("name", "TheKnife");
            });

            bridge.registerMethod("getHelloWorld", args -> {
                return Map.of("message", "Hello World from Java!");
            });

            URL url = getClass().getResource("/web/index.html");
            if (url != null) {
                webEngine.load(url.toExternalForm());
                System.out.println("File HTML caricato correttamente da: " + url);
            } else {
                throw new IllegalArgumentException("File HTML non trovato! Verifica il percorso.");
            }

            webEngine.setOnAlert(event -> System.out.println("Console JS: " + event.getData()));

            Scene scene = new Scene(webView, 800, 600);
            primaryStage.setTitle("App JavaFX con WebView");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            System.err.println("Errore durante il caricamento del file HTML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}