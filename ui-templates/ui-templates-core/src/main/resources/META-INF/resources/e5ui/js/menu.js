var e5ui_menu = {
    toggleCollapsed: function(menuItem) {
        if (typeof menuItem === "undefined" || menuItem == null) return;
        var mi = null;
        if (typeof menuItem === "string") mi = menuItem;
        else if (typeof menuItem === "object" && menuItem.id) mi = menuItem.id;
        if (mi == null) return;
        var mic = "e5ui-menuItem-collapsed-" + mi;
        var miv = jQuery("#" + mi.replace(/:/g, "\\:"));
        if (miv.hasClass("collapsable")) {
            miv.toggleClass("collapsed");
            var micv = jQuery.cookie(mic);
            var mico = {path: "/"};
            if (micv == null || micv == "null") jQuery.cookie(mic, "toggled", mico);
            else jQuery.cookie(mic, null, mico);
        }
    }
};
