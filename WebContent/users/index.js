class UsersManager {
    constructor() {
        this.main = $('main');

        this.message = this.main.find('.message');
        this.users = new List(this.main[0], {
            item: 'userTemplate',
            valueNames: ['_nick', '_role', {attr: 'src', name: '_img'}]
        });

        this.refresh = $('#refresh');
        this.refresh.on('click', () => this.loadUsers());
    }

    loadUsers() {
        Requester.request("gn", {}, (data) => {
            /**
             * @param {object[]} data.nl - Names list
             * @param {string} data.nl[].n - Nickname
             * @param {string} data.nl[].ia - Whether the user is an admin
             * @param {string} data.nl[].ha - Whether the user has an account
             * @param {string} data.nl[].p - Profile picture
             */

            this.users.clear();
            const list = data.nl;
            for (let i = 0; i < list.length; i++) {
                const user = list[i];
                this.users.add({
                    "_nick": user.n,
                    "_role": user.ia ? "Admin" : (user.ha ? "Registered" : "Guest"),
                    "_img": user.p === undefined || user.p === null ? "/css/images/no-profile.svg" : user.p
                });
            }

            if (list.length === 0) this.message.show();
            else this.message.hide();
        }, (error) => {
            Notifier.error("Failed loading users!", error);
        });
    }
}