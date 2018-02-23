class BottomSheet {
    constructor(elem) {
        this.elem = $(elem);
    }

    get open() {
        return this.elem.hasClass('bottom-sheet--open');
    }

    set open(open) {
        if (open) this.elem.addClass('bottom-sheet--open');
        else this.elem.removeClass('bottom-sheet--open');
    }
}