const _pollingListeners = {};
let shouldStop = false;

function stopPolling() {
    shouldStop = true;
}

// Will automatically start polling
function sendPollRequest(retry) {
    $.post("LongPollServlet").done(function (data) {
        processPollData(data);
        if (!shouldStop) sendPollRequest(false);
    }).fail(function () {
        if (!retry && !shouldStop) sendPollRequest(true);
    })
}

/**
 * @param {object[]} data.Es - Events
 */
function processPollData(data) {
    const events = data.Es;
    if (events.length === 0) return;

    for (const key in _pollingListeners) {
        if (_pollingListeners.hasOwnProperty(key)) {
            for (let i = 0; i < events.length; i++) {
                _pollingListeners[key](events[i]);
            }
        }
    }
}

function registerPollListener(key, listener) {
    _pollingListeners[key] = listener;
}

function unregisterPollListener(key) {
    delete _pollingListeners[key];
}