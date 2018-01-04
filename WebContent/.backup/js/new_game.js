/**
 #1 - it will be better to have all user inputs in the modal done entirely in JS. Don't clutter
 lobbies.html more than it needs to be
 #2 - This is a work in progress. Most of the logic for the "New Game" dialog should go in here as well.
 #3 - Happy hacking!!
 **/

function populateDropdown(dropdown, dgo) {
    for (var i = dgo.min; i <= dgo.max; i++) {
        var option = document.createElement("option");
        option.setAttribute("value", i);
        if (i === dgo.default) option.setAttribute("selected", '');
        option.innerHTML = i;
        dropdown.appendChild(option);
    }
}

function populateTimeMultiplier(dropdown, tm) {
    console.log(tm);

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

    var sets = new List('deck_select', {
        item: 'card-set-template',
        valueNames: ['_name', {'data': ['cid']}]
    });

    sets.clear();
    sets.add(setsList, function (items) {
        for (var i = 0; i < items.length; i++) {
            var cid = items[i].values().cid;
            var item = $(items[i].elm);

            //item.find('input.deck1').attr("id", "deck_" + cid);
            item.attr("for", "deck_" + cid);
        }
    })
}

