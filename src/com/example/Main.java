package src.com.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            // Crea il WebView
            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();

            // Carica il file HTML utilizzando il class loader
            URL url = getClass().getResource("/web/index.html");
            if (url != null) {
                webEngine.load(url.toExternalForm());
                System.out.println("File HTML caricato correttamente da: " + url);
            } else {
                throw new IllegalArgumentException("File HTML non trovato! Verifica il percorso.");
            }

            // Configura la scena
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