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
        Requester.request("fl", {}, (data) => {
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
            }, null,
            (error) => {
                Notifier.error("Failed contacting the server!", error);
            });
    }

    _handlePossibleRegister(ev) {
        if (ev !== undefined && ev.keyCode !== 13) return;
        Requester.request("r", {
            "aT": "pw",
            "n": this.loginNickname.val(),
            "pw": this.loginPassword.val()
        }, (data) => {
            Notifier.debug(data);
            LoginManager._postLoggedIn();
        }, (error) => {
            switch (error.ec) {
                case "wp":
                    Notifier.error("Wrong password!", error);
                    break;
                case "niu":
                    Notifier.error("This nickname is already in use.", error);
                    break;
                case "tmu":
                    Notifier.error("The server is full! Please try again later.", error);
                    break;
                case "in":
                    Notifier.error("This nickname must contain only alphanumeric characters and be between 2-29 characters long.", error);
                    break;
                case "Bd":
                    Notifier.error("You have been banned.", error);
                    break;
                default:
                    Notifier.error("Failed registering to the server.", error);
                    break;
            }
        });

    }
}

const loginManager = new LoginManager();
loginManager.setup();