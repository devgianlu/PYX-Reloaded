class LoginManager {
    constructor() {
        this._login = $('#login');
        this.loginNickname = this._login.find('input[type=text]');
        this.loginNickname.on('keydown', (ev) => this._handlePossibleRegister(ev));
        this.loginPassword = this._login.find('input[type=password]');
        this.loginPassword.on('keydown', (ev) => this._handlePossibleRegister(ev));
        this.loginSubmit = this._login.find('.mdc-button');
        this.loginSubmit.on('click', () => this._handlePossibleRegister(undefined));

        this._socials = $('#socialLogin');
    }

    static _postLoggedIn() {
        window.location = "/games/";
    }

    setup() {
        $.post("/AjaxServlet", "o=fl").fail((data) => {
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
    }

    _handlePossibleRegister(ev) {
        if (ev !== undefined && ev.keyCode !== 13) return;
        $.post("/AjaxServlet", "o=r&aT=pw&n=" + this.loginNickname.val() + "&pw=" + this.loginPassword.val()).fail((data) => {
            switch (data.responseJSON.ec) {
                case "wp":
                    Notifier.error("Wrong password!", data);
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
                case "Bd":
                    Notifier.error("You have been banned.", data);
                    break;
                default:
                    Notifier.error("Failed registering to the server.", data);
                    break;
            }
        }).done((data) => {
            Notifier.debug(data);
            LoginManager._postLoggedIn();
        });
    }
}

const loginManager = new LoginManager();
loginManager.setup();