var e5ui_navigator = {
    step: 0,
    setupH: function() {
        switch(e5ui_navigator.step) {
            case 0:
                if (window.location.hash == "") window.location.hash = "#b";
                else if (window.location.hash == "#b") window.location.hash = "#f";
                else if (window.location.hash == "#f") {
                    window.history.back();
                    e5ui_navigator.step = 1;
                }
                break;
            case 1:
                if (window.location.hash == "") {
                    e5ui_navigator.onBack();
                    window.history.forward();
                } else if (window.location.hash == "#f") {
                    e5ui_navigator.onForward();
                    window.history.back();
                }
                break;
        }
        setTimeout("e5ui_navigator.setupH();", 100);
    },
    setupF: function() {
        var nif = document.getElementById("e5ui_navigator-nif");
        if (nif) var nifd = nif.contentDocument || (nif.contentWindow ? nif.contentWindow.document : null) || nif.document;
        switch(e5ui_navigator.step) {
            case 0:
                if (nif) {
                    if (nifd.URL.contains("blank.html?b")) {
                        nif.src = e5ui_navigator.url.replace("navigator.js", "blank.html");
                        e5ui_navigator.step++;
                    }
                } else {
                    nif = document.createElement("iframe");
                    nif.id = "e5ui_navigator-nif";
                    nif.style.display = "none";
                    nif.src = e5ui_navigator.url.replace("navigator.js", "blank.html?b");
                    document.body.appendChild(nif);
                }
                break;
            case 1:
                if (nifd.URL.contains("blank.html")) {
                    nif.src = e5ui_navigator.url.replace("navigator.js", "blank.html?f");
                    e5ui_navigator.step++;
                }
                break;
            case 2:
                if (nifd.URL.contains("blank.html?f")) {
                    window.history.go(-1);
                    e5ui_navigator.step++;
                }
                break;
            case 3:
            case 5:
                if (nifd.URL.contains("blank.html")) e5ui_navigator.step = 4;
                break;
            case 4:
                if (nifd.URL.contains("blank.html?b")) {
                    e5ui_navigator.onBack();
                    window.history.go(1);
                    e5ui_navigator.step++;
                } else if (nifd.URL.contains("blank.html?f")) {
                    e5ui_navigator.onForward();
                    window.history.go(-1);
                    e5ui_navigator.step++;
                }
                break;
        }
        setTimeout("e5ui_navigator.setupF();", 50);
    },
    setup: function() {
        var iev = e5ui_util.getInternetExplorerVersion();
        if (iev < 0) e5ui_navigator.setupH();
        else e5ui_navigator.setupF();
    },
    eventBack: {},
    eventForward: {},
    onBack: function() {
        var event = {type: "back"};
        for (var ei in e5ui_navigator.eventBack) e5ui_navigator.eventBack[ei](event);
    },
    onForward: function() {
        var event = {type: "forward"};
        for (var ei in e5ui_navigator.eventForward) e5ui_navigator.eventForward[ei](event);
    }
};
e5ui_navigator.url = e5ui_util.getScriptUrl();
