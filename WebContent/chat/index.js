class ChatManager {
    constructor() {
        this._main = $('main');
        this.chat = new List(this._main[0], {
            item: 'chatMessageTemplate',
            valueNames: ['_text', '_img', '_nick']
        });
        this.chat.clear();

        this.noMessages = this._main.find('.message');

        this._chatMessage = this._main.find('#chatMessage');
        this._chatMessage.on('keydown', (ev) => this._handleSendChatMessage(ev));
        this._chatMessage.parent().find('.mdc-text-field__icon').on('click', () => this._handleSendChatMessage(undefined));

        registerPollListener("GLOBAL_CHAT", (data) => {
            Notifier.debug(data, false);
            if (data.E === "C" && data.gid === undefined) {
                this._handleChatMessage(data);
            }
        });
    }

    /**
     * @param {String} data.m - Message
     * @param {String} data.f - Sender
     * @private
     */
    _handleChatMessage(data) {
        this.chat.add({
            '_nick': data.f,
            '_text': data.m
        });

        this.noMessages.hide();
    }

    _handleSendChatMessage(ev) {
        if (ev !== undefined && ev.keyCode !== 13) return;

        const msg = this._chatMessage.val();
        if (msg.length === 0) return;

        this.sendGameChatMessage(msg);
    }

    sendGameChatMessage(msg) {
        Requester.request("c", {"m": msg}, () => {
            this._chatMessage.next().removeClass("mdc-text-field__label--float-above");
            this._chatMessage.val("");
            this._chatMessage.blur();
        }, (error) => {
            switch (error.ec) {
                case "tf":
                    Notifier.timeout(Notifier.WARN, "You are chatting too fast. Calm down.");
                    break;
                case "anv":
                    Notifier.error("You must be registered (and have verified your email) to send messages in the global chat.", error);
                    break;
                default:
                    Notifier.error("Failed sending the message!", error);
                    break;
            }
        });
    }
}