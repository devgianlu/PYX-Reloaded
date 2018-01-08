class Cardcast {
    constructor(code) {
        this.code = code;
        this.base_url = "https://api.cardcastgame.com/v1/";
    }

    info(listener) {
        $.get(this.base_url + "decks/" + this.code).fail(function (data) {
            console.error(data);
            listener(undefined);
        }).done(function (data) {
            listener(data);
        });
    }
}