class GameManager {
    constructor(gid) {
        this.root = $('body');
        this.gid = gid;

        this.drawer = new mdc.drawer.MDCPersistentDrawer($('#drawer')[0]);
        $('.mdc-toolbar__menu-icon').on('click', () => this.toggleDrawer());

        this._leaveGame = this.root.find('#leaveGame');
        this._leaveGame.on('click', () => this.leave());

        this._startGame = this.root.find('#startGame');
        this._startGame.on('click', () => this.start());

        this._chatMessage = this.root.find('#chatMessage');
        this._chatMessage.on('keydown', (ev) => this._handleSendChatMessage(ev));
        this._chatMessage.parent().find('.mdc-text-field__icon').on('click', () => this._handleSendChatMessage(undefined));

        let drawerStatus = Cookies.getJSON("PYX-Drawer");
        if (drawerStatus === undefined) drawerStatus = false;
        this.drawer.open = drawerStatus;

        this.scoreboard = new List(this.root.find('#scoreboard')[0], {
            item: 'playerItemTemplate',
            valueNames: ['_name', '_score', '_status']
        });

        this.chat = new List(this.root.find('#chat')[0], {
            item: 'chatMessageTemplate',
            valueNames: ['_msg', '_sender']
        });

        this._hand = this.root.find('#hand');
        this._hand_list = this._hand.find('.list');
        this.hand = new List(this._hand[0], {
            item: 'cardTemplate',
            valueNames: ['_text', '_pick', '_draw', '_watermark', {data: ['black', 'cid']}]
        });

        this.hand_sheet = new BottomSheet(this._hand[0]);

        this._toggle_hand_mask = this._hand.find('._toggleHand_mask');
        this._toggle_hand_mask.on('click', () => this._handleToggleHand());
        this._toggle_hand = this._hand.find('._toggleHand');
        this._toggle_hand.on('click', () => this._handleToggleHand());
        this._hand_info = this._hand.find('._handInfo');
        this._hand_toolbar = this._hand.find('.mdc-toolbar');

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

        this._unreadNotifs = this.root.find('._unreadNotifs');
        this._unreadNotifs.parent().on('click', () => this.toggleDrawer());
        this.unreadNotifications = 0;
        this.unreadNotifs_last = 0;
    }

    get unreadNotifications() {
        return this.unreadNotifs_count;
    }

    set unreadNotifications(value) {
        this.unreadNotifs_count = value;

        if (value === 0) {
            this._unreadNotifs.parent().hide();
        } else {
            this._unreadNotifs.parent().show();
            this._unreadNotifs.text(value + " unread");
        }

        if (value >= 5 && new Date().getTime() - this.unreadNotifs_last >= (30 + Notifier.DEFAULT_TIMEOUT) * 1000) {
            Notifier.timeout(Notifier.ALERT, "You have " + value + " unread notifications!");
            this.unreadNotifs_last = new Date().getTime();
        }
    }

