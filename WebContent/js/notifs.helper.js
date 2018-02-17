class Notifier {

    static get _debug() {
        return true;
    }

    static get INFO() {
        return "info";
    }

    static get ALERT() {
        return "alert";
    }

    static get SUCCESS() {
        return "success";
    }

    static get ERROR() {
        return "error";
    }

    static get WARN() {
        return "warning";
    }

    static get DEFAULT_TIMEOUT() {
        return 3;
    }

    static _notyDefault() {
        return {
            theme: 'mdc'
        }
    }

    static button(text, cb) {
        return Noty.button(text, 'mdc-button', cb);
    }

    static show(type, msg, timeout = false, progressBar = false, show = true, ...buttons) {
        const noty = new Noty(Object.assign({
            type: type,
            text: msg,
            buttons: buttons,
            timeout: timeout * 1000,
            progressBar: progressBar
        }, Notifier._notyDefault()));
        if (show) noty.show();
        return noty;
    }

    static timeout(type, msg, progressBar = false) {
        return Notifier.show(type, msg, this.DEFAULT_TIMEOUT, progressBar);
    }

    static countdown(type, msg_before, interval, msg_after) {
        const noty = new Noty(Object.assign({
            type: type,
            progressBar: true,
            text: msg_before + interval + msg_after,
            timeout: interval * 1000
        }, Notifier._notyDefault()));

        let clearOut = interval;
        window.setInterval(function () {
            if (clearOut === 0) {
                window.clearInterval(this);
                noty.close();
                return;
            }

            clearOut--;
            noty.setText(msg_before + clearOut + msg_after);
        }, 1000);

        noty.show();
        return noty;
    }

    static error(msg, data = undefined, progressBar = false, overrideRelocate = false) {
        if (Notifier._debug) console.error(msg);

        if (data !== undefined) {
            if ("ec" in data) {
                if (Notifier._debug) console.error(data);
                if (!overrideRelocate && (data.ec === "nr" || data.ec === "se")) window.location = "/";
            } else if ("responseJSON" in data) {
                if (Notifier._debug) {
                    console.error(data);
                    console.error(data.responseJSON);
                }

                if (!overrideRelocate && (data.responseJSON.ec === "nr" || data.responseJSON.ec === "se")) window.location = "/";
            } else {
                if (Notifier._debug) console.error(data);
            }
        }

        return Notifier.show(Notifier.ERROR, msg, this.DEFAULT_TIMEOUT * 1.5, progressBar);
    }

    static debug(data, error = false) {
        if (Notifier._debug)
            if (error) console.error(data);
            else console.log(data);
    }
}