let shouldStop = false;

function stopPolling() {
    shouldStop = true;
}

// Will automatically start polling
function sendPollRequest(retry) {
    $.post("LongPollServlet").always(function (data, status) {
        processPollData(data, status);

        if (!shouldStop && !retry) {
            sendPollRequest(status === "error");
        }
    })
}

function processPollData(data, status) {
    console.log("Poll data: " + JSON.stringify(data));
}