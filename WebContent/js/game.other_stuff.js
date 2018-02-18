class OtherStuffManager {

    /**
     * @param {GameManager} gameManager
     */
    constructor(gameManager) {
        this.root = $('#otherStuffLayout');
        this.gameManager = gameManager;

        this._setup();

        this.gameManager.submitGameOptionsModificationDecision_listener = (id) => this.removeSuggestedOptionsItem(id);

        registerPollListener("GAME_OTHER_STUFF", (data) => {
            this._handleEvent(data);
        });
    }

    _handleEvent(data) {
        switch (data.E) {
            case "goms":
                this.addSuggestedOptionsItem(data.sgo);
                break;
        }
    }

    static get SUGGESTED_GAME_OPTIONS() {
        return "suggestedGameOptions";
    }

    static _failedLoadingStuff(card, data) {
        card.find('.mdc-card__title').addClass('error');
        Notifier.debug(data, true);
    }

    shouldShow(id) {
        switch (id) {
            case OtherStuffManager.SUGGESTED_GAME_OPTIONS:
                return this.gameManager.amHost;
        }

        return false;
    }

    removeSuggestedOptionsItem(id) {
        this.suggestedGameOptions_list.remove("id", id);
        Noty.closeAll("SGO_" + id);

        if (this.suggestedGameOptions_list.size() === 0) this.suggestedGameOptions_empty.show();
        else this.suggestedGameOptions_empty.hide();
    }

    /**
     * @param {string} item.s - Suggester
     * @param {object} item.go - Game options
     * @param {string} item.soid - ID
     */
    addSuggestedOptionsItem(item) {
        const elm = $(this.suggestedGameOptions_list.add({
            "id": item.soid,
            "_suggester": item.s
        })[0].elm);

        elm.find('._accept').on('click', () => {
            this.gameManager.submitGameOptionsModificationDecision(item.soid, true, () => this.removeSuggestedOptionsItem(item.soid));
        });

        elm.find('._decline').on('click', () => {
            this.gameManager.submitGameOptionsModificationDecision(item.soid, false, () => this.removeSuggestedOptionsItem(item.soid));
        });

        elm.find('._info').tooltipster({
            content: () => this._generateGameOptionsDiffsFor(item),
            theme: 'tooltipster-light'
        });

        this.suggestedGameOptions_empty.hide();
    }

    static wrapCardcastDecks(CCs) {
        const names = [];
        for (let i = 0; i < CCs.length; i++) names[i] = "<i>" + CCs[i] + "</i>";
        return names;
    }

    static deckIdsToNames(ids) {
        const names = [];
        const css = localStorage["css"];
        if (css === undefined) return ids; // Shouldn't happen
        const json = JSON.parse(css);

        for (let i = 0; i < ids.length; i++) {
            for (let j = 0; j < json.length; j++) {
                if (ids[i] === json[j].csi) names[i] = json[j].csn;
            }
        }

        return names;
    }

    _generateGameOptionsDiffsFor(sgo) {
        const div = $('#gameOptionsDiffTooltipTemplate').clone();

        const go = this.gameManager.info.gi.go;

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

            let oldDecks = OtherStuffManager.deckIdsToNames(go.css);
            oldDecks = oldDecks.concat(OtherStuffManager.wrapCardcastDecks(go.CCs));

            let newDecks = OtherStuffManager.deckIdsToNames(sgo.go.css);
            newDecks = newDecks.concat(OtherStuffManager.wrapCardcastDecks(sgo.go.CCs));

            decks.html("<b>Decks:</b> " + oldDecks.join(", ") + " &#8594; " + newDecks.join(", "));
        }

        return div;
    }

    /**
     * One time setup method
     *
     * @param {string} id - HTML ID
     * @param {object} card - jQuery element
     */
    setupCard(id, card) {
        switch (id) {
            case OtherStuffManager.SUGGESTED_GAME_OPTIONS:
                this.suggestedGameOptions_empty = card.find('.message');
                this.suggestedGameOptions_list = new List(card[0], {
                    item: 'suggestedGameOptionsTemplate',
                    valueNames: ['_suggester', {data: ['id']}]
                });
                this.suggestedGameOptions_list.clear();

                $.post("/AjaxServlet", "o=ggso&gid=" + this.gameManager.id).done((data) => {
                    /** @param {object[]} data.sgo */
                    for (let i = 0; i < data.sgo.length; i++) this.addSuggestedOptionsItem(data.sgo[i])
                }).fail((data) => {
                    OtherStuffManager._failedLoadingStuff(card, data);
                });
                break;
        }
    }

    _setup() {
        this.root.children('.mdc-card').each((i, elm) => {
            elm = $(elm);
            const id = elm.attr('id');
            if (this.shouldShow(id)) {
                this.setupCard(id, elm);
                elm.show();
            } else {
                elm.hide();
            }
        });
    }
}