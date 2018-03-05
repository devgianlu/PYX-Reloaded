const _pollingListeners = {};

const ws = new WebSocket((location.protocol === "https:" ? "wss" : "ws") + "://" + location.hostname + (location.port ? ":" + location.port : "") + "/Events");
ws.onmessage = function (event) {
    /** @param {object[]} data.Es - Events **/
    const data = JSON.parse(event.data);
    if ("e" in data) {
        Notifier.debug(data);
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