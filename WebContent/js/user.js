class UserManager {

    constructor(nickname) {
        this.nickname = nickname;
    }

    setup() {
        Requester.request("gui", {"n": this.nickname}, (data) => {

        }, (error) => {

        });
    }
}

const userManager = new UserManager(getLastPathSegment());
userManager.setup();

function getLastPathSegment() {
    return decodeURIComponent(new RegExp('[^\\/]+(?=\\/$|$)').exec(window.location.href) || [null, '']) || null;
}