// Funzione di attesa per verificare che il bridge sia pronto
function waitForBridge(callback, maxAttempts = 10) {
    let attempts = 0;

    function checkBridge() {
        attempts++;
        if (window.javaConnector && typeof window.javaConnector.getAllRistoranti === 'function') {
            console.log("Bridge trovato, procedo con le operazioni");
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

// Carica tutti i ristoranti disponibili
function caricaRistoranti() {
    // Mostra l'indicatore di caricamento
    document.getElementById('loading-indicator').classList.remove('d-none');
    document.getElementById('no-ristoranti').classList.add('d-none');

    // Chiama il backend per ottenere i ristoranti
    window.javaConnector.getAllRistoranti({})
        .then(response => {
            // Nascondi l'indicatore di caricamento
            document.getElementById('loading-indicator').classList.add('d-none');

            // Ottieni il container dove inserire i ristoranti
            const ristorantiContainer = document.getElementById('ristoranti-container');

            if (response.success && response.ristoranti && response.ristoranti.length > 0) {
                // Salva i ristoranti per i filtri futuri
                window.allRistoranti = response.ristoranti;

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

// Cerca ristoranti in base al testo inserito
function cercaRistoranti(query) {
    if (!window.allRistoranti) return;

    const query_lower = query.toLowerCase();
    let risultati = window.allRistoranti;

    if (query_lower.trim() !== '') {
        risultati = window.allRistoranti.filter(ristorante => {
            return ristorante.nome.toLowerCase().includes(query_lower) ||
                ristorante.tipoCucina.toLowerCase().includes(query_lower);
        });
    }

    visualizzaRisultatiRicerca(risultati);
}

// Filtra ristoranti in base ai criteri selezionati
function filtraRistoranti() {
    if (!window.allRistoranti) return;

    const tipoCucina = document.getElementById('filtroCucina').value.toLowerCase();
    const fasciaPrezzo = parseInt(document.getElementById('filtroPrezzo').value) || 0;
    const consegnaDomicilio = document.getElementById('filtroConsegna').checked;
    const query = document.getElementById('searchInput').value.toLowerCase();

    let risultati = window.allRistoranti;

    // Filtra per la query di ricerca
    if (query.trim() !== '') {
        risultati = risultati.filter(ristorante => {
            return ristorante.nome.toLowerCase().includes(query) ||
                ristorante.tipoCucina.toLowerCase().includes(query);
        });
    }

    // Filtra per tipo cucina
    if (tipoCucina !== 'tutti') {
        risultati = risultati.filter(ristorante => {
            return ristorante.tipoCucina.toLowerCase().includes(tipoCucina);
        });
    }

    // Filtra per fascia prezzo
    if (fasciaPrezzo > 0) {
        risultati = risultati.filter(ristorante => {
            return ristorante.fasciaPrezzo === fasciaPrezzo;
        });
    }

    // Filtra per consegna a domicilio
    if (consegnaDomicilio) {
        risultati = risultati.filter(ristorante => {
            return ristorante.consegnaDomicilio === true;
        });
    }

    visualizzaRisultatiRicerca(risultati);
}

// Visualizza i risultati della ricerca
function visualizzaRisultatiRicerca(ristoranti) {
    const ristorantiContainer = document.getElementById('ristoranti-container');

    // Svuota il container
    ristorantiContainer.innerHTML = '';

    if (ristoranti.length === 0) {
        document.getElementById('no-risultati').classList.remove('d-none');
        return;
    }

    document.getElementById('no-risultati').classList.add('d-none');

    // Aggiungi ogni ristorante al container
    ristoranti.forEach(ristorante => {
        const card = creaRistoranteCard(ristorante);
        ristorantiContainer.appendChild(card);
    });
}

// Crea una card per un ristorante
function creaRistoranteCard(ristorante) {
    const colDiv = document.createElement('div');
    colDiv.className = 'col-md-6 col-lg-4 mb-4';
    colDiv.id = `ristorante-${ristorante.id}`;

    // Crea la card con i dettagli del ristorante
    colDiv.innerHTML = `
        <div class="card h-100 bg-dark-subtle border-0 shadow-sm">
            <div class="card-header bg-dark-subtle border-0 d-flex justify-content-between align-items-center">
                <h5 class="card-title mb-0">${ristorante.nome}</h5>
                <span class="badge bg-primary">${'€'.repeat(ristorante.fasciaPrezzo)}</span>
            </div>
            <div class="card-body">
                <p class="card-text"><i class="bi bi-tag me-2"></i>${ristorante.tipoCucina}</p>
                <p class="card-text"><i class="bi bi-telephone me-2"></i>${ristorante.numeroTelefono}</p>
                <p class="card-text">
                    <i class="bi bi-${ristorante.consegnaDomicilio ? 'bicycle' : 'x-circle'} me-2"></i>
                    ${ristorante.consegnaDomicilio ? 'Consegna a domicilio disponibile' : 'Nessuna consegna a domicilio'}
                </p>
            </div>
            <div class="card-footer bg-transparent border-0 d-flex justify-content-between">
                <button class="btn btn-sm btn-outline-primary view-details" data-id="${ristorante.id}">
                    <i class="bi bi-info-circle me-2"></i>Dettagli
                </button>
                <button class="btn btn-sm btn-outline-warning view-recensioni" data-id="${ristorante.id}">
                    <i class="bi bi-star me-2"></i>Recensioni
                </button>
            </div>
        </div>
    `;

    // Aggiungi event listener per i pulsanti nella card
    setTimeout(() => {
        const detailsBtn = colDiv.querySelector('.view-details');
        const recensioniBtn = colDiv.querySelector('.view-recensioni');

        if (detailsBtn) detailsBtn.addEventListener('click', function () {
            const ristoranteId = this.getAttribute('data-id');
            visualizzaDettagliRistorante(ristoranteId);
        });

        if (recensioniBtn) recensioniBtn.addEventListener('click', function () {
            const ristoranteId = this.getAttribute('data-id');
            visualizzaRecensioni(ristoranteId);
        });
    }, 0);

    return colDiv;
}

// Funzione per visualizzare i dettagli di un ristorante
function visualizzaDettagliRistorante(ristoranteId) {
    // Chiama il backend per ottenere i dettagli del ristorante
    window.javaConnector.getRistoranteById({id: ristoranteId})
        .then(response => {
            if (response.success) {
                const ristorante = response.ristorante;

                // Crea il contenuto del modal
                document.getElementById('dettagliRistoranteTitle').textContent = ristorante.nome;

                let orariHtml = '';
                const giorni = ['lunedi', 'martedi', 'mercoledi', 'giovedi', 'venerdi', 'sabato', 'domenica'];
                const giorniIta = ['Lunedì', 'Martedì', 'Mercoledì', 'Giovedì', 'Venerdì', 'Sabato', 'Domenica'];

                for (let i = 0; i < giorni.length; i++) {
                    const orario = ristorante.orariApertura[giorni[i]] || 'Chiuso';
                    orariHtml += `<p><strong>${giorniIta[i]}:</strong> ${orario}</p>`;
                }

                document.getElementById('dettagliRistoranteBody').innerHTML = `
                    <div class="mb-4">
                        <h5><i class="bi bi-info-circle me-2"></i>Informazioni</h5>
                        <p><strong>Tipo cucina:</strong> ${ristorante.tipoCucina}</p>
                        <p><strong>Fascia di prezzo:</strong> ${'€'.repeat(ristorante.fasciaPrezzo)}</p>
                        <p><strong>Telefono:</strong> ${ristorante.numeroTelefono}</p>
                        <p>
                            <i class="bi bi-${ristorante.consegnaDomicilio ? 'bicycle' : 'x-circle'} me-2"></i>
                            ${ristorante.consegnaDomicilio ? 'Consegna a domicilio disponibile' : 'Nessuna consegna a domicilio'}
                        </p>
                    </div>
                    
                    <div class="mb-4">
                        <h5><i class="bi bi-clock me-2"></i>Orari di apertura</h5>
                        ${orariHtml}
                    </div>
                `;

                // Mostra il modal
                const dettagliModal = new bootstrap.Modal(document.getElementById('dettagliRistoranteModal'));
                dettagliModal.show();
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
                    Errore di sistema: ${error}
                </div>
            `;
        });
}

// Funzione per visualizzare le recensioni di un ristorante
function visualizzaRecensioni(ristoranteId) {
    // Mostra l'indicatore di caricamento
    document.getElementById('recensioniBody').innerHTML = `
        <div class="text-center py-4">
            <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Caricamento...</span>
            </div>
            <p class="mt-2">Caricamento recensioni...</p>
        </div>
    `;

    // Mostra il modal
    const recensioniModal = new bootstrap.Modal(document.getElementById('recensioniModal'));
    recensioniModal.show();

    // Carica le recensioni dal backend
    window.javaConnector.getRecensioniByRistorante({idRistorante: ristoranteId})
        .then(response => {
            if (response.success) {
                const recensioni = response.recensioni;
                const recensioniContainer = document.getElementById('recensioniBody');

                // Aggiorna il titolo del modal con il nome del ristorante
                window.javaConnector.getRistoranteById({id: ristoranteId})
                    .then(ristoranteResponse => {
                        if (ristoranteResponse.success) {
                            document.getElementById('recensioniTitle').textContent =
                                `Recensioni per ${ristoranteResponse.ristorante.nome}`;
                        }
                    });

                // Se non ci sono recensioni
                if (!recensioni || recensioni.length === 0) {
                    recensioniContainer.innerHTML = `
                        <div class="text-center py-4">
                            <i class="bi bi-chat-square-text display-4"></i>
                            <p class="mt-3">Nessuna recensione disponibile per questo ristorante.</p>
                            <button class="btn btn-outline-primary mt-3" id="scrivi-recensione-btn" data-id="${ristoranteId}">
                                <i class="bi bi-pencil me-2"></i>Scrivi la prima recensione
                            </button>
                        </div>
                    `;

                    // Aggiungi event listener per il pulsante di scrittura recensione
                    setTimeout(() => {
                        const scriviRecensioneBtn = document.getElementById('scrivi-recensione-btn');
                        if (scriviRecensioneBtn) {
                            scriviRecensioneBtn.addEventListener('click', function () {
                                const ristoranteId = this.getAttribute('data-id');
                                apriFormRecensione(ristoranteId);
                            });
                        }
                    }, 0);

                    return;
                }

                // Visualizza le recensioni
                let recensioniHtml = `
                    <div class="mb-4">
                        <button class="btn btn-primary" id="scrivi-recensione-btn" data-id="${ristoranteId}">
                            <i class="bi bi-pencil me-2"></i>Scrivi recensione
                        </button>
                    </div>
                `;

                // Calcola la media delle valutazioni
                let sommaVoti = 0;
                recensioni.forEach(recensione => {
                    sommaVoti += recensione.voto;
                });
                const mediaVoti = (sommaVoti / recensioni.length).toFixed(1);

                recensioniHtml += `
                    <div class="mb-4 text-center">
                        <h4>Valutazione media: ${mediaVoti}/5</h4>
                        <div class="rating">
                            ${generaStelle(mediaVoti)}
                        </div>
                    </div>
                `;

                recensioni.forEach(recensione => {
                    recensioniHtml += `
                        <div class="card mb-3 bg-dark-subtle border-0">
                            <div class="card-body">
                                <div class="d-flex justify-content-between align-items-center mb-2">
                                    <h5 class="card-title">${recensione.titolo}</h5>
                                    <div class="rating">
                                        ${generaStelle(recensione.voto)}
                                    </div>
                                </div>
                                <p class="card-text">${recensione.testo}</p>
                                <div class="text-muted small">
                                    <i class="bi bi-person me-1"></i> Recensito da: ${recensione.nomeUtente || 'Utente anonimo'}
                                </div>
                            </div>
                        </div>
                    `;
                });

                recensioniContainer.innerHTML = recensioniHtml;

                // Aggiungi event listener per il pulsante di scrittura recensione
                setTimeout(() => {
                    const scriviRecensioneBtn = document.getElementById('scrivi-recensione-btn');
                    if (scriviRecensioneBtn) {
                        scriviRecensioneBtn.addEventListener('click', function () {
                            const ristoranteId = this.getAttribute('data-id');
                            apriFormRecensione(ristoranteId);
                        });
                    }
                }, 0);
            } else {
                document.getElementById('recensioniBody').innerHTML = `
                    <div class="alert alert-danger" role="alert">
                        Errore: ${response.error}
                    </div>
                `;
            }
        })
        .catch(error => {
            document.getElementById('recensioniBody').innerHTML = `
                <div class="alert alert-danger" role="alert">
                    Errore di sistema: ${error}
                </div>
            `;
        });
}

// Genera HTML per le stelle di valutazione
function generaStelle(voto) {
    const piene = Math.floor(voto);
    const mezza = voto % 1 >= 0.5;
    const vuote = 5 - piene - (mezza ? 1 : 0);

    let stelleHtml = '';
    for (let i = 0; i < piene; i++) {
        stelleHtml += '<i class="bi bi-star-fill text-warning"></i>';
    }
    if (mezza) {
        stelleHtml += '<i class="bi bi-star-half text-warning"></i>';
    }
    for (let i = 0; i < vuote; i++) {
        stelleHtml += '<i class="bi bi-star text-warning"></i>';
    }

    return stelleHtml;
}

// Apri il form per scrivere una recensione
function apriFormRecensione(ristoranteId) {
    // Verifica se l'utente è loggato
    const isLoggedIn = sessionStorage.getItem('isLoggedIn') === 'true';
    const userId = sessionStorage.getItem('userId');

    if (!isLoggedIn) {
        // Se l'utente non è loggato, mostra un messaggio
        document.getElementById('recensioniBody').innerHTML = `
            <div class="alert alert-warning" role="alert">
                <i class="bi bi-exclamation-triangle me-2"></i>
                Devi essere registrato per scrivere una recensione.
                <div class="mt-3">
                    <a href="index.html" class="btn btn-primary me-2">
                        <i class="bi bi-box-arrow-in-right me-2"></i>Accedi
                    </a>
                    <a href="register.html" class="btn btn-outline-primary">
                        <i class="bi bi-person-plus me-2"></i>Registrati
                    </a>
                </div>
            </div>
        `;
        return;
    }

    // Se l'utente è loggato, mostra il form
    document.getElementById('recensioniBody').innerHTML = `
        <h5 class="mb-3">Scrivi la tua recensione</h5>
        <form id="recensioneForm">
            <input type="hidden" id="ristoranteId" value="${ristoranteId}">
            <div class="mb-3">
                <label for="titoloRecensione" class="form-label">Titolo</label>
                <input type="text" class="form-control bg-dark text-white border-secondary" 
                       id="titoloRecensione" required maxlength="100">
            </div>
            <div class="mb-3">
                <label class="form-label">Valutazione</label>
                <div class="rating-input mb-2">
                    <div class="d-flex">
                        <span class="star-rating" data-value="1"><i class="bi bi-star"></i></span>
                        <span class="star-rating" data-value="2"><i class="bi bi-star"></i></span>
                        <span class="star-rating" data-value="3"><i class="bi bi-star"></i></span>
                        <span class="star-rating" data-value="4"><i class="bi bi-star"></i></span>
                        <span class="star-rating" data-value="5"><i class="bi bi-star"></i></span>
                    </div>
                </div>
                <input type="hidden" id="votoRecensione" value="0" required>
            </div>
            <div class="mb-3">
                <label for="testoRecensione" class="form-label">La tua esperienza</label>
                <textarea class="form-control bg-dark text-white border-secondary" 
                          id="testoRecensione" rows="4" required maxlength="1000"></textarea>
            </div>
            <div class="d-flex justify-content-between">
                <button type="button" class="btn btn-outline-secondary" id="tornaRecensioni">
                    <i class="bi bi-arrow-left me-2"></i>Torna alle recensioni
                </button>
                <button type="submit" class="btn btn-primary">
                    <i class="bi bi-send me-2"></i>Pubblica recensione
                </button>
            </div>
        </form>
    `;

    // Aggiungi event listener per le stelle
    document.querySelectorAll('.star-rating').forEach(star => {
        star.addEventListener('mouseenter', function () {
            const rating = parseInt(this.getAttribute('data-value'));
            aggiornaStelle(rating);
        });

        star.addEventListener('click', function () {
            const rating = parseInt(this.getAttribute('data-value'));
            document.getElementById('votoRecensione').value = rating;
            aggiornaStelle(rating, true);
        });
    });

    // Ripristina le stelle quando si esce dall'area di valutazione
    document.querySelector('.rating-input').addEventListener('mouseleave', function () {
        const currentRating = parseInt(document.getElementById('votoRecensione').value);
        aggiornaStelle(currentRating, true);
    });

    // Event listener per il pulsante Torna
    document.getElementById('tornaRecensioni').addEventListener('click', function () {
        visualizzaRecensioni(ristoranteId);
    });

    // Event listener per l'invio del form
    document.getElementById('recensioneForm').addEventListener('submit', function (e) {
        e.preventDefault();

        const ristoranteId = document.getElementById('ristoranteId').value;
        const titolo = document.getElementById('titoloRecensione').value;
        const voto = parseInt(document.getElementById('votoRecensione').value);
        const testo = document.getElementById('testoRecensione').value;

        if (voto === 0) {
            alert('Seleziona una valutazione da 1 a 5 stelle.');
            return;
        }

        // Invia la recensione al backend
        window.javaConnector.creaRecensione({
            idUtente: userId,
            idRistorante: ristoranteId,
            titolo: titolo,
            testo: testo,
            voto: voto
        })
            .then(response => {
                if (response.success) {
                    // Mostra un messaggio di successo e torna alla lista delle recensioni
                    visualizzaRecensioni(ristoranteId);
                } else {
                    document.getElementById('recensioniBody').innerHTML += `
                    <div class="alert alert-danger mt-3" role="alert">
                        Errore: ${response.error}
                    </div>
                `;
                }
            })
            .catch(error => {
                document.getElementById('recensioniBody').innerHTML += `
                <div class="alert alert-danger mt-3" role="alert">
                    Errore di sistema: ${error}
                </div>
            `;
            });
    });
}

// Aggiorna l'aspetto delle stelle
function aggiornaStelle(rating, permanent = false) {
    document.querySelectorAll('.star-rating').forEach(star => {
        const value = parseInt(star.getAttribute('data-value'));
        const icon = star.querySelector('i');

        if (value <= rating) {
            icon.className = permanent ? 'bi bi-star-fill text-warning' : 'bi bi-star-fill';
        } else {
            icon.className = 'bi bi-star';
        }
    });
}

// Inizializzazione al caricamento della pagina
document.addEventListener('DOMContentLoaded', function () {
    // Verifica se l'utente è loggato
    const isLoggedIn = sessionStorage.getItem('isLoggedIn') === 'true';
    const username = sessionStorage.getItem('username');

    // Mostra username nella navbar se loggato
    if (isLoggedIn && username) {
        document.getElementById('user-section').innerHTML = `
            <span class="nav-link me-3" id="username-display">${username}</span>
            <a class="nav-link" href="#" id="logout-btn">
                <i class="bi bi-box-arrow-right me-2"></i>Logout
            </a>
        `;

        // Event listener per il logout
        document.getElementById('logout-btn').addEventListener('click', function (e) {
            e.preventDefault();
            sessionStorage.clear();
            window.location.reload();
        });
    } else {
        document.getElementById('user-section').innerHTML = `
            <a class="nav-link me-2" href="index.html">
                <i class="bi bi-box-arrow-in-right me-2"></i>Accedi
            </a>
            <a class="nav-link" href="register.html">
                <i class="bi bi-person-plus me-2"></i>Registrati
            </a>
        `;
    }

    // Carica tutti i ristoranti all'avvio
    waitForBridge(function () {
        caricaRistoranti();
    });

    // Event listener per la ricerca
    document.getElementById('searchForm').addEventListener('submit', function (e) {
        e.preventDefault();
        const query = document.getElementById('searchInput').value;
        cercaRistoranti(query);
    });

    // Event listener per la ricerca in tempo reale (dopo 500ms)
    let searchTimeout;
    document.getElementById('searchInput').addEventListener('input', function () {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(() => {
            cercaRistoranti(this.value);
        }, 500);
    });

    // Event listener per i filtri
    document.getElementById('filtroCucina').addEventListener('change', filtraRistoranti);
    document.getElementById('filtroPrezzo').addEventListener('change', filtraRistoranti);
    document.getElementById('filtroConsegna').addEventListener('change', filtraRistoranti);

    // Event listener per pulire i filtri
    document.getElementById('pulisci-filtri').addEventListener('click', function () {
        document.getElementById('searchInput').value = '';
        document.getElementById('filtroCucina').value = 'tutti';
        document.getElementById('filtroPrezzo').value = '0';
        document.getElementById('filtroConsegna').checked = false;

        caricaRistoranti();
    });
});