window.onload = function () {
    $.post("AjaxServlet", "o=fl").fail(function (data) {
        Notifier.error("Failed contacting the server!", data);
    }).done(function (data) {
        data.css.sort(function (a, b) {
            return a.w - b.w;
        });

        localStorage['css'] = JSON.stringify(data.css);
        localStorage['dgo'] = JSON.stringify(data.dgo);

        if (data.ip) {
            if (data.next === "game") {
                window.location = "lobbies.html?gid=" + data.gid;
            } else {
                window.location = "lobbies.html";
            }
        }
    });
};

function register(ev) {
    if (ev !== undefined && ev.keyCode !== 13) return;

    const nickname = $("input#nickname").val();

    $.post("AjaxServlet", "o=r&n=" + nickname).fail(function (data) {
        Notifier.error("Failed registering to the server!", data);
    }).done(function () {
        window.location = "lobbies.html";
    });
}