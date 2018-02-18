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