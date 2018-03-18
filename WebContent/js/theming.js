class Theming {

    static get DEFAULT_PRIMARY() {
        return "rgb(103,58,183)"
    }

    static get DEFAULT_SECONDARY() {
        return "rgb(0,150,136)"
    }

    static applyKnown(primary, secondary, save = true) {
        document.documentElement.style.setProperty('--mdc-theme-primary', primary);
        document.documentElement.style.setProperty('--mdc-theme-secondary', secondary);

        let themeColor = document.head.querySelector('meta[name=theme-color]');
        if (themeColor === null) {
            themeColor = document.createElement('meta');
            document.head.appendChild(themeColor);
        }

        themeColor.name = "theme-color";
        themeColor.content = primary;

        const appleMobileCapable = document.createElement("meta");
        appleMobileCapable.name = "apple-mobile-web-app-capable";
        appleMobileCapable.content = "yes";
        document.head.appendChild(appleMobileCapable);

        const appleStatusBar = document.createElement("meta");
        appleStatusBar.name = "apple-apple-mobile-web-app-status-bar-style-web-app-capable";
        appleStatusBar.content = "black-translucent";
        document.head.appendChild(appleStatusBar);

        if (save) {
            Cookies.set("PYX-Theme-Primary", primary);
            Cookies.set("PYX-Theme-Secondary", secondary);

            Requester.request("sup", {
                "up": {"TpC": primary, "TsC": secondary}
            })
        }
    }

    /**
     * @callback colorsCallback
     * @param {string} primary
     * @param {string} secondary
     */

    /**
     * @param {colorsCallback} listener
     */
    static get(listener) {
        const xmlhttp = new XMLHttpRequest();
        xmlhttp.onreadystatechange = function () {
            if (xmlhttp.readyState === XMLHttpRequest.DONE) {
                if (xmlhttp.status === 200) {
                    const data = JSON.parse(xmlhttp.responseText);
                    if ("TpC" in data && "TsC" in data) listener(data.TpC, data.TsC);
                    else listener(Theming.DEFAULT_PRIMARY, Theming.DEFAULT_SECONDARY);
                } else {
                    console.debug(xmlhttp);
                    const cookies = Theming.getFromCookies();
                    listener(cookies[0], cookies[1]);
                }
            }
        };

        xmlhttp.open("POST", "/AjaxServlet", true);
        xmlhttp.send("o=gup&up=TpC,TsC");
    }

    static getFromCookies() {
        const primaryCookie = Cookies.getJSON("PYX-Theme-Primary");
        const secondaryCookie = Cookies.getJSON("PYX-Theme-Secondary");
        if (primaryCookie === undefined || secondaryCookie === undefined) return [Theming.DEFAULT_PRIMARY, Theming.DEFAULT_SECONDARY];
        else return [primaryCookie, secondaryCookie];
    }

    static apply() {
        const cookies = Theming.getFromCookies();
        Theming.applyKnown(cookies[0], cookies[1], false);

        Theming.get((primary, secondary) => {
            Theming.applyKnown(primary, secondary, false);
        });
    }

    static clear() {
        Cookies.remove("PYX-Theme-Primary");
        Cookies.remove("PYX-Theme-Secondary");
    }
}

Theming.apply();