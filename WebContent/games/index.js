class Games {
    constructor() {
        this._games = $('#games');
        this._games_list = this._games.find('.list');
        this._games_message = this._games.find('.message');

        this.games = new List(this._games[0], {
            item: 'gameInfoTemplate',
            valueNames: ['_host', '_players', '_spectators', '_goal', '_status', '_decks', '_likes', '_dislikes',
                {'data': ['gid', 'hp', 'like', 'dislike']}]
        });
    }

    static deckIdsToNames(ids) {
        const names = [];
        const css = localStorage["css"];
        if (css === undefined) return ids; // Shouldn't happen
        const json = JSON.parse(css);

        for (let i = 0; i < ids.length; i++) {
            for (let j = 0; j < json.length; j++) {
                if (ids[i] === json[j].csi) names[i] = json[j].csn;
            }
        }

        return names;
    }

    static askPassword() {
        return prompt("Enter the game password:", "");
    }

    static postJoinSpectate(gid) {
        window.location = "/game/" + gid;
    }

    /**
     *
     * @param {object[]} list - Games list
     * @param {object} list[].go - Game options
     * @param {int[]} list[].go.css - Card set IDs
     * @param {int} list[].go.wb - Win by X
     * @param {int} list[].go.sl - Score limit (goal)
     * @param {string[]} list[].P - Player names
     * @param {string[]} list[].V - Spectator names
     * @param {int} list[].LK - Like count
     * @param {int} list[].DLK - Dislike count
     * @param {boolean} list[].iLK - Do I like this game?
     * @param {boolean} list[].iDLK - Do I dislike this game?
     */
    setup(list) {
        this.games.clear();

        for (let i = 0; i < list.length; i++) {
            const game = list[i];

            let goal;
            if (game.go.wb === 0) goal = game.go.sl;
            else goal = game.go.sl + " (win by " + game.go.wb + ")";

            let status;
            if (game.gs === "l") status = "lobby";
            else status = "started";

            let decksNames = Games.deckIdsToNames(game.go.css);
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

            const elm = $(this.games.add({
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
            })[0].elm);

            let likeButton = elm.find('._likes');
            if (elm.attr("data-like") === "true") likeButton.addClass('mdc-button--raised');
            else likeButton.removeClass('mdc-button--raised');

            let dislikeButton = elm.find('._dislikes');
            if (elm.attr("data-dislike") === "true") dislikeButton.addClass('mdc-button--raised');
            else dislikeButton.removeClass('mdc-button--raised');
        }

        this.toggleNoGamesMessage(this.games.size() === 0);
    }

    filterGames(query) {
        if (query.length === 0) {
            this.games.filter(); // Remove all filters
        } else {
            this.games.filter(function (item) {
                return item.values()._host.indexOf(query) !== -1;
            })
        }
    }

    toggleNoGamesMessage(visible) {
        if (visible) {
            this._games_list.hide();
            this._games_message.show();
        } else {
            this._games_list.show();
            this._games_message.hide();
        }
    }

    createGame(go) {
        $.post("/AjaxServlet", "o=cg&go=" + JSON.stringify(go)).done(function (data) {
            Games.postJoinSpectate(data.gid);
            loadGamesList();
        }).fail(function (data) {
            Notifier.error("Failed creating the game!", data);
        });
    }

    joinGame(gid, hp) {
        let password = "";
        if (hp === "true") password = Games.askPassword();

        $.post("/AjaxServlet", "o=jg&gid=" + gid + "&pw=" + password).done(function () {
            Games.postJoinSpectate(gid)
        }).fail(function (data) {
            Notifier.error("Failed joining the game!", data);
        })
    }

    spectateGame(gid, hp) {
        let password = "";
        if (hp === "true") password = Games.askPassword();

        $.post("/AjaxServlet", "o=vg&gid=" + gid + "&pw=" + password).done(function () {
            Games.postJoinSpectate(gid)
        }).fail(function (data) {
            Notifier.error("Failed spectating the game!", data);
        })
    }
}

const games = new Games();


const drawer = new mdc.drawer.MDCTemporaryDrawer(document.getElementById('drawer'));
document.querySelector('.mdc-toolbar__menu-icon').addEventListener('click', function () {
    drawer.open = true
});

window.onload = function () {
    loadGamesList();

    let gid = getURLParameter('gid');
    if (gid !== null) {
        Games.postJoinSpectate(gid) // No need to join or spectate, just move the UI there
    }
};

function logout() {
    closeWebSocket();
    $.post("/AjaxServlet", "o=lo").always(function () {
        window.location = "/";
    });
}

function loadGamesList() {
    $.post("/AjaxServlet", "o=ggl").done(function (data) {
        /**
         * @param {object[]} data.gl - Games list
         */

        games.setup(data.gl);
    }).fail(function (data) {
        Notifier.error("Failed loading the games!", data);
    });

    registerPollListener("LOBBIES", function (data) {
        if (data["E"] === "glr") {
            loadGamesList();
        }
    });
}

function filterGames(query) {
    games.filterGames(query);
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
    const gid = button.parentElement.parentElement.getAttribute('data-gid');

    $.post("/AjaxServlet", "o=lk&gid=" + gid).done(function (data) {
        updateLikeDislike(button, undefined, data);
    }).fail(function (data) {
        Notifier.error("Failed liking the game!", data);
    });
}

function dislikeGame(button) {
    const gid = button.parentElement.parentElement.getAttribute('data-gid');

    $.post("/AjaxServlet", "o=dlk&gid=" + gid).done(function (data) {
        updateLikeDislike(undefined, button, data);
    }).fail(function (data) {
        Notifier.error("Failed disliking the game!", data);
    });
}

function joinGame(element) {
    games.joinGame(element.getAttribute('data-gid'), element.getAttribute('data-hp'));
}

function spectateGame(element) {
    games.spectateGame(element.getAttribute('data-gid'), element.getAttribute('data-hp'))
}