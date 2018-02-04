class GameManager {
    constructor(gid) {
        this.root = $('body');
        this.gid = gid;

        this.scoreboard = new List(this.root.find('#scoreboard')[0], {
            item: 'playerItemTemplate',
            valueNames: ['_name', '_score', '_status']
        });

        this.chat = new List(this.root.find('#chat')[0], {
            item: 'chatMessageTemplate',
            valueNames: ['_msg', '_sender']
        });

        this._hand = this.root.find('#hand');
        this.hand = new List(this._hand[0], {
            item: 'cardTemplate',
            valueNames: ['_text', '_pick', '_draw', '_watermark', {data: ['black', 'cid']}]
        });

        this.masonryOptions = {
            itemSelector: '.pyx-card',
            percentPosition: true,
            fitWidth: false
        };

        this._tableCards = this.root.find('#whiteCards');
        this._tableCards_masonry = this._tableCards.find('.list');
        this._tableCards_masonry.masonry(this.masonryOptions);

        this.table = new List(this._tableCards[0], {
            valueNames: ['_text', '_pick', '_draw', '_watermark', {data: ['black', 'cid']}],
            item: 'cardTemplate'
        });

        this._cardTemplate = this.root.find('#cardTemplate');
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
     * @param {object} card - Card
     * @param {int} card.PK - Num pick
     * @param {int} card.D - Num draw
     * @param {string} card.W - Watermark
     * @param {string} card.T - Card text
     */
    set blackCard(card) {
        this.bc = card;

        if (card === null) {
            this._blackCardContainer.empty();
            return;
        }

        const template = this._cardTemplate.clone();
        template.removeAttr("id");
        template.attr("data-black", "true");

        template.find('._text').html(card.T);

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
     * @param {string} info.gi.gs - Game status
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
                this._addWhiteCard(list, card[i], listener); // TODO: Group cards

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
            Notifier.error("Failed to send the message!", data);
        });
    }

    _updatePlayerStatus(info) {
        for (let i = 0; i < this.info.pi.length; i++) {
            if (this.info.pi[i].N === info.N) {
                this.info.pi[i] = info;
                break;
            }
        }
    }

    /**
     * @param {object[]} data.h - Hand cards
     * @param {object} data.pi - Player's info
     * @param {string} data.rw - Round winner
     * @param {string} data.n - Player's nickname
     * @param {object} data.pi.st - Player's status
     * @param {int} data.pi.sc - Player's score
     * @param {string} data.pi.N - Player's name
     * @param {string} data.gs - Game status
     * @param {object} data.gi - Game info
     * @param {object} data.i - Round intermission
     */
    handlePollEvent(data) {
        switch (data["E"]) {
            case "C":
                this._receivedGameChatMessage(data);
                break;
            case "gpj":
                this.info.pi.push({"N": data.n, "sc": 0, "st": "si"});
                this._reloadScoreboard();
                Notifier.timeout(Notifier.INFO, "<b>" + data.n + "</b> joined the game!")
                break;
            case "gpl":
                for (let i = 0; i < this.info.pi.length; i++) {
                    if (this.info.pi[i].N === data.n) {
                        this.info.pi.splice(i, 1);
                        break;
                    }
                }

                this._reloadScoreboard();
                Notifier.timeout(Notifier.INFO, "<b>" + data.n + "</b> left the game!")
                break;
            case "gpic":
                const pi = data.pi;
                this._updatePlayerStatus(pi);
                this._reloadScoreboard();

                if (pi.N === this.user.n) this.handleMyInfoChanged(pi);
                break;
            case "hd":
                this.addHandCards(data.h, data.h.length === 10);
                break;
            case "gsc":
                this.info.gi.gs = data.gs;
                this._handleGameStatusChange(data);
                break;
            case "gjl":
                Notifier.timeout(Notifier.ALERT, "The judge left.")
                Notifier.countdown(Notifier.ALERT, "A new round will begin in ", data.i / 1000, " seconds...");
                break;
            case "gjs":
                Notifier.timeout(Notifier.ALERT, "The judge has been skipped for beign idle. A new round just started.")
                break;
            case "goc":
                this.info.gi.go = data.gi;
                break;
            case "gpki":
                Notifier.timeout(Notifier.ALERT, "<b>" + data.n + "</b> has been kicked for beign idle.")
                break;
            case "gps":
                Notifier.timeout(Notifier.INFO, "<b>" + data.n + "</b> has been skipped for beign idle.")
                break;
            case "kfgi":
                this._postLeave();
                break;
            case "hu":
                Notifier.countdown(Notifier.WARN, "Hurry up! You have ", 10, " seconds to play!")
                break;
        }
    }

    /**
     * @param {string} info.st - Player's (my) status
     */
    handleMyInfoChanged(info) {
        Notifier.debug("My status is now: " + info.st);

        switch (info.st) {
            case "sj":
                this.toggleHandVisibility(false);
                Notifier.timeout(Notifier.ALERT, "You are the Card Czar.");
                break;
            case "sjj":
                this.toggleHandVisibility(false);
                Notifier.timeout(Notifier.ALERT, "Select the winning card.");
                break;
            case "sp":
                this.toggleHandVisibility(true);
                Notifier.timeout(Notifier.ALERT, "Select " + this.bc.PK + (this.bc.PK === 1 ? " card" : " cards") + " to play.");
                break;
            case "sh":
                this.toggleHandVisibility(false);
                Notifier.timeout(Notifier.ALERT, "You are the game host. Start when you're ready.");
                break;
            case "si":
                this.toggleHandVisibility(false);
                break;
            case "sw":
                this.toggleHandVisibility(false);
                Notifier.timeout(Notifier.ALERT, "You won the game!");
                break;
        }
    }

    _recreateMasonry() {
        this._tableCards_masonry.masonry('destroy');
        this._tableCards_masonry.masonry(this.masonryOptions)
    }

    _handleHandCardSelect(card) {
        const self = this;
        $.post("AjaxServlet", "o=pc&cid=" + card.cid + "&gid=" + gameManager.id).done(function (data) {
            /**
             * @param {int} data.ltp - Number of cards left to play
             */

            self.removeHandCard(card);
            if (data.ltp === 0) toggleHand(undefined, false);
        }).fail(function (data) {
            Notifier.error("Failed to play the card!", data);
        });
    }

    _handleTableCardSelect(card) {
        $.post("AjaxServlet", "o=js&cid=" + card.cid + "&gid=" + gameManager.id).done(function () {
            // Do nothing
        }).fail(function (data) {
            Notifier.error("Failed to select the card!", data);
        });
    }

    addTableCards(cards, clear = false) {
        if (!Array.isArray(cards)) cards = [cards];

        if (clear) this.table.clear();

        for (let i = 0; i < cards.length; i++) {
            GameManager._addWhiteCard(this.table, cards[i], (card) => this._handleTableCardSelect(card));
        }

        this._recreateMasonry();
    }

    _reloadScoreboard() {
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

    /**
     * @param {string} data.gs - Game status
     * @param {object} data.bc - Black card
     * @param {object[]} data.wc - Table cards
     * @param {int} data.WC - Winning card(s), comma separated list
     * @param {string} data.rw - Round winner nickname
     * @param {int} data.i - Round intermission
     */
    _handleGameStatusChange(data) {
        switch (data.gs) {
            case "l":
                this.blackCard = null;
                this.addHandCards([], true);
                this.addTableCards([], true);

                this.toggleStartButton(this.amHost);
                this.toggleHandVisibility(false);
                break;
            case "p":
                this.blackCard = data.bc;
                this.toggleStartButton(false);
                this.addTableCards([], true);
                this.toggleHandVisibility(this._getPlayer(this.user.n).st === "sp");
                break;
            case "j":
                this.addTableCards(data.wc, true);
                this.toggleStartButton(false);
                this.toggleHandVisibility(false);
                break;
            case "ro":
                this._highlightWinningCards(data.WC);
                if (data.rw !== this.user.n) Notifier.timeout(Notifier.ALERT, "<b>" + data.rw + "</b> won this round!");
                Notifier.countdown(Notifier.ALERT, "A new round will begin in ", data.i / 1000, " seconds...");
                break;
        }
    }

    _highlightWinningCards(cids) {
        cids = cids.split(",");
        this._tableCards_masonry.children().each(function () {
            const self = $(this);
            for (let i = 0; i < cids.length; i++) {
                console.log(cids[i] + "::" + self.attr("data-cid"))
                if (cids[i] === self.attr("data-cid")) self.addClass("highlighted");
            }
        });
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
        this.toggleStartButton(this.amHost && this.info.gi.gs === "l");
        this.toggleHandVisibility(this._getPlayer(this.user.n).st === "sp");

        this._reloadScoreboard();
    }

    leave() {
        const self = this;
        $.post("AjaxServlet", "o=lg&gid=" + gameManager.id).always(function () {
            self._postLeave();
        });
    }

    _postLeave() {
        window.location = "lobbies.html";
    }

    start() {
        $.post("AjaxServlet", "o=sg&gid=" + gameManager.id).done(function (data) {
            Notifier.debug(data);
        }).fail(function (data) {
            /**
             * @param {object} data.responseJSON - The response body
             *
             * @param {string} error.ec - Error code
             * @param {int} error.bcp - Provided black cards
             * @param {int} error.bcr - Required black cards
             * @param {int} error.wcp - Provided white cards
             * @param {int} error.wcr - Required white cards
             */
            const error = data.responseJSON;

            switch (error.ec) {
                case "nec":
                    Notifier.error("Not enough cards to start the game!" +
                        "<br>Black cards: " + error.bcp + "/" + error.bcr +
                        "<br>White cards: " + error.wcp + "/" + error.wcr, error);
                    break;
                case "nep":
                    Notifier.error("Not enough players to start the game!", data);
                    break;
                default:
                    Notifier.error("Failed starting the game!", data);
                    break;
            }
        })
    }
}

const gameManager = new GameManager(getURLParameter('gid'));

window.onload = function () {
    if (gameManager.id === null) {
        window.location = "lobbies.html";
    } else {
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
                Notifier.debug(data);
            }).fail(function (data) {
                Notifier.error("Failed loading the game!", data);
            });

            Notifier.debug(data);
        }).fail(function (data) {
            Notifier.error("Failed loading the game!", data);
        });
    }).fail(function (data) {
        Notifier.error("Failed loading the game!", data);
    });

    registerPollListener("GAME", function (data) {
        Notifier.debug(data);
        gameManager.handlePollEvent(data);
    });
}

function sendChatMessage(field, ev = undefined) {
    if (ev !== undefined && ev.keyCode !== 13) return;

    let input;
    if (field.tagName === "INPUT") input = field;
    else input = field.querySelector('input');

    const msg = input.value;
    if (msg.length === 0) return;

    gameManager.sendGameChatMessage(msg, () => {
        $(input.nextElementSibling).removeClass("mdc-text-field__label--float-above");
        input.value = "";
        $(input).blur();
    });
}

function leaveGame() {
    closeWebSocket();
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

