var e5ui_modalWindow = {
    getWi: function(modalWindowId) {
        var wi = e5ui_util.firstWithIdLike('body', modalWindowId + '.+ajaxModal$');
        if (wi) wi = wi.id; else return null;
        var ci = wi.substr(0, wi.lastIndexOf(":ajaxModal"));
        return [wi, ci];
    },
    clickOpen: function(modalWindowId) {
        var wc = e5ui_modalWindow.getWi(modalWindowId);
        if (!wc) return;
        var bi = document.getElementById(wc[1] + ":e5uiModalWindowOpenButton");
        if (bi) jsf.ajax.request(bi, {type: "click"}, {"javax.faces.behavior.event": "action", "execute": 0, "render": wc[0]});
    },
    clickSave: function(modalWindowId) {
        var wc = e5ui_modalWindow.getWi(modalWindowId);
        if (!wc) return;
        var bi = document.getElementById(wc[1] + ":e5uiModalWindowSaveButton");
        var ex = document.getElementById(wc[1] + ":execute");
        if (ex) ex = e5ui_util.transformIds(ex.value, wc[1]);
        var re = document.getElementById(wc[1] + ":render");
        if (re) re = e5ui_util.transformIds(re.value, wc[1]);
        if (bi) jsf.ajax.request(bi, {type: "click"}, {"javax.faces.behavior.event": "action", "execute": ex ? ex : 0, "render": (re ? re + " " : "") + wc[0]});
    },
    clickClose: function(modalWindowId) {
        var wc = e5ui_modalWindow.getWi(modalWindowId);
        if (!wc) return;
        var bi = document.getElementById(wc[1] + ":e5uiModalWindowCloseButton");
        if (bi) jsf.ajax.request(bi, {type: "click"}, {"javax.faces.behavior.event": "action", "execute": 0, "render": wc[0]});
    },
    place: function(modalWindowId) {
        var wi = e5ui_util.firstWithIdLike('body', modalWindowId + '.+ajaxModal$');
        if (wi == null) return;
        wi = wi.getAttribute("id").replace(/:/g, "\\:");
        var w = jQuery("#" + wi + " .e5ui-modal");
        if (w.length == 0) return;
        var c = jQuery("#" + wi + " .e5ui-modal-content");
        var bh = window.innerHeight ? window.innerHeight : document.documentElement ? document.documentElement.clientHeight : document.body.clientHeight;
        var bw = window.innerWidth ? window.innerWidth : document.documentElement ? document.documentElement.clientWidth : document.body.clientWidth;
        var wha =  e5ui_util.pxToInt(w.css("margin-top")) + e5ui_util.pxToInt(w.css("margin-bottom"));
        var wh = w[0].offsetHeight + wha;
        var ch = c[0].offsetHeight;
        var csh = c[0].scrollHeight;
        if (ch < csh) {
            wh += csh - ch;
            ch = csh;
        }
        if (wh > bh) {
            c.css("height", (ch - wh + bh) + "px");
            wh = w[0].offsetHeight + wha;
        } else c.css("height", ch + "px");
        w.css("top", Math.floor((bh - wh) / 2) + "px");
        w.css("left", Math.floor((bw - w[0].offsetWidth - e5ui_util.pxToInt(w.css("margin-left")) - e5ui_util.pxToInt(w.css("margin-right"))) / 2) + "px");
    }
};
