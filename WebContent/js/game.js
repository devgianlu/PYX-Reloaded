class GameManager {
    constructor(gid) {
        this.root = $('body');
        this.gid = gid;

        this.scoreboard = new List(this.root.find('#scoreboard')[0], {
            item: 'player-item-template',
            valueNames: ['_name', '_score', '_status']
        });

        this.chat = new List(this.root.find('#chat')[0], {
            item: 'chat-msg-template',
            valueNames: ['_msg', '_sender']
        });

        this.hand = new List(this.root.find('#hand')[0], {
            item: 'card-template',
            valueNames: ['_text', '_pick', '_draw', '_watermark', {data: ['black']}]
        });

        this.masonryOptions = {
            itemSelector: '.pyx-card',
            fitWidth: true
        };

        this._tableCards = this.root.find('#whiteCards');
        this._tableCards_masonry = this._tableCards.find('.list');
        this._tableCards_masonry.masonry(this.masonryOptions);

        this.table = new List(this._tableCards[0], {
            valueNames: ['_text', '_pick', '_draw', '_watermark', {data: ['black']}],
            item: 'card-template'
        });

        this._cardTemplate = this.root.find('#card-template');
        this._blackCardContainer = this.root.find('#blackCard');
        this._title = this.root.find('header ._title');
        this._startGame = this.root.find('#startGame');
    }

    set me(user) {
        this.user = user;
    }

    set blackCard(card) {
        if (card === null) {
            this._blackCardContainer.empty();
            return;
        }

        const template = this._cardTemplate.clone();
        template.removeAttr("id");
        template.attr("data-black", "true");

        template.find('._text').text(card.T);

        const watermark = template.find('._watermark');
        if (card.W !== undefined && card.W.length > 0) watermark.text(card.W);
        else watermark.remove();

        const pick = template.find('._pick');
        if (card.PK > 0) pick.text(card.PK.toString());
        else pick.parent().remove();

        const draw = template.find('._draw');
        if (card.D > 0) draw.text(card.D.toString());
        else draw.parent().remove();

        this._blackCardContainer.empty();
        this._blackCardContainer.append(template);

        this._recreateMasonry(); // Width changed
    }

    get id() {
        return this.gid;
    }

    set gameInfo(info) {
        this.info = info;
        this.setup();
    }

    static getStatusFromCode(st) {
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

    get amHost() {
        return this.info.gi.H === this.user.n;
    }

    _receivedGameChatMessage(data) {
        this.chat.add({
            "_msg": data.m,
            "_sender": data.f + ": "
        })
    }

    addHandCards(cards, clear = false) {
        if (!Array.isArray(cards)) cards = [cards];

        if (clear) this.hand.clear();

        for (let i = 0; i < cards.length; i++) {
            GameManager._addWhiteCard(this.hand, cards[i]);
        }
    }

    addTableCards(cards, clear = false) {
        if (!Array.isArray(cards)) cards = [cards];

        if (clear) this.table.clear();

        for (let i = 0; i < cards.length; i++) {
            GameManager._addWhiteCard(this.table, cards[i]);
        }

        this._recreateMasonry();
    }

    sendGameChatMessage(msg, clear) {
        $.post("AjaxServlet", "o=GC&m=" + msg + "&gid=" + gameManager.id).done(function () {
            clear();
        }).fail(function (data) {
            alert("Failed send message: " + JSON.stringify(data));
        });
    }

    handlePollEvent(data) {
        switch (data["E"]) {
            case "c":
                this._receivedGameChatMessage(data);
                break;
            case "gpj":
                this.scoreboard.add({"_name": data.n, "_score": 0, "_status": GameManager.getStatusFromCode("si")});
                break;
            case "gpl":
                this.scoreboard.remove("_name", data.n);
                break;
            case "gpic":
                const pi = data.pi;
                const item = this.scoreboard.get("_name", pi.N);
                // TODO: Handle player info changed
                break;
            case "hd":
                this.addHandCards(data.h, data.h.length === 10);
                break;
            case "gsc":
                this._handleGameStatusChange(data);
                break;
        }
    }

    _recreateMasonry() {
        this._tableCards_masonry.masonry('destroy');
        this._tableCards_masonry.masonry(this.masonryOptions)
    }

    static _addWhiteCard(list, card) {
        if (Array.isArray(card)) {
            for (let i = 0; i < card.length; i++)
                this._addWhiteCard(list, card);

            return;
        }

        const elm = $(list.add({"_text": card.T, "_watermark": card.W, "black": false})[0].elm);
        if (card.W === undefined || card.W.length === 0) elm.find('._watermark').remove();
        elm.find('._pick').parent().remove();
        elm.find('._draw').parent().remove();
    }

    _handleGameStatusChange(data) {
        switch (data.gs) {
            case "l":
                this.blackCard = null;
                this.addHandCards([], true);
                this.addTableCards([], true);

                this.toggleStartButton(this.amHost);
                break;
            case "p":
                this.blackCard = data.bc;
                this.toggleStartButton(false);
                break;
            case "j":
                this.addTableCards(data.wc, true);
                this.toggleStartButton(false);
                break;
        }
    }

    toggleStartButton(visible) {
        if (visible) this._startGame.show();
        else this._startGame.hide();
    }

    setup() {
        this._title.text(this.info.gi.H + " - PYX Reloaded");
        this.toggleStartButton(this.amHost);

        this.scoreboard.clear();
        for (let i = 0; i < this.info.pi.length; i++) {
            const player = this.info.pi[i];
            this.scoreboard.add({
                "_name": player.N,
                "_score": player.sc,
                "_status": GameManager.getStatusFromCode(player.st)
            });
        }
    }

    leave() {
        $.post("AjaxServlet", "o=lg&gid=" + gameManager.id).always(function () {
            window.location = "lobbies.html";
        });
    }

    start() {
        $.post("AjaxServlet", "o=sg&gid=" + gameManager.id).done(function (data) {
            console.log(data);
        }).fail(function (data) {
            alert("Failed starting game: " + JSON.stringify(data));
            // TODO: Show nice dialog
        })
    }
}

const gameManager = new GameManager(getURLParameter('gid'));

window.onload = function () {
    if (gameManager.id === null) {
        window.location = "lobbies.html";
    } else {
        sendPollRequest(false);
        loadUI();
    }
};

function loadUI() {
    $.post("AjaxServlet", "o=gme").done(function (data) {
        gameManager.me = data;

        $.post("AjaxServlet", "o=ggi&gid=" + gameManager.id).done(function (data) {
            gameManager.gameInfo = data;

            $.post("AjaxServlet", "o=gc&gid=" + gameManager.id).done(function (data) {
                gameManager.blackCard = data.bc;
                gameManager.addHandCards(data.h, true);
                gameManager.addTableCards(data.wc, true);
                console.log(data);
            }).fail(function (data) {
                alert("Failed load: " + JSON.stringify(data));
            });

            console.log(data);
        }).fail(function (data) {
            alert("Failed load: " + JSON.stringify(data));
        });
    }).fail(function (data) {
        alert("Failed load: " + JSON.stringify(data));
    });

    registerPollListener("GAME", function (data) {
        console.log(data);
        gameManager.handlePollEvent(data);
    });
}

function sendChatMessage(field) {
    const input = field.querySelector('input');
    const msg = input.value;
    if (msg.length === 0) return;

    gameManager.sendGameChatMessage(msg, () => {
        $(input.nextElementSibling).removeClass("mdc-text-field__label--float-above");
        input.value = "";
    });
}

function leaveGame() {
    stopPolling();
    gameManager.leave();
}

function startGame() {
    gameManager.start();
}

const drawer = new mdc.drawer.MDCTemporaryDrawer(document.getElementById('drawer'));
document.querySelector('.mdc-toolbar__menu-icon').addEventListener('click', function () {
    drawer.open = true
});

const hand = new BottomSheet(document.getElementById('hand'));

function toggleHand(button) {
    if (button === undefined) button = document.getElementById('toggleHand');

    if (hand.open) {
        button.innerHTML = "keyboard_arrow_up";
        hand.open = false;
    } else {
        button.innerHTML = "keyboard_arrow_down";
        hand.open = true;
    }
}

