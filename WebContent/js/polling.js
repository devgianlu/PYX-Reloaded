const _pollingListeners = {};

// Will automatically start polling
function sendPollRequest(retry) {
    $.post("LongPollServlet").always(function (data) {
        processPollData(data);
        if (!retry) sendPollRequest(status === "error");
    })
}

function _fakePollData(obj) {
    processPollData({"E": [obj]})
}

function processPollData(data) {
    console.log(data);

    const events = data["E"];
    // noinspection JSValidateTypes
    if (events === "_") return;

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