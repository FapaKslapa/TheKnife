// Funzione di attesa per verificare che il bridge sia pronto
const waitForBridge = async (callback, maxAttempts = 20) => {
    let attempts = 0;

    const checkBridge = async () => {
        attempts++;
        if (window.javaConnector && typeof window.javaConnector.getRistorantiByProprietario === 'function') {
            console.log("Bridge trovato, procedo con la chiamata");
            await callback();
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
            console.error("Dettagli bridge:", window.javaConnector ? "Bridge esiste" : "Bridge non esiste");
            if (window.javaConnector) {
                console.error("Metodi disponibili:", Object.keys(window.javaConnector));
            }
        }
    };

    await checkBridge();
};

// Implementazione geocoding con OpenStreetMap Nominatim
const geocodeAddress = async (address) => {
    try {
        const response = await fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address)}`);
        const data = await response.json();

        if (data && data.length > 0) {
            const result = data[0];
            return {
                success: true,
                coordinates: {
                    lat: parseFloat(result.lat),
                    lng: parseFloat(result.lon)
                }
            };
        } else {
            return {success: false, error: "Indirizzo non trovato"};
        }
    } catch (error) {
        return {success: false, error: error.toString()};
    }
};

// Funzione per caricare i ristoranti di un proprietario
const caricaRistoranti = async (idProprietario) => {
    const loadingIndicator = document.getElementById('loading-indicator');
    const noRistoranti = document.getElementById('no-ristoranti');

    loadingIndicator.classList.remove('d-none');
    noRistoranti.classList.add('d-none');

    try {
        console.log("Tentativo di caricamento ristoranti per proprietario ID:", idProprietario);

        if (!window.javaConnector || typeof window.javaConnector.getRistorantiByProprietario !== 'function') {
            throw new Error("Bridge non disponibile o metodo non trovato");
        }

        const response = await window.javaConnector.getRistorantiByProprietario({idProprietario});
        console.log("Risposta ricevuta:", response);

        loadingIndicator.classList.add('d-none');
        const ristorantiContainer = document.getElementById('ristoranti-container');

        if (response.success && response.ristoranti && response.ristoranti.length > 0) {
            ristorantiContainer.innerHTML = '';
            response.ristoranti.forEach(ristorante => {
                const card = creaRistoranteCard(ristorante);
                ristorantiContainer.appendChild(card);
            });
        } else {
            noRistoranti.classList.remove('d-none');
        }
    } catch (error) {
        loadingIndicator.classList.add('d-none');
        console.error("Errore durante il caricamento:", error);
        document.getElementById('alertArea').innerHTML = `
                        <div class="alert alert-danger" role="alert">
                            Errore durante il caricamento dei ristoranti: ${error}
                        </div>
                    `;
    }
};

// Funzione per creare una card di ristorante elegante
const creaRistoranteCard = (ristorante) => {
    const colDiv = document.createElement('div');
    colDiv.className = 'col-md-6 col-lg-4 mb-4';
    colDiv.id = `ristorante-${ristorante.id}`;

    const renderPrezzi = (fascia) => {
        return Array(3).fill().map((_, i) =>
            i < fascia
                ? '<i class="bi bi-currency-euro"></i>'
                : '<i class="bi bi-currency-euro text-muted opacity-25"></i>'
        ).join('');
    };

    colDiv.innerHTML = `
        <div class="restaurant-card h-100 shadow-lg">
            <div class="card-header glass-header d-flex align-items-start justify-content-between" style="border-bottom: none;">
                <div class="flex-grow-1">
                    <h5 class="card-title mb-0" style="margin-bottom:0; padding:0; line-height:1.2;">${ristorante.nome}</h5>
                </div>
                <div class="action-bar-glass ms-2">
                    <button class="btn-action btn-recensioni-ristorante" data-id="${ristorante.id}" title="Recensioni">
                        <i class="bi bi-star"></i>
                    </button>
                    <button class="btn-action btn-modifica-ristorante" data-id="${ristorante.id}" title="Modifica">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn-action btn-elimina-ristorante" data-id="${ristorante.id}" title="Elimina ristorante">
                        <i class="bi bi-trash"></i>
                    </button>
                </div>
            </div>
            <div class="card-body position-relative px-4 pt-3 pb-2">
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
                            ${renderOrariScrollable(ristorante.orariApertura)}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;

    setTimeout(() => {
        // Pulsante recensioni
        colDiv.querySelector('.btn-recensioni-ristorante').addEventListener('click', (e) => {
            e.preventDefault();
            apriModalRecensioni(ristorante.id);
        });
        // Pulsante modifica
        colDiv.querySelector('.btn-modifica-ristorante').addEventListener('click', (e) => {
            e.preventDefault();
            apriModalModifica(ristorante.id);
        });
        // Pulsante elimina
        colDiv.querySelector('.btn-elimina-ristorante').addEventListener('click', (e) => {
            e.preventDefault();
            document.getElementById('nome-ristorante-eliminazione').textContent = ristorante.nome;
            document.getElementById('id-ristorante-eliminazione').value = ristorante.id;
            const modal = new bootstrap.Modal(document.getElementById('confermaEliminazioneModal'));
            modal.show();
        });
        // Toggle orari apertura
        colDiv.querySelector('.toggle-hours').addEventListener('click', (e) => {
            e.preventDefault();
            const hoursDetails = colDiv.querySelector('.hours-details');
            const isVisible = hoursDetails.style.display !== 'none';
            if (isVisible) {
                hoursDetails.style.opacity = '0';
                hoursDetails.style.transform = 'translateY(-5px)';
                setTimeout(() => {
                    hoursDetails.style.display = 'none';
                }, 200);
            } else {
                hoursDetails.style.display = 'block';
                void hoursDetails.offsetWidth;
                hoursDetails.style.opacity = '1';
                hoursDetails.style.transform = 'translateY(0)';
            }
            const arrow = colDiv.querySelector('.toggle-hours i.bi-chevron-down, .toggle-hours i.bi-chevron-up');
            if (arrow) {
                arrow.classList.toggle('bi-chevron-down');
                arrow.classList.toggle('bi-chevron-up');
            }
        });
    }, 0);

    return colDiv;
};

