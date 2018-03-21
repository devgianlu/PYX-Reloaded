class EventsReceiver {

    constructor() {
        this.ws = new WebSocket((location.protocol === "https:" ? "wss" : "ws") + "://" + location.hostname + (location.port ? (":" + location.port) : "") + "/Events");
        this.ws.onmessage = (msg) => this.handleMessage(msg);
        this.eventListeners = {};
    }

    /**
     * @callback eventCallback
     * @param {string} data.E - Event code
     */

    /**
     * @param {String} key
     * @param {eventCallback} listener
     */
    register(key, listener) {
        this.eventListeners[key] = listener;
    }

    handleMessage(event) {
        /** @param {object[]} data.Es - Events **/
        const data = JSON.parse(event.data);
        if ("e" in data) {
            Notifier.debug(data, true);
            if (data.ec === "nr" || data.ec === "se") window.location = "/";
            return;
        }

        const events = data.Es;
        if (events.length === 0) return;

        this.processEvents(events);
    }

    /**
     * @param {string} events[].E - Event code
     */
    processEvents(events) {
        for (let i = 0; i < events.length; i++) {
            const event = events[i];
            switch (event.E) {
                case "pp":
                    Requester.request("PP", {});
                    continue;
                case "PS":
                    /** @param {int} event.bs - Time before shutdown */
                    let bs;
                    if (event.bs >= 60000) bs = Math.ceil(event.bs / 60000) + " minutes";
                    else bs = Math.ceil(event.bs / 1000) + " seconds";

                    Notifier.show(Notifier.WARN, "The server is preparing for shutdown. The server will be shutdown in " + bs, 20, false);
                    continue;
                case "SS":
                    window.location = "https://google.com"; // TODO: Redirect to server status page
                    continue;
                case "B&":
                case "kk":
                    window.location = "/"; // TODO: Redirect to banned/kicked page
                    continue;
                default:
                    for (const key in this.eventListeners) {
                        if (this.eventListeners.hasOwnProperty(key))
                            this.eventListeners[key](event);
                    }
            }
        }
    }

    close() {
        this.ws.close(1000);
    }
}

window.eventsReceiver = new EventsReceiver();



