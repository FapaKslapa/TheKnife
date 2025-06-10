// Funzione di attesa per verificare che il bridge sia pronto
const waitForBridge = async (callback, maxAttempts = 20) => {
    let attempts = 0;

    const checkBridge = async () => {
        attempts++;
        if (window.javaConnector && typeof window.javaConnector.getRistorantiByProprietario === 'function') {
            console.log("Bridge trovato, procedo con la chiamata");
            callback();
        } else if (attempts < maxAttempts) {
            console.log(`Tentativo ${attempts}: Bridge non pronto, riprovo tra 800ms...`);
            setTimeout(checkBridge, 800);
        } else {
            console.error("Bridge non disponibile dopo diversi tentativi");
            document.getElementById('alertArea').innerHTML = `
                <div class="alert alert-danger" role="alert">
                    Errore di connessione con l'applicazione. Ricarica la pagina.
                </div>
            `;
            // Mostra dettagli di debug
            console.error("Dettagli bridge:", window.javaConnector ? "Bridge esiste" : "Bridge non esiste");
            if (window.javaConnector) {
                console.error("Metodi disponibili:", Object.keys(window.javaConnector));
            }
        }
    };

    await checkBridge();
};

// Implementazione geocoding con OpenStreetMap Nominatim
const geocodeAddress = async (address, callback) => {
    try {
        const response = await fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address)}`);
        const data = await response.json();

        if (data && data.length > 0) {
            const result = data[0];
            callback(null, {
                lat: parseFloat(result.lat),
                lng: parseFloat(result.lon)
            });
        } else {
            callback("Indirizzo non trovato", null);
        }
    } catch (error) {
        callback(error.toString(), null);
    }
};

// Funzione per caricare i ristoranti di un proprietario
const caricaRistoranti = async (idProprietario) => {
    // Mostra l'indicatore di caricamento
    document.getElementById('loading-indicator').classList.remove('d-none');
    document.getElementById('no-ristoranti').classList.add('d-none');

    try {
        console.log("Tentativo di caricamento ristoranti per proprietario ID:", idProprietario);
        
        // Verifica se il bridge è disponibile prima di chiamarlo
        if (!window.javaConnector || typeof window.javaConnector.getRistorantiByProprietario !== 'function') {
            throw new Error("Bridge non disponibile o metodo non trovato");
        }
        
        // Chiamata con parametri corretti
        const response = await window.javaConnector.getRistorantiByProprietario({
            idProprietario: idProprietario
        });
        
        console.log("Risposta ricevuta:", response);

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
    } catch (error) {
        // Nascondi l'indicatore di caricamento e mostra l'errore
        document.getElementById('loading-indicator').classList.add('d-none');
        console.error("Errore durante il caricamento:", error);
        document.getElementById('alertArea').innerHTML = `
            <div class="alert alert-danger" role="alert">
                Errore durante il caricamento dei ristoranti: ${error}
            </div>
        `;
    }
};

// Funzione per creare una card di ristorante
const creaRistoranteCard = (ristorante) => {
    // Crea un elemento div per la colonna
    const colDiv = document.createElement('div');
    colDiv.className = 'col-md-6 col-lg-4 mb-4';
    colDiv.id = `ristorante-${ristorante.id}`;

    // Funzione per visualizzare la fascia di prezzo
    const renderPrezzi = (fascia) => {
        let html = '';
        for (let i = 0; i < fascia; i++) {
            html += '<i class="bi bi-currency-euro"></i>';
        }
        for (let i = fascia; i < 3; i++) {
            html += '<i class="bi bi-currency-euro text-muted opacity-25"></i>';
        }
        return html;
    };

    // Crea la card con i dettagli del ristorante
    colDiv.innerHTML = `
        <div class="restaurant-card h-100">
            <div class="card-header d-flex justify-content-between align-items-center py-3">
                <h5 class="card-title mb-0">${ristorante.nome}</h5>
                <div class="dropdown">
                    <button class="btn-action" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                        <i class="bi bi-three-dots-vertical"></i>
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
            <div class="card-body position-relative">
                <div class="restaurant-info">
                    <div class="price-tag mb-3">
                        ${renderPrezzi(ristorante.fasciaPrezzo)}
                    </div>
                    
                    <div class="restaurant-details">
                        <div class="detail-item">
                            <div class="detail-icon">
                                <i class="bi bi-tag-fill"></i>
                            </div>
                            <div class="detail-text">
                                ${ristorante.tipoCucina}
                            </div>
                        </div>
                        
                        <div class="detail-item">
                            <div class="detail-icon">
                                <i class="bi bi-telephone-fill"></i>
                            </div>
                            <div class="detail-text">
                                ${ristorante.numeroTelefono}
                            </div>
                        </div>
                        
                        <div class="detail-item">
                            <div class="detail-icon ${ristorante.consegnaDomicilio ? 'delivery-available' : 'delivery-unavailable'}">
                                <i class="bi bi-${ristorante.consegnaDomicilio ? 'bicycle' : 'x-circle'}"></i>
                            </div>
                            <div class="detail-text">
                                ${ristorante.consegnaDomicilio ? 'Consegna a domicilio disponibile' : 'Nessuna consegna a domicilio'}
                            </div>
                        </div>
                    </div>
                    
                    <div class="opening-hours mt-3">
                        <button class="btn-slim toggle-hours">
                            <i class="bi bi-clock me-1"></i>Orari di apertura <i class="bi bi-chevron-down ms-1"></i>
                        </button>
                        <div class="hours-details mt-2" style="display: none;">
                            ${renderOrari(ristorante.orariApertura)}
                        </div>
                    </div>
                </div>
            </div>
            <div class="card-footer py-3">
                <div class="action-bar">
                    <button class="btn-action view-recensioni" data-id="${ristorante.id}" title="Visualizza recensioni">
                        <i class="bi bi-star"></i>
                    </button>
                    <button class="btn-action edit-ristorante" data-id="${ristorante.id}" title="Modifica ristorante">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn-action view-menu" data-id="${ristorante.id}" title="Gestisci menu">
                        <i class="bi bi-list-ul"></i>
                    </button>
                </div>
            </div>
        </div>
    `;

    // Aggiungi event listener per i pulsanti nella card
    setTimeout(() => {
        const editBtns = colDiv.querySelectorAll('.edit-ristorante');
        const deleteBtns = colDiv.querySelectorAll('.delete-ristorante');
        const menuBtns = colDiv.querySelectorAll('.view-menu');
        const ordersBtns = colDiv.querySelectorAll('.view-orders');
        const recensioniBtns = colDiv.querySelectorAll('.view-recensioni');
        const toggleHoursBtn = colDiv.querySelector('.toggle-hours');

        editBtns.forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.preventDefault();
                const ristoranteId = btn.getAttribute('data-id');
                apriModalModifica(ristoranteId);
            });
        });

        deleteBtns.forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.preventDefault();
                const ristoranteId = btn.getAttribute('data-id');
                confermaEliminaRistorante(ristoranteId);
            });
        });

        menuBtns.forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.preventDefault();
                const ristoranteId = btn.getAttribute('data-id');
                // Redirigi alla pagina di gestione menu
                console.log(`Gestisci menu: ${ristoranteId}`);
            });
        });

        ordersBtns.forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.preventDefault();
                const ristoranteId = btn.getAttribute('data-id');
                // Redirigi alla pagina di gestione ordini
                console.log(`Visualizza ordini: ${ristoranteId}`);
            });
        });

        recensioniBtns.forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.preventDefault();
                const ristoranteId = btn.getAttribute('data-id');
                apriModalRecensioni(ristoranteId);
            });
        });

        if (toggleHoursBtn) {
            toggleHoursBtn.addEventListener('click', (e) => {
                e.preventDefault();
                const hoursDetails = colDiv.querySelector('.hours-details');
                const isVisible = hoursDetails.style.display !== 'none';

                // Animazione più fluida
                if (isVisible) {
                    hoursDetails.style.opacity = '0';
                    hoursDetails.style.transform = 'translateY(-5px)';
                    setTimeout(() => {
                        hoursDetails.style.display = 'none';
                    }, 200);
                } else {
                    hoursDetails.style.display = 'block';
                    // Forza il browser a calcolare lo stile prima di applicare l'animazione
                    void hoursDetails.offsetWidth;
                    hoursDetails.style.opacity = '1';
                    hoursDetails.style.transform = 'translateY(0)';
                }

                // Cambia l'icona della freccia
                const arrow = toggleHoursBtn.querySelector('.bi-chevron-down, .bi-chevron-up');
                if (arrow) {
                    arrow.classList.toggle('bi-chevron-down');
                    arrow.classList.toggle('bi-chevron-up');
                }
            });
        }
    }, 0);

    return colDiv;
};

// Funzione per renderizzare gli orari
const renderOrari = (orari) => {
    if (!orari) return '<p class="text-muted">Orari non disponibili</p>';

    const giorni = [
        { key: 'lunedi', label: 'Lunedì' },
        { key: 'martedi', label: 'Martedì' },
        { key: 'mercoledi', label: 'Mercoledì' },
        { key: 'giovedi', label: 'Giovedì' },
        { key: 'venerdi', label: 'Venerdì' },
        { key: 'sabato', label: 'Sabato' },
        { key: 'domenica', label: 'Domenica' }
    ];

    let html = '<div class="row">';
    giorni.forEach(giorno => {
        const orarioGiorno = orari[giorno.key] || 'Chiuso';
        html += `
            <div class="col-md-6">
                <span class="fw-medium">${giorno.label}:</span> ${orarioGiorno}
            </div>
        `;
    });
    html += '</div>';

    return html;
};

// Funzione per confermare l'eliminazione di un ristorante
const confermaEliminaRistorante = (ristoranteId) => {
    if (confirm('Sei sicuro di voler eliminare questo ristorante? Questa azione non può essere annullata.')) {
        eliminaristorante(ristoranteId);
    }
};

// Funzione per eliminare effettivamente un ristorante
const eliminaristorante = async (ristoranteId) => {
    try {
        const response = await window.javaConnector.eliminaRistorante({
            id: ristoranteId
        });

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
    } catch (error) {
        document.getElementById('alertArea').innerHTML = `
            <div class="alert alert-danger" role="alert">
                Errore di sistema: ${error}
            </div>
        `;
    }
};

// Funzione comune per gestire la ricerca degli indirizzi (sia per nuovo che per modifica)
const geocodeAddressHandler = (prefix = '') => {
    const indirizzo = document.getElementById(`${prefix}indirizzo`).value;
    const btnId = `${prefix}cercaIndirizzo`;
    const resultAreaId = `${prefix}risultatoRicerca`;
    const alertAreaId = `${prefix}modalAlertArea`;
    const latId = `${prefix}latitudine`;
    const lngId = `${prefix}longitudine`;
    const foundAddressId = `${prefix}indirizzoTrovato`;
    const confirmBtnId = `${prefix}confermaIndirizzo`;

    if (!indirizzo) {
        document.getElementById(alertAreaId).innerHTML = `
            <div class="alert alert-danger">
                <i class="bi bi-exclamation-triangle me-2"></i>Inserisci un indirizzo da cercare
            </div>
        `;
        return;
    }

    // Mostra un indicatore di caricamento
    document.getElementById(btnId).innerHTML = `
        <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
        Ricerca in corso...
    `;

    // Usa la funzione geocodeAddress
    geocodeAddress(indirizzo, (error, coordinates) => {
        document.getElementById(btnId).innerHTML = `<i class="bi bi-search me-2"></i>Verifica indirizzo`;

        if (error) {
            document.getElementById(resultAreaId).style.display = 'block';
            document.getElementById(resultAreaId).innerHTML = `
                <div class="alert alert-danger">
                    <i class="bi bi-exclamation-triangle me-2"></i>Errore: ${error}
                </div>
            `;
            return;
        }

        // Mostra il risultato
        document.getElementById(resultAreaId).style.display = 'block';
        document.getElementById(resultAreaId).innerHTML = `
            <div class="alert alert-info">
                <span id="${foundAddressId}">Trovato: "${indirizzo}" (lat: ${coordinates.lat}, lng: ${coordinates.lng})</span>
                <button type="button" class="btn btn-sm btn-outline-info float-end" id="${confirmBtnId}">Conferma</button>
            </div>
        `;

        // Quando l'utente conferma l'indirizzo
        document.getElementById(confirmBtnId).addEventListener('click', () => {
            document.getElementById(latId).value = coordinates.lat;
            document.getElementById(lngId).value = coordinates.lng;
            document.getElementById(resultAreaId).innerHTML = `
                <div class="alert alert-success">
                    <i class="bi bi-check-circle me-2"></i>Indirizzo confermato: ${indirizzo}
                </div>
            `;
        });
    });
};

// Funzione per validare i dati del form (comune per nuovo e modifica)
const validateRistoranteForm = (prefix = '') => {
    const nome = document.getElementById(`${prefix}nome`).value;
    const tipoCucina = document.getElementById(`${prefix}tipoCucina`).value;
    const latitudine = parseFloat(document.getElementById(`${prefix}latitudine`).value);
    const longitudine = parseFloat(document.getElementById(`${prefix}longitudine`).value);
    const numeroTelefono = document.getElementById(`${prefix}numeroTelefono`).value;
    const alertAreaId = `${prefix}modalAlertArea`;

    if (!nome || !tipoCucina || isNaN(latitudine) || isNaN(longitudine) || !numeroTelefono) {
        document.getElementById(alertAreaId).innerHTML = `
            <div class="alert alert-danger">
                <i class="bi bi-exclamation-triangle me-2"></i>Compila tutti i campi obbligatori.
            </div>
        `;
        return false;
    }
    return true;
};

// Funzione per raccogliere i dati del form (comune per nuovo e modifica)
const getRistoranteFormData = (prefix = '') => {
    const giorni = ['lunedi', 'martedi', 'mercoledi', 'giovedi', 'venerdi', 'sabato', 'domenica'];
    const orariApertura = {};

    giorni.forEach(giorno => {
        const fieldId = `${prefix}orari${giorno.charAt(0).toUpperCase() + giorno.slice(1)}`;
        const orarioInput = document.getElementById(fieldId);

        // Verifica che l'elemento esista prima di accedere al suo valore
        if (orarioInput) {
            orariApertura[giorno] = orarioInput.value || 'Chiuso';
        } else {
            console.warn(`Elemento ${fieldId} non trovato nel DOM`);
            orariApertura[giorno] = 'Chiuso';
        }
    });

    const data = {
        nome: document.getElementById(`${prefix}nome`).value,
        tipoCucina: document.getElementById(`${prefix}tipoCucina`).value,
        fasciaPrezzo: parseInt(document.getElementById(`${prefix}fasciaPrezzo`).value),
        orariApertura: orariApertura,
        latitudine: parseFloat(document.getElementById(`${prefix}latitudine`).value),
        longitudine: parseFloat(document.getElementById(`${prefix}longitudine`).value),
        numeroTelefono: document.getElementById(`${prefix}numeroTelefono`).value,
        consegnaDomicilio: document.getElementById(`${prefix}consegnaDomicilio`).checked
    };

    if (prefix === 'edit-') {
        data.id = document.getElementById('ristoranteId').value;
    } else {
        data.idProprietario = sessionStorage.getItem('userId');
    }

    return data;
};

// Funzione per salvare un nuovo ristorante
const salvaRistoranteHandler = async (e) => {
    if (e) e.preventDefault();

    if (!validateRistoranteForm()) return;

    const ristoranteData = getRistoranteFormData();

    document.getElementById('modalAlertArea').innerHTML = `
        <div class="alert alert-info">
            <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
            Creazione ristorante in corso...
        </div>
    `;

    await waitForBridge(async () => {
        try {
            const response = await window.javaConnector.creaRistorante(ristoranteData);

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

                // Ricarica immediatamente la lista dei ristoranti dal server
                // caricaRistoranti(sessionStorage.getItem('userId'));
            } else {
                document.getElementById('modalAlertArea').innerHTML = `
                    <div class="alert alert-danger" role="alert">
                        <i class="bi bi-exclamation-triangle me-2"></i>Errore: ${response.error}
                    </div>
                `;
            }
        } catch (error) {
            document.getElementById('modalAlertArea').innerHTML = `
                <div class="alert alert-danger" role="alert">
                    <i class="bi bi-exclamation-triangle me-2"></i>Errore di sistema: ${error}
                </div>
            `;
        }
    });
};
// Funzione per aprire il modal di modifica e popolare i campi
const apriModalModifica = async (ristoranteId) => {
    // Inserisci l'ID del ristorante in un campo nascosto
    document.getElementById('ristoranteId').value = ristoranteId;

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
    await waitForBridge(async () => {
        try {
            const response = await window.javaConnector.getRistoranteById({
                id: ristoranteId
            });

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
        } catch (error) {
            document.getElementById('edit-modalAlertArea').innerHTML = `
                <div class="alert alert-danger">
                    <i class="bi bi-exclamation-triangle me-2"></i>Errore di sistema: ${error}
                </div>
            `;
        }
    });
};

// Funzione per salvare le modifiche del ristorante

const salvaModificheRistoranteHandler = async (e) => {
    if (e) e.preventDefault();

    if (!validateRistoranteForm('edit-')) return;

    const ristoranteData = getRistoranteFormData('edit-');

    document.getElementById('edit-modalAlertArea').innerHTML = `
        <div class="alert alert-info">
            <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
            Salvataggio modifiche in corso...
        </div>
    `;

    await waitForBridge(async () => {
        try {
            const response = await window.javaConnector.modificaRistorante(ristoranteData);

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

                // Ricarica immediatamente la lista dei ristoranti dal server
                caricaRistoranti(sessionStorage.getItem('userId'));
            } else {
                document.getElementById('edit-modalAlertArea').innerHTML = `
                    <div class="alert alert-danger">
                        <i class="bi bi-exclamation-triangle me-2"></i>Errore: ${response.error}
                    </div>
                `;
            }
        } catch (error) {
            document.getElementById('edit-modalAlertArea').innerHTML = `
                <div class="alert alert-danger">
                    <i class="bi bi-exclamation-triangle me-2"></i>Errore di sistema: ${error}
                </div>
            `;
        }
    });
};

// Funzione per aprire il modal delle recensioni
const apriModalRecensioni = async (ristoranteId) => {
    // Recupera prima le info del ristorante per mostrare il nome
    try {
        const infoResponse = await window.javaConnector.getRistoranteById({
            id: ristoranteId
        });

        if (infoResponse.success) {
            const ristorante = infoResponse.ristorante;
            document.getElementById('recensioni-ristorante-nome').textContent = ristorante.nome;

            // Mostra indicatore di caricamento
            document.getElementById('recensioni-container').innerHTML = `
                <div class="text-center py-3">
                    <div class="spinner-border text-primary" role="status">
                        <span class="visually-hidden">Caricamento recensioni...</span>
                    </div>
                </div>
            `;

            // Apri il modal
            const recensioniModal = new bootstrap.Modal(document.getElementById('recensioniModal'));
            recensioniModal.show();

            // Carica le recensioni
            await caricaRecensioni(ristoranteId);
        }
    } catch (error) {
        console.error("Errore nel recupero delle informazioni del ristorante:", error);
    }
};

// Funzione per caricare le recensioni di un ristorante
const caricaRecensioni = async (ristoranteId) => {
    try {
        const response = await window.javaConnector.getRecensioniByRistorante({
            idRistorante: ristoranteId
        });

        const recensioniContainer = document.getElementById('recensioni-container');
        const noRecensioni = document.getElementById('no-recensioni');

        if (response.success && response.recensioni && response.recensioni.length > 0) {
            // Ci sono recensioni da visualizzare
            recensioniContainer.innerHTML = '';
            noRecensioni.classList.add('d-none');

            response.recensioni.forEach(recensione => {
                const recensioneElement = creaRecensioneElement(recensione);
                recensioniContainer.appendChild(recensioneElement);
            });
        } else {
            // Nessuna recensione
            recensioniContainer.innerHTML = '';
            noRecensioni.classList.remove('d-none');
        }
    } catch (error) {
        document.getElementById('recensioni-container').innerHTML = `
            <div class="alert alert-danger" role="alert">
                <i class="bi bi-exclamation-triangle me-2"></i>Errore nel caricamento delle recensioni: ${error}
            </div>
        `;
    }
};

// Funzione per creare l'elemento HTML di una recensione
const creaRecensioneElement = (recensione) => {
    const div = document.createElement('div');
    div.className = 'review-card-compact mb-3';
    div.id = `recensione-${recensione.id}`;

    // Funzione per renderizzare le stelle del voto
    const renderStelle = (voto) => {
        let html = '';
        for (let i = 0; i < voto; i++) {
            html += '<i class="bi bi-star-fill"></i>';
        }
        for (let i = voto; i < 5; i++) {
            html += '<i class="bi bi-star"></i>';
        }
        return html;
    };

    // Gestione della data
    let dataFormattata = 'Data non disponibile';
    try {
        // Verifica se stiamo usando il nuovo schema (key_r, key_user, date) o il vecchio
        const dataString = recensione.date || recensione.dataCreazione;
        if (dataString) {
            const data = new Date(dataString);
            if (!isNaN(data.getTime())) { // Verifica che la data sia valida
                dataFormattata = data.toLocaleDateString('it-IT', {
                    day: '2-digit',
                    month: '2-digit',
                    year: 'numeric'
                });
            }
        }
    } catch (e) {
        console.error("Errore nel parsing della data:", e);
    }

    // Gestione dell'utente
    const nomeUtente = recensione.nomeUtente || 'Utente';

    div.innerHTML = `
        <div class="review-header">
            <div class="review-title-wrap">
                <h5 class="review-title">${recensione.title || recensione.titolo}</h5>
                <div class="review-stars">
                    ${renderStelle(recensione.rate || recensione.voto)}
                </div>
            </div>
            <div class="review-meta">
                <span class="review-author">${nomeUtente}</span>
                <span class="review-date">${dataFormattata}</span>
            </div>
        </div>
        <div class="review-body">
            <p>${recensione.text || recensione.testo}</p>
        </div>
    `;

    return div;
};

// Funzione per aggiornare gli orari nei campi nascosti
const updateHiddenTimeFields = (prefix = '') => {
    const giorni = ['lunedi', 'martedi', 'mercoledi', 'giovedi', 'venerdi', 'sabato', 'domenica'];

    giorni.forEach(giorno => {
        const slotsContainerId = `${prefix}${giorno}-slots`;
        const hiddenFieldId = `${prefix}orari${giorno.charAt(0).toUpperCase() + giorno.slice(1)}`;
        const chiusoCheckboxId = `${prefix}chiuso-${giorno}`;

        const slotsContainer = document.getElementById(slotsContainerId);
        const hiddenField = document.getElementById(hiddenFieldId);
        const chiusoCheckbox = document.getElementById(chiusoCheckboxId);

        if (!slotsContainer || !hiddenField) return;

        // Se il giorno è marcato come chiuso
        if (chiusoCheckbox && chiusoCheckbox.checked) {
            hiddenField.value = 'Chiuso';
            return;
        }

        // Ottieni tutte le fasce orarie
        const timeSlots = slotsContainer.querySelectorAll('.time-slot');
        if (timeSlots.length === 0) {
            hiddenField.value = 'Chiuso';
            return;
        }

        // Formato: 12:00-15:00, 19:00-23:00
        let orarioGiorno = '';
        timeSlots.forEach((slot, index) => {
            const startTime = slot.querySelector('.time-start').value;
            const endTime = slot.querySelector('.time-end').value;

            if (startTime && endTime) {
                if (index > 0) orarioGiorno += ', ';
                orarioGiorno += `${startTime}-${endTime}`;
            }
        });

        // Se non ci sono fasce valide
        if (!orarioGiorno) {
            orarioGiorno = 'Chiuso';
        }

        hiddenField.value = orarioGiorno;
    });
};

// Funzione per gestire l'interazione con i controlli dell'orario
const setupTimeControls = (prefix = '') => {
    // Gestione dei tab per i giorni della settimana
    const dayItems = document.querySelectorAll(`${prefix ? '#' + prefix.slice(0, -1) + ' ' : ''}.day-item`);
    const daySlots = document.querySelectorAll(`${prefix ? '#' + prefix.slice(0, -1) + ' ' : ''}.day-time-slots`);

    dayItems.forEach(item => {
        item.addEventListener('click', () => {
            // Rimuovi la classe active da tutti i tab
            dayItems.forEach(i => i.classList.remove('active'));
            // Aggiungi la classe active al tab cliccato
            item.classList.add('active');

            // Nascondi tutti i contenitori di orari
            daySlots.forEach(slot => slot.classList.add('d-none'));

            // Mostra il contenitore corrispondente al giorno selezionato
            const giorno = item.getAttribute('data-day');
            const targetSlot = document.getElementById(`${prefix}slots-${giorno}`);
            if (targetSlot) {
                targetSlot.classList.remove('d-none');
            }
        });
    });

    // Gestione dei pulsanti per aggiungere fasce orarie
    const addSlotButtons = document.querySelectorAll(`${prefix ? '#' + prefix.slice(0, -1) + ' ' : ''}.btn-add-slot`);
    addSlotButtons.forEach(button => {
        button.addEventListener('click', () => {
            const giorno = button.getAttribute('data-day');
            const slotsContainer = document.getElementById(`${prefix}${giorno}-slots`);

            if (slotsContainer) {
                // Crea una nuova fascia oraria
                const newSlot = document.createElement('div');
                newSlot.className = 'time-slot';
                newSlot.innerHTML = `
                    <div class="slot-inputs">
                        <div class="time-input">
                            <label>Apertura</label>
                            <input type="time" class="form-control time-start" value="12:00">
                        </div>
                        <div class="time-separator">-</div>
                        <div class="time-input">
                            <label>Chiusura</label>
                            <input type="time" class="form-control time-end" value="15:00">
                        </div>
                    </div>
                    <button type="button" class="btn-remove-slot"><i class="bi bi-trash"></i></button>
                `;

                slotsContainer.appendChild(newSlot);

                // Aggiungi event listener per il pulsante di rimozione
                const removeButton = newSlot.querySelector('.btn-remove-slot');
                removeButton.addEventListener('click', () => {
                    newSlot.remove();
                    updateHiddenTimeFields(prefix);
                });

                // Aggiungi event listeners per gli input di orario
                const timeInputs = newSlot.querySelectorAll('input[type="time"]');
                timeInputs.forEach(input => {
                    input.addEventListener('change', () => {
                        updateHiddenTimeFields(prefix);
                    });
                });

                // Aggiorna i campi nascosti
                updateHiddenTimeFields(prefix);
            }
        });
    });

    // Gestione delle caselle di controllo "Chiuso"
    const closedToggles = document.querySelectorAll(`${prefix ? '#' + prefix.slice(0, -1) + ' ' : ''}.day-closed-toggle`);
    closedToggles.forEach(toggle => {
        toggle.addEventListener('change', () => {
            const giornoId = toggle.id.replace(`${prefix}chiuso-`, '');
            const slotsContainer = document.getElementById(`${prefix}${giornoId}-slots`);

            if (slotsContainer) {
                // Se chiuso, disabilita tutti gli input
                const isDisabled = toggle.checked;
                const timeSlots = slotsContainer.querySelectorAll('.time-slot');

                timeSlots.forEach(slot => {
                    const inputs = slot.querySelectorAll('input');
                    inputs.forEach(input => {
                        input.disabled = isDisabled;
                    });

                    if (isDisabled) {
                        slot.classList.add('disabled');
                    } else {
                        slot.classList.remove('disabled');
                    }
                });

                // Aggiorna i campi nascosti
                updateHiddenTimeFields(prefix);
            }
        });
    });

    // Aggiungi event listeners per i pulsanti di rimozione esistenti
    const removeButtons = document.querySelectorAll(`${prefix ? '#' + prefix.slice(0, -1) + ' ' : ''}.btn-remove-slot`);
    removeButtons.forEach(button => {
        button.addEventListener('click', () => {
            const slot = button.closest('.time-slot');
            if (slot) {
                slot.remove();
                updateHiddenTimeFields(prefix);
            }
        });
    });

    // Aggiungi event listeners per gli input di orario esistenti
    const timeInputs = document.querySelectorAll(`${prefix ? '#' + prefix.slice(0, -1) + ' ' : ''} input[type="time"]`);
    timeInputs.forEach(input => {
        input.addEventListener('change', () => {
            updateHiddenTimeFields(prefix);
        });
    });

    // Pulsante per copiare orari dal giorno precedente
    const copyTimeBtn = document.getElementById(`${prefix}btn-copia-orari`);
    if (copyTimeBtn) {
        copyTimeBtn.addEventListener('click', (e) => {
            e.preventDefault();

            // Trova il giorno corrente
            const activeDay = document.querySelector(`${prefix ? '#' + prefix.slice(0, -1) + ' ' : ''}.day-item.active`);
            if (!activeDay) return;

            const giornoAttuale = activeDay.getAttribute('data-day');
            const giorni = ['lunedi', 'martedi', 'mercoledi', 'giovedi', 'venerdi', 'sabato', 'domenica'];
            const indexGiornoAttuale = giorni.indexOf(giornoAttuale);

            // Se è lunedì, non c'è un giorno precedente
            if (indexGiornoAttuale <= 0) {
                alert('Non c\'è un giorno precedente da cui copiare gli orari.');
                return;
            }

            const giornoPrecedente = giorni[indexGiornoAttuale - 1];

            // Copia il valore dal campo nascosto del giorno precedente
            const fieldPrecedenteId = `${prefix}orari${giornoPrecedente.charAt(0).toUpperCase() + giornoPrecedente.slice(1)}`;
            const fieldAttualeId = `${prefix}orari${giornoAttuale.charAt(0).toUpperCase() + giornoAttuale.slice(1)}`;

            const fieldPrecedente = document.getElementById(fieldPrecedenteId);
            const fieldAttuale = document.getElementById(fieldAttualeId);

            if (fieldPrecedente && fieldAttuale) {
                fieldAttuale.value = fieldPrecedente.value;

                // Aggiorna anche la visualizzazione
                // (questa è una semplificazione - dovresti aggiornare gli slot effettivi)
                alert(`Orari copiati da ${giornoPrecedente} a ${giornoAttuale}`);
            }
        });
    }

    // Pulsante per aggiungere orari pranzo e cena
    const addLunchDinnerBtn = document.getElementById(`${prefix}btn-aggiungi-pranzo-cena`);
    if (addLunchDinnerBtn) {
        addLunchDinnerBtn.addEventListener('click', (e) => {
            e.preventDefault();

            // Trova il giorno corrente
            const activeDay = document.querySelector(`${prefix ? '#' + prefix.slice(0, -1) + ' ' : ''}.day-item.active`);
            if (!activeDay) return;

            const giorno = activeDay.getAttribute('data-day');
            const slotsContainer = document.getElementById(`${prefix}${giorno}-slots`);

            if (slotsContainer) {
                // Rimuovi tutti gli slot esistenti
                slotsContainer.innerHTML = '';

                // Aggiungi slot pranzo
                const lunchSlot = document.createElement('div');
                lunchSlot.className = 'time-slot';
                lunchSlot.innerHTML = `
                    <div class="slot-inputs">
                        <div class="time-input">
                            <label>Apertura</label>
                            <input type="time" class="form-control time-start" value="12:00">
                        </div>
                        <div class="time-separator">-</div>
                        <div class="time-input">
                            <label>Chiusura</label>
                            <input type="time" class="form-control time-end" value="15:00">
                        </div>
                    </div>
                    <button type="button" class="btn-remove-slot"><i class="bi bi-trash"></i></button>
                `;

                // Aggiungi slot cena
                const dinnerSlot = document.createElement('div');
                dinnerSlot.className = 'time-slot';
                dinnerSlot.innerHTML = `
                    <div class="slot-inputs">
                        <div class="time-input">
                            <label>Apertura</label>
                            <input type="time" class="form-control time-start" value="19:00">
                        </div>
                        <div class="time-separator">-</div>
                        <div class="time-input">
                            <label>Chiusura</label>
                            <input type="time" class="form-control time-end" value="23:00">
                        </div>
                    </div>
                    <button type="button" class="btn-remove-slot"><i class="bi bi-trash"></i></button>
                `;

                slotsContainer.appendChild(lunchSlot);
                slotsContainer.appendChild(dinnerSlot);

                // Aggiungi event listeners per i pulsanti di rimozione
                const removeButtons = slotsContainer.querySelectorAll('.btn-remove-slot');
                removeButtons.forEach(button => {
                    button.addEventListener('click', () => {
                        const slot = button.closest('.time-slot');
                        if (slot) {
                            slot.remove();
                            updateHiddenTimeFields(prefix);
                        }
                    });
                });

                // Aggiungi event listeners per gli input di orario
                const timeInputs = slotsContainer.querySelectorAll('input[type="time"]');
                timeInputs.forEach(input => {
                    input.addEventListener('change', () => {
                        updateHiddenTimeFields(prefix);
                    });
                });

                // Aggiorna i campi nascosti
                updateHiddenTimeFields(prefix);
            }
        });
    }

    // Pulsante per resettare orari
    const resetTimeBtn = document.getElementById(`${prefix}btn-reset-orari`);
    if (resetTimeBtn) {
        resetTimeBtn.addEventListener('click', (e) => {
            e.preventDefault();

            // Trova il giorno corrente
            const activeDay = document.querySelector(`${prefix ? '#' + prefix.slice(0, -1) + ' ' : ''}.day-item.active`);
            if (!activeDay) return;

            const giorno = activeDay.getAttribute('data-day');
            const slotsContainer = document.getElementById(`${prefix}${giorno}-slots`);

            if (slotsContainer) {
                // Rimuovi tutti gli slot esistenti
                slotsContainer.innerHTML = '';

                // Aggiungi uno slot predefinito
                const defaultSlot = document.createElement('div');
                defaultSlot.className = 'time-slot';
                defaultSlot.innerHTML = `
                    <div class="slot-inputs">
                        <div class="time-input">
                            <label>Apertura</label>
                            <input type="time" class="form-control time-start" value="12:00">
                        </div>
                        <div class="time-separator">-</div>
                        <div class="time-input">
                            <label>Chiusura</label>
                            <input type="time" class="form-control time-end" value="15:00">
                        </div>
                    </div>
                    <button type="button" class="btn-remove-slot"><i class="bi bi-trash"></i></button>
                `;

                slotsContainer.appendChild(defaultSlot);

                // Aggiungi event listener per il pulsante di rimozione
                const removeButton = defaultSlot.querySelector('.btn-remove-slot');
                removeButton.addEventListener('click', () => {
                    defaultSlot.remove();
                    updateHiddenTimeFields(prefix);
                });

                // Aggiungi event listeners per gli input di orario
                const timeInputs = defaultSlot.querySelectorAll('input[type="time"]');
                timeInputs.forEach(input => {
                    input.addEventListener('change', () => {
                        updateHiddenTimeFields(prefix);
                    });
                });

                // Aggiorna i campi nascosti
                updateHiddenTimeFields(prefix);
            }
        });
    }
};

// Inizializzazione all'avvio della pagina
document.addEventListener('DOMContentLoaded', async () => {
    console.log("Inizializzazione della pagina...");
    
    // Verifica se l'utente è loggato
    const isLoggedIn = sessionStorage.getItem('isLoggedIn');
    const userId = sessionStorage.getItem('userId');
    const username = sessionStorage.getItem('username');
    const ruolo = sessionStorage.getItem('ruolo');

    console.log("Stato login:", { isLoggedIn, userId, username, ruolo });

    // Mostra username nella navbar
    const usernameElement = document.getElementById('username-display');
    if (usernameElement) usernameElement.textContent = username || '';

    // Reindirizza alla pagina di login se l'utente non è loggato o non è un ristoratore
    if (!isLoggedIn || ruolo !== 'RISTORATORE') {
        console.log("Utente non autorizzato, reindirizzamento al login...");
        window.location.href = "index.html";
        return;
    }

    // CORREZIONI PER PREVENIRE IL REFRESH DELLA PAGINA

    // 1. Blocca tutti i form per impedirne l'invio automatico
    document.querySelectorAll('form').forEach(form => {
        form.addEventListener('submit', (e) => {
            e.preventDefault();
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
        link.addEventListener('click', (e) => {
            e.preventDefault();
        });
    });

    // Inizializza i controlli per la gestione degli orari
    setupTimeControls();
    setupTimeControls('edit-');
    
    // Aggiorna i campi nascosti per gli orari
    updateHiddenTimeFields();
    updateHiddenTimeFields('edit-');

    // Attendi che il bridge sia disponibile prima di chiamare la funzione
    console.log("In attesa del bridge...");
    await waitForBridge(async () => {
        console.log("Bridge pronto, caricamento ristoranti...");
        await caricaRistoranti(userId);
    });

    // Gestisci il logout
    const logoutLink = document.getElementById('logout-link');
    if (logoutLink) {
        logoutLink.addEventListener('click', (e) => {
            e.preventDefault();
            sessionStorage.clear();
            window.location.href = "index.html";
        });
    }

    // Event listeners per la creazione
    const cercaIndirizzoBtn = document.getElementById('cercaIndirizzo');
    if (cercaIndirizzoBtn) {
        cercaIndirizzoBtn.addEventListener('click', () => geocodeAddressHandler());
    }

    const salvaRistoranteBtn = document.getElementById('salvaRistorante');
    if (salvaRistoranteBtn) {
        salvaRistoranteBtn.addEventListener('click', salvaRistoranteHandler);
    }

    // Event listeners per la modifica
    const cercaIndirizzoEditBtn = document.getElementById('edit-cercaIndirizzo');
    if (cercaIndirizzoEditBtn) {
        cercaIndirizzoEditBtn.addEventListener('click', () => geocodeAddressHandler('edit-'));
    }

    const salvaModificheBtn = document.getElementById('salvaModificheRistorante');
    if (salvaModificheBtn) {
        salvaModificheBtn.addEventListener('click', salvaModificheRistoranteHandler);
    }

    // Modifica l'event listener per il pulsante recensioni nelle card
    setTimeout(() => {
        document.querySelectorAll('.view-recensioni').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.preventDefault();
                const ristoranteId = btn.getAttribute('data-id');
                apriModalRecensioni(ristoranteId);
            });
        });
    }, 1000); // Attendi un po' per assicurarti che le card siano state create
    
    console.log("Inizializzazione completata!");
});
