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
    clearElement(dropdown);
    for (var i = dgo.min; i <= dgo.max; i++) {
        var option = document.createElement("option");
        option.setAttribute("value", i);
        if (i === dgo.default) option.setAttribute("selected", '');
        option.innerHTML = i;
        dropdown.appendChild(option);
    }
}

function populateTimeMultiplier(dropdown, tm) {
    clearElement(dropdown);
    for (var i = 0; i < tm.values.length; i++) {
        var option = document.createElement("option");
        var val = tm.values[i];
        option.setAttribute("value", val);
        if (val === tm.default) option.setAttribute("selected", '');
        option.innerHTML = val;
        dropdown.appendChild(option);
    }
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
    return dropdown.options[dropdown.selectedIndex].value;
}

function resetCreateGameDialog() {
    var dgo = JSON.parse(localStorage['dgo']);
    populateDropdown(document.getElementById("scoreLimit"), dgo.sl);
    populateDropdown(document.getElementById("playersLimit"), dgo.pL);
    populateDropdown(document.getElementById("spectatorsLimit"), dgo.vL);
    populateDropdown(document.getElementById("blanksLimit"), dgo.bl);
    populateTimeMultiplier(document.getElementById("timeMultiplier"), dgo.tm);
    loadCardSets(document.getElementById("deck_select"), JSON.parse(localStorage['css']));
}
