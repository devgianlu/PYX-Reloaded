let games = new List('games-global-container', {
    item: 'game-info-template',
    valueNames: ['_host', '_players', '_spectators', '_goal', '_status', '_decks', '_likes', '_dislikes',
        {'data': ['gid', 'hp', 'like', 'dislike']}]
});

let createGameDialog = new mdc.dialog.MDCDialog(document.getElementById('createGameDialog'));
createGameDialog.listen('MDCDialog:accept', function () {
    let scoreLimit = getDropdownSelectedValue(document.getElementById('scoreLimit'));
    let playerLimit = getDropdownSelectedValue(document.getElementById('playersLimit'));
    let spectatorLimit = getDropdownSelectedValue(document.getElementById('spectatorsLimit'));
    let blanksLimit = getDropdownSelectedValue(document.getElementById('blanksLimit'));
    let winBy = getDropdownSelectedValue(document.getElementById('winBy'));
    let timeMultiplier = getDropdownSelectedValue(document.getElementById('timeMultiplier'));

    let go = {
        "vL": spectatorLimit,
        "pL": playerLimit,
        "sl": scoreLimit,
        "bl": blanksLimit,
        "tm": timeMultiplier,
        "wb": winBy,
        "css": getSelectedCardSets(document.getElementById('pyx_decks'))
    };

    console.log(go);

    createGame(go);
});
createGameDialog.listen('MDCDialog:cancel', function () {
    // Do nothing
});

let drawer = new mdc.drawer.MDCTemporaryDrawer(document.getElementById('drawer'));
document.querySelector('.mdc-toolbar__menu-icon').addEventListener('click', function () {
    drawer.open = true
});

window.onload = function () {
    sendPollRequest(false);
    loadGamesList();

    let gid = getURLParameter('gid');
    if (gid !== undefined) {
        postJoinSpectate(gid) // No need to join or spectate, just move the UI there
    }
};

