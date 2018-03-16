class OtherStuffManager {

    /**
     * @param {GameManager} gameManager
     */
    constructor(gameManager) {
        this.root = $('#otherStuffLayout');
        this.gameManager = gameManager;

        this._setup();

        this.gameManager.submitGameOptionsModificationDecision_listener = (id) => this.removeSuggestedOptionsItem(id);

        eventsReceiver.register("GAME_OTHER_STUFF", (data) => {
            this._handleEvent(data);
        });
    }

    static get SPECTATORS() {
        return "spectatorsList";
    }

    static get SUGGESTED_GAME_OPTIONS() {
        return "suggestedGameOptions";
    }

    /**
     * @param {string} data.n - Nickname
     * @param {string} data.E - Event code
     * @param {object} data.sgo - Suggested game options
     * @private
     */
    _handleEvent(data) {
        switch (data.E) {
            case "goms":
                this.addSuggestedOptionsItem(data.sgo);
                break;
            case "gvj":
                this.addSpectator(data.n);
                break;
            case "gvl":
                this.removeSpectator(data.n);
                break;
        }
    }

    static _failedLoadingStuff(card, error) {
        card.find('.mdc-card__title').addClass('error');
        Notifier.debug(error, true);
    }

    shouldShow(id) {
        switch (id) {
            case OtherStuffManager.SUGGESTED_GAME_OPTIONS:
                return this.gameManager.amHost;
            case OtherStuffManager.SPECTATORS:
                return true;
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
            content: () => this.gameManager.generateGameOptionsDiffsFor(item),
            theme: 'tooltipster-light'
        });

        this.suggestedGameOptions_empty.hide();
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

                Requester.request("ggso", {
                    "gid": this.gameManager.id
                }, (data) => {
                    /** @param {object[]} data.sgo */
                    for (let i = 0; i < data.sgo.length; i++) this.addSuggestedOptionsItem(data.sgo[i])
                }, (error) => {
                    OtherStuffManager._failedLoadingStuff(card, error);
                });
                break;
            case OtherStuffManager.SPECTATORS:
                this.spectators_empty = card.find('.message');
                this.spectators_list = new List(card[0], {
                    item: 'spectatorTemplate',
                    valueNames: ['_name']
                });
                this.spectators_list.clear();

                for (let i = 0; i < this.gameManager.info.gi.V.length; i++)
                    this.addSpectator(this.gameManager.info.gi.V[i])

                if (this.gameManager.info.gi.V.length === 0) this.spectators_empty.show();
                else this.spectators_empty.hide();
                break;
        }
    }

    removeSpectator(name) {
        this.spectators_list.remove("_name", name);
        if (this.spectators_list.size() === 0) this.spectators_empty.show();
        else this.spectators_empty.hide();
    }

    addSpectator(name) {
        this.spectators_list.add({"_name": name});
        this.spectators_empty.hide();
    }

    _setup() {
        this.root.find('.mdc-card').each((i, elm) => {
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