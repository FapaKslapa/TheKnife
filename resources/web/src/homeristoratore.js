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

// Implementazione completa del geocoding utilizzando OpenStreetMap Nominatim
function geocodeAddress(address, callback) {
    fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address)}`)
        .then(response => response.json())
        .then(data => {
            if (data && data.length > 0) {
                const result = data[0];
                callback(null, {
                    lat: parseFloat(result.lat),
                    lng: parseFloat(result.lon)
                });
            } else {
                callback("Indirizzo non trovato", null);
            }
        })
        .catch(error => {
            callback(error.toString(), null);
        });
}

// Funzione per caricare i ristoranti di un proprietario
function caricaRistoranti(idProprietario) {
    // Mostra l'indicatore di caricamento
    document.getElementById('loading-indicator').classList.remove('d-none');
    document.getElementById('no-ristoranti').classList.add('d-none');

    // Chiama il backend per ottenere i ristoranti
    window.javaConnector.getRistorantiByProprietario({
        idProprietario: idProprietario
    })
        .then(response => {
            // Nascondi l'indicatore di caricamento
            document.getElementById('loading-indicator').classList.add('d-none');

            // Ottieni il container dove inserire i ristoranti
            const ristorantiContainer = document.getElementById('ristoranti-container');

            if (response.success && response.ristoranti && response.ristoranti.length > 0) {
                // Svuota il container prima di aggiungere i ristoranti
                ristorantiContainer.innerHTML = '';

                // Aggiungi ogni ristorante al container
                response.ristoranti.forEach(ristorante => {
                    const card = creaRistoranteCard(ristorante);
                    ristorantiContainer.appendChild(card);
                });
            } else {
                // Mostra il messaggio di nessun ristorante
                document.getElementById('no-ristoranti').classList.remove('d-none');
            }
        })
        .catch(error => {
            // Nascondi l'indicatore di caricamento e mostra l'errore
            document.getElementById('loading-indicator').classList.add('d-none');
            document.getElementById('alertArea').innerHTML = `
            <div class="alert alert-danger" role="alert">
                Errore durante il caricamento dei ristoranti: ${error}
            </div>
        `;
        });
}

// Funzione per creare una card di ristorante
function creaRistoranteCard(ristorante) {
    // Crea un elemento div per la colonna
    const colDiv = document.createElement('div');
    colDiv.className = 'col-md-6 col-lg-4 mb-4';
    colDiv.id = `ristorante-${ristorante.id}`;

    // Crea la card con i dettagli del ristorante
    colDiv.innerHTML = `
        <div class="card h-100 bg-dark-subtle border-0 shadow-sm">
            <div class="card-header bg-dark-subtle border-0 d-flex justify-content-between align-items-center">
                <h5 class="card-title mb-0">${ristorante.nome}</h5>
                <div class="dropdown">
                    <button class="btn btn-sm btn-outline-secondary" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                        <i class="bi bi-three-dots"></i>
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end">
                        <li><a class="dropdown-item edit-ristorante" href="javascript:void(0)" data-id="${ristorante.id}"><i class="bi bi-pencil me-2"></i>Modifica</a></li>
                        <li><a class="dropdown-item view-menu" href="javascript:void(0)" data-id="${ristorante.id}"><i class="bi bi-list-ul me-2"></i>Gestisci menu</a></li>
                        <li><a class="dropdown-item view-orders" href="javascript:void(0)" data-id="${ristorante.id}"><i class="bi bi-bag me-2"></i>Ordini</a></li>
                        <li><hr class="dropdown-divider"></li>
                        <li><a class="dropdown-item text-danger delete-ristorante" href="javascript:void(0)" data-id="${ristorante.id}"><i class="bi bi-trash me-2"></i>Elimina</a></li>
                    </ul>
                </div>
            </div>
            <div class="card-body">
                <p class="card-text"><i class="bi bi-tag me-2"></i>${ristorante.tipoCucina}</p>
                <p class="card-text"><i class="bi bi-cash-coin me-2"></i>${'€'.repeat(ristorante.fasciaPrezzo)}</p>
                <p class="card-text"><i class="bi bi-telephone me-2"></i>${ristorante.numeroTelefono}</p>
                <p class="card-text">
                    <i class="bi bi-${ristorante.consegnaDomicilio ? 'bicycle' : 'x-circle'} me-2"></i>
                    ${ristorante.consegnaDomicilio ? 'Consegna a domicilio disponibile' : 'Nessuna consegna a domicilio'}
                </p>
            </div>
            <div class="card-footer bg-transparent border-0">
                <button class="btn btn-sm btn-outline-primary view-recensioni" data-id="${ristorante.id}">
                    <i class="bi bi-star me-2"></i>Recensioni
                </button>
            </div>
        </div>
    `;

    // Aggiungi event listener per i pulsanti nella card
    setTimeout(() => {
        const editBtn = colDiv.querySelector('.edit-ristorante');
        const deleteBtn = colDiv.querySelector('.delete-ristorante');
        const menuBtn = colDiv.querySelector('.view-menu');
        const ordersBtn = colDiv.querySelector('.view-orders');
        const recensioniBtn = colDiv.querySelector('.view-recensioni');

        if (editBtn) editBtn.addEventListener('click', function (e) {
            e.preventDefault();
            const ristoranteId = this.getAttribute('data-id');
            apriModalModifica(ristoranteId);
        });

        if (deleteBtn) deleteBtn.addEventListener('click', function (e) {
            e.preventDefault();
            const ristoranteId = this.getAttribute('data-id');
            confermaEliminaRistorante(ristoranteId);
        });

        if (menuBtn) menuBtn.addEventListener('click', function (e) {
            e.preventDefault();
            const ristoranteId = this.getAttribute('data-id');
            // Redirigi alla pagina di gestione menu
            console.log(`Gestisci menu: ${ristoranteId}`);
        });

        if (ordersBtn) ordersBtn.addEventListener('click', function (e) {
            e.preventDefault();
            const ristoranteId = this.getAttribute('data-id');
            // Redirigi alla pagina di gestione ordini
            console.log(`Visualizza ordini: ${ristoranteId}`);
        });

        if (recensioniBtn) recensioniBtn.addEventListener('click', function (e) {
            e.preventDefault();
            const ristoranteId = this.getAttribute('data-id');
            // Redirigi alla pagina delle recensioni
            console.log(`Visualizza recensioni: ${ristoranteId}`);
        });
    }, 0);

    return colDiv;
}

// Funzione per confermare l'eliminazione di un ristorante
function confermaEliminaRistorante(ristoranteId) {
    if (confirm('Sei sicuro di voler eliminare questo ristorante? Questa azione non può essere annullata.')) {
        eliminaristorante(ristoranteId);
    }
}

// Funzione per eliminare effettivamente un ristorante
function eliminaristorante(ristoranteId) {
    window.javaConnector.eliminaRistorante({
        id: ristoranteId
    })
        .then(response => {
            if (response.success) {
                // Rimuovi la card del ristorante dalla UI
                document.getElementById(`ristorante-${ristoranteId}`).remove();

                // Mostra messaggio di successo
                document.getElementById('alertArea').innerHTML = `
                <div class="alert alert-success" role="alert">
                    Ristorante eliminato con successo!
                </div>
            `;

                // Controlla se ci sono ancora ristoranti
                const ristorantiContainer = document.getElementById('ristoranti-container');
                if (ristorantiContainer.querySelectorAll('.col-md-6').length === 0) {
                    document.getElementById('no-ristoranti').classList.remove('d-none');
                }
            } else {
                // Mostra messaggio di errore
                document.getElementById('alertArea').innerHTML = `
                <div class="alert alert-danger" role="alert">
                    Errore durante l'eliminazione: ${response.error}
                </div>
            `;
            }
        })
        .catch(error => {
            document.getElementById('alertArea').innerHTML = `
            <div class="alert alert-danger" role="alert">
                Errore di sistema: ${error}
            </div>
        `;
        });
}

// Funzione per gestire la ricerca dell'indirizzo
function cercaIndirizzoHandler(e) {
    if (e) e.preventDefault();

    const indirizzo = document.getElementById('indirizzo').value;

    if (!indirizzo) {
        document.getElementById('modalAlertArea').innerHTML = `
            <div class="alert alert-danger">
                <i class="bi bi-exclamation-triangle me-2"></i>Inserisci un indirizzo da cercare
            </div>
        `;
        return;
    }

    // Mostra un indicatore di caricamento
    document.getElementById('cercaIndirizzo').innerHTML = `
        <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
        Ricerca in corso...
    `;

    // Usa la funzione geocodeAddress invece della simulazione con setTimeout
    geocodeAddress(indirizzo, function (error, coordinates) {
        document.getElementById('cercaIndirizzo').innerHTML = `<i class="bi bi-search me-2"></i>Verifica indirizzo`;

        if (error) {
            document.getElementById('risultatoRicerca').style.display = 'block';
            document.getElementById('risultatoRicerca').innerHTML = `
                <div class="alert alert-danger">
                    <i class="bi bi-exclamation-triangle me-2"></i>Errore: ${error}
                </div>
            `;
            return;
        }

        // Mostra il risultato
        document.getElementById('risultatoRicerca').style.display = 'block';
        document.getElementById('risultatoRicerca').innerHTML = `
            <div class="alert alert-info">
                <span id="indirizzoTrovato">Trovato: "${indirizzo}" (lat: ${coordinates.lat}, lng: ${coordinates.lng})</span>
                <button type="button" class="btn btn-sm btn-outline-info float-end" id="confermaIndirizzo">Conferma</button>
            </div>
        `;

        // Quando l'utente conferma l'indirizzo
        const confermaBtn = document.getElementById('confermaIndirizzo');
        if (confermaBtn) {
            confermaBtn.addEventListener('click', function (e) {
                if (e) e.preventDefault();
                document.getElementById('latitudine').value = coordinates.lat;
                document.getElementById('longitudine').value = coordinates.lng;
                document.getElementById('risultatoRicerca').innerHTML = `
                    <div class="alert alert-success">
                        <i class="bi bi-check-circle me-2"></i>Indirizzo confermato: ${indirizzo}
                    </div>
                `;
            });
        }
    });
}

// Funzione per gestire il salvataggio del ristorante
function salvaRistoranteHandler(e) {
    if (e) e.preventDefault();

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
                <i class="bi bi-exclamation-triangle me-2"></i>Compila tutti i campi obbligatori.
            </div>
        `;
        return;
    }

    // Chiamata al backend per creare il ristorante
    waitForBridge(function () {
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
                    <i class="bi bi-check-circle me-2"></i>Ristorante creato con successo!
                </div>
                `;

                    // Ricarica la lista dei ristoranti
                    setTimeout(() => {
                        caricaRistoranti(idProprietario);
                    }, 1000);
                } else {
                    document.getElementById('modalAlertArea').innerHTML = `
                <div class="alert alert-danger" role="alert">
                    <i class="bi bi-exclamation-triangle me-2"></i>Errore: ${response.error}
                </div>
                `;
                }
            })
            .catch(error => {
                document.getElementById('modalAlertArea').innerHTML = `
            <div class="alert alert-danger" role="alert">
                <i class="bi bi-exclamation-triangle me-2"></i>Errore di sistema: ${error}
            </div>
            `;
            });
    });
}

// NUOVE FUNZIONI PER LA MODIFICA DEI RISTORANTI

// Funzione per aprire il modal di modifica e popolare i campi
function apriModalModifica(ristoranteId) {
    // Inserisci l'ID del ristorante in un campo nascosto
    const idField = document.createElement('input');
    idField.type = 'hidden';
    idField.id = 'ristoranteId';
    idField.value = ristoranteId;

    // Rimuovi eventuali campi ID precedenti e aggiungi quello nuovo
    const oldIdField = document.getElementById('ristoranteId');
    if (oldIdField) oldIdField.remove();
    document.getElementById('modificaRistoranteForm').appendChild(idField);

    // Mostra un indicatore di caricamento
    document.getElementById('edit-modalAlertArea').innerHTML = `
        <div class="alert alert-info">
            <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
            Caricamento dati del ristorante...
        </div>
    `;

    // Apri il modal
    const modal = new bootstrap.Modal(document.getElementById('modificaRistoranteModal'));
    modal.show();

    // Recupera i dati del ristorante dal backend
    waitForBridge(function () {
        window.javaConnector.getRistoranteById({
            id: ristoranteId
        })
            .then(response => {
                if (response.success) {
                    const ristorante = response.ristorante;

                    // Popola i campi del form con i dati esistenti
                    document.getElementById('edit-nome').value = ristorante.nome;
                    document.getElementById('edit-tipoCucina').value = ristorante.tipoCucina;
                    document.getElementById('edit-fasciaPrezzo').value = ristorante.fasciaPrezzo;
                    document.getElementById('edit-latitudine').value = ristorante.latitudine;
                    document.getElementById('edit-longitudine').value = ristorante.longitudine;
                    document.getElementById('edit-numeroTelefono').value = ristorante.numeroTelefono;
                    document.getElementById('edit-consegnaDomicilio').checked = ristorante.consegnaDomicilio;

                    // Popola i campi degli orari usando un ciclo
                    const giorni = ['lunedi', 'martedi', 'mercoledi', 'giovedi', 'venerdi', 'sabato', 'domenica'];
                    for (let i = 0; i < giorni.length; i++) {
                        const giorno = giorni[i];
                        const orario = ristorante.orariApertura[giorno] || '';
                        const fieldId = `edit-orari${giorno.charAt(0).toUpperCase() + giorno.slice(1)}`;
                        document.getElementById(fieldId).value = orario;
                    }

                    // Rimuovi l'indicatore di caricamento
                    document.getElementById('edit-modalAlertArea').innerHTML = '';
                } else {
                    document.getElementById('edit-modalAlertArea').innerHTML = `
                    <div class="alert alert-danger">
                        <i class="bi bi-exclamation-triangle me-2"></i>Errore: ${response.error}
                    </div>
                `;
                }
            })
            .catch(error => {
                document.getElementById('edit-modalAlertArea').innerHTML = `
                <div class="alert alert-danger">
                    <i class="bi bi-exclamation-triangle me-2"></i>Errore di sistema: ${error}
                </div>
            `;
            });
    });
}

// Funzione per gestire la ricerca dell'indirizzo nella modalità modifica
function cercaIndirizzoModificaHandler(e) {
    if (e) e.preventDefault();

    const indirizzo = document.getElementById('edit-indirizzo').value;

    if (!indirizzo) {
        document.getElementById('edit-modalAlertArea').innerHTML = `
            <div class="alert alert-danger">
                <i class="bi bi-exclamation-triangle me-2"></i>Inserisci un indirizzo da cercare
            </div>
        `;
        return;
    }

    // Mostra un indicatore di caricamento
    document.getElementById('edit-cercaIndirizzo').innerHTML = `
        <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
        Ricerca in corso...
    `;

    // Usa la funzione geocodeAddress
    geocodeAddress(indirizzo, function (error, coordinates) {
        document.getElementById('edit-cercaIndirizzo').innerHTML = `<i class="bi bi-search me-2"></i>Verifica indirizzo`;

        if (error) {
            document.getElementById('edit-risultatoRicerca').style.display = 'block';
            document.getElementById('edit-risultatoRicerca').innerHTML = `
                <div class="alert alert-danger">
                    <i class="bi bi-exclamation-triangle me-2"></i>Errore: ${error}
                </div>
            `;
            return;
        }

        // Mostra il risultato
        document.getElementById('edit-risultatoRicerca').style.display = 'block';
        document.getElementById('edit-risultatoRicerca').innerHTML = `
            <div class="alert alert-info">
                <span id="edit-indirizzoTrovato">Trovato: "${indirizzo}" (lat: ${coordinates.lat}, lng: ${coordinates.lng})</span>
                <button type="button" class="btn btn-sm btn-outline-info float-end" id="edit-confermaIndirizzo">Conferma</button>
            </div>
        `;

        // Quando l'utente conferma l'indirizzo
        const confermaBtn = document.getElementById('edit-confermaIndirizzo');
        if (confermaBtn) {
            confermaBtn.addEventListener('click', function (e) {
                if (e) e.preventDefault();
                document.getElementById('edit-latitudine').value = coordinates.lat;
                document.getElementById('edit-longitudine').value = coordinates.lng;
                document.getElementById('edit-risultatoRicerca').innerHTML = `
                    <div class="alert alert-success">
                        <i class="bi bi-check-circle me-2"></i>Indirizzo confermato: ${indirizzo}
                    </div>
                `;
            });
        }
    });
}

// Funzione per salvare le modifiche del ristorante
function salvaModificheRistoranteHandler(e) {
    if (e) e.preventDefault();

    const id = document.getElementById('ristoranteId').value;
    const nome = document.getElementById('edit-nome').value;
    const tipoCucina = document.getElementById('edit-tipoCucina').value;
    const fasciaPrezzo = parseInt(document.getElementById('edit-fasciaPrezzo').value);
    const latitudine = parseFloat(document.getElementById('edit-latitudine').value);
    const longitudine = parseFloat(document.getElementById('edit-longitudine').value);
    const numeroTelefono = document.getElementById('edit-numeroTelefono').value;
    const consegnaDomicilio = document.getElementById('edit-consegnaDomicilio').checked;

    // Costruzione dell'oggetto orari con ciclo for
    const orariApertura = {};
    const giorni = ['lunedi', 'martedi', 'mercoledi', 'giovedi', 'venerdi', 'sabato', 'domenica'];
    for (let i = 0; i < giorni.length; i++) {
        const giorno = giorni[i];
        const orario = document.getElementById(`edit-orari${giorno.charAt(0).toUpperCase() + giorno.slice(1)}`).value;
        orariApertura[giorno] = orario;
    }

    // Validazione dei campi obbligatori
    if (!nome || !tipoCucina || isNaN(latitudine) || isNaN(longitudine) || !numeroTelefono) {
        document.getElementById('edit-modalAlertArea').innerHTML = `
            <div class="alert alert-danger">
                <i class="bi bi-exclamation-triangle me-2"></i>Compila tutti i campi obbligatori
            </div>
        `;
        return;
    }

    // Mostra indicatore di caricamento
    document.getElementById('edit-modalAlertArea').innerHTML = `
        <div class="alert alert-info">
            <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
            Salvataggio modifiche in corso...
        </div>
    `;

    // Chiamata al backend per modificare il ristorante
    waitForBridge(function () {
        window.javaConnector.modificaRistorante({
            id: id,
            nome: nome,
            tipoCucina: tipoCucina,
            fasciaPrezzo: fasciaPrezzo,
            orariApertura: orariApertura,
            latitudine: latitudine,
            longitudine: longitudine,
            numeroTelefono: numeroTelefono,
            consegnaDomicilio: consegnaDomicilio
        })
            .then(response => {
                if (response.success) {
                    // Chiudi il modal
                    const modal = bootstrap.Modal.getInstance(document.getElementById('modificaRistoranteModal'));
                    modal.hide();

                    // Mostra messaggio di successo
                    document.getElementById('alertArea').innerHTML = `
                    <div class="alert alert-success">
                        <i class="bi bi-check-circle me-2"></i>Ristorante aggiornato con successo!
                    </div>
                `;

                    // Ricarica la lista dei ristoranti
                    setTimeout(() => {
                        caricaRistoranti(sessionStorage.getItem('userId'));
                    }, 1000);
                } else {
                    document.getElementById('edit-modalAlertArea').innerHTML = `
                    <div class="alert alert-danger">
                        <i class="bi bi-exclamation-triangle me-2"></i>Errore: ${response.error}
                    </div>
                `;
                }
            })
            .catch(error => {
                document.getElementById('edit-modalAlertArea').innerHTML = `
                <div class="alert alert-danger">
                    <i class="bi bi-exclamation-triangle me-2"></i>Errore di sistema: ${error}
                </div>
            `;
            });
    });
}

// Inizializzazione all'avvio della pagina
document.addEventListener('DOMContentLoaded', function () {
    // Verifica se l'utente è loggato
    const isLoggedIn = sessionStorage.getItem('isLoggedIn');
    const userId = sessionStorage.getItem('userId');
    const username = sessionStorage.getItem('username');
    const ruolo = sessionStorage.getItem('ruolo');

    // Mostra username nella navbar
    const usernameElement = document.getElementById('username-display');
    if (usernameElement) usernameElement.textContent = username || '';

    // Reindirizza alla pagina di login se l'utente non è loggato o non è un ristoratore
    if (!isLoggedIn || ruolo !== 'RISTORATORE') {
        window.location.href = "index.html";
        return;
    }

    // CORREZIONI PER PREVENIRE IL REFRESH DELLA PAGINA

    // 1. Blocca tutti i form per impedirne l'invio automatico
    document.querySelectorAll('form').forEach(form => {
        form.addEventListener('submit', function (e) {
            e.preventDefault();
            console.log("Invio form bloccato");
            return false;
        });
    });

    // 2. Imposta l'attributo type="button" a tutti i bottoni all'interno dei form
    document.querySelectorAll('form button:not([type])').forEach(button => {
        button.setAttribute('type', 'button');
    });

    // 3. Modifica tutti i link con href="#"
    document.querySelectorAll('a[href="#"]').forEach(link => {
        link.setAttribute('href', 'javascript:void(0)');
        link.addEventListener('click', function (e) {
            e.preventDefault();
        });
    });

    console.log("Attendo che il bridge sia pronto...");
    // Attendi che il bridge sia disponibile prima di chiamare la funzione
    waitForBridge(function () {
        caricaRistoranti(userId);
    });

    // Gestisci il logout
// Gestisci il logout
    const logoutLink = document.getElementById('logout-link');
    if (logoutLink) {
        logoutLink.addEventListener('click', function (e) {
            e.preventDefault();
            sessionStorage.clear();
            window.location.href = "index.html";
        });
    }

    // Gestisci il click su cercaIndirizzo
    const cercaIndirizzoBtn = document.getElementById('cercaIndirizzo');
    if (cercaIndirizzoBtn) {
        cercaIndirizzoBtn.addEventListener('click', cercaIndirizzoHandler);
    }

    // Gestisci il click su salvaRistorante
    const salvaRistoranteBtn = document.getElementById('salvaRistorante');
    if (salvaRistoranteBtn) {
        salvaRistoranteBtn.addEventListener('click', salvaRistoranteHandler);
    }

    // Specifica correzione per il form nuovoRistoranteForm
    const nuovoRistoranteForm = document.getElementById('nuovoRistoranteForm');
    if (nuovoRistoranteForm) {
        nuovoRistoranteForm.setAttribute('onsubmit', 'return false;');
        nuovoRistoranteForm.addEventListener('submit', function (e) {
            e.preventDefault();
            console.log("Tentativo di invio del form nuovoRistoranteForm bloccato");
            return false;
        });
    }
});