function getURLParameter(name) {
    return decodeURIComponent((new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)').exec(location.search) || [null, ''])[1].replace(/\+/g, '%20')) || null;
}

function logout() {
    $.post("AjaxServlet?o=lo").always(function (data) {
        window.location = "/";
    });
}

function askPassword() {
    return prompt("Enter the game password:", "");
}

function joinGame(element) {
    let gid = element.getAttribute('data-gid');
    let hp = element.getAttribute('data-hp');

    let password = "";
    if (hp === true) password = askPassword();

    $.post("AjaxServlet?o=jg&gid=" + gid + "&pw=" + password).done(function (data) {
        console.log(data);
        alert("Joined game: " + JSON.stringify(data));
    }).fail(function (data) {
        console.log(data);
        alert("Error data: " + JSON.stringify(data));
    })
}

function spectateGame(element) {
    let gid = element.getAttribute('data-gid');
    let hp = element.getAttribute('data-hp');

    let password = "";
    if (hp === true) password = askPassword();

    $.post("AjaxServlet?o=vg&gid=" + gid + "&pw=" + password).done(function (data) {
        console.log(data);
        alert("Spectating game: " + JSON.stringify(data));
        postJoinSpectate(gid)
    }).fail(function (data) {
        console.log(data);
        alert("Error data: " + JSON.stringify(data));
    })
}

function postJoinSpectate(gid) {
    // TODO: Show game UI
}

function loadGamesList() {
    games.clear();

    $.post("AjaxServlet?o=ggl").done(function (data) {
        console.log(data);
        populateGamesList(data.gl);
    }).fail(function (data) {
        console.log(data);
        alert("Error data: " + JSON.stringify(data));
    })
}

function toggleNoGamesMessage(visible) {
    let container = $('#games-global-container');
    let list = container.find('.list');
    let message = container.find('.message');
    if (visible) {
        list.hide();
        message.show();
    } else {
        list.show();
        message.hide();
    }
}

function filterGames(query) {
    // TODO: Message when there are no search results
    if (query.length === 0) {
        games.filter(); // Remove all filters
    } else {
        games.filter(function (item) {
            return item.values()._host.indexOf(query) !== -1;
        })
    }
}

function populateGamesList(gamesList) {
    let items = [];
    for (let i = 0; i < gamesList.length; i++) {
        let game = gamesList[i];

        let goal;
        if (game.go.wb === 0) goal = game.go.sl;
        else goal = game.go.sl + " (win by " + game.go.wb + ")";

        let status;
        if (game.S === "l") status = "lobby";
        else status = "started";

        let decksNames = deckIdsToNames(game.go.css);
        let decks;
        if (decksNames.length === 0) decks = "none";
        else decks = decksNames.join(", ");

        let players;
        if (game.P.length === 0) players = "none";
        else players = game.P.join(", ");

        players += " (" + game.P.length + "/" + game.go.pL + ")";

        let spectators;
        if (game.V.length === 0) spectators = "none";
        else spectators = game.V.join(", ");

        spectators += " (" + game.V.length + "/" + game.go.vL + ")";

        items.push({
            "gid": game.gid,
            "hp": game.hp,
            "_likes": game.LK + (game.LK === 1 ? " LIKE" : " LIKES"),
            "_dislikes": game.DLK + (game.DLK === 1 ? " DISLIKE" : " DISLIKES"),
            "like": game.iLK,
            "dislike": game.iDLK,
            "_host": game.H,
            "_decks": decks,
            "_players": players,
            "_spectators": spectators,
            "_goal": goal,
            "_status": status
        });
    }

    games.clear();
    games.add(items, function (items) {
        for (let i = 0; i < items.length; i++) {
            let card = $(items[i].elm);

            let likeButton = card.find('._likes');
            if (card.attr("data-like") === "true") likeButton.addClass('mdc-button--raised');
            else likeButton.removeClass('mdc-button--raised');

            let dislikeButton = card.find('._dislikes');
            if (card.attr("data-dislike") === "true") dislikeButton.addClass('mdc-button--raised');
            else dislikeButton.removeClass('mdc-button--raised');
        }
    });

    toggleNoGamesMessage(games.size() === 0);
}

function deckIdsToNames(ids) {
    let names = [];
    let css = localStorage["css"];
    if (css === undefined) return ids; // Shouldn't happen
    let json = JSON.parse(css);

    for (let i = 0; i < ids.length; i++) {
        for (let j = 0; j < json.length; j++) {
            if (ids[i] === json[j].cid) names[i] = json[j].csn;
        }
    }
    return names;
}

function showCreateGameDialog() {
    resetCreateGameDialog();
    createGameDialog.show();
}

function createGame(go) {
    $.post("AjaxServlet?o=cg&go=" + JSON.stringify(go)).done(function (data) {
        postJoinSpectate(data.gid);
        loadGamesList();
    }).fail(function (data) {
        console.log(data);
        alert("Error create game: " + JSON.stringify(data));
    });
}

function updateLikeDislike(likeButton, disLikeButton, data) {
    if (likeButton === undefined) {
        likeButton = $(disLikeButton.parentElement).find('._likes');
        disLikeButton = $(disLikeButton);
    } else if (disLikeButton === undefined) {
        disLikeButton = $(likeButton.parentElement).find('._dislikes');
        likeButton = $(likeButton);
    } else {
        return;
    }

    if (data.iLK) likeButton.addClass('mdc-button--raised');
    else likeButton.removeClass('mdc-button--raised');
    likeButton.text(data.LK + (data.LK === 1 ? " LIKE" : " LIKES"));

    if (data.iDLK) disLikeButton.addClass('mdc-button--raised');
    else disLikeButton.removeClass('mdc-button--raised');
    disLikeButton.text(data.DLK + (data.DLK === 1 ? " DISLIKE" : " DISLIKES"));
}

function likeGame(button) {
    let gid = button.parentElement.parentElement.getAttribute('data-gid');

    $.post("AjaxServlet?o=lk&gid=" + gid).done(function (data) {
        updateLikeDislike(button, undefined, data);
        console.log(data);
    }).fail(function (data) {
        console.log(data);
    });
}

function dislikeGame(button) {
    let gid = button.parentElement.parentElement.getAttribute('data-gid');

    $.post("AjaxServlet?o=dlk&gid=" + gid).done(function (data) {
        updateLikeDislike(undefined, button, data);
        console.log(data);
    }).fail(function (data) {
        console.log(data);
    });
}
