const DEFAULT_PRIMARY = "rgb(103,58,183)";
const DEFAULT_SECONDARY = "rgb(0,150,136)";

class Theming {

    static setPrimaryColor(color) {
        document.documentElement.style.setProperty('--mdc-theme-primary', color);
        Cookies.set("PYX-Theme-Primary", color);
    }

    static setSecondaryColor(color) {
        document.documentElement.style.setProperty('--mdc-theme-secondary', color);
        Cookies.set("PYX-Theme-Secondary", color);
    }

    static apply() {
        const primary = Cookies.get("PYX-Theme-Primary");
        const secondary = Cookies.get("PYX-Theme-Secondary");

        if (primary === undefined || secondary === undefined) {
            Theming.setPrimaryColor(DEFAULT_PRIMARY);
            Theming.setSecondaryColor(DEFAULT_SECONDARY);
        } else {
            Theming.setPrimaryColor(primary);
            Theming.setSecondaryColor(secondary);

            const themeColor = document.createElement("meta");
            themeColor.name = "theme-color";
            themeColor.content = primary;
            document.head.appendChild(themeColor);
        }
    }

    static clear() {
        Cookies.remove("PYX-Theme-Primary");
        Cookies.remove("PYX-Theme-Secondary");
    }
}

Theming.apply();