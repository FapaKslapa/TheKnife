package services;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Servizio per la conversione di coordinate geografiche in indirizzi e viceversa.
 * Questa classe utilizza l'API di OpenStreetMap Nominatim per effettuare operazioni di:
 * <ul>
 *   <li>Reverse geocoding: conversione di coordinate (latitudine, longitudine) in indirizzi leggibili</li>
 *   <li>Geocoding: conversione di indirizzi testuali in coordinate geografiche</li>
 * </ul>
 * Il servizio gestisce automaticamente la validazione dei dati, la formattazione delle richieste,
 * e la gestione degli errori di comunicazione con l'API esterna.
 */
public class ReverseGeocodingService {

    /**
     * Converte coordinate geografiche in un indirizzo leggibile.
     * Effettua una richiesta all'API Nominatim di OpenStreetMap per ottenere l'indirizzo
     * corrispondente alle coordinate specificate. Il metodo include validazione delle coordinate
     * e gestione degli errori di comunicazione.
     *
     * @param lat Latitudine della posizione (deve essere compresa tra -90 e 90)
     * @param lon Longitudine della posizione (deve essere compresa tra -180 e 180)
     * @return Una stringa contenente l'indirizzo completo corrispondente alle coordinate,
     *         o un messaggio di errore se la conversione non è possibile
     */
    public String getAddress(double lat, double lon) {
        // Validazione base: lat [-90,90], lon [-180,180]
        if (lat < -90 || lat > 90 || lon < -180 || lon > 180) return "Coordinate non valide";
        // Arrotonda a 6 decimali per evitare errori di formato
        lat = Math.round(lat * 1_000_000d) / 1_000_000d;
        lon = Math.round(lon * 1_000_000d) / 1_000_000d;
        try {
            String urlStr = String.format(
                java.util.Locale.US,
                "https://nominatim.openstreetmap.org/reverse?format=json&lat=%f&lon=%f",
                lat, lon
            );
            java.net.URL url = new java.net.URL(urlStr);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "TheKnifeApp/1.0 (contatto: info@theknife.it)");
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            int status = conn.getResponseCode();
            if (status != 200) {
                System.err.println("Reverse geocoding HTTP status: " + status + " (" + urlStr + ")");
                return "Indirizzo non trovato (HTTP " + status + ")";
            }
            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) response.append(line);
            in.close();
            org.json.JSONObject obj = new org.json.JSONObject(response.toString());
            String displayName = obj.optString("display_name", "");
            return displayName.isEmpty() ? "Indirizzo non trovato" : displayName;
        } catch (java.net.SocketTimeoutException e) {
            System.err.println("Timeout nella richiesta a Nominatim: " + e.getMessage());
            return "Timeout nella richiesta dell'indirizzo";
        } catch (Exception e) {
            System.err.println("Errore Nominatim: " + e.getMessage());
            return "Indirizzo non trovato (errore)";
        }
    }

    /**
     * Converte un indirizzo testuale in coordinate geografiche.
     * Effettua una richiesta all'API Nominatim di OpenStreetMap per ottenere le coordinate
     * corrispondenti all'indirizzo specificato. Il metodo include validazione dell'input
     * e gestione degli errori di comunicazione.
     *
     * @param address L'indirizzo da convertire in coordinate (non può essere null o vuoto)
     * @return Un array di double contenente [latitudine, longitudine] in caso di successo,
     *         o null se l'indirizzo non può essere convertito in coordinate valide
     */
    public double[] geocode(String address) {
        if (address == null || address.trim().isEmpty()) return null;
        try {
            String urlStr = String.format(
                "https://nominatim.openstreetmap.org/search?format=json&q=%s&limit=1",
                java.net.URLEncoder.encode(address, "UTF-8")
            );
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "TheKnifeApp/1.0 (contatto: info@theknife.it)");
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            int status = conn.getResponseCode();
            if (status != 200) return null;
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) response.append(line);
            in.close();
            org.json.JSONArray arr = new org.json.JSONArray(response.toString());
            if (arr.length() == 0) return null;
            org.json.JSONObject obj = arr.getJSONObject(0);
            double lat = obj.optDouble("lat", 0.0);
            double lon = obj.optDouble("lon", 0.0);
            // Validazione: se lat/lon sono 0, probabilmente non trovato
            if (lat == 0.0 && lon == 0.0) return null;
            return new double[]{lat, lon};
        } catch (java.net.SocketTimeoutException e) {
            System.err.println("Timeout nella richiesta a Nominatim: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Errore Nominatim: " + e.getMessage());
            return null;
        }
    }
}