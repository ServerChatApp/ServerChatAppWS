let socket;
let username;

function login() {
    username = document.getElementById('username').value;
    let password = document.getElementById('password').value;

    fetch('http://localhost:5432/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({username: username, password: password})
    })
    .then(response => response.json())
    .then(data => {
        if (data) {
            // Si el inicio de sesión es exitoso, abrir la conexión WebSocket y mostrar el chat
            socket = new WebSocket('ws://localhost:5432/chat');
            document.getElementById('loginForm').style.display = 'none';
            document.getElementById('registerForm').style.display = 'none';
            document.getElementById('messageForm').style.display = 'block';
        } else {
            // Si el inicio de sesión falla, mostrar un mensaje de error
            alert('Login failed');
        }
    });
}

function register() {
    username = document.getElementById('registerUsername').value;
    let password = document.getElementById('registerPassword').value;
    let confirmPassword = document.getElementById('registerConfirmPassword').value;

    fetch('http://localhost:5432/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({username: username, password: password, confirmPassword: confirmPassword})
    })
    .then(response => response.json())
    .then(data => {
        if (data === true) {
            // Si el registro es exitoso, mostrar el formulario de inicio de sesión
            document.getElementById('registerForm').style.display = 'none';
            document.getElementById('loginForm').style.display = 'block';
            alert('Register successful, please login');
        } else {
            // Si el registro falla, mostrar un mensaje de error
            alert('Register failed');
        }
    });
}

function sendMessage() {
    let message = document.getElementById('message').value;
    socket.send(username + ': ' + message);
    document.getElementById('message').value = '';
}

socket.onmessage = function(event) {
    let chat = document.getElementById('chat');
    chat.innerHTML += event.data + '<br>';
    chat.scrollTop = chat.scrollHeight;
};
