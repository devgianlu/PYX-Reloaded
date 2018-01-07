function clearElement(elm) {
    while (elm.firstChild) {
        elm.removeChild(elm.firstChild);
    }
}

function populateDropdown(dropdown, dgo) {
    var list = dropdown.querySelector('.mdc-simple-menu__items');
    clearElement(list);
    for (var i = dgo.min; i <= dgo.max; i++) {
        var item = document.createElement("li");
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
    var list = dropdown.querySelector('.mdc-simple-menu__items');
    clearElement(list);
    for (var i = 0; i < tm.values.length; i++) {
        var item = document.createElement("li");
        var val = tm.values[i];
        item.className = "mdc-list-item";
        item.setAttribute("tabindex", "0");
        item.setAttribute("role", "option");
        if (val === tm.default) item.setAttribute("aria-selected", '');
        item.innerHTML = val;
        list.appendChild(item);
    }

    new mdc.select.MDCSelect(dropdown);
}

function toggleCardcastNoDecksMessage(container, visible) {
    container = $(container);
    var message = container.find('.message');
    var list = container.find('.list');
    if (visible) {
        message.show();
        list.hide();
    } else {
        message.hide();
        list.show();
    }
}

function setupCardcastDecks(container) {
    var list = container.querySelector('.mdc-list');
    clearElement(list);

    toggleCardcastNoDecksMessage(container, true);
}

function addCardcastDeck(container, deck) {
    var li = document.createElement("li");
    li.setAttribute("data-code", deck.code);
    li.className = "mdc-list-item";
    var text = document.createElement("span");
    text.className = "mdc-list-item__text";
    text.innerHTML = deck.name;
    var code = document.createElement("span");
    code.className = "mdc-list-item__secondary-text";
    code.innerHTML = deck.code;
    text.appendChild(code);
    li.appendChild(text);

    var remove = document.createElement("i");
    remove.className = "mdc-list-item__end-detail material-icons";
    remove.innerHTML = "delete";
    remove.onclick = function (ev) {
        removeCardcastDeck(container, deck.code);
    };
    li.appendChild(remove);

    var list = container.querySelector('.mdc-list');
    list.appendChild(li);

    toggleCardcastNoDecksMessage(container, false);
}

function removeCardcastDeck(container, code) {
    var list = container.querySelector('.mdc-list');
    for (var i = 0; i < list.children.length; i++) {
        var item = list.children[i];
        if (item.getAttribute("data-code") === code) {
            list.removeChild(item);
            break;
        }
    }

    toggleCardcastNoDecksMessage(container, list.children.length === 0);
}

function loadCardSets(container, css) {
    var setsList = [];
    for (var i = 0; i < css.length; i++) {
        var set = css[i];
        setsList.push({
            "_name": set.csn,
            "w": set.w,
            "cid": set.cid
        });
    }

    setsList.sort(function (a, b) {
        return a.w - b.w;
    });

    var sets = new List(container, {
        item: 'card-set-template',
        valueNames: ['_name', {'data': ['cid']}]
    });

    sets.clear();
    sets.add(setsList, function (items) {
        for (var i = 0; i < items.length; i++) {
            var cid = items[i].values().cid;
            var item = $(items[i].elm);

            item.find('input').attr("id", "deck_" + cid);
            item.find('label').attr("for", "deck_" + cid);
        }
    });
}

function getSelectedCardSets(container) {
    var list = container.querySelector('.list');
    var selected = [];
    for (var i = 0; i < list.children.length; i++) {
        var cardSet = list.children[i];
        var checkbox = cardSet.querySelector('input');
        if (checkbox.checked) {
            selected.push(cardSet.getAttribute("data-cid"));
        }
    }

    return selected;
}

function getDropdownSelectedValue(dropdown) {
    var list = dropdown.querySelector('.mdc-simple-menu__items');
    for (var i = 0; i < list.children.length; i++) {
        var item = list.children[i];
        if (item.hasAttribute("aria-selected"))
            return item.innerHTML;
    }

    return list.children[0].innerHTML; // Shouldn't happen
}

function resetCreateGameDialog() {
    var scoreLimitElm = document.getElementById('scoreLimit');
    var playersLimitElm = document.getElementById('playersLimit');
    var spectatorsLimitElm = document.getElementById('spectatorsLimit');
    var blanksLimitElm = document.getElementById('blanksLimit');
    var timeMultiplierElm = document.getElementById('timeMultiplier');
    var winByElm = document.getElementById('winBy');

    var dgo = JSON.parse(localStorage['dgo']);
    populateDropdown(scoreLimitElm, dgo.sl);
    populateDropdown(playersLimitElm, dgo.pL);
    populateDropdown(spectatorsLimitElm, dgo.vL);
    populateDropdown(blanksLimitElm, dgo.bl);
    populateTimeMultiplier(timeMultiplierElm, dgo.tm);
    populateDropdown(winByElm, dgo.wb);
    loadCardSets(document.getElementById('pyx_decks'), JSON.parse(localStorage['css']));

    var ccDecksElm = document.getElementById('cc_decks');
    setupCardcastDecks(ccDecksElm);
    addCardcastDeck(ccDecksElm, {"name": "Try this shit 123", "code": "ABC12"});
}
