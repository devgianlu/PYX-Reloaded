class LoginManager {
    constructor() {
        this._pyx = $('#login');
        this.pyxNickname = this._pyx.find('input[type=text]');
        this.pyxNickname.on('keydown', (ev) => this._handlePossibleRegister(ev));
        this.pyxPassword = this._pyx.find('input[type=password]');
        this.pyxPassword.on('keydown', (ev) => this._handlePossibleRegister(ev));
        this.pyxPassword = this._pyx.find('input[type=password]');
        this.pyxSubmit = this._pyx.find('._signIn');
        this.pyxSubmit.on('click', () => this._handlePossibleRegister(undefined));

        this.pyxEmail = this._pyx.find('input[type=email]');

        this.pyxSubmit = this._pyx.find('._register');
        this.pyxSubmit.on('click', () => this.createPyxAccount());

        this._socials = $('#socialLogin');
        this.googleSignIn = this._socials.find('#googleSignIn');
    }

    static _postLoggedIn() {
        window.location = "/games/";
    }

    static _handleGeneralLoginErrors(error) {
        switch (error.ec) {
            case "niu":
                Notifier.error("This nickname is already in use.", error);
                return true;
            case "in":
                Notifier.error("This nickname must contain only alphanumeric characters and be between 2-29 characters long.", error);
                return true;
            case "Bd":
                Notifier.error("You have been banned.", error);
                return true;
        }

        return false;
    }

    createPyxAccount() {
        Requester.request("ca", {
            "aT": "pw",
            "n": this.pyxNickname.val(),
            "pw": this.pyxPassword.val(),
            "em": this.pyxEmail.val()
        }, (data) => {
            Notifier.debug(data);
            Notifier.timeout(Notifier.SUCCESS, "Account created successfully! You can now sign in.");
        }, (error) => {
            if (LoginManager._handleGeneralLoginErrors(error)) return;
            if (error.ec === "br") Notifier.error("Please check all the fields are filled correctly.", error);
            else Notifier.debug("Failed creating an account.", error)
        })
    }

    setupGoogle() {
        gapi.load('auth2', () => {
            this.google_auth2 = gapi.auth2.init({
                client_id: '464424051073-loi5kmcc3qev5a1lr5hvrjiqb3kcu2cl.apps.googleusercontent.com',
                cookiepolicy: 'single_host_origin',
            });

            this.google_auth2.attachClickHandler(this.googleSignIn[0], {},
                (user) => this._googleSuccess(user),
                (error) => this._googleError(error));
        });
    }

    _googleSuccess(user) {
        Requester.request("r", {
            "aT": "g",
            "g": user.getAuthResponse().id_token
        }, (data) => {
            Notifier.debug(data);
        }, (error) => {
            switch (error.ec) {
                case "git":
                    Notifier.error("Invalid Google token. Please try again.", error);
                    break;
                case "gnr":
                    Notifier.error("Your Google account is not registered.", error); // TODO: Ask to create one immediately
                    break;
            }
        });
    }

    _googleError(error) {
        Notifier.error("Failed signing in with Google.", error);
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
                } else {
                    this.setupGoogle();
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
            "n": this.pyxNickname.val(),
            "pw": this.pyxPassword.val()
        }, (data) => {
            Notifier.debug(data);
            LoginManager._postLoggedIn();
        }, (error) => {
            if (LoginManager._handleGeneralLoginErrors(error)) return;

            switch (error.ec) {
                case "wp":
                    Notifier.error("Wrong password or nickname already taken.", error);
                    break;
                case "tmu":
                    Notifier.error("The server is full! Please try again later.", error);
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