    /**
     * @returns {boolean} - Whether I am the host or not
     */
    get amHost() {
        return this.info.gi.H === this.user.n;
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
        template.removeClass("mdc-ripple-surface");

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

    /**
     * @param {array} info.pi - Players info
     * @param {string} info.gi.H - Host's nickname
     * @param {object} info.gi.go - Game options
     * @param {string} info.gi.gs - Game status
     */
    set gameInfo(info) {
        this.info = info;
        this.setup();
    }

    /**
     * @param {GameOptionsDialog} dialog
     */
    set attachOptionsDialog(dialog) {
        this.gameOptionsDialog = dialog;
        this._updateOptionsDialog();
    }

    /**
     * @param list - The List.JS object
     * @param {object|object[]} card - A card or a list of cards
     * @param {string} card.W - Card watermark
     * @param {string} card.T - Card text
     * @param {int} card.cid - Card ID
     * @param {boolean} card.wi - Whether it is a blank card
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
     * @param {string} user.n - Nickname
     */
    set me(user) {
        this.user = user;
    }

    get id() {
        return this.gid;
    }

    static _askCardText() {
        return prompt("Enter the card text:", "");
    }

    static _getHandInfoText(ltp, ltd) {
        if (ltd === 0 && ltp === 0) return "";

        if (ltd === 0 && ltp !== 0) {
            return "pick or draw " + ltp + " more card" + (ltp === 1 ? "" : "s");
        } else if (ltp === 0 && ltd !== 0) {
            return "draw " + ltd + " more card" + (ltd === 1 ? "" : "s");
        } else {
            return "draw at least " + ltd + " more card" + (ltd === 1 ? "" : "s") + " for a total of " + (ltd + ltp) + " cards";
        }
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

    _handleSendChatMessage(ev) {
        if (ev !== undefined && ev.keyCode !== 13) return;

        const msg = this._chatMessage.val();
        if (msg.length === 0) return;

        this.sendGameChatMessage(msg, () => {
            this._chatMessage.next().removeClass("mdc-text-field__label--float-above");
            this._chatMessage.val("");
            this._chatMessage.blur();
        });
    }

    toggleDrawer() {
        this.drawer.open = !this.drawer.open;
        Cookies.set("PYX-Drawer", this.drawer.open);
        this.unreadNotifications = 0;

        this._recreateMasonry();
    }

    closeHand() {
        this._toggle_hand.html("keyboard_arrow_up");
        this.hand_sheet.open = false;
    }

    static _postLeave() {
        window.location = "/games/";
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

    openHand() {
        this._toggle_hand.html("keyboard_arrow_down");
        this.hand_sheet.open = true;
    }

    _updatePlayerStatus(info) {
        for (let i = 0; i < this.info.pi.length; i++) {
            if (this.info.pi[i].N === info.N) {
                this.info.pi[i] = info;
                break;
            }
        }
    }

    _handleToggleHand() {
        this._hand_list.scrollLeft(0);
        if (this.hand_sheet.open) this.closeHand();
        else this.openHand();
    }

    /**
     * @param {object} data
     * @param {string} data.m - Message
     * @param {string} data.f - Sender
     * @private
     */
    _receivedGameChatMessage(data) {
        this.chat.add({
            "_msg": data.m,
            "_sender": data.f + ": "
        });

        if (!this.drawer.open) {
            this.unreadNotifications = this.unreadNotifications + 1;
        }
    }

    _recreateMasonry() {
        this._tableCards_masonry.masonry('destroy');
        this._tableCards_masonry.masonry(this.masonryOptions)
    }

    handleMyInfoChanged(info) {
        Notifier.debug("My status is now: " + info.st);

        this._updateOptionsDialog(); // For accept button text

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

    changeGameOptions(go) {
        $.post("/AjaxServlet", "o=cgo&go=" + JSON.stringify(go) + "&gid=" + this.id).done((data) => {
            if ("H" in data) Notifier.timeout(Notifier.SUCCESS, "Your suggestion has been submitted to <b>" + data.H + "</b>.");
            else Notifier.timeout(Notifier.SUCCESS, "Game options changed successfully!");
        }).fail(function (data) {
            if ("responseJSON" in data) {
                switch (data.responseJSON.data) {
                    case "as":
                        Notifier.error("The game must be in lobby state.", data);
                        break;
                    default:
                        Notifier.error("Failed changing the game options.", data);
                        break;
                }
            } else {
                Notifier.error("Failed changing the game options.", data);
            }
        });

        this.gameOptionsDialog.updateOptions(this.info.gi.go); // Restore to actual state, wait for 'goc'
    }

    sendGameChatMessage(msg, clear) {
        $.post("/AjaxServlet", "o=GC&m=" + msg + "&gid=" + this.id).done(function () {
            clear();
        }).fail(function (data) {
            if ("responseJSON" in data) {
                if (data.responseJSON.ec === "tf") {
                    Notifier.timeout(Notifier.WARN, "You are chatting too fast. Calm down.");
                    Notifier.debug(data, true);
                    return;
                }
            }

            Notifier.error("Failed to send the message!", data);
        });
    }

    _handleHandCardSelect(card) {
        let text = "";
        if (card.wi) {
            text = GameManager._askCardText();
            if (text.length === 0) {
                Notifier.timeout(Notifier.WARN, "The card text must not be empty!");
                return;
            }
        }

        const self = this;
        $.post("/AjaxServlet", "o=pc&cid=" + card.cid + "&gid=" + this.id + "&wit=" + text).done(function (data) {
            /**
             * @param {int} data.ltp - Number of cards left to pick
             * @param {int} data.ltd - Number of cards left to draw
             */

            self.updateHandInfo(data.ltp, data.ltd);
            self.removeHandCard(card);
            if (data.ltp === 0 && data.ltd === 0) {
                self.closeHand();
            }
        }).fail(function (data) {
            if ("responseJSON" in data) {
                switch (data.responseJSON.ec) {
                    case "ap":
                        Notifier.error("You have already played all the necessary cards.", data);
                        break;
                    case "nyt":
                        Notifier.error("This is not your turn.", data);
                        break;
                    case "sdc":
                        Notifier.error("You have to draw all the remaining cards.", data);
                        break;
                    default:
                        Notifier.error("Failed to play the card!", data);
                        break;
                }
            } else {
                Notifier.error("Failed to play the card!", data);
            }
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
                this.updateHandInfo(this.bc.PK - this.bc.D, this.bc.D);
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
                if (data.wl) {
                    // Someone won the game
                    if (data.rw !== this.user.n) Notifier.timeout(Notifier.ALERT, "<b>" + data.rw + "</b> won the game!");
                } else {
                    if (data.rw === this.user.n) Notifier.timeout(Notifier.ALERT, "You won this round!");
                    else Notifier.timeout(Notifier.ALERT, "<b>" + data.rw + "</b> won this round!");
                    Notifier.countdown(Notifier.ALERT, "A new round will begin in ", data.i / 1000, " seconds...");
                }
                break;
        }
    }

    _highlightWinningCards(cids) {
        cids = cids.split(",");
        this._tableCards_masonry.children().each(function () {
            const self = $(this);
            for (let i = 0; i < cids.length; i++) {
                if (cids[i] === self.attr("data-cid")) self.addClass("highlighted");
            }
        });
    }

    toggleStartButton(visible) {
        if (visible) this._startGame.show();
        else this._startGame.hide();
    }

    _reloadDrawerPadding() {
        if (this._hand_toolbar.is(':visible'))
            this.drawer.root_.style.marginBottom = this._hand_toolbar.height() + "px";
        else
            this.drawer.root_.style.marginBottom = "0";
    }

    toggleHandVisibility(visible) {
        if (visible) this._hand_toolbar.show();
        else this._hand_toolbar.hide();
        this._reloadDrawerPadding();
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
        document.title = this.info.gi.H + " - PYX Reloaded";

        this.toggleStartButton(this.amHost && this.info.gi.gs === "l");
        this.toggleHandVisibility(this._getPlayer(this.user.n).st === "sp");

        this._reloadScoreboard();
    }

    _handleTableCardSelect(card) {
        $.post("/AjaxServlet", "o=js&cid=" + card.cid + "&gid=" + this.id).done(function () {
            // Do nothing
        }).fail(function (data) {
            if ("responseJSON" in data) {
                switch (data.responseJSON.ec) {
                    case "nj":
                        Notifier.error("You're not the judge.", data);
                        break;
                    case "nyt":
                        Notifier.error("This is not your turn.", data);
                        break;
                    default:
                        Notifier.error("Failed to select the card!", data);
                        break;
                }
            } else {
                Notifier.error("Failed to select the card!", data);
            }
        });
    }

    leave() {
        closeWebSocket();
        $.post("/AjaxServlet", "o=lg&gid=" + this.id).always(function () {
            GameManager._postLeave();
        });
    }

    start() {
        $.post("/AjaxServlet", "o=sg&gid=" + this.id).done(function (data) {
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
            if ("responseJSON" in data) {
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
            } else {
                Notifier.error("Failed starting the game!", data);
            }
        })
    }

    updateHandInfo(ltp, ltd) {
        this._hand_info.text(GameManager._getHandInfoText(ltp, ltd));

        if (ltp === 0 && ltd !== 0) {
            this.hand.filter(function (item) {
                return item.values()._watermark === "____";
            });
        } else {
            this.hand.filter();
        }

        this._reloadDrawerPadding();
    }

    /**
     * @param {object[]} data.h - Hand cards
     * @param {boolean} data.ch - Clear hand cards before adding newer
     * @param {object} data.pi - Player's info
     * @param {string} data.m - Message (chat)
     * @param {string} data.f - Sender (chat)
     * @param {string} data.n - Player's nickname
     * @param {object} data.pi.st - Player's status
     * @param {int} data.pi.sc - Player's score
     * @param {string} data.pi.N - Player's name
     * @param {object} data.gi - Game info
     * @param {string} data.gs - Game status
     * @param {object} data.bc - Blank card
     * @param {object[]} data.wc - Table cards
     * @param {int} data.WC - Winning card(s), comma separated list
     * @param {string} data.rw - Round winner nickname
     * @param {int} data.i - Round intermission
     * @param {object} data.go - Game options
     * @param {boolean} data.wl - Whether the game will stop
     * @param {object} data.cdi - Cardcast deck info
     * @param {string} data.cdi.csn - Card set name
     * @param {string} data.H - Game host
     * @param {string} data.soid - Suggested game options modification
     * @param {string} data.ss - Game options modification suggester
     */
    handlePollEvent(data) {
        switch (data["E"]) {
            case "cAc":
                Notifier.timeout(Notifier.INFO, "<b>" + data.cdi.csn + "</b> has been added to the game!");
                break;
            case "cRc":
                Notifier.timeout(Notifier.INFO, "<b>" + data.cdi.csn + "</b> has been removed from the game!");
                break;
            case "C":
                this._receivedGameChatMessage(data);
                break;
            case "gpj":
                this.info.pi.push({"N": data.n, "sc": 0, "st": "si"});
                this._reloadScoreboard();
                Notifier.timeout(Notifier.INFO, "<b>" + data.n + "</b> joined the game!");
                break;
            case "gpl":
                for (let i = 0; i < this.info.pi.length; i++) {
                    if (this.info.pi[i].N === data.n) {
                        this.info.pi.splice(i, 1);
                        break;
                    }
                }

                this._reloadScoreboard();
                Notifier.timeout(Notifier.INFO, "<b>" + data.n + "</b> left the game!");
                break;
            case "gpic":
                const pi = data.pi;
                this._updatePlayerStatus(pi);
                this._reloadScoreboard();

                if (pi.N === this.user.n) this.handleMyInfoChanged(pi);
                break;
            case "hd":
                this.addHandCards(data.h, data.ch);
                break;
            case "gsc":
                this.info.gi.gs = data.gs;
                this._handleGameStatusChange(data);
                break;
            case "gjl":
                Notifier.timeout(Notifier.ALERT, "The judge left.");
                if (!data.wl)
                    Notifier.countdown(Notifier.ALERT, "A new round will begin in ", data.i / 1000, " seconds...");
                break;
            case "gjs":
                Notifier.timeout(Notifier.ALERT, "The judge has been skipped for beign idle. A new round just started.");
                break;
            case "goc":
                this.info.gi.go = data.go;
                this._updateOptionsDialog();
                break;
            case "gpki":
                Notifier.timeout(Notifier.ALERT, "<b>" + data.n + "</b> has been kicked for being idle.");
                break;
            case "gps":
                Notifier.timeout(Notifier.INFO, "<b>" + data.n + "</b> has been skipped for being idle.");
                break;
            case "kfgi":
                GameManager._postLeave();
                break;
            case "hu":
                Notifier.countdown(Notifier.WARN, "Hurry up! You have ", 10, " seconds to play!");
                break;
            case "glk":
                Notifier.timeout(Notifier.INFO, "<b>" + data.n + "</b> liked this game.");
                break;
            case "gdlk":
                Notifier.timeout(Notifier.INFO, "<b>" + data.n + "</b> disliked this game.");
                break;
            case "gaso":
                Notifier.timeout(Notifier.SUCCESS, "Your suggested game options modification has been accepted.");
                break;
            case "gdso":
                Notifier.timeout(Notifier.ERROR, "Your suggested game options modification has been declined.");
                break;
            case "goms":
                const noty = Notifier.show(Notifier.WARN, "<b>" + data.s + "</b> suggested to modify the game options.", false, false, true,
                    Notifier.button("Accept", () => {
                        this.submitGameOptionsModificationDecision(data.soid, true);
                        noty.close();
                    }),
                    Notifier.button("Decline", () => {
                        this.submitGameOptionsModificationDecision(data.soid, false);
                        noty.close();
                    }));
                break;
        }
    }

    submitGameOptionsModificationDecision(id, decision) {
        $.post("/AjaxServlet", "o=gosd&soid=" + id + "&d=" + decision + "&gid=" + this.id).done(function () {
            // Nothing
        }).fail(function (data) {
            Notifier.error("Failed submitting decision on the suggested game options.", data);
        });
    }

    _updateOptionsDialog() {
        this.gameOptionsDialog.updateOptions(this.info.gi.go);
        this.gameOptionsDialog.acceptText = this.amHost ? "Apply" : "Suggest";
    }
}

/**
 * @param {GameManager} gameManager
 * @param {GameOptionsDialog} gameOptionsDialog
 */
function loadUI(gameManager, gameOptionsDialog) {
    $.post("/AjaxServlet", "o=gme").done(function (data) {
        gameManager.me = data;

        $.post("/AjaxServlet", "o=ggi&gid=" + gameManager.id).done(function (data) {
            gameManager.gameInfo = data;
            Notifier.debug(data);

            $.post("/AjaxServlet", "o=gc&gid=" + gameManager.id).done(function (data) {
                registerPollListener("GAME", function (data) {
                    Notifier.debug(data);
                    gameManager.handlePollEvent(data);
                });

                gameManager.blackCard = data.bc;
                gameManager.updateHandInfo(data.ltp, data.ltd);

                gameManager.addHandCards(data.h, true);
                gameManager.addTableCards(data.wc, true);

                gameManager.attachOptionsDialog = gameOptionsDialog;

                new OtherStuffManager(gameManager);

                Notifier.debug(data);
            }).fail(function (data) {
                Notifier.error("Failed loading the game!", data);
            });
        }).fail(function (data) {
            Notifier.error("Failed loading the game!", data);
        });
    }).fail(function (data) {
        Notifier.error("Failed loading the game!", data);
    });
}
