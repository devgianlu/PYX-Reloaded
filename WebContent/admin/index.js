class AdminPanel {
    constructor() {
        this.root = $('main');
        this.prepareShutdown = this.root.find('#prepareShutdown');
        this.prepareShutdown.on('click', () => {
            Requester.request("ps", {}, () => {
                Notifier.timeout(Notifier.SUCCESS, "Server is preparing for shutdown.");
            }, (error) => {
                Notifier.error("Failed preparing for shutdown.", error);
            })
        });
    }
}

function load() {
    Requester.request("gme", {}, (data) => {
        if (data.a === undefined || !data.a.ia) {
            Notifier.debug("Not admin: " + JSON.stringify(data), true);
            window.location = "/";
        }

        Notifier.debug(data);
        new AdminPanel();
    }, (error) => {
        Notifier.error("Failed loading!", error);
    })
}