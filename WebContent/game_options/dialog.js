class MDCMultiSelect extends mdc.select.MDCSelect {

    constructor(root, foundation = undefined, ...args) {
        super(root, foundation, ...args);

        this.menu_.unlisten("MDCMenu:cancel", this.foundation_.cancelHandler_);
        this.menu_.unlisten("MDCMenu:selected", this.foundation_.selectionHandler_);
    }

    set uiListener(set) {
        this.listener = set;
    }

    /**
     * @returns {array}
     */
    get selectedItems() {
        const names = [];

        for (let i = 0; i < this.options.length; i++) {
            const item = this.options[i];
            if (item.querySelector('input').checked)
                names.push(item.querySelector('label').innerText);
        }

        return names;
    }

    /**
     * @param {array} items
     */
    set selectedItems(items) {
        for (let i = 0; i < this.options.length; i++) {
            const item = this.options[i];
            item.querySelector('input').checked = MDCMultiSelect._contains(items, item);
        }

        this.updateUI();
    }

    static _contains(items, item) {
        for (let i = 0; i < items.length; i++) {
            if (items[i].toString() === item.getAttribute('data-csi'))
                return true;
        }

        return false;
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
        const selected = this.selectedItems;

        if (selected.length === 0) {
            this.label_.classList.remove("mdc-select__label--float-above");
        } else {
            this.selectedText_.textContent = selected.join(", ");
            this.label_.classList.add("mdc-select__label--float-above");
        }

        this.listener();
    }

    clear() {
        this.selectedText_.textContent = "";
        this.label_.classList.remove("mdc-select__label--float-above")
    }
}

class GameOptionsDialog {
    constructor(id, title, acceptText, acceptListener) {
        this._dialog = $('#' + id);
        this.dialog = new mdc.dialog.MDCDialog(this._dialog[0]);
        this.dialog.listen('MDCDialog:accept', () => this._acceptDialog());

        this._dialog.find('.mdc-dialog__header__title').text(title);

        this._accept = this._dialog.find('.mdc-dialog__footer__button--accept');
        this.acceptText = acceptText;

        this.scoreLimit = new mdc.select.MDCSelect(this._dialog.find('#scoreLimit')[0]);
        this.playersLimit = new mdc.select.MDCSelect(this._dialog.find('#playersLimit')[0]);
        this.spectatorsLimit = new mdc.select.MDCSelect(this._dialog.find('#spectatorsLimit')[0]);
        this.blanksLimit = new mdc.select.MDCSelect(this._dialog.find('#blanksLimit')[0]);
        this.timeMultiplier = new mdc.select.MDCSelect(this._dialog.find('#timeMultiplier')[0]);
        this.winBy = new mdc.select.MDCSelect(this._dialog.find('#winBy')[0]);

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
        this._cardcastAddDeckCode.parent().find('.mdc-text-field__icon').on('click', () => this.loadCardcastDeckInfo(this._cardcastAddDeckCode.val()));
        this.cardcastAddDeckCode = new mdc.textField.MDCTextField(this._cardcastAddDeckCode.parent()[0]);
        this._cardcastAddDeckInfo = this._dialog.find('#cardcastAddDeckInfo');
        this._cardcastAddDeckInfo_loading = this._cardcastAddDeckInfo.find('.mdc-linear-progress');
        this._cardcastAddDeckInfo_details = this._cardcastAddDeckInfo.find('.details');
        this._cardcastAddDeckButton = this._cardcastAddDeckInfo.find('.details .mdc-button');
        this._cardcastAddDeckButton.on('click', () => this.addCardcastDeck(this._cardcastAddDeckInfo_details.data('code')));

        this._cardsTitle = this._dialog.find('#cardsTitle');
        this._cardcastTitle = this._dialog.find('#cardcastDecksTitle');
        this._pyxTitle = this._dialog.find('#pyxDecksTitle');

        this._password = this._dialog.find('#gamePassword');
        mdc.textField.MDCTextField.attachTo(this._password.parent()[0]);

        this.acceptListener = acceptListener;

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
        const list = $(dropdown.root_).find('.mdc-menu__items');
        list.empty();

        let selected = 0;
        for (let i = 0; i < tm.v.length; i++) {
            let item = document.createElement("li");
            let val = tm.v[i];
            item.className = "mdc-list-item";
            item.setAttribute("tabindex", "0");
            item.setAttribute("role", "option");
            if (val === tm.def) selected = i;
            item.innerHTML = val;
            list.append(item);
        }

        dropdown.selectedIndex = selected;
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
        const list = $(dropdown.root_).find('.mdc-menu__items');
        list.empty();

        let selected = 0;
        for (let i = dgo.min; i <= dgo.max; i++) {
            let item = document.createElement("li");
            item.className = "mdc-list-item";
            item.setAttribute("tabindex", "0");
            item.setAttribute("role", "option");
            if (i === dgo.def) selected = i - dgo.min; // Gives us the index
            item.innerHTML = i.toString();
            list.append(item);
        }

        dropdown.selectedIndex = selected;
    }

