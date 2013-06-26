jQuery.extend(e5ui_navigator,    {
    getNi: function(navigatorId) {
        var ni = e5ui_util.firstWithIdLike('body', navigatorId + '.+e5uiNavigatorAction$');
        if (ni) ni = ni.id; else return null;
        var ci = ni.substr(0, ni.lastIndexOf(":e5uiNavigatorAction"));
        return [ni, ci];
    },
    click: function(navigatorId) {
        var nc = e5ui_navigator.getNi(navigatorId);
        if (!nc) return;
        var bi = document.getElementById(nc[1] + ":e5uiNavigatorAction");
        var ex = document.getElementById(nc[1] + ":execute");
        if (ex) ex = e5ui_util.transformIds(ex.value, nc[1]);
        var re = document.getElementById(nc[1] + ":render");
        if (re) re = e5ui_util.transformIds(re.value, nc[1]);
        if (bi) jsf.ajax.request(bi, {
            type: "click"
        }, {
            "javax.faces.behavior.event": "action", 
            "execute": ex ? ex : 0, 
            "render": (re ? re + " " : "") + nc[0]
            });
    }
});
