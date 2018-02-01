class Notifier {

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

    static _notyDefault() {
        return {
            theme: 'mdc'
        }
    }

    static show(type, msg) {
        const noty = new Noty(Object.assign({type: type, text: msg}, Notifier._notyDefault()));
        noty.show();
        return noty;
    }

    static countdown(type, msg_before, interval, msg_after) {
        const noty = new Noty(Object.assign({
            type: type,
            text: msg_before + interval + msg_after,
            timeout: interval * 1000
        }, Notifier._notyDefault()));

        let clearOut = interval;
        window.setInterval(function () {
            if (clearOut === 0) {
                window.clearInterval(this);
                return;
            }

            clearOut--;
            noty.setText(msg_before + clearOut + msg_after);
        }, 1000);

        noty.show();
        return noty;
    }
}

Notifier.show(Notifier.INFO, "Some notification text");
Notifier.show(Notifier.ALERT, "Some notification text");
Notifier.show(Notifier.SUCCESS, "Some notification text");
Notifier.show(Notifier.WARN, "Some notification text");
Notifier.countdown(Notifier.ERROR, "A new round will begin in ", 8, " seconds.");