// Gestione conferma eliminazione ristorante con modal
document.addEventListener('DOMContentLoaded', () => {
    const confermaBtn = document.getElementById('conferma-elimina-btn');
    if (confermaBtn) {
        confermaBtn.addEventListener('click', async () => {
            const ristoranteId = document.getElementById('id-ristorante-eliminazione').value;
            await eliminaristorante(ristoranteId);
            // Chiudi il modal
            const modal = bootstrap.Modal.getInstance(document.getElementById('confermaEliminazioneModal'));
            modal.hide();
        });
    }
});

// Funzione per renderizzare gli orari in modalità scrollabile con card per ogni giorno
const renderOrariScrollable = (orari) => {
    if (!orari) return '<p class="text-muted">Orari non disponibili</p>';

    const giorni = [
        {key: 'lunedi', label: 'Lun'},
        {key: 'martedi', label: 'Mar'},
        {key: 'mercoledi', label: 'Mer'},
        {key: 'giovedi', label: 'Gio'},
        {key: 'venerdi', label: 'Ven'},
        {key: 'sabato', label: 'Sab'},
        {key: 'domenica', label: 'Dom'}
    ];

    // Trova il giorno corrente (0 = domenica, 1 = lunedì, ...)
    const oggi = new Date();
    let idxOggi = oggi.getDay(); // 0 domenica, 1 lunedì, ...
    idxOggi = idxOggi === 0 ? 6 : idxOggi - 1; // 0 -> lunedì, 6 -> domenica

    // Aggiungi user-select: none direttamente qui per fallback
    let html = `<div class="orari-scroll-wrapper" style="user-select:none;"><div class="orari-scroll-inner" style="user-select:none;">`;
    giorni.forEach((giorno, idx) => {
        const orarioGiorno = orari[giorno.key] || 'Chiuso';
        const isToday = idx === idxOggi;

        // Split orari multipli
        let orariListHtml = '';
        if (orarioGiorno === 'Chiuso') {
            orariListHtml = `<div class="orario-item text-danger">Chiuso</div>`;
        } else {
            const fasce = orarioGiorno.split(',').map(f => f.trim()).filter(Boolean);
            orariListHtml = fasce.map(fascia => `<div class="orario-item">${fascia}</div>`).join('');
        }

        html += `
            <div class="giorno-card${isToday ? ' oggi' : ''}">
                <div class="giorno-label">${giorno.label}</div>
                <div class="orari-list">
                    ${orariListHtml}
                </div>
            </div>
        `;
    });
    html += `</div></div>`;
    return html;
};

