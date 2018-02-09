class MDCMultiSelect extends mdc.select.MDCSelect {

    constructor(root, foundation = undefined, ...args) {
        super(root, foundation, ...args);

        this.menu_.unlisten("MDCMenu:cancel", this.foundation_.cancelHandler_);
        this.menu_.unlisten("MDCMenu:selected", this.foundation_.selectionHandler_);
    }

    set uiListener(set) {
        this.listener = set;
    }

    get getSelectedItemNames() {
        const names = [];

        for (let i = 0; i < this.options.length; i++) {
            let item = this.options[i];
            if (item.querySelector('input').checked)
                names.push(item.querySelector('label').innerText);
        }

        return names;
    }

    // noinspection JSUnusedGlobalSymbols
    initialize(menuFactory) {
        super.initialize(menuFactory);

        this.menu_.listen("MDCMenu:selected", (evt) => {
            this.updateUI();
            this.foundation_.close_();
            evt.preventDefault();
        });
        this.menu_.listen("MDCMenu:cancel", (evt) => {
            this.updateUI();
            this.foundation_.close_();
            evt.preventDefault();
        });
    }

    updateUI() {
        const selected = this.getSelectedItemNames;
        this.selectedText_.textContent = selected.join(", ");

        if (selected.length === 0) this.label_.classList.remove("mdc-select__label--float-above");
        else this.label_.classList.add("mdc-select__label--float-above");

        this.listener();
    }

    clear() {
        this.selectedText_.textContent = "";
        this.label_.classList.remove("mdc-select__label--float-above")
    }
}

class CreateGameDialog {
    constructor(id) {
        this._dialog = $('#' + id);
        this.dialog = new mdc.dialog.MDCDialog(this._dialog[0]);
        this.dialog.listen('MDCDialog:accept', () => this._acceptDialog());

        this._scoreLimit = this._dialog.find('#scoreLimit');
        this._playersLimit = this._dialog.find('#playersLimit');
        this._spectatorsLimit = this._dialog.find('#spectatorsLimit');
        this._blanksLimit = this._dialog.find('#blanksLimit');
        this._timeMultiplier = this._dialog.find('#timeMultiplier');
        this._winBy = this._dialog.find('#winBy');

        this._pyxDecks = this._dialog.find('#pyxDecks');
        this.pyxDecks_select = new MDCMultiSelect(this._pyxDecks[0]);
        this.pyxDecks_select.uiListener = () => this.updateTitles();
        this.pyxDecks = new List(this._pyxDecks[0], {
            item: 'pyxDeckTemplate',
            valueNames: ['_name', {'data': ['csi']}]
        });

        this._cardcastDecks = this._dialog.find('#cardcastDecks');
        this._cardcastDecks_list = this._cardcastDecks.find('.list');
        this._cardcastDecks_message = this._cardcastDecks.find('.message');
        this.cardcastDecks = new List(this._cardcastDecks[0], {
            item: 'cardcastDeckTemplate',
            valueNames: ['_name', '_code', {'data': ['code']}]
        });

        this._cardcastAdd = this._dialog.find('#cardcastAdd');
        this._cardcastAddDeckCode = this._cardcastAdd.find('#cardcastAddDeckCode');
        this.cardcastAddDeckCode = new mdc.textField.MDCTextField(this._cardcastAddDeckCode.parent()[0]);
        this._cardcastAddDeckInfo = this._dialog.find('#cardcastAddDeckInfo');
        this._cardcastAddDeckInfo_loading = this._cardcastAddDeckInfo.find('.mdc-linear-progress');
        this._cardcastAddDeckInfo_details = this._cardcastAddDeckInfo.find('.details');

        this._cardsTitle = this._dialog.find('#cardsTitle');
        this._cardcastTitle = this._dialog.find('#cardcastDecksTitle');
        this._pyxTitle = this._dialog.find('#pyxDecksTitle');

        /**
         * @param {int} dgo.vL - Spectators limit
         * @param {int} dgo.sl - Score limit
         * @param {int} dgo.pL - Players limit
         * @param {int} dgo.wb - Win by
         * @param {int} dgo.tm - Time multiplier
         * @param {int} dgo.bl - Blank cards limit
         *
         * @type {object}
         */
        this.dgo = JSON.parse(localStorage['dgo']);

        /**
         * @param {string} css[].csn - Card set name
         * @param {int} css[].csi - Card set id
         *
         * @type {object[]}
         */
        this.css = JSON.parse(localStorage['css']);

        this._loadedCardcastDecks = {};

        this.reset();
    }

