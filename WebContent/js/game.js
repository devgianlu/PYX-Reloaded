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

        this._hand = this.root.find('#hand');
        this.hand = new List(this._hand[0], {
            item: 'card-template',
            valueNames: ['_text', '_pick', '_draw', '_watermark', {data: ['black', 'cid']}]
        });

        this.masonryOptions = {
            itemSelector: '.pyx-card',
            fitWidth: true
        };

        this._tableCards = this.root.find('#whiteCards');
        this._tableCards_masonry = this._tableCards.find('.list');
        this._tableCards_masonry.masonry(this.masonryOptions);

        this.table = new List(this._tableCards[0], {
            valueNames: ['_text', '_pick', '_draw', '_watermark', {data: ['black', 'cid']}],
            item: 'card-template'
        });

        this._cardTemplate = this.root.find('#card-template');
        this._blackCardContainer = this.root.find('#blackCard');
        this._title = this.root.find('header ._title');
        this._startGame = this.root.find('#startGame');
        this._hand_toolbar = this._hand.find(".mdc-toolbar");
    }

    /**
     * @param {string} user.n - Nickname
     */
    set me(user) {
        this.user = user;
    }

    /**
     * @param {int} card.PK - Num pick
     * @param {int} card.D - Num draw
     * @param {string} card.W - Watermark
     * @param {string} card.T - Card text
     */
    set blackCard(card) {
        if (card === undefined) {
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

    /**
     * @param {array} info.pi - Players info
     * @param {string} info.gi.H - Host's nickname
     * @param {string} info.gi.S - Game status
     */
    set gameInfo(info) {
        this.info = info;
        this.setup();
    }

    /**
     * @returns {boolean} - Wheteter I am the host or not
     */
    get amHost() {
        return this.info.gi.H === this.user.n;
    }

    /**
     * @param st - Status code
     * @returns {string} - Status string
     */
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

    /**
     * @param list - The List.JS object
     * @param {object|object[]} card - A card or a list of cards
     * @param {string} card.W - Card watermark
     * @param {string} card.T - Card text
     * @param {int} card.cid - Card ID
     * @param listener - A click listener
     * @private
     */
    static _addWhiteCard(list, card, listener = undefined) {
        if (Array.isArray(card)) {
            for (let i = 0; i < card.length; i++)
                this._addWhiteCard(list, card[i]); // TODO: Group cards

            return;
        }

        const elm = $(list.add({"cid": card.cid, "_text": card.T, "_watermark": card.W, "black": false})[0].elm);
        if (card.W === undefined || card.W.length === 0) elm.find('._watermark').remove();
        elm.find('._pick').parent().remove();
        elm.find('._draw').parent().remove();

        if (listener === undefined) elm.removeClass("mdc-ripple-surface");
        else elm.on("click", () => listener(card));
    }

    /**
     * @param {string} data.m - Message
     * @param {string} data.f - Sender
     * @private
     */
    _receivedGameChatMessage(data) {
        this.chat.add({
            "_msg": data.m,
            "_sender": data.f + ": "
        })
    }

    static _removeWhiteCard(list, card) {
        list.remove("cid", card.cid);
    }

    addHandCards(cards, clear = false) {
        if (!Array.isArray(cards)) cards = [cards];

        if (clear) this.hand.clear();

        for (let i = 0; i < cards.length; i++) {
            GameManager._addWhiteCard(this.hand, cards[i], (card) => this._handleHandCardSelect(card));
        }
    }

    removeHandCard(card) {
        GameManager._removeWhiteCard(this.hand, card);
    }

    sendGameChatMessage(msg, clear) {
        $.post("AjaxServlet", "o=GC&m=" + msg + "&gid=" + gameManager.id).done(function () {
            clear();
        }).fail(function (data) {
            alert("Failed send message: " + JSON.stringify(data));
        });
    }

    /**
     * @param {object[]} data.h - Hand cards
     * @param {object} data.pi - Player's info
     * @param {string} data.n - Player's nickname
     * @param {object} data.pi.st - Player's status
     * @param {int} data.pi.sc - Player's score
     * @param {string} data.pi.N - Player's name
     */
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
                if (pi.N === this.user.n) this.handleMyInfoChanged(pi);

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

    /**
     * @param {string} info.st - Player's (my) status
     */
    handleMyInfoChanged(info) {
        switch (info.st) {
            case "sj":
                break;
            case "sjj":
                break;
            case "sp":
                break;
            case "sh":
                break;
            case "si":
                this.toggleHandVisibility(false);
                break;
            case "sw":
                break;
            case "sv":
                break;
        }
    }

    _recreateMasonry() {
        this._tableCards_masonry.masonry('destroy');
        this._tableCards_masonry.masonry(this.masonryOptions)
    }

    _handleHandCardSelect(card) {
        // TODO: Support for write-in cards
        const self = this;
        $.post("AjaxServlet", "o=pc&cid=" + card.cid + "&gid=" + gameManager.id).done(function () {
            toggleHand(undefined, false);
            self.removeHandCard(card);
        }).fail(function (data) {
            alert("Failed play card: " + JSON.stringify(data));
        });
    }

    addTableCards(cards, clear = false) {
        if (!Array.isArray(cards)) cards = [cards];

        if (clear) this.table.clear();

        for (let i = 0; i < cards.length; i++) {
            GameManager._addWhiteCard(this.table, cards[i], undefined);
        }

        this._recreateMasonry();
    }

    /**
     * @param {string} data.gs - Game status
     * @param {object} data.bc - Black card
     * @param {object[]} data.wc - Table cards
     */
    _handleGameStatusChange(data) {
        switch (data.gs) {
            case "l":
                this.blackCard = undefined;
                this.addHandCards([], true);
                this.addTableCards([], true);

                this.toggleStartButton(this.amHost);
                this.toggleHandVisibility(false);
                break;
            case "p":
                this.blackCard = data.bc;
                this.toggleStartButton(false);
                this.toggleHandVisibility(this._getPlayer(this.user.n).st === "sp");
                break;
            case "j":
                this.addTableCards(data.wc, true);
                this.toggleStartButton(false);
                this.toggleHandVisibility(false);
                break;
        }
    }

    toggleStartButton(visible) {
        if (visible) this._startGame.show();
        else this._startGame.hide();
    }

    toggleHandVisibility(visible) {
        if (visible) this._hand_toolbar.show();
        else this._hand_toolbar.hide();
    }

    _getPlayer(nick) {
        for (let i = 0; i < this.info.pi.length; i++) {
            const player = this.info.pi[i];
            if (player.N === nick)
                return player;
        }
    }

    setup() {
        this._title.text(this.info.gi.H + " - PYX Reloaded");
        this.toggleStartButton(this.amHost && this.info.gi.S === "l");
        this.toggleHandVisibility(this._getPlayer(this.user.n).st === "sp");

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

function toggleHand(button, open = undefined) {
    if (button === undefined) button = document.getElementById('toggleHand');
    const list = document.querySelector("#hand .list");
    list.scrollLeft = 0;

    if (open === undefined) open = !hand.open;

    if (open) {
        button.innerHTML = "keyboard_arrow_down";
        hand.open = true;
    } else {
        button.innerHTML = "keyboard_arrow_up";
        hand.open = false;
    }
}