// Funzione per renderizzare gli orari
const renderOrari = (orari) => {
    if (!orari) return '<p class="text-muted">Orari non disponibili</p>';

    const giorni = [
        {key: 'lunedi', label: 'Lunedì'},
        {key: 'martedi', label: 'Martedì'},
        {key: 'mercoledi', label: 'Mercoledì'},
        {key: 'giovedi', label: 'Giovedì'},
        {key: 'venerdi', label: 'Venerdì'},
        {key: 'sabato', label: 'Sabato'},
        {key: 'domenica', label: 'Domenica'}
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
        const response = await window.javaConnector.eliminaRistorante({id: ristoranteId});

        if (response.success) {
            document.getElementById(`ristorante-${ristoranteId}`)?.remove();

            document.getElementById('alertArea').innerHTML = `
                            <div class="alert alert-success" role="alert">
                                Ristorante eliminato con successo!
                            </div>
                        `;

            const ristorantiContainer = document.getElementById('ristoranti-container');
            if (ristorantiContainer.querySelectorAll('.col-md-6').length === 0) {
                document.getElementById('no-ristoranti').classList.remove('d-none');
            }
        } else {
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
const geocodeAddressHandler = async (prefix = '') => {
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

    // Mostra indicatore di caricamento
    document.getElementById(btnId).innerHTML = `
                    <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
                    Ricerca in corso...
                `;

    // Usa la funzione geocodeAddress
    const result = await geocodeAddress(indirizzo);
    document.getElementById(btnId).innerHTML = `<i class="bi bi-search me-2"></i>Verifica indirizzo`;

    if (!result.success) {
        document.getElementById(resultAreaId).style.display = 'block';
        document.getElementById(resultAreaId).innerHTML = `
                        <div class="alert alert-danger">
                            <i class="bi bi-exclamation-triangle me-2"></i>Errore: ${result.error}
                        </div>
                    `;
        return;
    }

    const coordinates = result.coordinates;

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
        // Corretto: usa lo stesso formato dell'ID nel template HTML
        const fieldId = `${prefix}orari${giorno}`;
        const orarioInput = document.getElementById(fieldId);

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
        orariApertura,
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

                document.getElementById('alertArea').innerHTML = `
                                <div class="alert alert-success" role="alert">
                                    <i class="bi bi-check-circle me-2"></i>Ristorante creato con successo!
                                </div>
                            `;

                // Ricarica i ristoranti
                await caricaRistoranti(sessionStorage.getItem('userId'));
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
            const response = await window.javaConnector.getRistoranteById({id: ristoranteId});

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

                // Popola gli orari - Corretto l'ID dei campi orari
                const giorni = ['lunedi', 'martedi', 'mercoledi', 'giovedi', 'venerdi', 'sabato', 'domenica'];
                giorni.forEach(giorno => {
                    const orario = ristorante.orariApertura[giorno] || 'Chiuso';
                    // Corretto: usa la stessa logica del template, senza capitalizzare
                    const fieldId = `edit-orari${giorno}`;
                    const field = document.getElementById(fieldId);
                    
                    if (field) {
                        field.value = orario;
                        // Popola gli slot UI per questo giorno
                        popolaSlotOrari(giorno, orario);
                    } else {
                        console.warn(`Campo orario non trovato: ${fieldId}`);
                    }
                });

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

// Funzione per popolare gli slot orari nel form di modifica
const popolaSlotOrari = (giorno, orarioStringa, prefix = '') => {
    // Seleziona il container degli slot per questo giorno
    const slotsContainer = document.getElementById(`edit-${giorno}-slots`);
    if (!slotsContainer) {
        console.warn(`Container slot non trovato per il giorno: ${giorno}`);
        return;
    }
    
    // Svuota il container
    slotsContainer.innerHTML = '';
    
    // Controlla se il ristorante è chiuso in questo giorno
    if (orarioStringa === 'Chiuso') {
        // Imposta il checkbox "Chiuso" come selezionato
        const chiusoCheckbox = document.getElementById(`edit-chiuso-${giorno}`);
        if (chiusoCheckbox) {
            chiusoCheckbox.checked = true;
        }
        
        // Aggiungi comunque uno slot di default (disabilitato)
        const defaultSlot = document.createElement('div');
        defaultSlot.className = 'time-slot disabled';
        defaultSlot.innerHTML = `
            <div class="slot-inputs">
                <div class="time-input">
                    <label>Apertura</label>
                    <input type="time" class="form-control time-start" value="12:00" disabled>
                </div>
                <div class="time-separator">-</div>
                <div class="time-input">
                    <label>Chiusura</label>
                    <input type="time" class="form-control time-end" value="15:00" disabled>
                </div>
            </div>
            <button type="button" class="btn-remove-slot" disabled>
                <i class="bi bi-trash"></i>
            </button>
        `;
        slotsContainer.appendChild(defaultSlot);
        return;
    }
    
    // Se il ristorante è aperto, analizza le fasce orarie
    const fasce = orarioStringa.split(',').map(fascia => fascia.trim());
    
    if (fasce.length === 0 || (fasce.length === 1 && fasce[0] === '')) {
        // Nessuna fascia oraria specificata, aggiungi uno slot di default
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
            <button type="button" class="btn-remove-slot">
                <i class="bi bi-trash"></i>
            </button>
        `;
        slotsContainer.appendChild(defaultSlot);
    } else {
        // Crea uno slot per ogni fascia oraria
        fasce.forEach(fascia => {
            const [inizio, fine] = fascia.split('-').map(orario => orario.trim());
            
            const slot = document.createElement('div');
            slot.className = 'time-slot';
            slot.innerHTML = `
                <div class="slot-inputs">
                    <div class="time-input">
                        <label>Apertura</label>
                        <input type="time" class="form-control time-start" value="${inizio || '12:00'}">
                    </div>
                    <div class="time-separator">-</div>
                    <div class="time-input">
                        <label>Chiusura</label>
                        <input type="time" class="form-control time-end" value="${fine || '15:00'}">
                    </div>
                </div>
                <button type="button" class="btn-remove-slot">
                    <i class="bi bi-trash"></i>
                </button>
            `;
            slotsContainer.appendChild(slot);
        });
    }
    
    // Aggiungi listener per gli input di orario
    slotsContainer.querySelectorAll('input[type="time"]').forEach(input => {
        input.addEventListener('change', () => updateHiddenTimeFields('edit-'));
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

                document.getElementById('alertArea').innerHTML = `
                                <div class="alert alert-success">
                                    <i class="bi bi-check-circle me-2"></i>Ristorante aggiornato con successo!
                                </div>
                            `;

                // Ricarica i ristoranti
                await caricaRistoranti(sessionStorage.getItem('userId'));
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
    try {
        const infoResponse = await window.javaConnector.getRistoranteById({id: ristoranteId});

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
        const response = await window.javaConnector.getRecensioniByRistorante({idRistorante: ristoranteId});

        const recensioniContainer = document.getElementById('recensioni-container');
        const noRecensioni = document.getElementById('no-recensioni');

        if (response.success && response.recensioni && response.recensioni.length > 0) {
            recensioniContainer.innerHTML = '';
            noRecensioni.classList.add('d-none');

            response.recensioni.forEach(recensione => {
                const recensioneElement = creaRecensioneElement(recensione);
                recensioniContainer.appendChild(recensioneElement);
            });
        } else {
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
        return Array(5).fill().map((_, i) =>
            i < voto
                ? '<i class="bi bi-star-fill"></i>'
                : '<i class="bi bi-star"></i>'
        ).join('');
    };

    // Gestione della data
    let dataFormattata = 'Data non disponibile';
    try {
        const dataString = recensione.date || recensione.dataCreazione;
        if (dataString) {
            const data = new Date(dataString);
            if (!isNaN(data.getTime())) {
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
        // Importante: usa lo stesso formato del campo nascosto nel template HTML
        const hiddenFieldId = `${prefix}orari${giorno}`;
        const chiusoCheckboxId = `${prefix}chiuso-${giorno}`;

        const slotsContainer = document.getElementById(slotsContainerId);
        const hiddenField = document.getElementById(hiddenFieldId);
        const chiusoCheckbox = document.getElementById(chiusoCheckboxId);

        if (!slotsContainer || !hiddenField) {
            console.warn(`Impossibile aggiornare orari per ${giorno}: container=${slotsContainer}, hidden=${hiddenField}`);
            return;
        }

        // Se il giorno è marcato come chiuso
        if (chiusoCheckbox && chiusoCheckbox.checked) {
            hiddenField.value = 'Chiuso';
            return;
        }

        // Ottieni tutte le fasce orarie
        const timeSlots = slotsContainer.querySelectorAll('.time-slot:not(.disabled)');
        if (timeSlots.length === 0) {
            hiddenField.value = 'Chiuso';
            return;
        }

        // Formato: 12:00-15:00, 19:00-23:00
        const fasce = Array.from(timeSlots).map(slot => {
            const startTime = slot.querySelector('.time-start')?.value;
            const endTime = slot.querySelector('.time-end')?.value;
            return startTime && endTime ? `${startTime}-${endTime}` : null;
        }).filter(Boolean);

        hiddenField.value = fasce.length > 0 ? fasce.join(', ') : 'Chiuso';
        console.log(`Aggiornato ${hiddenFieldId} con valore: ${hiddenField.value}`);
    });
};

// Funzione per inizializzare i template dei giorni della settimana
const initializeDayTemplates = () => {
    const giorni = [
        {key: 'lunedi', label: 'Lunedì'},
        {key: 'martedi', label: 'Martedì'},
        {key: 'mercoledi', label: 'Mercoledì'},
        {key: 'giovedi', label: 'Giovedì'},
        {key: 'venerdi', label: 'Venerdì'},
        {key: 'sabato', label: 'Sabato'},
        {key: 'domenica', label: 'Domenica'}
    ];

    // Per il form di nuovo ristorante
    const dayTemplate = document.getElementById('day-template');
    const daysContainer = document.getElementById('orari-giorni-container');

    if (dayTemplate && daysContainer) {
        giorni.forEach(giorno => {
            const dayClone = dayTemplate.content.cloneNode(true);
            const daySlots = dayClone.querySelector('.day-time-slots');

            // Sostituisci i placeholder
            daySlots.id = daySlots.id.replace('[DAY]', giorno.key);
            dayClone.querySelector('.day-title').textContent = giorno.label;

            // Sostituisci [DAY] in tutti gli altri elementi
            const elements = dayClone.querySelectorAll('[id*="[DAY]"], [data-day="[DAY]"]');
            elements.forEach(el => {
                if (el.id) el.id = el.id.replace('[DAY]', giorno.key);
                if (el.getAttribute('data-day')) el.setAttribute('data-day', giorno.key);
                if (el.getAttribute('for')) el.setAttribute('for', el.getAttribute('for').replace('[DAY]', giorno.key));
            });

            daysContainer.appendChild(dayClone);

            // Mostra solo il primo giorno, nascondi gli altri
            if (giorno.key !== 'lunedi') {
                document.getElementById(`slots-${giorno.key}`).classList.add('d-none');
            }
        });
    }

    // Per il form di modifica ristorante
    const editDayTemplate = document.getElementById('edit-day-template');
    const editDaysContainer = document.getElementById('edit-orari-giorni-container');

    if (editDayTemplate && editDaysContainer) {
        giorni.forEach(giorno => {
            const dayClone = editDayTemplate.content.cloneNode(true);
            const daySlots = dayClone.querySelector('.day-time-slots');

            // Sostituisci i placeholder
            daySlots.id = daySlots.id.replace('[DAY]', giorno.key);
            dayClone.querySelector('.day-title').textContent = giorno.label;

            // Sostituisci [DAY] in tutti gli altri elementi
            const elements = dayClone.querySelectorAll('[id*="[DAY]"], [data-day="[DAY]"]');
            elements.forEach(el => {
                if (el.id) el.id = el.id.replace('[DAY]', giorno.key);
                if (el.getAttribute('data-day')) el.setAttribute('data-day', giorno.key);
                if (el.getAttribute('for')) el.setAttribute('for', el.getAttribute('for').replace('[DAY]', giorno.key));
            });

            editDaysContainer.appendChild(dayClone);

            // Mostra solo il primo giorno, nascondi gli altri
            if (giorno.key !== 'lunedi') {
                document.getElementById(`edit-slots-${giorno.key}`).classList.add('d-none');
            }
        });
    }
};

// Funzione per gestire l'interazione con i controlli dell'orario
const setupTimeControls = (prefix = '') => {
    // PATCH: selettore base più robusto per distinguere i modali
    let baseSelector;
    if (prefix === 'edit-') {
        baseSelector = '#modificaRistoranteModal ';
    } else {
        baseSelector = '#nuovoRistoranteModal ';
    }

    // Gestione dei tab per i giorni della settimana
    const dayItems = document.querySelectorAll(`${baseSelector}.day-item`);
    const daySlots = document.querySelectorAll(`${baseSelector}.day-time-slots`);

    // Event delegation per i tab dei giorni
    const dayTabs = document.querySelector(`${baseSelector}.day-selector`);
    if (dayTabs) {
        dayTabs.addEventListener('click', (e) => {
            const dayItem = e.target.closest('.day-item');
            if (!dayItem) return;

            // Attiva il tab selezionato
            dayItems.forEach(i => i.classList.remove('active'));
            dayItem.classList.add('active');

            // Mostra solo il contenitore relativo al giorno selezionato
            const giorno = dayItem.getAttribute('data-day');

            // Debug per vedere quale giorno è stato selezionato
            // console.log(`Cambio al giorno: ${giorno}, prefisso: ${prefix}`);

            // Costruisci l'ID corretto per gli slot
            const slotId = `${prefix}slots-${giorno}`;

            // Itera su tutti i contenitori dei giorni e mostra solo quello selezionato
            daySlots.forEach(slot => {
                const shouldShow = slot.id === slotId;
                slot.classList.toggle('d-none', !shouldShow);
            });
        });
    }

    // Event delegation per i pulsanti di aggiunta fasce orarie
    document.querySelectorAll(`${baseSelector}.time-slots-container`).forEach(container => {
        container.addEventListener('click', (e) => {
            const addButton = e.target.closest('.btn-add-slot');
            if (addButton) {
                const giorno = addButton.getAttribute('data-day');
                console.log(`Tentativo di aggiungere slot per giorno: ${giorno}, prefisso: ${prefix}`);

                // Costruisci l'ID corretto per il container degli slot
                const slotsContainerId = `${prefix}${giorno}-slots`;
                const slotsContainer = document.getElementById(slotsContainerId);

                if (slotsContainer) {
                    console.log(`Container slot trovato: ${slotsContainerId}`);

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

                    // Aggiungi event listeners per gli input di orario
                    newSlot.querySelectorAll('input[type="time"]').forEach(input => {
                        input.addEventListener('change', () => updateHiddenTimeFields(prefix));
                    });

                    updateHiddenTimeFields(prefix);
                } else {
                    console.error(`Container slot non trovato: ${slotsContainerId}`);
                }
            }

            const removeButton = e.target.closest('.btn-remove-slot');
            if (removeButton) {
                const slot = removeButton.closest('.time-slot');
                if (slot) {
                    slot.remove();
                    updateHiddenTimeFields(prefix);
                }
            }
        });

        // Event delegation per i cambiamenti negli input time
        container.addEventListener('change', (e) => {
            if (e.target.type === 'time') {
                updateHiddenTimeFields(prefix);
            }
        });
    });

    // Gestione delle caselle di controllo "Chiuso"
    document.querySelectorAll(`${baseSelector}.day-closed-toggle`).forEach(toggle => {
        toggle.addEventListener('change', () => {
            const giornoId = toggle.id.replace(`${prefix}chiuso-`, '');
            const slotsContainer = document.getElementById(`${prefix}${giornoId}-slots`);

            if (slotsContainer) {
                const isDisabled = toggle.checked;
                const timeSlots = slotsContainer.querySelectorAll('.time-slot');

                timeSlots.forEach(slot => {
                    slot.querySelectorAll('input').forEach(input => {
                        input.disabled = isDisabled;
                    });
                    slot.classList.toggle('disabled', isDisabled);
                });

                updateHiddenTimeFields(prefix);
            }
        });
    });
};

// Funzione per pulire il form di nuovo ristorante
const resetNuovoRistoranteForm = () => {
    document.getElementById('nuovoRistoranteForm').reset();
    document.getElementById('latitudine').value = '';
    document.getElementById('longitudine').value = '';
    document.getElementById('risultatoRicerca').style.display = 'none';
    document.getElementById('modalAlertArea').innerHTML = '';

    // Reset orari
    const slotsContainers = document.querySelectorAll('[id$="-slots"]');
    slotsContainers.forEach(container => {
        if (container.id.startsWith('lunedi') || container.id.startsWith('edit-lunedi')) {
            // Mantieni solo un time slot nel giorno di lunedì
            while (container.children.length > 1) {
                container.removeChild(container.lastChild);
            }

            // Reset dell'unico time slot rimanente
            const slot = container.querySelector('.time-slot');
            if (slot) {
                slot.querySelector('.time-start').value = '12:00';
                slot.querySelector('.time-end').value = '15:00';
            }
        } else {
            // Svuota completamente gli altri giorni
            container.innerHTML = '';
        }
    });

    // Aggiorna i campi nascosti degli orari
    updateHiddenTimeFields();

    // Reset della visualizzazione dei giorni
    document.querySelectorAll('.day-item').forEach((item, index) => {
        item.classList.toggle('active', index === 0);
    });

    document.querySelectorAll('.day-time-slots').forEach((slots, index) => {
        slots.classList.toggle('d-none', index !== 0);
    });

    // Deseleziona tutti i checkbox "Chiuso"
    document.querySelectorAll('.day-closed-toggle').forEach(checkbox => {
        checkbox.checked = false;
    });
};

// Funzione per pulire il form di modifica ristorante
const resetModificaRistoranteForm = () => {
    document.getElementById('modificaRistoranteForm').reset();
    document.getElementById('ristoranteId').value = '';
    document.getElementById('edit-latitudine').value = '';
    document.getElementById('edit-longitudine').value = '';
    document.getElementById('edit-risultatoRicerca').style.display = 'none';
    document.getElementById('edit-modalAlertArea').innerHTML = '';

    // Reset orari (stessa logica del form nuovo)
    const editSlotsContainers = document.querySelectorAll('[id^="edit-"][id$="-slots"]');
    editSlotsContainers.forEach(container => {
        if (container.id.startsWith('edit-lunedi')) {
            // Mantieni solo un time slot nel giorno di lunedì
            while (container.children.length > 1) {
                container.removeChild(container.lastChild);
            }

            // Reset dell'unico time slot rimanente
            const slot = container.querySelector('.time-slot');
            if (slot) {
                slot.querySelector('.time-start').value = '12:00';
                slot.querySelector('.time-end').value = '15:00';
            }
        } else {
            // Svuota completamente gli altri giorni
            container.innerHTML = '';
        }
    });

    // Aggiorna i campi nascosti degli orari
    updateHiddenTimeFields('edit-');

    // Reset della visualizzazione dei giorni
    document.querySelectorAll('#modificaRistoranteModal .day-item').forEach((item, index) => {
        item.classList.toggle('active', index === 0);
    });

    document.querySelectorAll('#modificaRistoranteModal .day-time-slots').forEach((slots, index) => {
        slots.classList.toggle('d-none', index !== 0);
    });

    // Deseleziona tutti i checkbox "Chiuso"
    document.querySelectorAll('#modificaRistoranteModal .day-closed-toggle').forEach(checkbox => {
        checkbox.checked = false;
    });
};

// Funzione per abilitare fade e drag scroll sugli slider orari
function abilitaSliderOrari() {
    document.querySelectorAll('.orari-scroll-wrapper').forEach(wrapper => {
        const inner = wrapper.querySelector('.orari-scroll-inner');
        if (!inner) return;

        // Rimuovi eventuali frecce precedenti
        wrapper.querySelectorAll('.orari-slider-arrow').forEach(el => el.remove());

        // Aggiorna fade laterale in base all'overflow
        function aggiornaFade() {
            const hasOverflow = inner.scrollWidth > wrapper.clientWidth + 2;
            wrapper.classList.toggle('has-overflow', hasOverflow);
        }

        aggiornaFade();
        window.addEventListener('resize', aggiornaFade);

        // Drag scroll con mouse
        let isDown = false;
        let startX, scrollLeft;

        wrapper.addEventListener('mousedown', (e) => {
            isDown = true;
            wrapper.classList.add('dragging');
            startX = e.pageX - wrapper.offsetLeft;
            scrollLeft = wrapper.scrollLeft;
            document.body.style.userSelect = 'none';
        });
        wrapper.addEventListener('mouseleave', () => {
            isDown = false;
            wrapper.classList.remove('dragging');
            document.body.style.userSelect = '';
        });
        wrapper.addEventListener('mouseup', () => {
            isDown = false;
            wrapper.classList.remove('dragging');
            document.body.style.userSelect = '';
        });
        wrapper.addEventListener('mousemove', (e) => {
            if (!isDown) return;
            e.preventDefault();
            const x = e.pageX - wrapper.offsetLeft;
            const walk = (x - startX) * 1.2;
            wrapper.scrollLeft = scrollLeft - walk;
        });

        // Drag scroll touch
        let isTouching = false;
        let touchStartX = 0;
        let touchScrollLeft = 0;

        wrapper.addEventListener('touchstart', (e) => {
            if (e.touches.length !== 1) return;
            isTouching = true;
            wrapper.classList.add('dragging');
            touchStartX = e.touches[0].pageX;
            touchScrollLeft = wrapper.scrollLeft;
        }, {passive: true});
        wrapper.addEventListener('touchend', () => {
            isTouching = false;
            wrapper.classList.remove('dragging');
        });
        wrapper.addEventListener('touchmove', (e) => {
            if (!isTouching || e.touches.length !== 1) return;
            const x = e.touches[0].pageX;
            const walk = (x - touchStartX) * 1.2;
            wrapper.scrollLeft = touchScrollLeft - walk;
        }, {passive: false});

        // Aggiorna fade su scroll
        wrapper.addEventListener('scroll', aggiornaFade);

        // Aggiorna fade anche se cambia contenuto
        new ResizeObserver(aggiornaFade).observe(inner);
    });
}

// Inizializzazione all'avvio della pagina
document.addEventListener('DOMContentLoaded', async () => {
    console.log("Inizializzazione della pagina...");

    // Verifica se l'utente è loggato
    const isLoggedIn = sessionStorage.getItem('isLoggedIn');
    const userId = sessionStorage.getItem('userId');
    const username = sessionStorage.getItem('username');
    const ruolo = sessionStorage.getItem('ruolo');

    console.log("Stato login:", {isLoggedIn, userId, username, ruolo});

    // Mostra username nella navbar
    const usernameElement = document.getElementById('username-display');
    if (usernameElement) usernameElement.textContent = username || '';

    // Reindirizza alla pagina di login se l'utente non è loggato o non è un ristoratore
    if (!isLoggedIn || ruolo !== 'RISTORATORE') {
        console.log("Utente non autorizzato, reindirizzamento al login...");
        window.location.href = "index.html";
        return;
    }

    // Previeni il refresh della pagina dai form
    document.querySelectorAll('form').forEach(form => {
        form.addEventListener('submit', e => e.preventDefault());
    });

    // Imposta type="button" ai bottoni nei form
    document.querySelectorAll('form button:not([type])').forEach(button => {
        button.setAttribute('type', 'button');
    });

    // Modifica i link con href="#"
    document.querySelectorAll('a[href="#"]').forEach(link => {
        link.setAttribute('href', 'javascript:void(0)');
        link.addEventListener('click', e => e.preventDefault());
    });

    // Inizializza i template dei giorni della settimana
    initializeDayTemplates();

    // Inizializza i controlli per la gestione degli orari
    setupTimeControls();
    setupTimeControls('edit-');

    // Aggiorna i campi nascosti per gli orari
    updateHiddenTimeFields();
    updateHiddenTimeFields('edit-');

    // Aggiungi event listener per resettare i form quando i modal vengono chiusi
    document.getElementById('nuovoRistoranteModal').addEventListener('hidden.bs.modal', resetNuovoRistoranteForm);
    document.getElementById('modificaRistoranteModal').addEventListener('hidden.bs.modal', resetModificaRistoranteForm);

    // Attendi che il bridge sia disponibile prima di chiamare la funzione
    console.log("In attesa del bridge...");
    await waitForBridge(async () => {
        console.log("Bridge pronto, caricamento ristoranti...");
        await caricaRistoranti(userId);
    });

    // Dopo ogni render delle card, abilita lo slider orari
    const oldCaricaRistoranti = caricaRistoranti;
    caricaRistoranti = async function(...args) {
        await oldCaricaRistoranti.apply(this, args);
        setTimeout(abilitaSliderOrari, 0);
    };

    // Primo caricamento
    await waitForBridge(async () => {
        // ...existing code...
        await caricaRistoranti(userId);
    });

    // Event delegation per gestire gli eventi sui bottoni principali
    document.addEventListener('click', async (e) => {
        // Gestione logout
        if (e.target.closest('#logout-link')) {
            e.preventDefault();
            sessionStorage.clear();
            window.location.href = "index.html";
        }

        // Ricerca indirizzi
        if (e.target.closest('#cercaIndirizzo')) {
            await geocodeAddressHandler();
        }

        if (e.target.closest('#edit-cercaIndirizzo')) {
            await geocodeAddressHandler('edit-');
        }

        // Salvataggio ristoranti
        if (e.target.closest('#salvaRistorante')) {
            await salvaRistoranteHandler(e);
        }

        if (e.target.closest('#salvaModificheRistorante')) {
            await salvaModificheRistoranteHandler(e);
        }
    });

    console.log("Inizializzazione completata!");
});
