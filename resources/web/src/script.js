document.getElementById('loginForm').addEventListener('submit', function (e) {
    e.preventDefault();

    const usernameOrEmail = document.getElementById('usernameOrEmail').value;
    const password = document.getElementById('password').value;

    window.javaConnector.login({
        usernameOrEmail: usernameOrEmail,
        password: password
    })
        .then(response => {
            const alertArea = document.getElementById('alertArea');

            if (response.success) {
                // Salva i dati dell'utente in sessionStorage per mantenerli durante la navigazione
                sessionStorage.setItem('userId', response.userId);
                sessionStorage.setItem('username', response.username);
                sessionStorage.setItem('email', response.email);
                sessionStorage.setItem('ruolo', response.ruolo);
                sessionStorage.setItem('isLoggedIn', 'true');

                alertArea.innerHTML = `
                        <div class="alert alert-success" role="alert">
                            Login effettuato con successo! Reindirizzamento in corso...
                        </div>`;

                // Reindirizzamento in base al ruolo dell'utente
                setTimeout(() => {
                    if (response.ruolo === 'UTENTE') {
                        window.location.href = 'homeutente.html';
                    } else if (response.ruolo === 'RISTORATORE') {
                        window.location.href = 'home.html';
                    } else {
                        window.location.href = 'homeutente.html'; // Default fallback
                    }
                }, 1000);
            } else {
                alertArea.innerHTML = `
                        <div class="alert alert-danger" role="alert">
                            Errore: ${response.error}
                        </div>`;
            }
        })
        .catch(error => {
            document.getElementById('alertArea').innerHTML = `
                    <div class="alert alert-danger" role="alert">
                        Errore di sistema: ${error}
                    </div>`;
        });
});
