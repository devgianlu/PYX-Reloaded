window.onload = function () {
    $.post("AjaxServlet", "o=fl").fail(function (data) {
        alert("Error data: " + JSON.stringify(data));
    }).done(function (data) {
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

function register() {
    const nickname = $("input#nickname").val();

    $.post("AjaxServlet", "o=r&n=" + nickname).fail(function (data) {
        alert("Error data: " + JSON.stringify(data));
    }).done(function () {
        window.location = "lobbies.html";
    });
}