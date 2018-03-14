const _pollingListeners = {};

const ws = new WebSocket((location.protocol === "https:" ? "wss" : "ws") + "://" + location.hostname + (location.port ? ":" + location.port : "") + "/Events");
ws.onmessage = function (event) {
    /** @param {object[]} data.Es - Events **/
    const data = JSON.parse(event.data);
    if ("e" in data) {
        Notifier.debug(data, true);
        if (data.ec === "nr" || data.ec === "se") window.location = "/";
        return;
    }

    const events = data.Es;
    if (events.length === 0) return;

    processPollData(events);
};

function closeWebSocket() {
    ws.close(1000);
}

/**
 * @param {string} events[].E - Event code
 */
function processPollData(events) {
    for (let i = 0; i < events.length; i++) {
        const event = events[i];
        if (event.E === "pp") {
            Requester.request("PP", {});
            continue;
        } else if (event.E === "PS") {
            /** @param {int} event.bs - Time before shutdown */
            let bs;
            if (event.bs >= 60000) bs = Math.ceil(event.bs / 60000) + " minutes";
            else bs = Math.ceil(event.bs / 1000) + " seconds";

            Notifier.show(Notifier.WARN, "The server is preparing for shutdown. The server will be shutdown in " + bs, 20, false);
            continue;
        } else if (event.E === "SS") {
            window.location = "https://google.com"; // TODO: Redirect to status page
            continue;
        }

        for (const key in _pollingListeners) {
            if (_pollingListeners.hasOwnProperty(key)) {
                _pollingListeners[key](event);
            }
        }
    }

}

/**
 * @callback eventCallback
 * @param {string} data.E - Event code
 */

/**
 * @param {String} key
 * @param {eventCallback} listener
 */
function registerPollListener(key, listener) {
    _pollingListeners[key] = listener;
}

function unregisterPollListener(key) {
    delete _pollingListeners[key];
}