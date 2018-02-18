class OtherStuffManager {

    /**
     * @param {GameManager} gameManager
     */
    constructor(gameManager) {
        this.root = $('#otherStuffLayout');
        this.gameManager = gameManager;

        this._setup();
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

    setupCard(id, card) {
        switch (id) {
            case OtherStuffManager.SUGGESTED_GAME_OPTIONS:
                $.post("/AjaxServlet", "o=ggso&gid=" + this.gameManager.id).done((data) => {
                    /**
                     * @param {object[]} data.sol
                     * @param {string} data.sol[].s - Suggester
                     * @param {string} data.sol[].soid - ID
                     */

                    const list = new List(card[0], {
                        item: 'suggestedGameOptionsTemplate',
                        valueNames: ['_suggester', {data: ['id']}]
                    });

                    list.clear();
                    for (let i = 0; i < data.sol.length; i++) {
                        const item = data.sol[i];
                        const elm = $(list.add({
                            "id": item.soid,
                            "_suggester": item.s
                        })[0].elm);

                        elm.find('._accept').on('click', () => {
                            gameManager.submitGameOptionsModificationDecision(item.soid, true);
                            this.setupCard(id, card);
                        });

                        elm.find('._decline').on('click', () => {
                            gameManager.submitGameOptionsModificationDecision(item.soid, false);
                            this.setupCard(id, card);
                        });
                    }

                    console.log(data);
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