var e5ui_calendarPlan = {
    updating: {},
    updatePresentation: function(ci, options) {
        if (e5ui_calendarPlan.updating[ci]) return;
        e5ui_calendarPlan.updating[ci] = true;
        var event;
        for (event in options) break;
        options["javax.faces.behavior.event"] = event, 
        options["render"] = ci;
        var ajaxEnd = function(data) {
            e5ui_calendarPlan.updating[ci] = false;
        };
        jsf.ajax.addOnEvent(ajaxEnd);
        jsf.ajax.addOnError(ajaxEnd);
        jsf.ajax.request(ci, {type: event}, options);
    },
    highlightEvent: function(evc, hg) {
        evc = evc.replace(/:/g, "\\:");
        if (hg) jQuery(".event." + evc).addClass("highlight");
        else jQuery(".event." + evc).removeClass("highlight");
    },
    makeDayTooltip: function(own, events) {
        var tipt = "<div class=\"dayTooltip\"><ul>";
        for (var ei = 0; ei < events.length; ei++) tipt += "<li><div class=\"event\">" + events[ei] + "</div></li>";
        tipt += "</ul></div>";
        var tp = jQuery(own).parent();
        tp.append(tipt);
        var tip = tp.find(".dayTooltip");
        var top = 0;
        var left = 0;
        var e = own;
        while (e != null) {
            top += e.offsetTop;
            left += e.offsetLeft;
            e = e.offsetParent;
        }
        left += own.offsetWidth + 2;
        tip.css({
            "left": left + "px", 
            "top": top + "px"
        })
        return tip;
    },
    initDayTooltip: function(own, events) {
        var tip;
        jQuery(own).mouseenter(function(){
            tip = e5ui_calendarPlan.makeDayTooltip(own, events);
            tip.css("display", "block");
        }).mouseleave(function(){
            tip[0].parentNode.removeChild(tip[0]);
        });
    }
}
