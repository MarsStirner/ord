var e5ui_util = {
    firstWithIdLike: function(nodeSelector, idPattern) {
        var res = null;
        jQuery(nodeSelector).find("*").each(function() {
            var a = this.getAttribute("id");
            if (a != undefined && a.match(idPattern)) {
                res = this;
                return false;
            }
        });
        return res;
    },
    clickElementTag: true,
    clickElement: function(nodeSelector, idPattern) {
        if (e5ui_util.clickElementTag) {
            if (!(typeof nodeSelector === 'undefined' || nodeSelector == null)) {
                e5ui_util.clickElementTag = false;
                if (typeof idPattern === 'undefined' || idPattern == null) {
                    jQuery(nodeSelector).click();
                } else {
                    var e = e5ui_util.firstWithIdLike(nodeSelector, idPattern);
                    if (e != null) jQuery(e).click();
                }
                e5ui_util.clickElementTag = true;
            }
        } else return false;
    },
    pxToInt: function (cssValue) {
        var r = typeof cssValue === "string" ? parseInt(cssValue.replace(/px/, "")) : 0;
        return isNaN(r) ? 0 : r;
    },
    transformIds: function(ids, ci) {
        var pi = ci.substr(0, ci.lastIndexOf(":"));
        if (pi) pi += ":";
        if (ci) ci += ":";
        var ra = ids.split(/\s+/);
        var r = "";
        for (var i = 0; i < ra.length; i++)
            if (ra[i]) {
                if (ra[i].indexOf("@this:") == 0) r += " " + ci + ra[i].substr(6);
                else if (ra[i].indexOf(":") == 0 || ra[i].indexOf("@") == 0) r += " " + ra[i].substr(1);
                else r += " " + pi + ra[i];
            }
        return r;
    },
    isChildOf: function(parentEl, el, container) {
        if (parentEl == el) {
            return true;
        }
        if (parentEl.contains) {
            return parentEl.contains(el);
        }
        if ( parentEl.compareDocumentPosition ) {
            return !!(parentEl.compareDocumentPosition(el) & 16);
        }
        var prEl = el.parentNode;
        while(prEl && prEl != container) {
            if (prEl == parentEl)
                return true;
            prEl = prEl.parentNode;
        }
        return false;
    },
    getInternetExplorerVersion: function() {
        var rv = -1; // the version of Internet Explorer or a -1 indicating the use of another browser
        if (navigator.appName == 'Microsoft Internet Explorer')
        {
            var ua = navigator.userAgent;
            var re  = new RegExp("MSIE ([0-9]{1,}[\\.0-9]{0,})");
            if (re.exec(ua) != null)
                rv = parseFloat( RegExp.$1 );
        }
        return rv;
    },
    getScriptUrl: function() {
        var scripts = document.getElementsByTagName('script');
        var index = scripts.length - 1;
        var thisScript = scripts[index];
        return thisScript.src;
    },
    getViewport: function () {
        var m = document.compatMode == 'CSS1Compat';
        var res = {
            l : window.pageXOffset || (m ? document.documentElement.scrollLeft : document.body.scrollLeft),
            t : window.pageYOffset || (m ? document.documentElement.scrollTop : document.body.scrollTop),
            w : window.innerWidth || (m ? document.documentElement.clientWidth : document.body.clientWidth),
            h : window.innerHeight || (m ? document.documentElement.clientHeight : document.body.clientHeight)
        };
        res.r = res.l + res.w;
        res.b = res.t + res.h;
        return res;
    }
};
String.prototype.endsWith = function(suffix) {
    if (typeof suffix === "string") {
        if (this.lastIndexOf(suffix) == (this.length - suffix.length)) return true;
    }
    return false;
}
String.prototype.contains = function(substr) {
    if (typeof substr === "string") {
        return this.indexOf(substr) >= 0;
    }
    return false;
}
String.prototype.leftPad = function(len, pad) {
    var res = this;
    if (typeof pad === "undefined" || !pad) pad = " ";
    while (res.length < len) res = pad + res;
    return res;
}
String.prototype.rightPad = function(len, pad) {
    var res = this;
    if (typeof pad === "undefined" || !pad) pad = " ";
    while (res.length < len) res = res + pad;
    return res;
}
