function clearElement(elm) {
    while (elm.firstChild) {
        elm.removeChild(elm.firstChild);
    }
}

function populateDropdown(dropdown, dgo) {
    const list = dropdown.querySelector('.mdc-simple-menu__items');
    clearElement(list);
    for (let i = dgo.min; i <= dgo.max; i++) {
        let item = document.createElement("li");
        item.className = "mdc-list-item";
        item.setAttribute("tabindex", "0");
        item.setAttribute("role", "option");
        if (i === dgo.default) item.setAttribute("aria-selected", '');
        item.innerHTML = i;
        list.appendChild(item);
    }

    new mdc.select.MDCSelect(dropdown);
}

function populateTimeMultiplier(dropdown, tm) {
    const list = dropdown.querySelector('.mdc-simple-menu__items');
    clearElement(list);
    for (let i = 0; i < tm.values.length; i++) {
        let item = document.createElement("li");
        let val = tm.values[i];
        item.className = "mdc-list-item";
        item.setAttribute("tabindex", "0");
        item.setAttribute("role", "option");
        if (val === tm.default) item.setAttribute("aria-selected", '');
        item.innerHTML = val;
        list.appendChild(item);
    }

    new mdc.select.MDCSelect(dropdown);
}

function loadCardSets(container, css) {
    const setsList = [];
    for (let i = 0; i < css.length; i++) {
        let set = css[i];
        setsList.push({
            "_name": set.csn,
            "w": set.w,
            "cid": set.cid
        });
    }

    setsList.sort(function (a, b) {
        return a.w - b.w;
    });

    const sets = new List(container, {
        item: 'card-set-template',
        valueNames: ['_name', {'data': ['cid']}]
    });

    sets.clear();
    sets.add(setsList, function (items) {
        for (let i = 0; i < items.length; i++) {
            let cid = items[i].values().cid;
            let item = $(items[i].elm);

            item.find('input').attr("id", "pyx_deck_" + cid);
            item.find('label').attr("for", "pyx_deck_" + cid);
        }
    });
}

function getSelectedCardSets(container) {
    const list = container.querySelector('.list');
    let selected = [];
    for (let i = 0; i < list.children.length; i++) {
        let cardSet = list.children[i];
        let checkbox = cardSet.querySelector('input');
        if (checkbox.checked) {
            selected.push(cardSet.getAttribute("data-cid"));
        }
    }

    return selected;
}

function getDropdownSelectedValue(dropdown) {
    const list = dropdown.querySelector('.mdc-simple-menu__items');
    for (let i = 0; i < list.children.length; i++) {
        let item = list.children[i];
        if (item.hasAttribute("aria-selected"))
            return item.innerHTML;
    }

    return list.children[0].innerHTML; // Shouldn't happen
}

function resetCreateGameDialog() {
    const scoreLimitElm = document.getElementById('scoreLimit');
    const playersLimitElm = document.getElementById('playersLimit');
    const spectatorsLimitElm = document.getElementById('spectatorsLimit');
    const blanksLimitElm = document.getElementById('blanksLimit');
    const timeMultiplierElm = document.getElementById('timeMultiplier');
    const winByElm = document.getElementById('winBy');

    const dgo = JSON.parse(localStorage['dgo']);
    populateDropdown(scoreLimitElm, dgo.sl);
    populateDropdown(playersLimitElm, dgo.pL);
    populateDropdown(spectatorsLimitElm, dgo.vL);
    populateDropdown(blanksLimitElm, dgo.bl);
    populateTimeMultiplier(timeMultiplierElm, dgo.tm);
    populateDropdown(winByElm, dgo.wb);
    loadCardSets(document.getElementById('pyx_decks'), JSON.parse(localStorage['css']));

    const ccDecksElm = document.getElementById('cc_decks');
    setupCardcastDecks(ccDecksElm);
    resetLoadCardcast();
}

function resetLoadCardcast() {
    const container = document.getElementById('cc_add');
    const inputElm = container.querySelector('#cc_deck_code');
    const input = new mdc.textField.MDCTextField(inputElm.parentElement);
    input.valid = true;
    $(inputElm.nextElementSibling).removeClass("mdc-text-field__label--float-above");
    inputElm.value = "";

    $(container.querySelector('.mdc-linear-progress')).hide();
    $(container.querySelector('#deck_info_details')).hide();
}

