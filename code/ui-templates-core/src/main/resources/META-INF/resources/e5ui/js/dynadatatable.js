function getEmHeight(element) {
    element= element || document.body;
    return parseInt((element.currentStyle ? element.currentStyle.fontSize : window.getComputedStyle(element).fontSize).replace("px", ""));
}

function addOnMouseWheel(element, handler, useCapture) {
    var mw = function (event) {
        var delta;
        if ("wheelDelta" in event) delta = event.wheelDelta / -40;
        else delta = event.detail;
        return handler(event, delta);
    };
    if (element.addEventListener) {    // all browsers except IE before version 9
        // Internet Explorer, Opera, Google Chrome and Safari
        element.addEventListener("mousewheel", mw, !!useCapture);
        // Firefox
        element.addEventListener("DOMMouseScroll", mw, !!useCapture);
    }
    else if (element.attachEvent) element.attachEvent("onmousewheel", mw); // IE before version 9
}

var e5ui_dynaDataTable = {
    tv: {},
    scheduleUpdate: function(ti) {
        var tv = e5ui_dynaDataTable.tv[ti];
        if (tv.updatingTimer) window.clearTimeout(tv.updatingTimer);
        tv.updatingTimer = window.setTimeout(function() {
            e5ui_dynaDataTable.update(ti);
        }, 200);
    },
    showRows: function(tv) {
        var trs = jQuery("#" + tv.ti.replace(/:/g, "\\:") + " tbody tr");
        var tri;
        for (tri = 0; tri < tv.wOffset; tri++) {
            jQuery(trs.get(tri)).hide();
        }
        for (tri = tv.wOffset; tri < tv.wOffset + tv.wSize; tri++) {
            jQuery(trs.get(tri)).show();
        }
        for (tri = tv.wOffset + tv.wSize; tri < tv.rows; tri++) {
            jQuery(trs.get(tri)).hide();
        }
    },
    update: function(ti) {
        var tv = e5ui_dynaDataTable.tv[ti];
        if (tv.tblInUpd) {
            e5ui_dynaDataTable.scheduleUpdate(ti);
            return;
        }
            
        var pos = Math.round(tv.ts.scrollTop * tv.rowCount / tv.ts.scrollHeight);
        var wofs = pos % tv.wSize;
        var wpos = pos - wofs;
        var nf;
        var nwofs;
        if (wpos < tv.first || wpos + wofs + tv.wSize > tv.first + tv.rows) {
            if (wpos >= tv.wSize) {
                nf = wpos - tv.wSize;
                nwofs = wofs + tv.wSize;
            } else {
                nf = wpos;
                nwofs = wofs;
            }
        } else {
            nf = tv.first;
            nwofs = wpos - tv.first + wofs;
        }
        var lf = tv.rowCount - tv.rows;
        if (nf > lf) {
            nwofs += nf - lf;
            nf = lf;
        }
        if (tv.first == nf && tv.wOffset == nwofs) return;
        else {
            tv.wOffset = nwofs;
            jQuery("#" + ti.replace(/:/g, "\\:") + "-wOffset").val(tv.wOffset);
            if (tv.first == nf) {
                tv.updating = true; // begin of table's updating
                e5ui_dynaDataTable.showRows(tv);
                e5ui_dynaDataTable.resizeTs(ti);
                e5ui_dynaDataTable.updateC(tv, true);
                tv.updating = false; // end of table's updating
                return;
            }
            tv.first = nf;
        }
        
        tv.updating = true; // begin of table's updating
        var options = {
            "javax.faces.behavior.event": "action", 
            "render": ti
        };
        jQuery("#" + ti.replace(/:/g, "\\:") + "-first").val(tv.first);
        jQuery("#" + ti.replace(/:/g, "\\:") + "-rows").val(tv.rows);
        jQuery("#" + ti.replace(/:/g, "\\:") + "-rowCount").val(tv.rowCount);
        jQuery("#" + ti.replace(/:/g, "\\:") + "-inSelector").val(tv.selector ? true : false);
        jQuery("#" + ti.replace(/:/g, "\\:") + "-wSize").val(tv.wSize);
        var ajaxEnd = function(data) {
            tv.updating = false; // end of table's updating
        };
        jsf.ajax.addOnEvent(ajaxEnd);
        jsf.ajax.addOnError(ajaxEnd);
        jsf.ajax.request(ti, {type: "scroll"}, options);
    },
    onWheelTableData: function(ti, delta) {
        var tv = e5ui_dynaDataTable.tv[ti];
        var nt = delta * getEmHeight(tv.ts) + tv.ts.scrollTop;
        if (nt < 0) nt = 0;
        else if (nt + tv.ts.offsetHeight > tv.ts.scrollHeight) nt = tv.ts.scrollHeight - tv.ts.offsetHeight;
        if (nt != tv.ts.scrollTop) {
            tv.ts.scrollTop = nt;
            e5ui_dynaDataTable.scheduleUpdate(ti);
        }
    },
    onPageTableData: function(ti, keyCode) {
        var tv = e5ui_dynaDataTable.tv[ti];
        var processDefault = false;
        var nt;
        switch(keyCode) {
            case 40: //dn
                e5ui_dynaDataTable.onWheelTableData(ti, 3);
                break;
            case 38: //up
                e5ui_dynaDataTable.onWheelTableData(ti, -3);
                break;
            case 34: //pg dn
                nt = tv.ts.scrollTop + tv.ts.offsetHeight;
                break;
            case 33: //pg up
                nt = tv.ts.scrollTop - tv.ts.offsetHeight;
                break;
            case 35: //end
                nt = tv.ts.scrollHeight - tv.ts.offsetHeight;
                break;
            case 36: //home
                nt = 0;
                break;
            default:
                processDefault = true;
        }
        switch(keyCode) {
            case 34: case 33: case 35: case 36:
                if (nt < 0) nt = 0;
                else if (nt + tv.ts.offsetHeight > tv.ts.scrollHeight) nt = tv.ts.scrollHeight - tv.ts.offsetHeight;
                if (nt != tv.ts.scrollTop) {
                    tv.ts.scrollTop = nt;
                    e5ui_dynaDataTable.scheduleUpdate(ti);
                }
        }
        return processDefault;
    },
    updateC: function(tv, upd) {
        var scc = jQuery.cookie("e5ui-dataTable-scroll");
        if (!scc) scc = "";
        var scs = scc.split("##");
        var act = function(scv) {
            var wOffset;
            if (scv) wOffset = parseInt(scv[5], 10);
            if (!upd && scv && tv.first == scv[1] && wOffset + tv.wSize <= tv.rows) {
                tv.wOffset = wOffset;
                jQuery("#" + tv.ti.replace(/:/g, "\\:") + "-wOffset").val(tv.wOffset);
            }
            else return [tv.ti, tv.first, tv.rows, tv.rowCount, tv.selector, tv.wOffset, tv.wSize];
        };
        var walk = function(act) {
            var scvu;
            for (var sci = 0; sci < scs.length; sci++) {
                var scv = scs[sci].split("#");
                if (scv[0] == tv.ti) {
                    scvu = act(scv);
                    if (scvu) scs[sci] = scvu.join("#");
                    return !!scvu;
                }
            }
            scvu = act();
            if (scvu) scs.push(scvu.join("#"));
            return !!scvu;
        }
        if (walk(act)) jQuery.cookie("e5ui-dataTable-scroll", scs.join("##"), {path: "/"});
    },
    resize: function(tvm) {
        var tv;
        if (typeof tvm === "string") tv = e5ui_dynaDataTable.tv[tvm];
        else if (e5ui_dynaDataTable.tv[tvm.ti]) {
            tv = e5ui_dynaDataTable.tv[tvm.ti];
            tv.first = tvm.first;
            tv.rows = tvm.rows;
            tv.rowCount = tvm.rowCount;
            tv.selector = tvm.selector;
            tv.wOffset = tvm.wOffset;
            tv.wSize = tvm.wSize;
        } else {
            tv = e5ui_dynaDataTable.tv[tvm.ti] = tvm;
            tv.ts = document.getElementById(tv.ti + "-scroller");
        }
        e5ui_dynaDataTable.updateC(tv);
        var td = document.getElementById(tv.ti);
        var tss = document.getElementById(tv.ti + "-scroller-size");
        e5ui_dynaDataTable.showRows(tv);
        tv.ts.style.height = td.offsetHeight + "px";
        tss.style.height = Math.floor(td.clientHeight * tv.rowCount / tv.wSize) + "px";
        tv.ts.scrollTop =  Math.round((tv.first + tv.wOffset) * tv.ts.scrollHeight / tv.rowCount);
        td.onclick = function(){
            tv.tt.focus();
        };
        addOnMouseWheel(td, function(event, delta){
            e5ui_dynaDataTable.onWheelTableData(tv.ti, delta);
            return false;
        });
        return tv;
    },
    resizeTs: function(ti) {
        var tv = e5ui_dynaDataTable.tv[ti];
        if (!tv || tv.tblInUpd) return;
        var td = document.getElementById(ti);
        if (tv.ts.offsetHeight != td.offsetHeight) {
            tv.ts.style.height = td.offsetHeight + "px";
            tv.ts.scrollTop = tv.ts.scrollHeight * (tv.first + tv.wOffset) / tv.rowCount;
            //tv.resizingTs = true;
        }
    },
    makeup: function(tvm) {
        var tv = e5ui_dynaDataTable.resize(tvm);
        tv.ts.onscroll = function(){
            if (tv.resizingTs) tv.resizingTs = false;
            else e5ui_dynaDataTable.scheduleUpdate(tv.ti);
        };
        tv.tt = document.getElementById(tv.ti + "-selector");
        tv.tt.onkeydown = function(event_){
            if (!event_) event_ = event;
            return e5ui_dynaDataTable.onPageTableData(tv.ti, event_.keyCode);
        };
        tv.tt.onfocus = function() {
            tv.selector = true;
            jQuery("#" + ti.replace(/:/g, "\\:") + "-inSelector").val(true);
        }
        tv.tt.onblur = function() {
            tv.selector = false;
            jQuery("#" + ti.replace(/:/g, "\\:") + "-inSelector").val(false);
        }
        if (tv.selector) tv.tt.focus();
        jQuery(window).resize(function(){e5ui_dynaDataTable.resizeTs(tv.ti);});
        var rszt = function() {
            e5ui_dynaDataTable.resizeTs(tv.ti);
            window.setTimeout(rszt, 200);
        }
        window.setTimeout(rszt, 200);
    }
}
