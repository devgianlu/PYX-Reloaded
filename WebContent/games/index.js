class Games {
    constructor() {
        this._games = $('#games');
        this._games_list = this._games.find('.list');
        this._games_message = this._games.find('.message');

        this._searchField = $('#gamesSearch');
        this._searchField.on('keydown', () => this.submitSearch());
        this._searchField.parent().find('.mdc-text-field__icon').on('click', () => this.submitSearch());

        this._adminPanel = $('._adminPanel');

        this._refresh = $('._refresh');
        this._refresh.on('click', () => {
            this.loadGamesList();
            this.closeDrawer();
        });

        this._theming = $('._themingDialog');
        this._theming.on('click', () => {
            showThemingDialog();
            this.closeDrawer();
        });

        this._logout = $('._logout');
        this._logout.on('click', () => {
            Games.logout();
            this.closeDrawer();
        });

        this.games = new List(this._games[0], {
            item: 'gameInfoTemplate',
            valueNames: ['_host', '_players', '_spectators', '_goal', '_status', '_decks', '_likes', '_dislikes',
                {'data': ['gid']}]
        });

        this._drawer = $('#drawer');
        this.drawer = new mdc.drawer.MDCTemporaryDrawer(this._drawer[0]);
        $('.mdc-toolbar__menu-icon').on('click', () => this.drawer.open = true);

        this.profilePicture = this._drawer.find('.details--profile');
        this.profileNickname = this._drawer.find('.details--nick');
        this.profileEmail = this._drawer.find('.details--email');
        this.loadUserInfo()
    }

    loadUserInfo() {
        Requester.request("gme", {}, (data) => {
            /**
             * @param {object} data.a - User account
             * @param {string} data.n - User nickname
             * @param {string} data.a.p - Profile picture URL
             * @param {string} data.a.em - Profile email
             */
            Notifier.debug(data);

            this.profileNickname.text(data.n);
            if (data.a !== undefined) {
                if (data.a.p !== null) this.profilePicture.attr('src', data.a.p);
                this.profileEmail.show();
                this.profileEmail.text(data.a.em);
                if (data.a.ia) this._adminPanel.show();
                else this._adminPanel.hide();
            } else {
                this.profileEmail.hide();
                this._adminPanel.hide();
            }
        }, (error) => {
            Notifier.error("Failed loading user info.", error)
        });
    }

    static logout() {
        eventsReceiver.close();
        Requester.always("lo", {}, () => {
            window.location = "/";
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

    static wrapCardcastDecks(CCs) {
        const names = [];
        for (let i = 0; i < CCs.length; i++) names[i] = "<i>" + CCs[i] + "</i>";
        return names;
    }

    closeDrawer() {
        this.drawer.open = false;
    }

    toggleLike(gid, elm, toggleLike, toggleDislike) {
        Requester.request("lk", {
            "gid": gid
        }, (data) => {
            Games._updateLikeDislike(elm, toggleLike, toggleDislike, data);
        }, (error) => {
            Notifier.error("Failed liking the game!", error);
        });
    }

    toggleDislike(gid, elm, toggleLike, toggleDislike) {
        Requester.request("dlk", {
            "gid": gid
        }, (data) => {
            Games._updateLikeDislike(elm, toggleLike, toggleDislike, data);
        }, (error) => {
            Notifier.error("Failed disliking the game!", error);
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
        Requester.request("cg", {
            "go": go
        }, (data) => {
            Games.postJoinSpectate(data.gid);
            this.loadGamesList();
        }, (error) => {
            Notifier.error("Failed creating the game!", error);
        });
    }

    loadGamesList() {
        Requester.request("ggl", {}, (data) => {
            /** @param {object[]} data.gl - Games list */
            this.setup(data.gl);
        }, (error) => {
            Notifier.error("Failed loading the games!", error);
        });

        eventsReceiver.register("LOBBIES", (data) => {
            if (data["E"] === "glr") this.loadGamesList();
        });
    }

    /**
     *
     * @param {object[]} list - Games list
     * @param {object} list[].go - Game options
     * @param {boolean} list[].hp - Has password
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
            if (game.gs === "l") status = "hourglass_empty";
            else status = "casino";

            let decksNames = Games.deckIdsToNames(game.go.css);
            decksNames = decksNames.concat(Games.wrapCardcastDecks(game.go.CCs));

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

            elm.find('._join').on('click', () => {
                this.joinGame(game.gid, game.hp);
            });

            elm.find('._spectate').on('click', () => {
                this.spectateGame(game.gid, game.hp);
            });

            const _toggleLike = elm.find('._toggleLike')[0];
            const toggleLike = mdc.iconToggle.MDCIconToggle.attachTo(_toggleLike);
            const _toggleDislike = elm.find('._toggleDislike')[0];
            const toggleDislike = mdc.iconToggle.MDCIconToggle.attachTo(_toggleDislike);

            toggleLike.on = game.iLK;
            _toggleLike.addEventListener('MDCIconToggle:change', () => {
                this.toggleLike(game.gid, elm, toggleLike, toggleDislike);
            });

            toggleDislike.on = game.iDLK;
            _toggleDislike.addEventListener('MDCIconToggle:change', () => {
                this.toggleDislike(game.gid, elm, toggleLike, toggleDislike);
            });
        }

        this.toggleNoGamesMessage(this.games.size() === 0);
    }

    joinGame(gid, hp) {
        let password = "";
        if (hp) password = Games.askPassword();
        Requester.request("jg", {
            "gid": gid,
            "pw": password
        }, () => {
            Games.postJoinSpectate(gid)
        }, (error) => {
            switch (error.ec) {
                case "wp":
                    Notifier.error("Wrong game password.", error);
                    return;
            }

            Notifier.error("Failed joining the game.", error);
        });
    }

    spectateGame(gid, hp) {
        let password = "";
        if (hp) password = Games.askPassword();
        Requester.request("vg", {
            "gid": gid,
            "pw": password
        }, () => {
            Games.postJoinSpectate(gid)
        }, (error) => {
            switch (error.ec) {
                case "wp":
                    Notifier.error("Wrong game password.", error);
                    return;
            }

            Notifier.error("Failed spectating the game.", error);
        });
    }

    submitSearch() {
        this.filterGames(this._searchField.val());
    }
}

function getURLParameter(name) {
    return decodeURIComponent((new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)').exec(location.search) || [null, ''])[1].replace(/\+/g, '%20')) || null;
}

const games = new Games();
const gid = getURLParameter('gid');
if (gid !== null) Games.postJoinSpectate(gid); // No need to join or spectate, just move the UI there
else games.loadGamesList();