package example.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Adattatore GSON per la serializzazione e deserializzazione di oggetti {@link LocalDateTime}.
 * <p>
 * Questa classe permette di convertire oggetti LocalDateTime in stringhe JSON e viceversa,
 * utilizzando il formato ISO standard per la rappresentazione delle date e orari.
 * </p>
 */
public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {

    /**
     * Formattatore utilizzato per la conversione tra LocalDateTime e stringhe.
     * Utilizza il formato standard ISO per data e ora.
     */
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Serializza un oggetto LocalDateTime in una stringa JSON.
     *
     * @param out   JsonWriter utilizzato per scrivere il valore
     * @param value Il valore LocalDateTime da serializzare
     * @throws IOException se si verifica un errore di I/O durante la scrittura
     */
    @Override
    public void write(JsonWriter out, LocalDateTime value) throws IOException {
        if (value == null) {
            // Gestione del caso null
            out.nullValue();
        } else {
            // Formatta il LocalDateTime come stringa e lo scrive nell'output JSON
            out.value(formatter.format(value));
        }
    }

    /**
     * Deserializza una stringa JSON in un oggetto LocalDateTime.
     *
     * @param in JsonReader da cui leggere il valore
     * @return L'oggetto LocalDateTime deserializzato, o null se il valore JSON è null
     * @throws IOException se si verifica un errore di I/O durante la lettura
     */
    @Override
    public LocalDateTime read(JsonReader in) throws IOException {
        if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
            // Gestione del caso null
            in.nextNull();
            return null;
        }
        // Converte la stringa letta in un oggetto LocalDateTime
        return LocalDateTime.parse(in.nextString(), formatter);
    }
}