class Cardcast {
    constructor(code) {
        this.code = code;
    }

    static get base_url() {
        return "https://api.cardcastgame.com/v1/";
    }

    static decks(query, category, direction, limit, nsfw, offset, sort, listener) {
        $.get(Cardcast.base_url + "decks?category=" + category + "&direction=" + direction + "&limit=" + limit + "&nsfw=" + nsfw + "&offset=" + offset + "&sort=" + sort + (query !== undefined ? "&search=" + query : "")).fail(function (data) {
            console.error(data);
            listener(undefined);
        }).done(function (data) {
            listener(data);
        });
    }

    info(listener) {
        $.get(Cardcast.base_url + "decks/" + this.code).fail(function (data) {
            console.error(data);
            listener(undefined);
        }).done(function (data) {
            listener(data);
        });
    }

    calls(listener) {
        $.get(Cardcast.base_url + "decks/" + this.code + "/calls").fail(function (data) {
            console.error(data);
            listener(undefined);
        }).done(function (data) {
            listener(data);
        });
    }

    responses(listener) {
        $.get(Cardcast.base_url + "decks/" + this.code + "/responses").fail(function (data) {
            console.error(data);
            listener(undefined);
        }).done(function (data) {
            listener(data);
        });
    }
}