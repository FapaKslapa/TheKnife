// Funzione di attesa per verificare che il bridge sia pronto
function waitForBridge(callback, maxAttempts = 10) {
    let attempts = 0;

    function checkBridge() {
        attempts++;
        if (window.javaConnector && typeof window.javaConnector.getRistorantiByProprietario === 'function') {
            console.log("Bridge trovato, procedo con la chiamata");
            callback();
        } else if (attempts < maxAttempts) {
            console.log(`Tentativo ${attempts}: Bridge non pronto, riprovo tra 500ms...`);
            setTimeout(checkBridge, 500);
        } else {
            console.error("Bridge non disponibile dopo diversi tentativi");
            document.getElementById('alertArea').innerHTML = `
                <div class="alert alert-danger" role="alert">
                    Errore di connessione con l'applicazione. Ricarica la pagina.
                </div>
            `;
        }
    }

    checkBridge();
}

document.addEventListener('DOMContentLoaded', function () {
    // Verifica se l'utente è loggato
    const isLoggedIn = sessionStorage.getItem('isLoggedIn');
    const userId = sessionStorage.getItem('userId');
    const username = sessionStorage.getItem('username');
    const ruolo = sessionStorage.getItem('ruolo');

    // Mostra username nella navbar
    document.getElementById('username-display').textContent = username || '';

    // Reindirizza alla pagina di login se l'utente non è loggato o non è un ristoratore
    if (!isLoggedIn || ruolo !== 'RISTORATORE') {
        window.location.href = "index.html";
        return;
    }

    console.log("Attendo che il bridge sia pronto...");
    // Attendi che il bridge sia disponibile prima di chiamare la funzione
    waitForBridge(function () {
        caricaRistoranti(userId);
    });

    // Gestisci click sul pulsante "Nuovo ristorante"
    document.getElementById('nuovo-ristorante').addEventListener('click', function () {
        window.location.href = "crearistorante.html";
    });

    // Gestisci il logout
    document.getElementById('logout-link').addEventListener('click', function (e) {
        e.preventDefault();
        // Pulisci i dati di sessione
        sessionStorage.clear();
        // Reindirizza alla pagina di login
        window.location.href = "index.html";
    });

    document.getElementById('salvaRistorante').addEventListener('click', function () {
        const nome = document.getElementById('nome').value;
        const tipoCucina = document.getElementById('tipoCucina').value;
        const fasciaPrezzo = parseInt(document.getElementById('fasciaPrezzo').value);
        const latitudine = parseFloat(document.getElementById('latitudine').value);
        const longitudine = parseFloat(document.getElementById('longitudine').value);
        const numeroTelefono = document.getElementById('numeroTelefono').value;
        const consegnaDomicilio = document.getElementById('consegnaDomicilio').checked;
        const idProprietario = sessionStorage.getItem('userId');

        // Costruzione dell'oggetto orari
        const orariApertura = {
            lunedi: document.getElementById('orariLunedi').value,
            martedi: document.getElementById('orariMartedi').value,
            mercoledi: document.getElementById('orariMercoledi').value,
            giovedi: document.getElementById('orariGiovedi').value,
            venerdi: document.getElementById('orariVenerdi').value,
            sabato: document.getElementById('orariSabato').value,
            domenica: document.getElementById('orariDomenica').value
        };

        // Validazione dei campi obbligatori
        if (!nome || !tipoCucina || isNaN(latitudine) || isNaN(longitudine) || !numeroTelefono) {
            document.getElementById('modalAlertArea').innerHTML = `
                <div class="alert alert-danger" role="alert">
                    Compila tutti i campi obbligatori.
                </div>
            `;
            return;
        }

        // Chiamata al backend per creare il ristorante
        window.javaConnector.creaRistorante({
            nome: nome,
            tipoCucina: tipoCucina,
            fasciaPrezzo: fasciaPrezzo,
            orariApertura: orariApertura,
            latitudine: latitudine,
            longitudine: longitudine,
            idProprietario: idProprietario,
            numeroTelefono: numeroTelefono,
            consegnaDomicilio: consegnaDomicilio
        })
            .then(response => {
                if (response.success) {
                    // Chiudi il modal
                    const modal = bootstrap.Modal.getInstance(document.getElementById('nuovoRistoranteModal'));
                    modal.hide();

                    // Mostra messaggio di successo
                    document.getElementById('alertArea').innerHTML = `
                    <div class="alert alert-success" role="alert">
                        Ristorante creato con successo!
                    </div>
                `;

                    // Ricarica la lista dei ristoranti
                    setTimeout(() => {
                        caricaRistoranti(idProprietario);
                    }, 1000);
                } else {
                    document.getElementById('modalAlertArea').innerHTML = `
                    <div class="alert alert-danger" role="alert">
                        Errore: ${response.error}
                    </div>
                `;
                }
            })
            .catch(error => {
                document.getElementById('modalAlertArea').innerHTML = `
                <div class="alert alert-danger" role="alert">
                    Errore di sistema: ${error}
                </div>
            `;
            });
    });
});

