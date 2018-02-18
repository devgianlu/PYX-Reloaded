class TabsManager {
    constructor(id, indexToId, defaultIndex = 0) {
        this.indexToElm = TabsManager._createIndexToElement(indexToId);

        this.tabs = new mdc.tabs.MDCTabBar(document.getElementById(id));
        this.tabs.listen('MDCTabBar:change', ({detail: tabs}) => {
            this._handleTabChange(tabs.activeTabIndex);
        });

        this._handleTabChange(defaultIndex)
    }

    static _createIndexToElement(indexToId) {
        const indexToElm = [];
        for (let i = 0; i < indexToId.length; i++) indexToElm[i] = $('#' + indexToId[i]);
        return indexToElm;
    }

    static init(id, indexToId, defaultIndex = 0) {
        new TabsManager(id, indexToId, defaultIndex);
    }

    _handleTabChange(index) {
        for (let i = 0; i < this.indexToElm.length; i++) {
            const elm = this.indexToElm[i];
            if (i === index) elm.show();
            else elm.hide();
        }
    }
}