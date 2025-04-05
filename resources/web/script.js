function processMessageFromJava(jsonObject) {
    try {
        // Ottieni l'elemento h1
        const h1Element = document.querySelector('h1');

        // Aggiorna il testo dell'h1 con il messaggio
        if (h1Element && jsonObject && jsonObject.message) {
            h1Element.textContent = jsonObject.message;
        }

        // Manteniamo anche il log nella console per debug
        console.log("Messaggio ricevuto da Java:", jsonObject.message);

        return jsonObject;
    } catch (error) {
        console.error("Errore durante l'elaborazione del messaggio:", error);
    }
}

// Rimuoviamo la chiamata senza parametri che potrebbe causare errori
document.addEventListener('DOMContentLoaded', function () {
    // Non chiamare processMessageFromJava qui, sarà chiamata da Java
});