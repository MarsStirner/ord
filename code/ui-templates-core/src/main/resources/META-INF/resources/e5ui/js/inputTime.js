var e5ui_inputTime = {
    hide: function(ev) {
        ev.data.selecting = false;
        ev.data.selector.hide();
        jQuery(document).unbind("mousedown", e5ui_inputTime.hideOnClick);
        jQuery(document).unbind("keydown keypress keyup", e5ui_inputTime.hideOnEsc);
    },
    hideOnClick: function (ev) {
        if (ev.target != ev.data.button.get(0) && !e5ui_util.isChildOf(ev.data.selector.get(0), ev.target, ev.data.selector.get(0))) e5ui_inputTime.hide(ev);
    },
    hideOnEsc: function (ev) {
        if (ev.keyCode == '27') e5ui_inputTime.hide(ev);
    },
    click: function(ev) {
        if (ev.data.button.hasClass("disabled")) return false;
        if (ev.data.selecting) {
            e5ui_inputTime.hide(ev);
        } else {
            ev.data.selecting = true;
            var tm = ev.data.editor.val().split(":");
            var tu = ["hours", "minutes"];
            if (!ev.data.noSeconds) tu.push("seconds");
            for (var tui = 0; tui < tu.length; tui++) {
                var to = ev.data[tu[tui]].find("option");
                to.removeAttr("selected");
                var tuv = parseInt(tm[tui]);
                if (!tuv || isNaN(tuv)) t = 0;
                jQuery(to.get(Math.round(tuv / ev.data[tu[tui] + "Interval"]))).attr("selected", "true");
            }
            var b = ev.data.button.get(0);
            var s = ev.data.selector.get(0);
            var l = b.offsetLeft;
            var t = b.offsetTop + b.offsetHeight;
            var o = b.offsetParent;
            while (o) {
                l += o.offsetLeft;
                t += o.offsetTop;
                o = o.offsetParent;
            }
            var vp = e5ui_util.getViewport();
            if (t + s.offsetHeight > vp.r && t - s.offsetHeight - b.offsetHeight <= vp.t) t -= s.offsetHeight + b.offsetHeight;
            ev.data.selector.css({"left": l + "px", "top": t + "px"});
            ev.data.selector.show();
            jQuery(document).bind("mousedown", ev.data, e5ui_inputTime.hideOnClick);
            jQuery(document).bind("keydown keypress keyup", ev.data, e5ui_inputTime.hideOnEsc);
        }
        return false;
    },
    select: function(ev) {
        ev.data.editor.val(ev.data.hours.val() + ":" + ev.data.minutes.val() + (ev.data.noSeconds ? "" : ":" + ev.data.seconds.val()));
        e5ui_inputTime.hide(ev);
    },
    complete: function(ev) {
        var tm = ev.data.editor.val().split(":");
        var etm = "";
        for (var tmi = 0; tmi < (ev.data.noSeconds ? 2 : 3); tmi++) {
            if (tmi > 0) etm += ":";
            if (tm[tmi]) etm += tm[tmi].replace(/_/g, "0").leftPad(2, "0");
            else etm += "00";
        }
        ev.data.editor.val(etm);
    },
    makeUp: function(params) {
        params.editor = jQuery(document.getElementById(params.id + ":timeEditor"));
        if (params.noedit) params.editor.attr("readonly", "true");
        else {
            params.editor.blur(params, e5ui_inputTime.complete);
            params.editor.keydown(params, function(ev){
                if (ev.which == 13) e5ui_inputTime.complete(ev);
            });
            params.editor.mask(params.noSeconds ? "99:99" : "99:99:99");
        }
        params.hoursInterval = parseInt(params.hoursInterval);
        if (isNaN(params.hoursInterval)) params.hoursInterval = 1;
        params.minutesInterval = parseInt(params.miuntesInterval);
        if (isNaN(params.minutesInterval)) params.minutesInterval = 1;
        params.secondsInterval = parseInt(params.secondsInterval);
        if (isNaN(params.secondsInterval)) params.secondsInterval = 1;
        
        params.lang = e5ui_inputTime["strings_" + (typeof params.lang === "undefined" || params.lang == null || typeof e5ui_inputTime["strings_" + params.lang] === "undefined" ? "en" : params.lang)];
        if (!params.vsize) params.vsize = 10;
        
        var st = "<div id=\"" + params.id + "-selector\" class=\"e5ui-inputTime-selector\">" +
            "<div class=\"e5ui-inputTime-Container\">" +
            "<table class=\"e5ui-inputTime-selector-table\"><tr class=\"e5ui-inputTime-selector-table-names\">" +
            "<td>" + params.lang.hours + "</td>" +
            "<td>" + params.lang.minutes + "</td>";
        if (!params.noSeconds) st += "<td>" + params.lang.seconds + "</td>";
        st += "</tr><tr class=\"e5ui-inputTime-selector-table-values\">" +
            "<td><select class=\"e5ui-inputTime-selector-table-hours\" size=\"" + params.vsize + "\">";
        var tu;
        for (tu = 0;tu < 24;tu += params.hoursInterval) st += "<option>" + String(tu).leftPad(2, "0") + "</option>";
        st += "</select></td><td><select class=\"e5ui-inputTime-selector-table-minutes\" size=\"" + params.vsize + "\">";
        for (tu = 0; tu < 60; tu += params.minutesInterval) st += "<option>" + String(tu).leftPad(2, "0") + "</option>";
        st += "</select></td>";
        if (!params.noSeconds) {
            st += "<td><select class=\"e5ui-inputTime-selector-table-seconds\" size=\"" + params.vsize + "\">";
            for (tu = 0; tu < 60; tu += params.secondsInterval) st += "<option>" + String(tu).leftPad(2, "0") + "</option>";
            st += "</select></td>";
        }
        st += "</tr></table><div class=\"e5ui-inputTime-selector-buttons\">" + 
            "<button class=\"cancel\" type=\"button\">" + params.lang.cancel + "</button>" +
            "<button class=\"select\" type=\"button\">" + params.lang.select + "</button>" +
            "<div style=\"clear: both;\"></div></div></div>" +
            "<div class=\"e5ui-inputTime-BorderT\"></div>" +
            "<div class=\"e5ui-inputTime-BorderB\"></div>" +
            "<div class=\"e5ui-inputTime-BorderL\"></div>" +
            "<div class=\"e5ui-inputTime-BorderR\"></div>" +
            "<div class=\"e5ui-inputTime-BorderTL\"></div>" +
            "<div class=\"e5ui-inputTime-BorderTR\"></div>" +
            "<div class=\"e5ui-inputTime-BorderBL\"></div>" +
            "<div class=\"e5ui-inputTime-BorderBR\"></div>" +
            "</div>";
        jQuery("body").append(st);
        params.selector = jQuery(document.getElementById(params.id + "-selector"));
        params.selectButton = params.selector.find("button.select");
        params.cancelButton = params.selector.find("button.cancel");
        params.hours = params.selector.find("select.e5ui-inputTime-selector-table-hours");
        params.minutes = params.selector.find("select.e5ui-inputTime-selector-table-minutes");
        if (!params.noSeconds) params.seconds = params.selector.find("select.e5ui-inputTime-selector-table-seconds");
        
        params.button = jQuery(document.getElementById(params.id + ":selectButton"));
        params.selecting = false;
        params.button.click(params, e5ui_inputTime.click);
        params.selectButton.click(params, e5ui_inputTime.select);
        params.cancelButton.click(params, e5ui_inputTime.hide);
    },
    strings_en: {
        hours: "Hours:",
        minutes: "Minutes:",
        seconds: "Seconds:",
        select: "Select",
        cancel: "Cancel"
    }
};
