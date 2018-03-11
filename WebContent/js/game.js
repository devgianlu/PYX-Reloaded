class GameManager {
    constructor(gid, drawer) {
        this.root = $('body');
        this.gid = parseInt(gid);

        this.drawer = drawer;
        $('.mdc-toolbar__menu-icon').on('click', () => this.toggleDrawer());

        this._lobbyMessage = this.root.find('#gameLayout .message');

        this._leaveGame = this.root.find('#leaveGame');
        this._leaveGame.on('click', () => this.leave());

        this._startGame = this.root.find('#startGame');
        this._startGame.on('click', () => this.start());

        this._chatMessage = this.root.find('#chatMessage');
        this._chatMessage.on('keydown', (ev) => this._handleSendChatMessage(ev));
        this._chatMessage.parent().find('.mdc-text-field__icon').on('click', () => this._handleSendChatMessage(undefined));

        if (this.drawer instanceof mdc.drawer.MDCPersistentDrawer) {
            let drawerStatus = Cookies.getJSON("PYX-Drawer");
            if (drawerStatus === undefined) drawerStatus = false;
            this.drawer.open = drawerStatus;
        }

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

        if (card === undefined || card === null) {
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

    /**
     * @returns {int}
     */
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
            this._chatMessage.next().removeClass("mdc-floating-label--float-above");
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
                Notifier.timeout(Notifier.ALERT, "Select the winning card(s).");
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
        Requester.request("cgo", {
            "gid": this.id,
            "go": JSON.stringify(go)
        }, (data) => {
            if ("H" in data) Notifier.timeout(Notifier.SUCCESS, "Your suggestion has been submitted to <b>" + data.H + "</b>.");
            else Notifier.timeout(Notifier.SUCCESS, "Game options changed successfully!");
        }, (error) => {
            switch (error.ec) {
                case "AS":
                    Notifier.error("You have already suggested a modification. Wait for it to be accepted or declined.", data);
                    break;
                case "as":
                    Notifier.error("The game must be in lobby state.", data);
                    break;
                default:
                    Notifier.error("Failed changing the game options.", data);
                    break;
            }
        });

        this.gameOptionsDialog.updateOptions(this.info.gi.go); // Restore to actual state, wait for 'goc'
    }

    sendGameChatMessage(msg, clear) {
        Requester.request("GC", {
            "m": msg,
            "gid": this.id
        }, () => {
            clear();
        }, (error) => {
            if (error.ec === "tf") {
                Notifier.timeout(Notifier.WARN, "You are chatting too fast. Calm down.");
                Notifier.debug(error, true);
                return;
            }

            Notifier.error("Failed sending the message!", error);
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

        Requester.request("pc", {
            "cid": card.cid,
            "gid": this.id,
            "wit": text
        }, (data) => {
            /**
             * @param {int} data.ltp - Number of cards left to pick
             * @param {int} data.ltd - Number of cards left to draw
             */

            this.updateHandInfo(data.ltp, data.ltd);
            this.removeHandCard(card);
            if (data.ltp === 0 && data.ltd === 0) this.closeHand();
        }, (error) => {
            switch (error.ec) {
                case "ap":
                    Notifier.error("You have already played all the necessary cards.", error);
                    break;
                case "nyt":
                    Notifier.error("This is not your turn.", error);
                    break;
                case "sdc":
                    Notifier.error("You have to draw all the remaining cards.", error);
                    break;
                default:
                    Notifier.error("Failed to play the card!", error);
                    break;
            }
        })
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
                this.blackCard = undefined;
                this.addHandCards([], true);
                this.addTableCards([], true);
                this.toggleStartButton(this.amHost);
                this.toggleHandVisibility(false);
                this._lobbyMessage.show();
                break;
            case "p":
                this.blackCard = data.bc;
                this.updateHandInfo(this.bc.PK - this.bc.D, this.bc.D);
                this.toggleStartButton(false);
                this.addTableCards([], true);
                this.toggleHandVisibility(this.getPlayerStatus(this.user.n) === "sp");
                this._lobbyMessage.hide();
                break;
            case "j":
                this.addTableCards(data.wc, true);
                this.toggleStartButton(false);
                this.toggleHandVisibility(false);
                this._lobbyMessage.hide();
                break;
            case "ro":
                this._lobbyMessage.hide();
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
        if (visible) this._hand.show();
        else this._hand.hide();
        this._reloadDrawerPadding();
    }

    getPlayerStatus(nick) {
        for (let i = 0; i < this.info.pi.length; i++) {
            const player = this.info.pi[i];
            if (player.N === nick)
                return player.st;
        }

        return undefined;
    }

    setup() {
        this._title.text(this.info.gi.H + " - PYX Reloaded");
        document.title = this.info.gi.H + " - PYX Reloaded";

        if (this.info.gi.gs === "l") this._lobbyMessage.show();
        else this._lobbyMessage.hide();

        this.toggleStartButton(this.amHost && this.info.gi.gs === "l");
        this.toggleHandVisibility(this.getPlayerStatus(this.user.n) === "sp");

        this._reloadScoreboard();
    }

    _handleTableCardSelect(card) {
        Requester.request("js", {
                "cid": card.cid,
                "gid": this.id
            }, null,
            (error) => {
                switch (error.ec) {
                    case "nj":
                        Notifier.error("You're not the judge.", error);
                        break;
                    case "nyt":
                        Notifier.error("This is not your turn.", error);
                        break;
                    default:
                        Notifier.error("Failed to select the card!", error);
                        break;
                }
            });
    }

    leave() {
        closeWebSocket();
        Requester.always("lg", {
            "gid": this.id
        }, () => {
            GameManager._postLeave();
        });
    }

    start() {
        Requester.request("sg", {
            "gid": this.id
        }, (data) => {
            Notifier.debug(data);
        }, (error) => {
            /**
             * @param {int} error.bcp - Provided black cards
             * @param {int} error.bcr - Required black cards
             * @param {int} error.wcp - Provided white cards
             * @param {int} error.wcr - Required white cards
             */
            switch (error.ec) {
                case "nec":
                    Notifier.error("Not enough cards to start the game!" +
                        "<br>Black cards: " + error.bcp + "/" + error.bcr +
                        "<br>White cards: " + error.wcp + "/" + error.wcr, error);
                    break;
                case "nep":
                    Notifier.error("Not enough players to start the game!", error);
                    break;
                default:
                    Notifier.error("Failed starting the game!", error);
                    break;
            }
        });
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

    _removePlayer(nick) {
        for (let i = 0; i < this.info.pi.length; i++) {
            if (this.info.pi[i].N === nick) {
                this.info.pi.splice(i, 1);
                break;
            }
        }
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
     * @param {object} data.sgo- Suggested game options
     * @param {string} data.sgo.soid - Suggested game options modification
     * @param {string} data.sgo.s - Game options modification suggester
     * @param {int} data.gid - Game ID
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
                console.log(data.gid + ":" + this.id);
                if (data.gid === this.id) this._receivedGameChatMessage(data);
                break;
            case "gpj":
                this.info.pi.push({"N": data.n, "sc": 0, "st": "si"});
                this._reloadScoreboard();
                Notifier.timeout(Notifier.INFO, "<b>" + data.n + "</b> joined the game!");
                break;
            case "gpl":
                this._removePlayer(data.n);
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
                this._removePlayer(data.n);
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
                const sgo = data.sgo;
                const noty = Notifier.show(Notifier.WARN, "<b>" + sgo.s + "</b> suggested to modify the game options.", false, false, false,
                    "SGO_" + sgo.soid,
                    Notifier.button("Accept", () => {
                        this.submitGameOptionsModificationDecision(sgo.soid, true, () => noty.close());
                    }),
                    Notifier.button("Decline", () => {
                        this.submitGameOptionsModificationDecision(sgo.soid, false, () => noty.close());
                    }));

                const self = this;
                noty.on('onTemplate', function () {
                    $(this.barDom).tooltipster({
                        content: () => self.generateGameOptionsDiffsFor(sgo),
                        theme: 'tooltipster-light'
                    });
                }).show();
                break;
        }
    }

    generateGameOptionsDiffsFor(sgo) {
        const div = $('#gameOptionsDiffTooltipTemplate').clone();

        const go = this.info.gi.go;

        const sl = div.find('._goal');
        if (sgo.go.sl === go.sl) {
            sl.hide();
        } else {
            sl.show();
            sl.html("<b>Goal:</b> " + go.sl + " &#8594; " + sgo.go.sl);
        }

        const pL = div.find('._playersLimit');
        if (sgo.go.pL === go.pL) {
            pL.hide();
        } else {
            pL.show();
            pL.html("<b>Players limit:</b> " + go.pL + " &#8594; " + sgo.go.pL);
        }

        const vL = div.find('._spectatorsLimit');
        if (sgo.go.vL === go.vL) {
            vL.hide();
        } else {
            vL.show();
            vL.html("<b>Spectators limit:</b> " + go.vL + " &#8594; " + sgo.go.vL);
        }

        const bl = div.find('._blanksLimit');
        if (sgo.go.bl === go.bl) {
            bl.hide();
        } else {
            bl.show();
            bl.html("<b>Blank cards:</b> " + go.bl + " &#8594; " + sgo.go.bl);
        }

        const tm = div.find('._timeMultiplier');
        if (sgo.go.tm === go.tm) {
            tm.hide();
        } else {
            tm.show();
            tm.html("<b>Time multiplier:</b> " + go.tm + " &#8594; " + sgo.go.tm);
        }

        const wb = div.find('._winBy');
        if (sgo.go.wb === go.wb) {
            wb.hide();
        } else {
            wb.show();
            wb.html("<b>Win by:</b> " + go.wb + " &#8594; " + sgo.go.wb);
        }

        const pw = div.find('._password');
        if (sgo.go.pw === go.pw) {
            pw.hide();
        } else {
            pw.show();
            pw.html("<b>Password:</b> " + go.pw + " &#8594; " + sgo.go.pw);
        }

        const decks = div.find('._decks');
        if (arraysEqual(sgo.go.css, go.css) && arraysEqual(sgo.go.CCs, go.CCs)) {
            decks.hide();
        } else {
            decks.show();

            let oldDecks = GameManager.deckIdsToNames(go.css);
            oldDecks = oldDecks.concat(GameManager.wrapCardcastDecks(go.CCs));

            let newDecks = GameManager.deckIdsToNames(sgo.go.css);
            newDecks = newDecks.concat(GameManager.wrapCardcastDecks(sgo.go.CCs));

            decks.html("<b>Decks:</b> " + oldDecks.join(", ") + " &#8594; " + newDecks.join(", "));
        }

        return div;
    }

    set submitGameOptionsModificationDecision_listener(set) {
        this._submitGameOptionsModificationDecision_listener = set;
    }

    submitGameOptionsModificationDecision(id, decision, done = undefined) {
        Requester.request("gosd", {
            "soid": id,
            "d": decision,
            "gid": this.id
        }, () => {
            if (done !== undefined) done();
            if (this._submitGameOptionsModificationDecision_listener !== undefined)
                this._submitGameOptionsModificationDecision_listener(id);
        }, (error) => {
            Notifier.error("Failed submitting decision on the suggested game options.", error);
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
    Requester.request("gme", {}, (meData) => {
        gameManager.me = meData;

        Requester.request("ggi", {
            "gid": gameManager.id
        }, (ggiData) => {
            gameManager.gameInfo = ggiData;
            Notifier.debug(ggiData);

            Requester.request("gc", {
                "gid": gameManager.id
            }, (gcData) => {
                registerPollListener("GAME", (ev) => {
                    Notifier.debug(ev);
                    gameManager.handlePollEvent(ev);
                });

                gameManager.blackCard = gcData.bc;
                gameManager.updateHandInfo(gcData.ltp, gcData.ltd);

                gameManager.addHandCards(gcData.h, true);
                gameManager.addTableCards(gcData.wc, true);

                gameManager.attachOptionsDialog = gameOptionsDialog;

                new OtherStuffManager(gameManager);

                Notifier.debug(gcData);
            }, (error) => {
                Notifier.error("Failed loading the game!", error);
            });
        }, (error) => {
            Notifier.error("Failed loading the game!", error);
        });
    }, (error) => {
        Notifier.error("Failed loading the game!", error);
    });
}

function getLastPathSegment() {
    return decodeURIComponent(new RegExp('[^\\/]+(?=\\/$|$)').exec(window.location.href) || [null, '']) || null;
}

function arraysEqual(a, b) {
    if (a === b) return true;
    if (a == null || b == null) return false;
    if (a.length !== b.length) return false;
    for (let i = 0; i < a.length; ++i) if (a[i] !== b[i]) return false;
    return true;
}
