class NicknameDialog {
    constructor() {
        this._dialog = $('#requestNicknameDialog');
        this.dialog = new mdc.dialog.MDCDialog(this._dialog[0]);
        this._dialog.find('._submit').on('click', () => this._accept());
        this.dialog.listen('MDCDialog:cancel', () => this._cancel());

        this._input = this._dialog.find('input');
        this.input = new mdc.textField.MDCTextField(this._input.parent()[0]);

        this.validation = new mdc.textField.MDCTextFieldHelperText(this._input.parent().next()[0]);
    }

    set error(error) {
        if (error === false) {
            this.input.valid = true;
            return;
        }

        this.input.valid = false;
        this.validation.foundation.setContent(error);
    }

    set accept(set) {
        this.acceptListener = set;
    }

    show() {
        this.error = false;
        this._input.val("");

        this.dialog.show();
    }

    close() {
        this.dialog.close();
    }

    _accept() {
        if (this.acceptListener !== null) this.acceptListener(this._input.val());
    }

    _cancel() {
        this.dialog.close();
    }
}

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

        this.pyxRegister = this._pyx.find('._register');
        this.pyxRegister.on('click', () => this.createPyxAccount());

        this._socials = $('#socialLogin');
        this.googleSignIn = this._socials.find('#googleSignIn');
        this.facebookSignIn = this._socials.find('#facebookSignIn');
        this.twitterSignIn = this._socials.find('#twitterSignIn');
        this.githubSignIn = this._socials.find('#githubSignIn');

        this.nickDialog = new NicknameDialog();

        this.statusPage = $('#statusPage');
        this.statusPage.on('click', () => redirectToStatusPage());
    }

    static _postLoggedIn() {
        window.location = "/games/";
    }

    static _ERR_MSG_IN() {
        return "This nickname must contain only alphanumeric characters and be between 2-29 characters long.";
    }

    static _ERR_MSG_EMIU() {
        return "This email is already registered. You may want to login instead.";
    }

    static _ERR_MSG_NIU() {
        return "This nickname has already been taken.";
    }

    static _ERR_MSG_GIT() {
        return "Invalid Google token. Please try again.";
    }

    static _ERR_MSG_FIT() {
        return "Invalid Facebook token. Please try again.";
    }

    static _ERR_MSG_GHIT() {
        return "Invalid Github token. Please try again.";
    }

    static _ERR_MSG_TWIT() {
        return "Invalid Twitter token. Please try again.";
    }

    static _ERR_MSG_TWEMNV() {
        return "You have to verify your Twitter account email.";
    }

    static _ERR_MSG_FBEMNV() {
        return "You have to verify your Facebook account email.";
    }

    static _handleGeneralLoginErrors(error) {
        switch (error.ec) {
            case "uaT":
                Notifier.error("This authentication method isn't supported by this server.", error);
                return true;
            case "niu":
                Notifier.error(LoginManager._ERR_MSG_NIU(), error);
                return true;
            case "emiu":
                Notifier.error(LoginManager._ERR_MSG_EMIU(), error);
                return true;
            case "in":
                Notifier.error(LoginManager._ERR_MSG_IN(), error);
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

    setupGoogle(appId) {
        if (appId === undefined) {
            this.googleSignIn.hide();
            return false;
        } else {
            $.getScript("https://apis.google.com/js/api:client.js", () => {
                this.googleSignIn.show();
                gapi.load('auth2', () => {
                    this.google_auth2 = gapi.auth2.init({
                        client_id: appId,
                        cookiepolicy: 'single_host_origin',
                    });

                    this.google_auth2.attachClickHandler(this.googleSignIn[0], {},
                        (user) => this._googleSuccess(user),
                        (error) => Notifier.error("Failed signing in with Google.", error));
                });
            });
            return true;
        }
    }

    setupFacebook(appId) {
        if (appId === undefined) {
            this.facebookSignIn.hide();
            return false;
        } else {
            $.getScript("https://connect.facebook.net/en_US/sdk.js", () => {
                FB.init({
                    appId: appId,
                    cookie: true,
                    xfbml: true,
                    autoLogAppEvents: Notifier._debug,
                    version: 'v2.12'
                });

                FB.AppEvents.logPageView();

                this.facebookSignIn.show();
                this.facebookSignIn.on('click', () => FB.login((user) => {
                    this._facebookSuccess(user);
                }, {
                    scope: 'email,public_profile',
                }));
            });
            return true;
        }
    }

    setupGitHub(appId) {
        if (appId === undefined) {
            this.githubSignIn.hide();
            return false;
        } else {
            this.githubSignIn.show();
            this.githubSignIn.on('click', () => {
                window.location = "https://github.com/login/oauth/authorize?scope=read:user,user:email&client_id=" + appId
            });
            return true;
        }
    }

    setupTwitter(appId) {
        if (appId === undefined) {
            this.twitterSignIn.hide();
            return false;
        } else {
            this.twitterSignIn.show();
            this.twitterSignIn.on('click', () => {
                window.location = "/TwitterStartAuthFlow"
            });
            return true;
        }
    }

    _showNickDialog(params, success) {
        this.nickDialog.accept = (nick) => {
            Requester.request("ca", Object.assign(params, {"n": nick}), (data) => {
                Notifier.debug(data);
                success();
                this.nickDialog.close();
            }, (error) => {
                if (error.ec === "in") {
                    Notifier.debug(error, true);
                    this.nickDialog.error = LoginManager._ERR_MSG_IN();
                    return;
                }

                if (error.ec === "niu") {
                    Notifier.debug(error, true);
                    this.nickDialog.error = LoginManager._ERR_MSG_NIU();
                    return;
                }

                this.nickDialog.close();
                switch (error.ec) {
                    case "emiu":
                        Notifier.error(LoginManager._ERR_MSG_EMIU(), error);
                        break;
                    case "fit":
                        Notifier.error(LoginManager._ERR_MSG_FIT(), error);
                        break;
                    case "git":
                        Notifier.error(LoginManager._ERR_MSG_GIT(), error);
                        break;
                    case "ghit":
                        Notifier.error(LoginManager._ERR_MSG_GHIT(), error);
                        break;
                    case "twit":
                        Notifier.error(LoginManager._ERR_MSG_TWIT(), error);
                        break;
                    case "twemnv":
                        Notifier.error(LoginManager._ERR_MSG_TWEMNV(), error);
                        break;
                    case "fbemnv":
                        Notifier.error(LoginManager._ERR_MSG_FBEMNV(), error);
                        break;
                }
            })
        };

        this.nickDialog.show();
    }

    _facebookSuccess(user) {
        const accessToken = user.authResponse.accessToken;
        Requester.request("r", {
            "aT": "fb",
            "fb": accessToken
        }, (data) => {
            Notifier.debug(data);
            LoginManager._postLoggedIn();
        }, (error) => {
            switch (error.ec) {
                case "fit":
                    Notifier.error(LoginManager._ERR_MSG_FIT(), error);
                    break;
                case "fbemnv":
                    Notifier.error(LoginManager._ERR_MSG_FBEMNV(), error);
                    break;
                case "fnr":
                    Notifier.error("Your Facebook account is not registered.", error);
                    this._showNickDialog({
                        "aT": "fb",
                        "fb": accessToken
                    }, () => Notifier.timeout(Notifier.SUCCESS, "Facebook account successfully registered. Sing in by pressing Facebook again."));
                    break;
                default:
                    Notifier.debug(error, true);
                    break;
            }
        })
    }

    _googleSuccess(user) {
        const id_token = user.getAuthResponse().id_token;
        Requester.request("r", {
            "aT": "g",
            "g": id_token
        }, (data) => {
            Notifier.debug(data);
            LoginManager._postLoggedIn();
        }, (error) => {
            switch (error.ec) {
                case "git":
                    Notifier.error(LoginManager._ERR_MSG_GIT(), error);
                    break;
                case "gnr":
                    Notifier.error("Your Google account is not registered.", error);
                    this._showNickDialog({
                        "aT": "g",
                        "g": id_token
                    }, () => Notifier.timeout(Notifier.SUCCESS, "Google account successfully registered. Sing in by pressing Google again."));
                    break;
                default:
                    Notifier.debug(error, true);
                    break;
            }
        });
    }

    _githubSuccess(token) {
        Requester.request("r", {
            "aT": "gh",
            "gh": token
        }, (data) => {
            Notifier.debug(data);
            LoginManager._postLoggedIn();
        }, (error) => {
            switch (error.ec) {
                case "ghit":
                    Notifier.error(LoginManager._ERR_MSG_GHIT(), error);
                    break;
                case "ghnr":
                    Notifier.error("Your Github account is not registered.", error);
                    this._showNickDialog({
                        "aT": "gh",
                        "gh": token
                    }, () => Notifier.timeout(Notifier.SUCCESS, "Github account successfully registered. Sing in by pressing Github again."));
                    break;
                default:
                    Notifier.debug(error, true);
                    break;
            }
        });
    }

    _twitterSuccess(tokens) {
        Requester.request("r", {
            "aT": "tw",
            "tw": tokens
        }, (data) => {
            Notifier.debug(data);
            LoginManager._postLoggedIn();
        }, (error) => {
            switch (error.ec) {
                case "twit":
                    Notifier.error(LoginManager._ERR_MSG_TWIT(), error);
                    break;
                case "twemnv":
                    Notifier.error(LoginManager._ERR_MSG_TWEMNV(), error);
                    break;
                case "twnr":
                    Notifier.error("Your Twitter account is not registered.", error);
                    this._showNickDialog({
                        "aT": "tw",
                        "tw": tokens
                    }, () => Notifier.timeout(Notifier.SUCCESS, "Twitter account successfully registered. Sing in by pressing Twitter again."));
                    break;
                default:
                    Notifier.debug(error, true);
                    break;
            }
        });
    }

    setup() {
        Requester.request("fl", {}, (data) => {
                /**
                 * @param {string} data.ssp - Server status page
                 * @param {object} data.aC - Auth config
                 * @param {string} data.aC.g - Google app id
                 * @param {string} data.aC.fb - Facebook app id
                 * @param {string} data.aC.gh - Github app id
                 * @param {string} data.aC.tw - Twitter app id
                 * @param {string} data.aC.pw - Verification email sender
                 */
                data.css.sort(function (a, b) {
                    return a.w - b.w;
                });

                localStorage['css'] = JSON.stringify(data.css);
                localStorage['dgo'] = JSON.stringify(data.dgo);

                if (data.ssp === undefined || data.ssp === null) delete localStorage['ssp'];
                else localStorage['ssp'] = data.ssp;

                if (data.ip) {
                    if (data.next === "game") window.location = "/games/?gid=" + data.gid;
                    else window.location = "/games/";
                } else {
                    const authConfig = data.aC;
                    Notifier.debug(authConfig);

                    if ("pw" in authConfig) {
                        this.pyxEmail.parent().show();
                        this.pyxRegister.show();
                    } else {
                        this.pyxEmail.parent().hide();
                        this.pyxRegister.hide();
                    }

                    if (!this.setupGoogle(authConfig.g)
                        && !this.setupFacebook(authConfig.fb)
                        && !this.setupGitHub(authConfig.gh)
                        && !this.setupTwitter(authConfig.tw)) {

                        this._socials.hide();
                    } else {
                        this._socials.show();
                        switch (getURLParameter("aT")) {
                            case "gh":
                                const token = Cookies.getJSON("PYX-Github-Token");
                                Cookies.remove("PYX-Github-Token");
                                this._githubSuccess(token);
                                break;
                            case "tw":
                                const tokens = Cookies.getJSON("PYX-Twitter-Token");
                                Cookies.remove("PYX-Twitter-Token");
                                this._twitterSuccess(tokens);
                                break;
                        }
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

function getURLParameter(name) {
    return decodeURIComponent((new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)').exec(location.search) || [null, ''])[1].replace(/\+/g, '%20')) || null;
}

const loginManager = new LoginManager();
loginManager.setup();