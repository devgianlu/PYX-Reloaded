/**
 #1 - it will be better to have all user inputs in the modal done entirely in JS. Don't clutter
 lobbies.html more than it needs to be
 #2 - This is a work in progress. Most of the logic for the "New Game" dialog should go in here as well.
 #3 - Happy hacking!!
 **/

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
    loadCardSets(document.getElementById("deck_select"), JSON.parse(localStorage['css']));
}