function caricaRistoranti(idProprietario) {
    const ristorantiContainer = document.getElementById('ristoranti-container');
    const loadingIndicator = document.getElementById('loading-indicator');
    const noRistoranti = document.getElementById('no-ristoranti');

    console.log("Chiamata al backend per recuperare i ristoranti con ID proprietario:", idProprietario);

    // Chiamata al backend per recuperare i ristoranti
    window.javaConnector.getRistorantiByProprietario({
        idProprietario: idProprietario
    })
        .then(response => {
            console.log("Risposta ricevuta:", response);
            // Nascondi l'indicatore di caricamento
            loadingIndicator.classList.add('d-none');

            if (response.success && response.ristoranti && response.ristoranti.length > 0) {
                // Mostra i ristoranti
                renderizzaRistoranti(response.ristoranti);
            } else {
                // Mostra il messaggio "nessun ristorante"
                noRistoranti.classList.remove('d-none');
            }
        })
        .catch(error => {
            console.error("Errore nella chiamata:", error);
            loadingIndicator.classList.add('d-none');
            document.getElementById('alertArea').innerHTML = `
            <div class="alert alert-danger" role="alert">
                Errore nel caricamento dei ristoranti: ${error}
            </div>
        `;
        });
}

function renderizzaRistoranti(ristoranti) {
    const container = document.getElementById('ristoranti-container');

    // Pulisci il container (rimuovi l'indicatore di caricamento)
    container.innerHTML = '';

    // Crea una card per ogni ristorante
    ristoranti.forEach(ristorante => {
        const ristoranteCard = document.createElement('div');
        ristoranteCard.className = 'col-md-6 col-lg-4 mb-4';

        // Genera le icone per la fascia di prezzo
        let fasciaPrezzo = '';
        for (let i = 0; i < ristorante.fasciaPrezzo; i++) {
            fasciaPrezzo += '€';
        }

        ristoranteCard.innerHTML = `
            <div class="card h-100">
                <div class="card-body">
                    <h5 class="card-title">${ristorante.nome}</h5>
                    <h6 class="card-subtitle mb-2 text-muted">${ristorante.tipoCucina}</h6>
                    <p class="card-text mb-2">
                        <span class="badge bg-info">${fasciaPrezzo}</span>
                        ${ristorante.consegnaDomicilio ?
            '<span class="badge bg-success ms-2">Consegna a domicilio</span>' : ''}
                    </p>
                    <p class="card-text">
                        <small class="text-muted">
                            <img src="../assets/icons/add.call" alt="" class="material-symbols-rounded">
                            ${ristorante.numeroTelefono}
                        </small>
                    </p>
                </div>
               <div class="card-footer d-flex justify-content-around">
                    <button class="btn btn-sm btn-outline-primary"
                            onclick="visualizzaDettaglio('${ristorante.id}')">
                       <img src="../assets/icons/expand.svg" alt="Open" class="material-symbols-rounded icon-color">
                        Dettagli
                    </button>
                    <button class="btn btn-sm btn-outline-warning"
                            onclick="modificaRistorante('${ristorante.id}')">
                       <img src="../assets/icons/edit.svg" alt="Edit" class="material-symbols-rounded icon-color">
                        Modifica
                    </button>
                    <button class="btn btn-sm btn-outline-danger"
                            onclick="eliminaRistorante('${ristorante.id}', '${ristorante.nome}')">
                       <img src="../assets/icons/delete.svg" alt="Delete" class="material-symbols-rounded icon-color">
                        Elimina
                    </button>
                </div>
            </div>
        `;

        container.appendChild(ristoranteCard);
    });
}

function visualizzaDettaglio(ristoranteId) {
    // Reindirizza alla pagina di dettaglio del ristorante
    window.location.href = `dettaglioristorante.html?id=${ristoranteId}`;
}

function modificaRistorante(ristoranteId) {
    // Reindirizza alla pagina di modifica del ristorante
    window.location.href = `modificaristorante.html?id=${ristoranteId}`;
}

function eliminaRistorante(ristoranteId, nomeRistorante) {
    if (confirm(`Sei sicuro di voler eliminare il ristorante "${nomeRistorante}"?`)) {
        window.javaConnector.eliminaRistorante({
            id: ristoranteId
        })
            .then(response => {
                if (response.success) {
                    document.getElementById('alertArea').innerHTML = `
                    <div class="alert alert-success" role="alert">
                        Ristorante eliminato con successo!
                    </div>
                `;

                    // Ricarica la lista dei ristoranti
                    setTimeout(() => {
                        caricaRistoranti(sessionStorage.getItem('userId'));
                    }, 1000);
                } else {
                    document.getElementById('alertArea').innerHTML = `
                    <div class="alert alert-danger" role="alert">
                        Errore: ${response.error}
                    </div>
                `;
                }
            })
            .catch(error => {
                document.getElementById('alertArea').innerHTML = `
                <div class="alert alert-danger" role="alert">
                    Errore: ${error}
                </div>
            `;
            });
    }
}