function init() {
    const self = new MaterialCustomizer(document.querySelector("#wheel > svg"));
    const request = new XMLHttpRequest;
    request.onload = function () {
        self.template = this.responseText;
        self.highlightField("Indigo");
        self.highlightField("Pink");
        window.requestAnimationFrame(function () {
            self.updateStylesheet();
        });
    };
    request.open("get", "./index_files/material.min.css.template", true);
    request.send();
}

MaterialCustomizer = function () {
    function getTarget(e) {
        return e.parentElement || e.parentNode;
    }

    const colors = ["Cyan", "Teal", "Green", "Light Green", "Lime", "Yellow", "Amber", "Orange", "Brown", "Blue Grey", "Grey", "Deep Orange", "Red", "Pink", "Purple", "Deep Purple", "Indigo", "Blue", "Light Blue"];
    const forbiddenAccents = ["Blue Grey", "Brown", "Grey"];
    const channels = ["Red", "Pink", "Purple", "Deep Purple", "Indigo", "Blue", "Light Blue", "Cyan", "Teal", "Green", "Light Green", "Lime", "Yellow", "Amber", "Orange", "Deep Orange", "Brown", "Grey", "Blue Grey"];
    const lightnessIndices = ["50", "100", "200", "300", "400", "500", "600", "700", "800", "900", "A100", "A200", "A400", "A700"];
    const palettes = [["255,235,238", "255,205,210", "239,154,154", "229,115,115", "239,83,80", "244,67,54", "229,57,53", "211,47,47", "198,40,40", "183,28,28", "255,138,128", "255,82,82", "255,23,68", "213,0,0"], ["252,228,236", "248,187,208", "244,143,177", "240,98,146", "236,64,122", "233,30,99", "216,27,96", "194,24,91", "173,20,87", "136,14,79", "255,128,171", "255,64,129", "245,0,87", "197,17,98"], ["243,229,245", "225,190,231", "206,147,216", "186,104,200", "171,71,188", "156,39,176", "142,36,170",
        "123,31,162", "106,27,154", "74,20,140", "234,128,252", "224,64,251", "213,0,249", "170,0,255"], ["237,231,246", "209,196,233", "179,157,219", "149,117,205", "126,87,194", "103,58,183", "94,53,177", "81,45,168", "69,39,160", "49,27,146", "179,136,255", "124,77,255", "101,31,255", "98,0,234"], ["232,234,246", "197,202,233", "159,168,218", "121,134,203", "92,107,192", "63,81,181", "57,73,171", "48,63,159", "40,53,147", "26,35,126", "140,158,255", "83,109,254", "61,90,254", "48,79,254"], ["227,242,253",
        "187,222,251", "144,202,249", "100,181,246", "66,165,245", "33,150,243", "30,136,229", "25,118,210", "21,101,192", "13,71,161", "130,177,255", "68,138,255", "41,121,255", "41,98,255"], ["225,245,254", "179,229,252", "129,212,250", "79,195,247", "41,182,246", "3,169,244", "3,155,229", "2,136,209", "2,119,189", "1,87,155", "128,216,255", "64,196,255", "0,176,255", "0,145,234"], ["224,247,250", "178,235,242", "128,222,234", "77,208,225", "38,198,218", "0,188,212", "0,172,193", "0,151,167", "0,131,143",
        "0,96,100", "132,255,255", "24,255,255", "0,229,255", "0,184,212"], ["224,242,241", "178,223,219", "128,203,196", "77,182,172", "38,166,154", "0,150,136", "0,137,123", "0,121,107", "0,105,92", "0,77,64", "167,255,235", "100,255,218", "29,233,182", "0,191,165"], ["232,245,233", "200,230,201", "165,214,167", "129,199,132", "102,187,106", "76,175,80", "67,160,71", "56,142,60", "46,125,50", "27,94,32", "185,246,202", "105,240,174", "0,230,118", "0,200,83"], ["241,248,233", "220,237,200", "197,225,165",
        "174,213,129", "156,204,101", "139,195,74", "124,179,66", "104,159,56", "85,139,47", "51,105,30", "204,255,144", "178,255,89", "118,255,3", "100,221,23"], ["249,251,231", "240,244,195", "230,238,156", "220,231,117", "212,225,87", "205,220,57", "192,202,51", "175,180,43", "158,157,36", "130,119,23", "244,255,129", "238,255,65", "198,255,0", "174,234,0"], ["255,253,231", "255,249,196", "255,245,157", "255,241,118", "255,238,88", "255,235,59", "253,216,53", "251,192,45", "249,168,37", "245,127,23",
        "255,255,141", "255,255,0", "255,234,0", "255,214,0"], ["255,248,225", "255,236,179", "255,224,130", "255,213,79", "255,202,40", "255,193,7", "255,179,0", "255,160,0", "255,143,0", "255,111,0", "255,229,127", "255,215,64", "255,196,0", "255,171,0"], ["255,243,224", "255,224,178", "255,204,128", "255,183,77", "255,167,38", "255,152,0", "251,140,0", "245,124,0", "239,108,0", "230,81,0", "255,209,128", "255,171,64", "255,145,0", "255,109,0"], ["251,233,231", "255,204,188", "255,171,145", "255,138,101",
        "255,112,67", "255,87,34", "244,81,30", "230,74,25", "216,67,21", "191,54,12", "255,158,128", "255,110,64", "255,61,0", "221,44,0"], ["239,235,233", "215,204,200", "188,170,164", "161,136,127", "141,110,99", "121,85,72", "109,76,65", "93,64,55", "78,52,46", "62,39,35"], ["250,250,250", "245,245,245", "238,238,238", "224,224,224", "189,189,189", "158,158,158", "117,117,117", "97,97,97", "66,66,66", "33,33,33"], ["236,239,241", "207,216,220", "176,190,197", "144,164,174", "120,144,156", "96,125,139",
        "84,110,122", "69,90,100", "55,71,79", "38,50,56"]];

    const jQuery = function (context) {
        this.wheel = context;
        this.paletteIndices = channels;
        this.lightnessIndices = lightnessIndices;
        this.palettes = palettes;
        this.init_();
    };

    jQuery.prototype.init_ = function () {
        this.config = {
            width: 650,
            height: 650,
            r: 250,
            ri: 100,
            hd: 40,
            c: 40,
            mrs: 0.5,
            alphaIncr: 0.005,
            colors: colors
        };
        this.forbiddenAccents = forbiddenAccents;
        this.calculateValues_();
        if (this.wheel) {
            this.buildWheel_();
        }
    };

    jQuery.prototype.calculateValues_ = function () {
        const params = this.config;
        params.alphaDeg = 360 / params.colors.length;
        params.alphaRad = params.alphaDeg * Math.PI / 180;
        params.rs = (params.c + params.r) * Math.sin(params.alphaRad / 2);
        params.rs *= params.mrs;
        params.selectorAlphaRad = 2 * Math.atan(params.rs / params.c);
        params.gamma1 = params.alphaRad / 2 - params.selectorAlphaRad / 2;
        params.gamma2 = params.alphaRad / 2 + params.selectorAlphaRad / 2;
        params.cx = (params.c + params.r) * Math.sin(params.alphaRad) / 2;
        params.cy = -(params.c + params.r) * (1 + Math.cos(params.alphaRad)) / 2;
        this.config = params;
    };

    jQuery.prototype.buildWheel_ = function () {
        const opts = this.config;
        const r = this.wheel.querySelector("g.wheel--maing");
        const parent = this.wheel.parentNode;
        this.wheel.setAttribute("viewBox", "0 0 " + this.config.width + " " + this.config.height);
        this.wheel.setAttribute("preserveAspectRatio", "xMidYMid meet");
        this.wheel.setAttribute("width", this.config.width);
        this.wheel.setAttribute("height", this.config.height);
        const frag = this.generateFieldTemplate_();
        const svgns = "http://www.w3.org/2000/svg";
        opts.colors.forEach(function (value, i) {
            const el = frag.cloneNode(true);
            const elem = document.createElement("div");
            for (let x = 1; x <= 2; x++) {
                const g = document.createElementNS(svgns, "g");
                const text = document.createElementNS(svgns, "text");
                text.setAttribute("class", "label label--" + x);
                text.setAttribute("transform", "rotate(" + -opts.alphaDeg * i + ")");
                text.setAttribute("dy", "0.5ex");
                /** @type {string} */
                text.textContent = "" + x;
                g.appendChild(text);
                g.setAttribute("transform", "translate(" + opts.cx + "," + opts.cy + ")");
                el.appendChild(g);
            }
            el.setAttribute("data-color", value);
            /** @type {string} */
            el.id = value;
            /** @type {string} */
            el.querySelector(".polygons > *:nth-child(1)").style.fill = "rgb(" + this.getColor(value, "500") + ")";
            /** @type {string} */
            el.querySelector(".polygons > *:nth-child(2)").style.fill = "rgb(" + this.getColor(value, "700") + ")";
            el.querySelector(".polygons").addEventListener("click", this.fieldClicked_.bind(this));
            el.setAttribute("transform", "rotate(" + opts.alphaDeg * i + ")");
            r.appendChild(el);
            elem.setAttribute("for", value);
            /** @type {string} */
            elem.className = "mdl-tooltip mdl-tooltip--large";
            /** @type {string} */
            elem.innerHTML = value;
            parent.appendChild(elem);
        }.bind(this));
        r.setAttribute("transform", "translate(" + opts.width / 2 + "," + opts.height / 2 + ")");
    };

    jQuery.prototype.generateFieldTemplate_ = function () {
        const svgns = "http://www.w3.org/2000/svg";
        const params = this.config;
        const el = document.createElementNS(svgns, "g");
        const g = document.createElementNS(svgns, "g");
        const shape = document.createElementNS(svgns, "polygon");
        shape.setAttribute("points", [[params.ri * Math.sin(params.alphaRad + params.alphaIncr), -params.ri * Math.cos(params.alphaRad + params.alphaIncr)].join(","), [params.r * Math.sin(params.alphaRad + params.alphaIncr), -params.r * Math.cos(params.alphaRad + params.alphaIncr)].join(","), [0, -params.r].join(","), [0, -(params.ri + params.hd)].join(",")].join(" "));
        const path = document.createElementNS(svgns, "polygon");
        path.setAttribute("points", [[params.ri * Math.sin(params.alphaRad + params.alphaIncr), -params.ri * Math.cos(params.alphaRad + params.alphaIncr)].join(","), [(params.ri + params.hd) * Math.sin(params.alphaRad + params.alphaIncr), -(params.ri + params.hd) * Math.cos(params.alphaRad + params.alphaIncr)].join(","), [0, -(params.ri + params.hd)].join(","), [0, -params.ri].join(",")].join(" "));
        g.appendChild(shape);
        g.appendChild(path);
        g.setAttribute("class", "polygons");
        el.appendChild(g);
        const node = document.createElementNS(svgns, "path");
        node.setAttribute("class", "selector");
        node.setAttribute("d", " M " + params.r * Math.sin(params.alphaRad) / 2 + " " + -(params.r * (1 + Math.cos(params.alphaRad)) / 2) + " L " + (params.cx - params.rs * Math.cos(params.gamma1)) + " " + (params.cy - params.rs * Math.sin(params.gamma1)) + " A " + params.rs + " " + params.rs + " " + params.alphaDeg + " 1 1 " + (params.cx + params.rs * Math.cos(params.gamma2)) + " " + (params.cy + params.rs * Math.sin(params.gamma2)) + " z ");
        el.appendChild(node);
        return el;
    };

    jQuery.prototype.getNumSelected = function () {
        return this.wheel.querySelector(".selected--2") ? 2 : this.wheel.querySelector(".selected--1") ? 1 : 0;
    };

    jQuery.prototype.fieldClicked_ = function (e) {
        const target = getTarget(getTarget(e.target));
        const getNumSelected = this.getNumSelected();
        if ((target.getAttribute("class") || "").indexOf("selected--1") === -1 || 1 !== getNumSelected) {
            switch (getNumSelected) {
                case 1:
                    if (this.forbiddenAccents.indexOf(target.getAttribute("data-color")) !== -1) {
                        return;
                    }
                    this.highlightField(target.getAttribute("data-color"));
                    this.wheel.setAttribute("class", "");
                    window.requestAnimationFrame(function () {
                        this.updateStylesheet();
                    }.bind(this));
                    break;
                case 2:
                    Array.prototype.forEach.call(this.wheel.querySelector("g.wheel--maing").children, function (section) {
                        section.setAttribute("class", "");
                        section.querySelector(".polygons").setAttribute("filter", "");
                    });
                case 0:
                    this.highlightField(target.getAttribute("data-color"));
                    window.requestAnimationFrame(function () {
                        this.wheel.setAttribute("class", "hide-nonaccents");
                    }.bind(this));
            }
        }
    };

    jQuery.prototype.replaceDict = function (s, pairs) {
        for (let r in pairs) {
            if (pairs.hasOwnProperty(r)) {
                s = s.replace(new RegExp(r, "g"), pairs[r]);
            }
        }

        return s;
    };

    jQuery.prototype.getSelectedPrimary = function () {
        return this.wheel.querySelector(".selected--1").getAttribute("data-color");
    };

    jQuery.prototype.getSelectedSecondary = function () {
        return this.wheel.querySelector(".selected--2").getAttribute("data-color");
    };

    jQuery.prototype.highlightField = function (dataAndEvents) {
        const e = this.wheel.querySelector('[data-color="' + dataAndEvents + '"]');
        const list = getTarget(e);
        list.removeChild(e);
        list.appendChild(e);
        e.setAttribute("class", "selected selected--" + (this.getNumSelected() + 1));
        if (!window.navigator.msPointerEnabled) {
            e.querySelector(".polygons").setAttribute("filter", "url(#drop-shadow)");
        }
    };

    jQuery.prototype.getColor = function (key, i) {
        const result = this.palettes[this.paletteIndices.indexOf(key)];
        return result ? result[this.lightnessIndices.indexOf(i)] : null;
    };

    jQuery.prototype.processTemplate = function (key, ast) {
        const paths = this.getColor(key, "500");
        const environment = this.getColor(ast, "A200");
        return this.replaceDict(this.template, {
            "\\$color-primary-dark": this.getColor(key, "700"),
            "\\$color-primary-contrast": this.calculateTextColor(paths),
            "\\$color-accent-contrast": this.calculateTextColor(environment),
            "\\$color-primary": paths,
            "\\$color-accent": environment
        });
    };

    jQuery.prototype.calculateChannel = function (dataAndEvents) {
        dataAndEvents /= 255;
        return dataAndEvents < 0.03928 ? dataAndEvents / 12.92 : Math.pow((dataAndEvents + 0.055) / 1.055, 2.4);
    };

    jQuery.prototype.calculateLuminance = function (deepDataAndEvents) {
        const octalLiteral = deepDataAndEvents.split(",");
        return 0.2126 * this.calculateChannel(parseInt(octalLiteral[0])) + 0.7152 * this.calculateChannel(parseInt(octalLiteral[1])) + 0.0722 * this.calculateChannel(parseInt(octalLiteral[2]));
    };

    jQuery.prototype.calculateContrast = function (deepDataAndEvents, dataAndEvents) {
        const area = this.calculateLuminance(deepDataAndEvents) + 0.05;
        const desired = this.calculateLuminance(dataAndEvents) + 0.05;
        return Math.max(area, desired) / Math.min(area, desired);
    };

    jQuery.prototype.calculateTextColor = function (env) {
        const dataAndEvents = "255,255,255";
        /** @type {string} */
        const node = "66,66,66";
        const b = this.calculateContrast(env, dataAndEvents);
        if (b >= 3.1) {
            return dataAndEvents;
        }
        return this.calculateContrast(env, node) > b ? node : dataAndEvents;
    };

    jQuery.prototype.updateStylesheet = function () {
        const tabPage = document.getElementById("main-stylesheet");
        const st = document.createElement("style");
        st.id = "main-stylesheet";
        const udataCur = this.processTemplate(this.getSelectedPrimary(), this.getSelectedSecondary());
        if (tabPage) {
            if (tabPage.parentNode) {
                tabPage.parentNode.removeChild(tabPage);
            }
        }
        st.textContent = udataCur;
        document.head.appendChild(st);
    };

    return jQuery;
}();
