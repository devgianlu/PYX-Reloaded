const DECKS_PER_PAGE = 12;
const PAGINATION_WINDOW = 4;
let _cardcastOffset = 0;

function loadDecks(query, category, nsfw, sort) {
    const container = document.getElementById('cardcast-container');
    const list = new List(container, {
        valueNames: ['_name', '_category', '_author', '_sample', {data: ['code']}],
        item: 'cardcast-deck-template',
        paginationClass: 'not-pagination'
    });

    Cardcast.decks(query, category.join(','), getDefaultDirectionFor(sort), DECKS_PER_PAGE, nsfw, _cardcastOffset, sort, function (data) {
        list.clear();

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

        document.querySelector('main').scrollTop = 0;
        generatePagination(container.querySelector('.pagination__inner'), data.results.count);
    })
}

function generatePagination(container, itemCount) {
    while (container.firstChild) {
        container.removeChild(container.firstChild);
    }

    let currPage = Math.floor(_cardcastOffset / DECKS_PER_PAGE);
    if (currPage !== 0) {
        const prev = document.createElement("button");
        prev.className = "mdc-button";
        prev.innerHTML = "&laquo;";
        container.appendChild(prev);
        prev.addEventListener('click', function () {
            changePage(currPage - 1);
        });
    }

    const maxPages = itemCount / DECKS_PER_PAGE;

    let startPage = 0;
    if (currPage > PAGINATION_WINDOW) startPage = currPage - PAGINATION_WINDOW;

    let endPage = currPage + 1 + PAGINATION_WINDOW;
    if (endPage > maxPages) endPage = maxPages;

    for (let i = startPage; i < endPage; i++) {
        const page = document.createElement("button");
        page.className = "mdc-button";
        if (i === currPage) page.className += " mdc-button--raised";
        page.innerText = (i + 1).toString();
        container.appendChild(page);
        page.addEventListener('click', function () {
            changePage(i);
        });
    }

    if (currPage !== maxPages) {
        const next = document.createElement("button");
        next.className = "mdc-button";
        next.innerHTML = "&raquo;";
        container.appendChild(next);
        next.addEventListener('click', function () {
            changePage(currPage + 1);
        });
    }
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

function cardcastOptionsChanged() {
    const menu = document.getElementById('cardcastMenu');
    _cardcastOffset = 0;
    loadDecks(getSearchQuery(),
        getCurrentCardcastCategories(menu.querySelector('.\_categories')),
        getNSFWSelected(),
        getCurrentCardcastSorting(menu.querySelector('.\_sort')));
}

function changePage(page) {
    const menu = document.getElementById('cardcastMenu');
    _cardcastOffset = DECKS_PER_PAGE * page;
    loadDecks(getSearchQuery(),
        getCurrentCardcastCategories(menu.querySelector('.\_categories')),
        getNSFWSelected(),
        getCurrentCardcastSorting(menu.querySelector('.\_sort')));
}

function getNSFWSelected() {
    return document.getElementById('toggleNsfw').checked;
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

function getSearchQuery() {
    const input = document.getElementById('cardcastSearch');
    return input.value.length === 0 ? undefined : input.value;
}

function setupCardcastSearch() {
    const input = document.getElementById('cardcastSearch');
    input.addEventListener('keyup', function (ev) {
        if (ev.keyCode === 13) cardcastOptionsChanged();
    })
}

function setupCardcastMenu() {
    const menuElm = document.getElementById('cardcastMenu');
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