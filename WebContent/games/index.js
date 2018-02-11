class Games {
    constructor() {
        this._games = $('#games');
        this._games_list = this._games.find('.list');
        this._games_message = this._games.find('.message');

        this._searchField = $('#gamesSearch');

        this.games = new List(this._games[0], {
            item: 'gameInfoTemplate',
            valueNames: ['_host', '_players', '_spectators', '_goal', '_status', '_decks', '_likes', '_dislikes',
                {'data': ['gid']}]
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

    static _updateLikeDislike(elm, toggleLike, toggleDislike, data) {
        toggleLike.on = data.iLK;
        toggleDislike.on = data.iDLK;

        const likes = elm.find('._likes');
        likes.text(data.LK + (data.LK === 1 ? " like" : " likes"));

        const dislikes = elm.find('._dislikes');
        dislikes.text(data.DLK + (data.DLK === 1 ? " dislike" : " dislikes"));
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

        const self = this;
        for (let i = 0; i < list.length; i++) {
            const game = list[i];

            let goal;
            if (game.go.wb === 0) goal = game.go.sl;
            else goal = game.go.sl + " (win by " + game.go.wb + ")";

            let status;
            if (game.gs === "l") status = "hourglass_empty";
            else status = "casino";

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
                "_likes": game.LK + (game.LK === 1 ? " like" : " likes"),
                "_dislikes": game.DLK + (game.DLK === 1 ? " dislike" : " dislikes"),
                "_host": game.H,
                "_decks": decks,
                "_players": players,
                "_spectators": spectators,
                "_goal": goal,
                "_status": status
            })[0].elm);

            elm.find('._join').on('click', function () {
                self.joinGame(game.gid, game.hp);
            });

            elm.find('._spectate').on('click', function () {
                self.spectateGame(game.gid, game.hp);
            });

            const _toggleLike = elm.find('._toggleLike')[0];
            const toggleLike = mdc.iconToggle.MDCIconToggle.attachTo(_toggleLike);
            const _toggleDislike = elm.find('._toggleDislike')[0];
            const toggleDislike = mdc.iconToggle.MDCIconToggle.attachTo(_toggleDislike);

            toggleLike.on = game.iLK;
            _toggleLike.addEventListener('MDCIconToggle:change', () => {
                self.toggleLike(game.gid, elm, toggleLike, toggleDislike);
            });

            toggleDislike.on = game.iDLK;
            _toggleDislike.addEventListener('MDCIconToggle:change', () => {
                self.toggleDislike(game.gid, elm, toggleLike, toggleDislike);
            });
        }

        this.toggleNoGamesMessage(this.games.size() === 0);
    }

    toggleLike(gid, elm, toggleLike, toggleDislike) {
        $.post("/AjaxServlet", "o=lk&gid=" + gid).done(function (data) {
            Games._updateLikeDislike(elm, toggleLike, toggleDislike, data);
        }).fail(function (data) {
            Notifier.error("Failed liking the game!", data);
        });
    }

    toggleDislike(gid, elm, toggleLike, toggleDislike) {
        $.post("/AjaxServlet", "o=dlk&gid=" + gid).done(function (data) {
            Games._updateLikeDislike(elm, toggleLike, toggleDislike, data);
        }).fail(function (data) {
            Notifier.error("Failed disliking the game!", data);
        });
    }

    filterGames(query) {
        if (query === null || query.length === 0) {
            this.games.filter(); // Remove all filters
        } else {
            this.games.filter(function (item) {
                return item.values()._host.indexOf(query) !== -1;
            })
        }

        this.toggleNoGamesMessage(this.games.visibleItems.length === 0);
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

    submitSearch() {
        this.filterGames(this._searchField.val());
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

function submitSearch() {
    games.submitSearch();
}