    /**
     *
     * @param dropdown
     * @param {object} tm
     * @param {string[]} tm.v - Possible values
     * @param {string} tm.def - Default value
     * @private
     */
    static _populateTimeMultiplier(dropdown, tm) {
        const list = dropdown.find('.mdc-menu__items');
        list.empty();

        for (let i = 0; i < tm.v.length; i++) {
            let item = document.createElement("li");
            let val = tm.v[i];
            item.className = "mdc-list-item";
            item.setAttribute("tabindex", "0");
            item.setAttribute("role", "option");
            if (val === tm.def) item.setAttribute("aria-selected", "true");
            item.innerHTML = val;
            list.append(item);
        }
    }

    /**
     *
     * @param dropdown
     * @param {object} dgo
     * @param {int} dgo.min - Minimum value
     * @param {int} dgo.max - Maximum value
     * @param {int} dgo.def - Default value
     * @private
     */
    static _populateDropdown(dropdown, dgo) {
        const list = dropdown.find('.mdc-menu__items');
        list.empty();

        for (let i = dgo.min; i <= dgo.max; i++) {
            let item = document.createElement("li");
            item.className = "mdc-list-item";
            item.setAttribute("tabindex", "0");
            item.setAttribute("role", "option");
            if (i === dgo.def) item.setAttribute("aria-selected", "true");
            item.innerHTML = i;
            list.append(item);
        }
    }

    static getDropdownSelectedValue(dropdown) {
        const list = dropdown.find('.mdc-menu__items');
        for (let i = 0; i < list.children().length; i++) {
            const item = $(list.children()[i]);
            if (item.attr("aria-selected"))
                return item.text();
        }

        console.log("DEFAULTING");
        return list.children()[0].innerHTML; // Shouldn't happen
    }

    static createDetailsString(decks, whites, blacks) {
        if (decks === 0) {
            return "0 decks";
        } else {
            return decks + (decks === 1 ? " deck, " : " decks, ")
                + whites + (whites === 1 ? " white card, " : " white cards, ")
                + blacks + (blacks === 1 ? " black card" : " black cards");
        }
    }

    _acceptDialog() {
        const go = {
            "vL": CreateGameDialog.getDropdownSelectedValue(this._spectatorsLimit),
            "pL": CreateGameDialog.getDropdownSelectedValue(this._playersLimit),
            "sl": CreateGameDialog.getDropdownSelectedValue(this._scoreLimit),
            "bl": CreateGameDialog.getDropdownSelectedValue(this._blanksLimit),
            "tm": CreateGameDialog.getDropdownSelectedValue(this._timeMultiplier),
            "wb": CreateGameDialog.getDropdownSelectedValue(this._winBy),
            "CCs": this.getCardcastDeckCodes(),
            "css": this.getSelectedPyxDecks()
        };

        Notifier.debug(go);
        games.createGame(go); // Reference to lobbies.js
    }

    show() {
        this.reset();
        this.dialog.show();
    }

    reset() {
        // Gameplay
        CreateGameDialog._populateDropdown(this._scoreLimit, this.dgo.sl);
        CreateGameDialog._populateDropdown(this._playersLimit, this.dgo.pL);
        CreateGameDialog._populateDropdown(this._spectatorsLimit, this.dgo.vL);
        CreateGameDialog._populateDropdown(this._blanksLimit, this.dgo.bl);
        CreateGameDialog._populateTimeMultiplier(this._timeMultiplier, this.dgo.tm);
        CreateGameDialog._populateDropdown(this._winBy, this.dgo.wb);

        // PYX
        this.pyxDecks_select.clear();
        this.pyxDecks.clear();
        for (let i = 0; i < this.css.length; i++) {
            const set = this.css[i];
            const elm = $(this.pyxDecks.add({
                "_name": set.csn,
                "w": set.w,
                "csi": set.csi
            })[0].elm);

            elm.find('input').attr("id", "pyx_deck_" + set.csi);
            elm.find('label').attr("for", "pyx_deck_" + set.csi);
        }

        // Cardcast
        this.cardcastDecks.clear();
        this.toggleCardcastNoDecksMessage(true);
        this._resetAddCardcast();

        // Titles
        this.updateTitles();
    }

    getSelectedPyxDecks() {
        const selected = [];
        this._pyxDecks.find('.list').children().each(function () {
            const input = $(this).find('input');
            if (input.prop('checked')) selected.push($(this).attr('data-csi'))

        });

        return selected;
    }

    updateTitles() {
        const pyxDetails = this.getPyxDecksDetails();
        this._pyxTitle.text("PYX (" + CreateGameDialog.createDetailsString(pyxDetails.decks, pyxDetails.whites, pyxDetails.blacks) + ")");

        const ccDetails = this.getCardcastDecksDetails();
        this._cardcastTitle.text("Cardcast (" + CreateGameDialog.createDetailsString(ccDetails.decks, ccDetails.whites, ccDetails.blacks) + ")");

        this._cardsTitle.text("Cards (" + CreateGameDialog.createDetailsString(pyxDetails.decks + ccDetails.decks, pyxDetails.whites + ccDetails.whites, pyxDetails.blacks + ccDetails.blacks) + ")");
    }

