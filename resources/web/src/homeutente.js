// Funzione di attesa per verificare che il bridge sia pronto
const waitForBridge = async (callback, maxAttempts = 10) => {
    let attempts = 0;

    const checkBridge = async () => {
        attempts++;
        if (window.javaConnector && typeof window.javaConnector.getAllRistoranti === 'function') {
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

// Funzione per verificare l'indirizzo nei filtri
const verificaIndirizzo = () => {
    const indirizzo = document.getElementById('filtro-indirizzo').value;
    if (!indirizzo) {
        document.getElementById('alertArea').innerHTML = `
            <div class="alert alert-warning" role="alert">
                <i class="bi bi-exclamation-triangle me-2"></i>Inserisci un indirizzo da verificare
            </div>
        `;
        return;
    }

    // Mostra indicatore di caricamento
    document.getElementById('btn-cerca-indirizzo').innerHTML = `
        <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
    `;

    geocodeAddress(indirizzo, (error, coordinates) => {
        // Ripristina il pulsante
        document.getElementById('btn-cerca-indirizzo').innerHTML = `
            <i class="bi bi-geo-alt me-1"></i>Verifica
        `;

        if (error) {
            document.getElementById('alertArea').innerHTML = `
                <div class="alert alert-danger" role="alert">
                    <i class="bi bi-exclamation-triangle me-2"></i>Errore: ${error}
                </div>
            `;
            return;
        }

        // Mostra il risultato
        document.getElementById('risultato-indirizzo').classList.remove('d-none');
        document.getElementById('indirizzo-trovato').textContent = `Indirizzo verificato (${coordinates.lat.toFixed(6)}, ${coordinates.lng.toFixed(6)})`;
        
        // Salva le coordinate nei campi nascosti
        document.getElementById('filtro-latitudine').value = coordinates.lat;
        document.getElementById('filtro-longitudine').value = coordinates.lng;
        
        // Abilita il campo distanza
        document.getElementById('filtro-distanza').disabled = false;
    });
};

// Funzione per caricare tutti i ristoranti
const caricaRistoranti = async () => {
    // Mostra l'indicatore di caricamento
    document.getElementById('loading-indicator').classList.remove('d-none');
    document.getElementById('no-ristoranti').classList.add('d-none');

    try {
        // Chiama il backend per ottenere i ristoranti
        const response = await window.javaConnector.getAllRistoranti({});

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
        document.getElementById('alertArea').innerHTML = `
            <div class="alert alert-danger" role="alert">
                Errore durante il caricamento dei ristoranti: ${error}
            </div>
        `;
    }
};

// Funzione per filtrare i ristoranti
const filtriRicerca = async (event) => {
    if (event) event.preventDefault();
    
    // Mostra l'indicatore di caricamento
    document.getElementById('loading-indicator').classList.remove('d-none');
    document.getElementById('no-ristoranti').classList.add('d-none');
    
    // Raccogli i valori dei filtri
    const tipoCucina = document.getElementById('filtro-tipo-cucina').value;
    const fasciaPrezzo = document.getElementById('filtro-fascia-prezzo').value;
    const consegnaDomicilio = document.getElementById('filtro-consegna-domicilio').checked;
    const apertoOra = document.getElementById('filtro-aperto-ora').checked;
    const latitudine = document.getElementById('filtro-latitudine').value;
    const longitudine = document.getElementById('filtro-longitudine').value;
    const distanzaMassima = document.getElementById('filtro-distanza').value;
    
    try {
        // Costruisci l'oggetto filtri
        const filtri = {};
        
        if (tipoCucina) filtri.tipoCucina = tipoCucina;
        if (fasciaPrezzo) filtri.fasciaPrezzo = parseInt(fasciaPrezzo);
        filtri.consegnaDomicilio = consegnaDomicilio;
        filtri.apertoOra = apertoOra;
        
        // Aggiungi le coordinate solo se sono state specificate
        if (latitudine && longitudine) {
            filtri.latitudine = parseFloat(latitudine);
            filtri.longitudine = parseFloat(longitudine);
            filtri.distanzaMassima = parseInt(distanzaMassima);
        }
        
        // Chiama il backend per filtrare i ristoranti
        const response = await window.javaConnector.filtriRicerca(filtri);
        
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
        document.getElementById('alertArea').innerHTML = `
            <div class="alert alert-danger" role="alert">
                Errore durante la ricerca dei ristoranti: ${error}
            </div>
        `;
    }
};

// Funzione per creare una card di ristorante
const creaRistoranteCard = (ristorante) => {
    // Crea un elemento div per la colonna
    const colDiv = document.createElement('div');
    colDiv.className = 'col-md-6 col-lg-4';
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
                <span class="badge bg-gradient rounded-pill">${ristorante.tipoCucina}</span>
            </div>
            <div class="card-body position-relative">
                <div class="restaurant-info">
                    <div class="price-tag mb-3">
                        ${renderPrezzi(ristorante.fasciaPrezzo)}
                    </div>
                    
                    <div class="restaurant-details">
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
                    <button class="btn-action view-recensioni" data-id="${ristorante.id}" data-nome="${ristorante.nome}" title="Recensioni">
                        <i class="bi bi-star"></i>
                    </button>
                    <button class="btn-action favorite-toggle" title="Aggiungi ai preferiti">
                        <i class="bi bi-heart"></i>
                    </button>
                    <button class="btn-action details-btn" data-id="${ristorante.id}" title="Dettagli">
                        <i class="bi bi-info-circle"></i>
                    </button>
                </div>
            </div>
        </div>
    `;

    // Aggiungi event listener per i pulsanti nella card
    setTimeout(() => {
        const recensioniBtn = colDiv.querySelector('.view-recensioni');
        const toggleHoursBtn = colDiv.querySelector('.toggle-hours');
        const favoriteBtn = colDiv.querySelector('.favorite-toggle');
        const detailsBtn = colDiv.querySelector('.details-btn');

        if (recensioniBtn) {
            recensioniBtn.addEventListener('click', (e) => {
                e.preventDefault();
                const ristoranteId = recensioniBtn.getAttribute('data-id');
                const ristoranteNome = recensioniBtn.getAttribute('data-nome');
                apriModalRecensioni(ristoranteId, ristoranteNome);
            });
        }

        if (toggleHoursBtn) {
            toggleHoursBtn.addEventListener('click', (e) => {
                e.preventDefault();
                // Corretto per aprire solo questa specifica card
                const hoursDetails = colDiv.querySelector('.hours-details');
                const isVisible = hoursDetails.style.display !== 'none';

                hoursDetails.style.display = isVisible ? 'none' : 'block';

                // Cambia l'icona della freccia
                const arrow = toggleHoursBtn.querySelector('.bi-chevron-down, .bi-chevron-up');
                if (arrow) {
                    arrow.classList.toggle('bi-chevron-down');
                    arrow.classList.toggle('bi-chevron-up');
                }
            });
        }

        if (favoriteBtn) {
            favoriteBtn.addEventListener('click', (e) => {
                e.preventDefault();
                const icon = favoriteBtn.querySelector('i');
                icon.classList.toggle('bi-heart');
                icon.classList.toggle('bi-heart-fill');
                favoriteBtn.classList.toggle('favorite-active');
            });
        }

        if (detailsBtn) {
            detailsBtn.addEventListener('click', (e) => {
                e.preventDefault();
                const ristoranteId = detailsBtn.getAttribute('data-id');
                // Implementazione futura per visualizzare dettagli
                console.log(`Visualizza dettagli per ristorante ID: ${ristoranteId}`);
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

// Funzione per aprire il modal delle recensioni
const apriModalRecensioni = async (ristoranteId, ristoranteNome) => {
    // Aggiorna il titolo del modal
    document.getElementById('recensioni-ristorante-nome').textContent = ristoranteNome;
    document.getElementById('recensione-ristorante-id').value = ristoranteId;
    
    // Mostra indicatore di caricamento
    document.getElementById('recensioni-container').innerHTML = `
        <div class="text-center py-3">
            <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Caricamento recensioni...</span>
            </div>
        </div>
    `;
    
    // Verifica stato login per decidere se mostrare il form di recensione
    const isLoggedIn = sessionStorage.getItem('isLoggedIn') === 'true';
    if (isLoggedIn) {
        document.getElementById('logged-in-recensione-controls').classList.remove('d-none');
        document.getElementById('logged-out-recensione-alert').classList.add('d-none');
    } else {
        document.getElementById('logged-in-recensione-controls').classList.add('d-none');
        document.getElementById('logged-out-recensione-alert').classList.remove('d-none');
    }
    
    // Apri il modal
    const recensioniModal = new bootstrap.Modal(document.getElementById('recensioniModal'));
    recensioniModal.show();
    
    // Carica le recensioni
    await caricaRecensioni(ristoranteId);
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
    const userId = recensione.key_user || recensione.idUtente;
    const nomeUtente = recensione.nomeUtente || 'Utente';

    // Determina se l'utente corrente è l'autore della recensione
    const currentUserId = sessionStorage.getItem('userId');
    const isAuthor = currentUserId === userId;
    
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
        ${isAuthor ? `
        <div class="review-actions">
            <button class="btn-slim edit-recensione" data-id="${recensione.id}">
                <i class="bi bi-pencil me-1"></i>Modifica
            </button>
            <button class="btn-slim btn-slim-danger delete-recensione" data-id="${recensione.id}">
                <i class="bi bi-trash me-1"></i>Elimina
            </button>
        </div>` : ''}
    `;
    
    // Aggiungi event listener per modifica ed eliminazione
    if (isAuthor) {
        setTimeout(() => {
            const editBtn = div.querySelector('.edit-recensione');
            const deleteBtn = div.querySelector('.delete-recensione');
            
            if (editBtn) {
                editBtn.addEventListener('click', () => {
                    // Implementare la modifica della recensione
                    console.log('Modifica recensione', recensione.id);
                });
            }
            
            if (deleteBtn) {
                deleteBtn.addEventListener('click', () => {
                    if (confirm('Sei sicuro di voler eliminare questa recensione?')) {
                        eliminaRecensione(recensione.id);
                    }
                });
            }
        }, 0);
    }
    
    return div;
};

// Funzione per aggiungere una nuova recensione
const inviaRecensione = async (event) => {
    event.preventDefault();
    
    const ristoranteId = document.getElementById('recensione-ristorante-id').value;
    const titolo = document.getElementById('recensione-titolo').value;
    const voto = document.getElementById('recensione-voto').value;
    const testo = document.getElementById('recensione-testo').value;
    const userId = sessionStorage.getItem('userId');
    
    if (!ristoranteId || !titolo || !voto || !testo || !userId) {
        alert('Compila tutti i campi per inviare la recensione');
        return;
    }
    
    try {
        const response = await window.javaConnector.creaRecensione({
            idRistorante: ristoranteId,
            idUtente: userId,
            titolo: titolo,
            testo: testo,
            voto: parseInt(voto)
        });
        
        if (response.success) {
            // Resetta il form
            document.getElementById('form-nuova-recensione').reset();
            
            // Mostra messaggio di successo
            alert('Recensione pubblicata con successo!');
            
            // Ricarica le recensioni
            await caricaRecensioni(ristoranteId);
        } else {
            alert('Errore durante il salvataggio della recensione: ' + (response.error || 'Errore sconosciuto'));
        }
    } catch (error) {
        alert('Errore di sistema: ' + error);
    }
};

// Funzione per eliminare una recensione
const eliminaRecensione = async (recensioneId) => {
    try {
        const response = await window.javaConnector.eliminaRecensione({
            recensioneId: recensioneId
        });
        
        if (response.success) {
            // Rimuovi la recensione dall'UI
            document.getElementById(`recensione-${recensioneId}`).remove();
            
            // Mostra messaggio di successo
            alert('Recensione eliminata con successo!');
            
            // Controlla se ci sono ancora recensioni
            const recensioniContainer = document.getElementById('recensioni-container');
            if (recensioniContainer.childElementCount === 0) {
                document.getElementById('no-recensioni').classList.remove('d-none');
            }
        } else {
            alert('Errore durante l\'eliminazione della recensione: ' + (response.error || 'Errore sconosciuto'));
        }
    } catch (error) {
        alert('Errore di sistema: ' + error);
    }
};

// Gestione dello stato di login
const gestisciStatoLogin = () => {
    const isLoggedIn = sessionStorage.getItem('isLoggedIn') === 'true';
    const username = sessionStorage.getItem('username');
    
    if (isLoggedIn && username) {
        // Mostra il menu utente loggato
        document.getElementById('logged-in-menu').classList.remove('d-none');
        document.getElementById('logged-out-menu').classList.add('d-none');
        document.getElementById('logged-out-menu-register').classList.add('d-none');
        document.getElementById('username-display').textContent = username;
        
        // Mostra pulsanti che richiedono login
        document.querySelectorAll('.richiede-login').forEach(el => {
            el.classList.remove('d-none');
        });
    } else {
        // Mostra il menu per utenti non loggati
        document.getElementById('logged-in-menu').classList.add('d-none');
        document.getElementById('logged-out-menu').classList.remove('d-none');
        document.getElementById('logged-out-menu-register').classList.remove('d-none');
        
        // Nascondi pulsanti che richiedono login
        document.querySelectorAll('.richiede-login').forEach(el => {
            el.classList.add('d-none');
        });
    }
};

// Inizializzazione all'avvio della pagina
document.addEventListener('DOMContentLoaded', async () => {
    // Gestisci lo stato di login
    gestisciStatoLogin();
    
    // Attendi che il bridge sia disponibile prima di chiamare la funzione
    await waitForBridge(async () => {
        await caricaRistoranti();
    });
    
    // Event listener per il pulsante di verifica indirizzo
    document.getElementById('btn-cerca-indirizzo').addEventListener('click', verificaIndirizzo);
    
    // Event listener per il form dei filtri
    document.getElementById('filtri-form').addEventListener('submit', filtriRicerca);
    
    // Event listener per il form di recensione
    document.getElementById('form-nuova-recensione').addEventListener('submit', inviaRecensione);
    
    // Gestione il logout
    const logoutLink = document.getElementById('logout-link');
    if (logoutLink) {
        logoutLink.addEventListener('click', (e) => {
            e.preventDefault();
            sessionStorage.clear();
            window.location.reload();
        });
    }
    
    // Gestione apertura e chiusura del modal delle recensioni
    document.getElementById('recensioniModal').addEventListener('hidden.bs.modal', function () {
        // Rimuove eventuali backdrop rimasti e altri elementi problematici
        document.querySelectorAll('.modal-backdrop').forEach(el => el.remove());
        document.body.classList.remove('modal-open');
        document.body.style.overflow = '';
        document.body.style.paddingRight = '';
    });
});