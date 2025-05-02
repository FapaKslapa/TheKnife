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

                // Reindirizzamento alla home dopo 1 secondo
                setTimeout(() => {
                    window.location.href = 'home.html';
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