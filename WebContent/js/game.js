const game_id = getURLParameter('gid');

window.onload = function () {
    if (game_id === null) {
        window.location = "lobbies.html";
    } else {
        sendPollRequest(false);
        loadUI(game_id);
    }
};

function setTitle(host) {
    document.querySelector('header ._title').innerHTML = host + " - PYX Reloaded";
}

function loadUI(gid) {
    $.post("AjaxServlet", "o=ggi&gid=" + gid).done(function (data) {
        setTitle(data.gi.H);
        loadPlayers(data.pi);

        console.log(data);
    }).fail(function (data) {
        alert("Failed load: " + JSON.stringify(data));
    });

    registerPollListener("GAME", function (data) {
        switch (data["E"]) {
            case "c":
                handleChatMessage(data);
                break;
            case "gpj":
                addPlayer(document.getElementById('scoreboard'), {"N": data.n, "sc": 0, "st": "si"});
                break;
            case "gpl":
                removePlayer(document.getElementById('scoreboard'), data.n);
                break;
        }
    });
}

const chat = new List('chat', {
    item: 'chat-msg-template',
    valueNames: ['_msg', '_sender']
});

function handleChatMessage(data) {
    chat.add({
        "_msg": data.m,
        "_sender": data.f + ": "
    })
}

function sendChatMessage(field) {
    const input = field.querySelector('input');
    const msg = input.value;
    if (msg.length === 0) return;

    $.post("AjaxServlet", "o=GC&m=" + msg + "&gid=" + game_id).done(function (data) {
        $(input.nextElementSibling).removeClass("mdc-text-field__label--float-above");
        input.value = "";
    }).fail(function (data) {
        alert("Failed send message: " + JSON.stringify(data));
    });
}

function removePlayer(container, nick) {
    $(container).find('span.mdc-list-item__text:contains(' + nick + ')').filter(function () {
        return $(this).contents().get(0).textContent === nick;
    }).parent().remove();
}

function addPlayer(container, player) {
    const li = document.createElement("li");
    li.className = "mdc-list-item";
    const graphic = document.createElement("span");
    graphic.className = "mdc-list-item__graphic mdc-typography mdc-typography--display1";
    graphic.innerText = player.sc.toString();
    li.appendChild(graphic);
    const primary = document.createElement("span");
    primary.className = "mdc-list-item__text";
    primary.innerText = player.N;
    const secondary = document.createElement("span");
    secondary.className = "mdc-list-item__secondary-text";
    secondary.innerText = getStatusFromCode(player.st);
    primary.appendChild(secondary);
    li.appendChild(primary);

    container.appendChild(li);
}

function loadPlayers(players) {
    const scoreboard = document.getElementById('scoreboard');
    clearElement(scoreboard);

    for (let i = 0; i < players.length; i++) {
        addPlayer(scoreboard, players[i]);
    }
}

function getStatusFromCode(st) {
    switch (st) {
        case "sh":
            return "Host";
        case "si":
            return "Idle";
        case "sj":
            return "Judge";
        case "sjj":
            return "Judging";
        case "sp":
            return "Playing";
        case "sv":
            return "Spectator";
        case "sw":
            return "Winner";
    }
}

function leaveGame() {
    stopPolling();
    $.post("AjaxServlet", "o=lg&gid=" + game_id).always(function (data) {
        window.location = "lobbies.html";
    });
}

const drawer = new mdc.drawer.MDCTemporaryDrawer(document.getElementById('drawer'));
document.querySelector('.mdc-toolbar__menu-icon').addEventListener('click', function () {
    drawer.open = true
});

