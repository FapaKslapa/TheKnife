// Funzione per il primo pulsante
function showName() {
    window.javaConnector.getName({})
        .then(data => {
            document.getElementById("dynamic-text").innerText = "Nome ricevuto da Java: " + data.name;
        })
        .catch(error => {
            document.getElementById("dynamic-text").innerText = "Errore: " + error;
        });
}

// Funzione per il secondo pulsante
function showHelloWorld() {
    window.javaConnector.getHelloWorld({})
        .then(data => {
            document.getElementById("dynamic-text").innerText = "Messaggio ricevuto da Java: " + data.message;
        })
        .catch(error => {
            document.getElementById("dynamic-text").innerText = "Errore: " + error;
        });
}

// Verifica connessione al bridge
window.onload = function () {
    setTimeout(function () {
        const status = document.getElementById("status");
        if (window.javaConnector) {
            status.textContent = "✓ Bridge connesso";
            status.className = "status-ok";
        } else {
            status.textContent = "✗ Bridge non disponibile";
            status.className = "status-error";
        }
    }, 500);
};