function toggleCardcastNoDecksMessage(container, visible) {
    container = $(container);
    const message = container.find('.message');
    const list = container.find('.list');
    if (visible) {
        message.show();
        list.hide();
    } else {
        message.hide();
        list.show();
    }
}

function setupCardcastDecks(container) {
    clearElement(container.querySelector('.mdc-list'));
    toggleCardcastNoDecksMessage(container, true);
}

function removeCardcastDeckFromLayout(container, code) {
    const list = container.querySelector('.mdc-list');
    for (let i = 0; i < list.children.length; i++) {
        let item = list.children[i];
        if (item.getAttribute("data-code") === code) {
            list.removeChild(item);
            break;
        }
    }

    toggleCardcastNoDecksMessage(container, list.children.length === 0);
}

function addCardcastDeckToLayout(button) {
    const deck = _loadedCardcastDecks[button.parentElement.getAttribute("data-code")]; // The deck must have been loaded
    if (deck === undefined) return;

    const container = document.getElementById('cc_decks');
    const list = container.querySelector('.mdc-list');
    for (let i = 0; i < list.children.length; i++) {
        if (list.children[i].getAttribute("data-code") === deck.code)
            return;
    }

    const li = document.createElement("li");
    li.setAttribute("data-code", deck.code);
    li.className = "mdc-list-item";
    const text = document.createElement("span");
    text.className = "mdc-list-item__text";
    text.innerHTML = deck.name;
    const code = document.createElement("span");
    code.className = "mdc-list-item__secondary-text";
    code.innerHTML = deck.code;
    text.appendChild(code);
    li.appendChild(text);

    const remove = document.createElement("i");
    remove.className = "mdc-list-item__end-detail material-icons";
    remove.innerHTML = "delete";
    remove.onclick = function () {
        removeCardcastDeckFromLayout(container, deck.code);
    };
    li.appendChild(remove);

    list.appendChild(li);

    toggleCardcastNoDecksMessage(container, false);
}

const _loadedCardcastDecks = {};

function loadCardcastDeckInfo(button) {
    const container = document.getElementById('deck_info');
    const inputElm = button.parentElement.querySelector('input');
    const input = new mdc.textField.MDCTextField(inputElm.parentElement);
    const loading = $(container.querySelector('.mdc-linear-progress'));
    const details = $(container.querySelector('#deck_info_details'));
    loading.show();
    details.hide();

    const code = inputElm.value;
    if (code.match(/[A-Z0-9]{5}/) !== null) {
        let cc = new Cardcast(code);
        cc.info(function (info) {
            loading.hide();
            if (info === undefined) {
                details.hide();
                input.valid = false;
                return;
            }

            _loadedCardcastDecks[info.code] = info;

            details.show();
            input.valid = true;

            details.attr("data-code", info.code);

            details.find('.\_name').text(info.name);
            details.find('.\_code').text(info.code);

            // TODO: Add more details
        });
    } else {
        details.hide();
        loading.hide();
        input.valid = false;
    }
}

function getCardcastDeckCodes(container) {
    const codes = [];
    const list = container.querySelector('.mdc-list');
    for (let i = 0; i < list.children.length; i++) {
        codes.push(list.children[i].getAttribute("data-code"));
    }

    return codes;
}

/*
{
  "name": "By Assholes, For Assholes (Updated Regularly)",
  "code": "SBYGD",
  "description": "If you want to meet the fucked up people who made this deck for some reason. Join our discord\nhttps://discord.gg/9e9Gtvv\n\nWe do take card suggestions there, but only the worst make it through.\n\nWe never expected anyone but us to use this deck, so any shitty inside jokes that I haven't removed tell us about them so I can kill them. \n\nI know this deck is past its prime but we still want to improve it in any way we can for the people who still play with it. So we will keep adding as long as we keep thinking of fucked up stuff. If You have fucked up ideas you want us to add tell us on our discord.\n\nYou're all terrible people, but we made this deck so we aren't in any place to judge.",
  "unlisted": false,
  "created_at": "2015-02-18T05:27:33+00:00",
  "updated_at": "2017-12-29T10:44:57+00:00",
  "external_copyright": false,
  "copyright_holder_url": null,
  "category": "random",
  "call_count": "89",
  "response_count": "421",
  "author": {
    "id": "b26fdc21-3189-41a7-933b-b196aa22f7ec",
    "username": "LordFapplesauce"
  },
  "rating": "4.6"
}
 */
