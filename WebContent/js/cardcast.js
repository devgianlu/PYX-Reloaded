function loadDecks() {
    const list = new List('cardcast-container', {
        valueNames: ['_name', '_code', '_category', '_author', '_sample'],
        item: 'cardcast-deck-template'
    });

    Cardcast.decks("", "desc", 12, true, 0, "rating", function (data) {
        const results = data.results.data;
        console.log(results);

        for (let i = 0; i < results.length; i++) {
            const item = results[i];

            let category;
            switch (item.category) {
                case "books":
                    category = "book";
                    break;
                case "community":
                    category = "people";
                    break;
                case "gaming":
                    category = "videogame_asset";
                    break;
                case "movies":
                    category = "movie";
                    break;
                case "music":
                    category = "music_note";
                    break;
                case "sports":
                    category = "directions_run";
                    break;
                case "technology":
                    category = "phonelink";
                    break;
                case "television":
                    category = "tv";
                    break;
                case "translation":
                    category = "translate";
                    break;
                case "other":
                    category = "more";
                    break;
                case "random":
                    category = "casino";
                    break;
            }


            const elm = list.add({
                '_name': item.name,
                '_code': item.code,
                '_category': category,
                '_author': "by " + item.author.username,
                '_sample': createSamplePhrase(item.sample_calls, item.sample_responses.slice())
            })[0];

            console.log(elm);
            const rating = $(elm.elm.querySelector('.\_rating'));
            rating.barrating({
                theme: 'fontawesome-stars-o',
                readonly: true,
                initialRating: item.rating
            });
        }
    })
}

function createSamplePhrase(calls, responses) {
    if (calls.length === 0 || responses.length === 0) return "";
    const randomCall = calls[Math.floor((Math.random() * calls.length))];
    const randomResp = [];
    const neededResponses = randomCall.text.length - 1;

    for (let i = 0; i < neededResponses; i++) {
        const index = Math.floor(Math.random() * (responses.length - 1));
        randomResp.push(responses[index]);
        responses.splice(index, 1);
    }

    let str = "";
    for (let i = 0; i < neededResponses; i++) {
        str += randomCall.text[i];
        str += "<b>";
        str += randomResp[i].text[0];
        str += "</b>";
    }

    return str + randomCall.text[neededResponses];
}

class Cardcast {
    constructor(code) {
        this.code = code;
    }

    static get base_url() {
        return "https://api.cardcastgame.com/v1/";
    }

    static decks(category, direction, limit, nsfw, offset, sort, listener) {
        $.get(this.base_url + "decks?category=" + category + "&direction=" + direction + "&limit=" + limit + "&nsfw=" + nsfw + "&offset=" + offset + "&sort=" + sort).fail(function (data) {
            console.error(data);
            listener(undefined);
        }).done(function (data) {
            listener(data);
        });
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