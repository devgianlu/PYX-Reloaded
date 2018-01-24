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

        _loadDummyData();

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

function addWhiteCard(list, card) {
    const elm = $(list.add({"_text": card.T, "_watermark": card.W, "black": false})[0].elm);
    if (card.W === undefined || card.W.length === 0) elm.find('._watermark').remove();
    elm.find('._pick').parent().remove();
    elm.find('._draw').parent().remove();
}

function setBlackCard(card) {
    const template = $('#card-template').clone();
    template.removeAttr("id");
    template.attr("data-black", "true");

    template.find('._text').text(card.T);

    const watermark = template.find('._watermark');
    if (card.W !== undefined && card.W.length > 0) watermark.text(card.W);
    else watermark.remove();

    const pick = template.find('._pick');
    if (card.PK > 0) pick.innerText = card.PK.toString();
    else pick.parent().remove();

    const draw = template.find('._draw');
    if (card.D > 0) draw.innerText = card.D.toString();
    else draw.parent().remove();

    const blackCard = $('#blackCard');
    blackCard.empty();
    blackCard.append(template);
}

function _loadDummyData() {
    setBlackCard({
        "T": "Fool me once, I'm mad. Fool me twice? How could you. Fool me three times, you're officially ____.",
        "PK": 1, "D": 0,
        "W": "Cards!!?"
    });


    const whiteCards = new List('whiteCards', {
        valueNames: ['_text', '_pick', '_draw', '_watermark', {data: ['black']}],
        item: 'card-template'
    });

    addWhiteCard(whiteCards, {
        "T": "Some text here: ____",
        "W": "Oh GOSH!"
    });

    addWhiteCard(whiteCards, {
        "T": "Oh look another card!!"
    });

    addWhiteCard(whiteCards, {
        "T": "This card will have more text than the others because I need to see how that looks."
    });

    addWhiteCard(whiteCards, {
        "T": "4 cards should be enough to fill the screen. Right?",
        "W": "PYX!!"
    });

    addWhiteCard(whiteCards, {
        "T": "Apparently 4 cards wasn't enough so here it goes another card."
    });
}

const drawer = new mdc.drawer.MDCTemporaryDrawer(document.getElementById('drawer'));
document.querySelector('.mdc-toolbar__menu-icon').addEventListener('click', function () {
    drawer.open = true
});

