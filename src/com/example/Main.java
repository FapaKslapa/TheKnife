package src.com.example;

import javafx.application.Application;
import javafx.concurrent.Worker;
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

            URL url = getClass().getResource("/web/index.html");
            if (url != null) {
                webEngine.load(url.toExternalForm());
                System.out.println("File HTML caricato correttamente da: " + url);
            } else {
                throw new IllegalArgumentException("File HTML non trovato! Verifica il percorso.");
            }

            // Crea e configura il bridge
            JavaScriptBridge bridge = new JavaScriptBridge(webEngine);

            webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == Worker.State.SUCCEEDED) {
                    // Inietta la funzione javaInvoke per chiamare il bridge
                    String invokeFunction =
                            "window.javaInvoke = function(jsonPayload) { " +
                                    "   return " + createInvokeCallback(bridge) + ";" +
                                    "};";
                    webEngine.executeScript(invokeFunction);

                    // Inietta il meccanismo di callback
                    bridge.injectBridge();

                    // Registra i metodi disponibili
                    bridge.registerMethod("getName", args -> {
                        return Map.of("name", "Stefano");
                    });

                    bridge.registerMethod("getHelloWorld", args -> {
                        return Map.of("message", "Hello, World!");
                    });
                }
            });

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

    private String createInvokeCallback(JavaScriptBridge bridge) {
        return "'" + bridge.invoke("' + jsonPayload + '") + "'";
    }

    public static void main(String[] args) {
        launch(args);
    }
}