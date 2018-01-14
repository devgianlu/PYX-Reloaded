function loadDecks() {
    const list = new List('cardcast-container', {
        valueNames: ['_name', '_category', '_author', '_sample', {data: ['code']}],
        item: 'cardcast-deck-template'
    });

    Cardcast.decks("", "desc", 12, true, 0, "rating", function (data) {
        const results = data.results.data;
        for (let i = 0; i < results.length; i++) {
            const item = results[i];

            const elm = list.add({
                "_name": item.name,
                "code": item.code,
                "_category": getCategoryMaterialIconsName(item.category),
                "_author": "by " + item.author.username,
                "_sample": createSamplePhrase(item.sample_calls, item.sample_responses.slice())
            })[0];

            const rating = $(elm.elm.querySelector('.\_rating'));
            rating.barrating({
                theme: 'fontawesome-stars-o',
                readonly: true,
                initialRating: item.rating
            });
        }
    })
}

function getCategoryMaterialIconsName(category) {
    switch (category) {
        case "books":
            return "book";
        case "community":
            return "people";
        case "gaming":
            return "videogame_asset";
        case "movies":
            return "movie";
        case "music":
            return "music_note";
        case "sports":
            return "directions_run";
        case "technology":
            return "phonelink";
        case "television":
            return "tv";
        case "translation":
            return "translate";
        default:
        case "other":
            return "more";
        case "random":
            return "casino";
    }
}

function createCategoryAndAuthorString(category, author) {
    let str = "";
    if (category === "other") str += "An ";
    else str += "A ";
    return str + category + " deck created by " + author;
}

function showCardcastDetailsDialog(code) {
    const elm = $(document.getElementById('cardcastDetailsDialog'));
    const dialog = new mdc.dialog.MDCDialog(elm[0]);

    elm.find('.mdc-dialog__body--scrollable')[0].scrollTop = 0;

    const cc = new Cardcast(code);
    cc.info(function (data) {
        if (data === undefined) {
            alert("ERROR!");
        } else {
            elm.find('.\_name').text(data.name);
            elm.find('.\_author-category').text(createCategoryAndAuthorString(data.category, data.author.username));
            elm.find('.\_desc').text(data.description);
            elm.find('.\_code').text(data.code);
            elm.find('.\_category').text(getCategoryMaterialIconsName(data.category));
            elm.find('.\_calls').text("Calls (" + data.call_count + ")");
            elm.find('.\_responses').text("Responses (" + data.response_count + ")");

            const rating = elm.find('.\_rating');
            rating.barrating('destroy');
            rating.barrating({
                theme: 'fontawesome-stars-o',
                readonly: true,
                initialRating: data.rating
            });

            rating.parent().css('margin-top', '6px');
            rating.parent().css('margin-right', '16px');

            dialog.show();
        }
    });

    cc.calls(function (calls) {
        if (calls === undefined) {
            alert("ERROR!");
        } else {
            const list = _initCardsList(elm.find('#cardcastDetailsCalls')[0]);
            for (let i = 0; i < calls.length; i++) {
                list.add({
                    "_text": calls[i].text.join("____"),
                    "black": true
                });
            }
        }
    });

    cc.responses(function (calls) {
        if (calls === undefined) {
            alert("ERROR!");
        } else {
            const list = _initCardsList(elm.find('#cardcastDetailsResponses')[0]);
            for (let i = 0; i < calls.length; i++) {
                list.add({
                    "_text": calls[i].text[0],
                    "black": false
                });
            }
        }
    });
}

function _initCardsList(container) {
    const list = new List(container, {
        item: 'card-template',
        valueNames: ['_text', {data: ['black']}]
    });
    list.clear();
    return list;
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
        $.get(Cardcast.base_url + "decks?category=" + category + "&direction=" + direction + "&limit=" + limit + "&nsfw=" + nsfw + "&offset=" + offset + "&sort=" + sort).fail(function (data) {
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