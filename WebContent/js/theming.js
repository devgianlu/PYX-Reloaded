function applyCustomTheme() {
    const primary = Cookies.getJSON("PYX-Theme-Primary");
    const secondary = Cookies.getJSON("PYX-Theme-Secondary");
    const background = Cookies.getJSON("PYX-Theme-Background");

    if (primary === undefined || secondary === undefined || background === undefined) return;

    setPrimaryColor(primary);
    setSecondaryColor(secondary);
    setBackgroundColor(background);
}

function setPrimaryColor(color) {
    document.documentElement.style.setProperty('--mdc-theme-primary', color);
    Cookies.set("PYX-Theme-Primary", color);
}

function setSecondaryColor(color) {
    document.documentElement.style.setProperty('--mdc-theme-secondary', color);
    Cookies.set("PYX-Theme-Secondary", color);
}

function setBackgroundColor(color) {
    document.documentElement.style.setProperty('--mdc-theme-background', color);
    Cookies.set("PYX-Theme-Background", color);
}

function clearColors() {
    Cookies.remove("PYX-Theme-Primary");
    Cookies.remove("PYX-Theme-Secondary");
    Cookies.remove("PYX-Theme-Background");
}

function showThemingDialog() {
    const themingDialog = new mdc.dialog.MDCDialog(document.getElementById('themingDialog'));
    themingDialog.listen('MDCDialog:accept', function () {
    });
    themingDialog.listen('MDCDialog:cancel', function () {
    });

    themingDialog.show();
}