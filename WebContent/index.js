window.onload = function () {
    $.post("/AjaxServlet", "o=fl").fail(function (data) {
        if (data.ec === "se" || data.ec === "nr") Notifier.debug(data);
        else Notifier.error("Failed contacting the server!", data);
    }).done(function (data) {
        data.css.sort(function (a, b) {
            return a.w - b.w;
        });

        localStorage['css'] = JSON.stringify(data.css);
        localStorage['dgo'] = JSON.stringify(data.dgo);

        if (data.ip) {
            if (data.next === "game") {
                window.location = "/games/?gid=" + data.gid;
            } else {
                window.location = "/games/";
            }
        }
    });
};

function register(ev) {
    if (ev !== undefined && ev.keyCode !== 13) return;

    const nickname = $("input#nickname").val();

    $.post("/AjaxServlet", "o=r&n=" + nickname).fail(function (data) {
        switch (data.responseJSON.ec) {
            case "nns":
                Notifier.error("Please specify a nickname.", data);
                break;
            case "niu":
                Notifier.error("This nickname is already in use.", data);
                break;
            case "tmu":
                Notifier.error("The server is full! Please try again later.", data);
                break;
            case "in":
                Notifier.error("This nickname must contain only alphanumeric characters and be between 2-29 characters long.", data);
                break;
            case "rn":
                Notifier.error("This nickname is reserved.", data);
                break;
            case "Bd":
                Notifier.error("You have been banned.", data);
                break;
            default:
                Notifier.error("Failed registering to the server.", data);
                break;
        }

    }).done(function () {
        window.location = "/games/";
    });
}