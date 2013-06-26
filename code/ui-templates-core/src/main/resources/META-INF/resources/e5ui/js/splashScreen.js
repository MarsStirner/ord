var e5ui_splashScreen = {
    timeoutID: {},
    onTimeout: {},
    timeout: {},
    overlayOpacity: {},
    setTimeout: function(spsc, tm) {
        var tmv = parseInt(tm);
        if (isNaN(tmv) || tmv < 0) tmv = 3000;
        e5ui_splashScreen.timeout[spsc] = tmv;
    },
    getSpscId: function(splashScreenId) {
        var spsc = e5ui_util.firstWithIdLike('body', splashScreenId + '.+e5uiSplashScreen$')
        if (spsc != undefined) return spsc.getAttribute("id");
        else return undefined;
    },
    kbEvent: function() {
        return false;
    },
    clearTimeout: function(spsc) {
        if (e5ui_splashScreen.timeoutID[spsc] != undefined) {
            window.clearTimeout(e5ui_splashScreen.timeoutID[spsc]);
            e5ui_splashScreen.timeoutID[spsc] = undefined;
        }
    },
    startShow: function(splashScreenId) {
        var spsc = e5ui_splashScreen.getSpscId(splashScreenId);
        if (spsc == undefined) return;
        jQuery(document).bind('keypress keydown keyup', e5ui_splashScreen.kbEvent);
        var p = "#" + spsc.replace(/:/g, '\\:');
        jQuery(p).css('display', 'block');
        jQuery(p + ' .e5ui-spsc-overlay.timeout').css({'background': '#000', 'opacity': '0.01'});
        var tmh = function() {
            e5ui_splashScreen.clearTimeout(spsc);
            jQuery(p + ' .e5ui-spsc-overlay.timeout').css({'background': '#000', 'opacity': e5ui_splashScreen.overlayOpacity[spsc]});
            jQuery(p + ' .e5ui-spsc-timeoutPanel').css('display', 'block');
            if (typeof e5ui_splashScreen.onTimeout[spsc] === 'function') e5ui_splashScreen.onTimeout[spsc]({splashScreenId: spsc});
        };
        if (e5ui_splashScreen.timeout[spsc] > 0) e5ui_splashScreen.timeoutID[spsc] = window.setTimeout(tmh, e5ui_splashScreen.timeout[spsc]);
        else tmh();
    },
    stopShow: function(splashScreenId) {
        var spsc = e5ui_splashScreen.getSpscId(splashScreenId);
        if (spsc == undefined) return;
        e5ui_splashScreen.clearTimeout(spsc);
        var p = "#" + spsc.replace(/:/g, '\\:');
        jQuery(p).css('display', 'none');
        jQuery(p + ' .e5ui-spsc-overlay.timeout').css({'background': '', 'opacity': ''});
        jQuery(p + ' .e5ui-spsc-timeoutPanel').css('display', 'none');
        jQuery(document).unbind('keypress keydown keyup', e5ui_splashScreen.kbEvent);
    },
    startShowOnSubmit: function(splashScreenId, forms) {
        for (var i = 0; i < forms.length; i++) {
            var fm = document.getElementById(forms[i]);
            fm.submit_ = fm.submit;
            fm.submit = function() {
                e5ui_splashScreen.startShow(splashScreenId);
                this.submit_();
            };
            jQuery(fm).submit(function(){
                e5ui_splashScreen.startShow(splashScreenId);
            });
        }
    }
};