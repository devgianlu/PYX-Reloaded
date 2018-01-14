function loadDecks(category, nsfw, sort) {
    const list = new List('cardcast-container', {
        valueNames: ['_name', '_category', '_author', '_sample', {data: ['code']}],
        item: 'cardcast-deck-template'
    });
    list.clear();

    Cardcast.decks(category.join(','), getDefaultDirectionFor(sort), 12, nsfw, 0, sort, function (data) {
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
    for (let i = 0; i < cardcastCategories.length; i++) {
        const item = cardcastCategories[i];
        if (category === item.value) return item.icon;
    }

    return "other";
}

const cardcastSorts = [{print: "Rating", name: "rating", type: "rating", default_direction: "desc"},
    {print: "Name", name: "name", type: "name", default_direction: "asc"},
    {print: "Newest", name: "newest", type: "created_at", default_direction: "desc"},
    {print: "Size", name: "size", type: "card_count", default_direction: "desc"}];

const cardcastCategories = [{name: "Books", icon: "book", value: "books"},
    {name: "Community", icon: "people", value: "community"},
    {name: "Gaming", icon: "videogame_asset", value: "gaming"},
    {name: "Movies", icon: "movie", value: "movies"},
    {name: "Music", icon: "music_note", value: "music"},
    {name: "Sports", icon: "directions_run", value: "sports"},
    {name: "Technology", icon: "phonelink", value: "technology"},
    {name: "Television", icon: "tv", value: "television"},
    {name: "Translation", icon: "translate", value: "translation"},
    {name: "Other", icon: "more", value: "other"},
    {name: "Random", icon: "casino", value: "random"}];

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

let _cardcastMenu = undefined;

function showCardcastMenu() {
    _cardcastMenu.open = !_cardcastMenu.open;
}

function cardcastOptionsChanged(menu) {
    loadDecks(getCurrentCardcastCategories(menu.querySelector('.\_categories')), true, getCurrentCardcastSorting(menu.querySelector('.\_sort')));
}

function getDefaultDirectionFor(sort) {
    for (let i = 0; i < cardcastSorts.length; i++) {
        const item = cardcastSorts[i];
        if (sort === item.value) return item.default_direction;
    }

    return "desc";
}

function getCurrentCardcastCategories(categories) {
    const selected = [];
    for (let i = 0; i < categories.children.length; i++) {
        const child = categories.children[i];
        if (child.querySelector('input').checked) selected.push(child.getAttribute("data-value"));
    }

    return selected;
}

function getCurrentCardcastSorting(sort) {
    for (let i = 0; i < sort.children.length; i++) {
        const child = sort.children[i];
        if (child.querySelector('input').checked) return child.getAttribute("data-value");
    }

    return "rating";
}

function setupCardcastMenu(menuElm) {
    if (_cardcastMenu === undefined) {
        _cardcastMenu = new mdc.menu.MDCSimpleMenu(menuElm);
        _cardcastMenu.listen('MDCSimpleMenu:selected', () => cardcastOptionsChanged(menuElm));
        _cardcastMenu.listen('MDCSimpleMenu:cancel', () => cardcastOptionsChanged(menuElm));
    }

    const categories = new List(menuElm, {
        item: 'category-item-template',
        listClass: '_categories',
        valueNames: ['_name', '_icon', {data: ['value']}]
    });
    categories.clear();

    for (let i = 0; i < cardcastCategories.length; i++) {
        const category = cardcastCategories[i];
        const elm = categories.add({
            "_name": category.name,
            "_icon": category.icon,
            "value": category.value
        })[0];
        elm.elm.querySelector('input').setAttribute("id", "category_" + category.value);
        elm.elm.querySelector('label').setAttribute("for", "category_" + category.value);
    }


    const sort = new List(menuElm, {
        item: 'sort-item-template',
        listClass: '_sort',
        valueNames: ['_name', {data: ['value']}]
    });
    sort.clear();

    for (let i = 0; i < cardcastSorts.length; i++) {
        const orderBy = cardcastSorts[i];
        const elm = sort.add({"_name": orderBy.print, "value": orderBy.type})[0];
        const input = elm.elm.querySelector('input');
        input.setAttribute("id", "sort_" + orderBy.name);
        elm.elm.querySelector('label').setAttribute("for", "sort_" + orderBy.name);

        if (orderBy.type === "rating") input.checked = true;

        input.addEventListener('change', function () {
            if (this.checked) {
                $(menuElm.querySelector('.\_sort')).find("input[id!=sort_" + orderBy.name + "]").each(function () {
                    $(this)[0].checked = false;
                });
            }
        })
    }
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