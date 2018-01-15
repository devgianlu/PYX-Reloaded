window.onload = function () {
    const gid = getURLParameter('gid');
    if (gid === null) {
        window.location = "lobbies.html";
    } else {
        sendPollRequest(false);
        loadUI(gid);
    }
};

function setTitle(host) {
    document.querySelector('header ._title').innerHTML = host + " - PYX Reloaded";
}

function loadUI(gid) {
    $.post("AjaxServlet?o=ggi&gid=" + gid).done(function (data) {
        setTitle(data.gi.H);

        console.log(data);
    }).fail(function (data) {
        alert("Failed load: " + JSON.stringify(data));
    });
}