var shouldStop = false;

function stopPolling() {
    shouldStop = true;
}

// Will automatically start polling
function sendPollRequest() {
    $.post("LongPollServlet").always(function (data, status) {
        processPollData(data, status);

        if (!shouldStop) sendPollRequest();
    })
}

function processPollData(data, status) {
    console.log("Poll data: " + JSON.stringify(data));
}