    static setDropdownSelectedValue(dropdown, value) {
        const list = $(dropdown.root_).find('.mdc-menu__items');

        let selected = 0;
        for (let i = 0; i < list.children().length; i++) {
            const item = $(list.children()[i]);
            if (item.text() === value.toString()) selected = i;
        }

        dropdown.selectedIndex = selected;
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

    static getDropdownSelectedValue(dropdown) {
        const list = $(dropdown.root_).find('.mdc-menu__items');
        return list.children()[dropdown.selectedIndex].innerText;
    }

    _acceptDialog() {
        const go = {
            "vL": GameOptionsDialog.getDropdownSelectedValue(this.spectatorsLimit),
            "pL": GameOptionsDialog.getDropdownSelectedValue(this.playersLimit),
            "sl": GameOptionsDialog.getDropdownSelectedValue(this.scoreLimit),
            "bl": GameOptionsDialog.getDropdownSelectedValue(this.blanksLimit),
            "tm": GameOptionsDialog.getDropdownSelectedValue(this.timeMultiplier),
            "wb": GameOptionsDialog.getDropdownSelectedValue(this.winBy),
            "pw": this.getPassword(),
            "CCs": this.getCardcastDeckCodes(),
            "css": this.getSelectedPyxDecks()
        };

        Notifier.debug(go);
        this.acceptListener(go);
    }

    set acceptText(text) {
        this._accept.text(text);
    }

    show(reset = false) {
        if (reset) this.reset();
        this.dialog.show();
    }

    updateOptions(go) {
        if (go === undefined) return;

        // Gameplay
        GameOptionsDialog.setDropdownSelectedValue(this.scoreLimit, go.sl);
        GameOptionsDialog.setDropdownSelectedValue(this.playersLimit, go.pL);
        GameOptionsDialog.setDropdownSelectedValue(this.spectatorsLimit, go.vL);
        GameOptionsDialog.setDropdownSelectedValue(this.blanksLimit, go.bl);
        GameOptionsDialog.setDropdownSelectedValue(this.timeMultiplier, go.tm);
        GameOptionsDialog.setDropdownSelectedValue(this.winBy, go.wb);

        // Access
        if (go.pw.length > 0) {
            this._password.next().addClass("mdc-text-field__label--float-above");
            this._password.val(go.pw);
        } else {
            this._password.next().removeClass("mdc-text-field__label--float-above");
            this._password.val("");
        }

        // PYX
        this.pyxDecks_select.selectedItems = go.css;

        // Cardcast
        for (let i = 0; i < go.CCs.length; i++) {
            const code = go.CCs[i];
            this.loadCardcastDeckInfo(code, () => this.addCardcastDeck(code));
        }
    }

    reset() {
        // Gameplay
        GameOptionsDialog._populateDropdown(this.scoreLimit, this.dgo.sl);
        GameOptionsDialog._populateDropdown(this.playersLimit, this.dgo.pL);
        GameOptionsDialog._populateDropdown(this.spectatorsLimit, this.dgo.vL);
        GameOptionsDialog._populateDropdown(this.blanksLimit, this.dgo.bl);
        GameOptionsDialog._populateTimeMultiplier(this.timeMultiplier, this.dgo.tm);
        GameOptionsDialog._populateDropdown(this.winBy, this.dgo.wb);

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

            mdc.checkbox.MDCCheckbox.attachTo(elm[0]);

            elm.find('input').attr("id", "pyx_deck_" + set.csi);
            elm.find('label').attr("for", "pyx_deck_" + set.csi);
        }

        // Access
        this._password.val("");
        this._password.next().removeClass("mdc-text-field__label--float-above");

        // Cardcast
        this.cardcastDecks.clear();
        this.toggleCardcastNoDecksMessage(true);
        this._resetAddCardcast();

        // Titles
        this.updateTitles();
    }

    getPassword() {
        return this._password.val();
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
        this._pyxTitle.text("PYX (" + GameOptionsDialog.createDetailsString(pyxDetails.decks, pyxDetails.whites, pyxDetails.blacks) + ")");

        const ccDetails = this.getCardcastDecksDetails();
        this._cardcastTitle.text("Cardcast (" + GameOptionsDialog.createDetailsString(ccDetails.decks, ccDetails.whites, ccDetails.blacks) + ")");

        this._cardsTitle.text("Cards (" + GameOptionsDialog.createDetailsString(pyxDetails.decks + ccDetails.decks, pyxDetails.whites + ccDetails.whites, pyxDetails.blacks + ccDetails.blacks) + ")");
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

    loadCardcastDeckInfo(code, loaded = undefined) {
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

                if (loaded !== undefined) loaded();
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
            const elm = $(this.cardcastDecks.add({
                "_name": deck.name,
                "_code": deck.code,
                "code": deck.code
            })[0].elm);

            const self = this;
            elm.find('._remove').on('click', function () {
                self.removeCardcastDeck(deck.code);
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

function setupGameOptionsDialog(trigger, title, resetBeforeShow = false, acceptText, acceptListener, done = undefined) {
    $.get("/game_options/dialog.html").done(function (data) {
        $('body').append($(data));

        const gameOptionsDialog = new GameOptionsDialog('gameOptionsDialog', title, acceptText, acceptListener);
        if (done !== undefined) done(gameOptionsDialog);
        trigger.on('click', () => gameOptionsDialog.show(resetBeforeShow))
    }).fail(function (data) {
        Notifier.debug(data, true); // Shouldn't happen!!
    });
}