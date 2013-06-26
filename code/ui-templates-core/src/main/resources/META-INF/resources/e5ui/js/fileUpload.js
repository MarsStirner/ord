var e5ui_fileUpload = {
    onLoad: function(tif, params) {
        if (params.list && params.list.length) {
            tif.parentNode.removeChild(tif);
            for (var fflii = 0; fflii < params.list.length; fflii++) {
                var ffli = params.list[fflii];
                ffli.parentNode.removeChild(ffli);
            }
            params.list = [];
            params.count = 0;
            params.countId = 0;
            var onUpl = function() {
                if (params.onuploaded_) {
                    params.onuploaded_ = false;
                    if (typeof params.onuploaded === 'function') params.onuploaded(params);
                }
            }
            if (typeof params.ajax === "function") {
                e5ui_ajaxStatus.eventSuccess[params.fuId] = e5ui_ajaxStatus.eventError[params.fuId] = onUpl;
                params.ajax();
            } else onUpl();
            e5ui_fileUpload.addInput(params);
        }
    },
    addInput: function(params) {
        var td;
        var tc;
        var btnsel = document.getElementById(params.fuId + "-btnsel");
        if (params.form) {
            var tif = document.getElementById(params.fuId + "-target");
            if (!tif) {
                tif = document.createElement("iframe");
                tif.id = params.fuId + "-target";
                tif.frameborder = 0;
                tif.scrolling="no";
                tif.style.position = "absolute";
                tif.style.width = "100%";
                tif.style.height = "100%";
                tif.style.right = "0px";
                tif.style.top = "0px";
                tif.style.zIndex = 1;
                tif.style.opacity = 0;
                tif.style.filter = "alpha(opacity=0)";
                btnsel.appendChild(tif);
                
                td = tif.document_ = tif.contentDocument || (tif.contentWindow ? tif.contentWindow.document : null) || tif.document;
                td.open();
                td.close();
                
                tc = td.createElement("form");
                tc.id = tc.name = params.form.id;
                tc.action = params.form.action;
                tc.encoding = tc.enctype = "multipart/form-data";
                tc.method = "post";
                td.body.appendChild(tc);
            
                var fih = td.createElement("input");
                fih.type = "hidden";
                fih.name = fih.value = params.form.id;
                tc.appendChild(fih);
            } else {
                td = tif.document_;
                tc = td.forms[0];
            }
        } else {
            td = document;
            tc = btnsel;
        }
        
        var ff = td.getElementById(params.fuId +"-files");
        if (!ff) {
            ff = td.createElement("div");
            ff.id = params.fuId +"-files";
            ff.style.display = "none";
            tc.appendChild(ff);
        }
        
        if (!params.count) params.count = 0;
        if (!params.countId) params.countId = 0;
        
        var btnupl = document.getElementById(params.fuId + "-btnupl");
        if (btnupl) {
            if (params.count) jQuery(btnupl).removeClass("disabled").click(
                function(){
                    if (tc.nodeName.toLowerCase() == "form") {
                        var ffl_s = "#" + params.fuId.replace(/:/g, "\\:") + "-list"
                        jQuery(ffl_s + " .e5uiFileUploadButton.remove").addClass("sending");
                        jQuery(ffl_s + " .e5uiFileUploadBtnText.remove").addClass("sending").text(params.lang.sending);
                        var ffl = jQuery(ffl_s + " .row");
                        params.list = [];
                        for (var ffli = 0; ffli < ffl.length; ffli++) params.list.push(ffl[ffli]);
                        if (typeof params.onuploading === 'function') params.onuploading(params);
                        params.onuploaded_ = true;
                        tc.submit();
                        var lh = function(){
                            if (tif.document_ != (tif.contentDocument || (tif.contentWindow ? tif.contentWindow.document : null) || tif.document)) e5ui_fileUpload.onLoad(tif, params);
                            else window.setTimeout(lh, 200);
                        };
                        window.setTimeout(lh, 200);
                    }
                });
            else jQuery(btnupl).addClass("disabled").unbind("click");
        }
        if (params.maxCount && params.count >= params.maxCount) {
            jQuery(btnsel).addClass("disabled");
            return;
        } else jQuery(btnsel).removeClass("disabled");
        
        if (td.getElementById(params.fuId + "-" + params.countId)) return;
        var ffi = td.createElement("input");
        ffi.type = "file";
        ffi.id = ffi.name = params.fuId + "-" + params.countId;
        ffi.style.position = "absolute";
        ffi.style.right = "0px";
        ffi.style.top = "0px";
        ffi.style.zIndex = 1;
        ffi.style.opacity = 0;
        ffi.style.filter = "alpha(opacity=0)";
        ffi.style.fontSize = "100px";
        ffi.style.cursor = btnsel.style.cursor;
        ffi.onchange = function(){
            ff.appendChild(ffi.parentNode.removeChild(ffi));
            var fflr = document.createElement("tr");
            fflr.id = ffi.id + "-li";
            fflr.className = "row";
            var fflr_c1 = document.createElement("td");
            fflr_c1.className = "cell text";
            var fni = ffi.value.lastIndexOf("\\");
            fflr_c1.innerHTML = fni > 0 ? ffi.value.substr(fni + 1) : ffi.value;
            fflr.appendChild(fflr_c1);
            var fflr_c2 = document.createElement("td");
            fflr_c2.className = "cell";
            var fflrb = document.createElement("div");
            fflrb.className = "e5uiFileUploadButton remove";
            fflrb.onclick = function(){
                fflr.parentNode.removeChild(fflr);
                ffi.parentNode.removeChild(ffi);
                params.count--;
                e5ui_fileUpload.addInput(params);
            };
            var fflrbt = document.createElement("span");
            fflrbt.className = "e5uiFileUploadBtnText remove";
            fflrbt.innerHTML = params.lang.remove;
            fflrb.appendChild(fflrbt);
            fflr_c2.appendChild(fflrb);
            fflr.appendChild(fflr_c2);
            var ffl = document.getElementById(params.fuId + "-list");
            var fflb = ffl.getElementsByTagName("tbody");
            (fflb.length ? fflb[0] : ffl).appendChild(fflr);
            params.count++;
            params.countId++;
            e5ui_fileUpload.addInput(params);
        };
        tc.appendChild(ffi);
    },
    makeUp: function(params) {
        if (typeof params === "undefined" || !params.id) return;
        params.fuId = params.id + "-e5uiFileUpload";
        params.lang = e5ui_fileUpload["strings_" + (typeof params.lang === "undefined" || params.lang == null || typeof e5ui_fileUpload["strings_" + params.lang] === "undefined" ? "en" : params.lang)];
        if (params.form) params.form = document.getElementById(params.form);
        e5ui_fileUpload[params.fuId + "-params"] = params;
        var fub = "#" + params.fuId.replace(/:/g, "\\:") + " .e5uiFileUploadBtnText.";
        jQuery(fub + "select").text(params.lang.select);
        jQuery(fub + "upload").text(params.lang.upload);
        e5ui_fileUpload.addInput(params);
    },
    strings_en: {
        select: "Select file",
        upload: "Upload",
        remove: "Remove",
        sending: "Sending..."
    }
};
