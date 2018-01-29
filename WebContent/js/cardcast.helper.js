class Cardcast {
    /**
     * @param {string} code - Deck code
     */
    constructor(code) {
        this.code = code;
    }

    static get base_url() {
        return "https://api.cardcastgame.com/v1/";
    }

    /**
     * @callback decksCallback
     * @param {object} data.results - Results object
     * @param {int} data.total - Total decks in the system
     * @param {int} data.results.count - Result count
     * @param {object[]} data.results.data[] - Actual results
     * @param {string} data.results.data[].code - Deck code
     * @param {string} data.results.data[].category - Deck category
     * @param {object[]} data.results.data[].sample_calls - Actual results
     * @param {object[]} data.results.data[].sample_responses - Actual results
     * @param {int} data.results.data[].rating - Deck rating
     * @param {object} data.results.data[].author - Deck author
     * @param {string} data.results.data[].author.username - Deck author username
     */

    /**
     * @callback infoCallback
     * @param {string} data.description - Deck description
     * @param {int} data.call_count - Call count
     * @param {int} data.response_count - Response count
     */

    /**
     * @param {string} query
     * @param {string} category
     * @param {string} direction
     * @param {int} limit
     * @param {boolean} nsfw
     * @param {int} offset
     * @param {string} sort
     * @param {decksCallback} listener
     */
    static decks(query, category, direction, limit, nsfw, offset, sort, listener) {
        $.get(Cardcast.base_url + "decks?category=" + category + "&direction=" + direction + "&limit=" + limit + "&nsfw=" + nsfw + "&offset=" + offset + "&sort=" + sort + (query !== undefined ? "&search=" + query : "")).fail(function (data) {
            console.error(data);
            listener(undefined);
        }).done(function (data) {
            listener(data);
        });
    }

    /**
     * @param {infoCallback} listener
     */
    info(listener) {
        $.get(Cardcast.base_url + "decks/" + this.code).fail(function (data) {
            console.error(data);
            listener(undefined);
        }).done(function (data) {
            data.call_count = parseInt(data.call_count);
            data.response_count = parseInt(data.response_count);
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