    getPyxDecksDetails() {
        const pyxIds = this.getSelectedPyxDecks();

        let whites = 0;
        let blacks = 0;

        for (let i = 0; i < this.css.length; i++) {
            /**
             * @param {int} cardSet.wcid - White cards in deck
             * @param {int} cardSet.bcid - Black cards in deck
             * @type {object}
             */
            const cardSet = this.css[i];
            for (let j = 0; j < pyxIds.length; j++) {
                if (cardSet.csi.toString() === pyxIds[j]) {
                    whites += cardSet.wcid;
                    blacks += cardSet.bcid;
                }
            }
        }

        return {"decks": pyxIds.length, "whites": whites, "blacks": blacks}
    }

    // *******************
    // Cardcast stuff
    // *******************

    getCardcastDecksDetails() {
        const deckCodes = this.getCardcastDeckCodes();

        let whites = 0;
        let blacks = 0;

        for (let i = 0; i < deckCodes.length; i++) {
            const deck = this._loadedCardcastDecks[deckCodes[i]];
            whites += deck.response_count;
            blacks += deck.call_count;
        }

        return {"decks": deckCodes.length, "whites": whites, "blacks": blacks}
    }

    _resetAddCardcast() {
        this.cardcastAddDeckCode.valid = true;
        this._cardcastAddDeckCode.val("");
        this._cardcastAddDeckCode.next().removeClass("mdc-text-field__label--float-above");

        this._cardcastAddDeckInfo_loading.hide();
        this._cardcastAddDeckInfo_details.hide();
    }

    toggleCardcastNoDecksMessage(visible) {
        if (visible) {
            this._cardcastDecks_list.hide();
            this._cardcastDecks_message.show();
        } else {
            this._cardcastDecks_list.show();
            this._cardcastDecks_message.hide();
        }
    }

    getCardcastDeckCodes() {
        const codes = [];
        for (let i = 0; i < this.cardcastDecks.items.length; i++)
            codes.push(this.cardcastDecks.items[i]._values.code);
        return codes;
    }

    loadCardcastDeckInfo() {
        const code = this._cardcastAddDeckCode.val();
        this._cardcastAddDeckInfo_loading.show();
        this._cardcastAddDeckInfo_details.hide();

        if (code.match(/[A-Z0-9]{5}/) !== null) {
            const self = this;
            new Cardcast(code).info(function (info, error) {
                self._cardcastAddDeckInfo_loading.hide();
                if (info === null) {
                    Notifier.debug(error);
                    self._cardcastAddDeckInfo_details.hide();
                    self.cardcastAddDeckCode.valid = false;
                    return;
                }

                self._loadedCardcastDecks[info.code] = info;

                self._cardcastAddDeckInfo_details.show();
                self.cardcastAddDeckCode.valid = true;

                self._cardcastAddDeckInfo_details.attr("data-code", info.code);

                self._cardcastAddDeckInfo_details.find('.\_name').text(info.name);
                self._cardcastAddDeckInfo_details.find('.\_author').text("by " + info.author.username);
            });
        } else {
            this._cardcastAddDeckInfo_loading.hide();
            this._cardcastAddDeckInfo_details.hide();
            this.cardcastAddDeckCode.valid = false;
        }
    }

    addCardcastDeck(code) {
        const deck = this._loadedCardcastDecks[code]; // The deck must have been loaded
        if (deck === undefined) return;

        if (this.cardcastDecks.get("code", code).length === 0) {
            this.cardcastDecks.add({
                "_name": deck.name,
                "_code": deck.code,
                "code": deck.code
            });
        }

        this.toggleCardcastNoDecksMessage(false);
        this._resetAddCardcast();
        this.updateTitles();
    }

    removeCardcastDeck(code) {
        this.cardcastDecks.remove("code", code);
        this.toggleCardcastNoDecksMessage(this.cardcastDecks.size() === 0);
        this.updateTitles();
    }
}

const createGameDialog = new CreateGameDialog('createGameDialog');

function showCreateGameDialog() {
    createGameDialog.show();
}

function loadCardcastDeckInfo() {
    createGameDialog.loadCardcastDeckInfo();
}

function removeCardcastDeck(button) {
    createGameDialog.removeCardcastDeck(button.parentElement.parentElement.getAttribute("data-code"));
}

function addCardcastDeck(button) {
    createGameDialog.addCardcastDeck(button.parentElement.getAttribute("data-code"));
}
