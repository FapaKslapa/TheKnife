document.addEventListener('DOMContentLoaded', function () {
    // Debug - visualizza nella console quando la pagina è caricata
    console.log('Pagina di registrazione caricata');

    document.getElementById('registerForm').addEventListener('submit', function (e) {
        e.preventDefault();
        console.log('Form di registrazione inviato');

        const username = document.getElementById('username').value;
        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;
        const ruolo = document.querySelector('input[name="ruolo"]:checked').value;

        console.log('Dati inseriti:', {username, email, ruolo});

        window.javaConnector.registraUtente({
            username: username,
            password: password,
            email: email,
            ruolo: ruolo
        })
            .then(response => {
                console.log('Risposta ricevuta:', response);
                const alertArea = document.getElementById('alertArea');

                if (response.success) {
                    alertArea.innerHTML = `
                        <div class="alert alert-success" role="alert">
                            Registrazione completata con successo! Reindirizzamento alla pagina di login...
                        </div>`;

                    // Reindirizzamento al login dopo 2 secondi
                    setTimeout(() => {
                        window.location.href = 'index.html';
                    }, 2000);
                } else {
                    alertArea.innerHTML = `
                        <div class="alert alert-danger" role="alert">
                            Errore: ${response.error}
                        </div>`;
                }
            })
            .catch(error => {
                console.error('Si è verificato un errore:', error);
                document.getElementById('alertArea').innerHTML = `
                    <div class="alert alert-danger" role="alert">
                        Errore di sistema: ${error}
                    </div>`;
            });
    });
});