package services;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ReverseGeocodingService {
    public String getAddress(double lat, double lon) {
        if (lat == 0.0 && lon == 0.0) return "Coordinate non valide";
        try {
            String urlStr = String.format(
                    "https://nominatim.openstreetmap.org/reverse?format=json&lat=%f&lon=%f",
                    lat, lon
            );
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; TheKnifeApp/1.0)");
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000); // 15 secondi
            conn.setReadTimeout(15000);    // 15 secondi

            int status = conn.getResponseCode();
            if (status != 200) return "Indirizzo non trovato";

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) response.append(line);
            in.close();

            System.out.println("Risposta Nominatim: " + response);

            JSONObject obj = new JSONObject(response.toString());
            String displayName = obj.optString("display_name", "");
            return displayName.isEmpty() ? "Indirizzo non trovato" : displayName;
        } catch (java.net.SocketTimeoutException e) {
            System.err.println("Timeout nella richiesta a Nominatim: " + e.getMessage());
            return "Timeout nella richiesta dell'indirizzo";
        } catch (Exception e) {
            e.printStackTrace();
            return "Indirizzo non trovato";
        }
    }
}