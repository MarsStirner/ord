// Call to apply fixes
fix();

function fix() {
    console.log("Start adding fixes to PF components");
    fixDialogPosition();
    fixDataTableMultipleSelection();
}

function fixDialogPosition(){
    console.log("Fix DialogFramework openDialog method (add option \'position\' (JQUERY)");
    PrimeFaces.dialog.DialogHandler.openDialog = function (e) {
        var d = e.sourceComponentId + "_dlg";
        if (document.getElementById(d)) {
            return
        }
        var h = e.sourceComponentId.replace(/:/g, "_") + "_dlgwidget",
            f = $('<div id="' + d + '" class="ui-dialog ui-widget ui-widget-content ui-corner-all ui-shadow ui-hidden-container ui-overlay-hidden" data-pfdlgcid="' + e.pfdlgcid + '" data-widgetvar="' + h + '"></div>').append('<div class="ui-dialog-titlebar ui-widget-header ui-helper-clearfix ui-corner-top"><span class="ui-dialog-title"></span></div>');

        if (e.options.closable !== false) {
            f.children(".ui-dialog-titlebar").append('<a class="ui-dialog-titlebar-icon ui-dialog-titlebar-close ui-corner-all" href="#" role="button"><span class="ui-icon ui-icon-closethick"></span></a>')
        }
        f.append('<div class="ui-dialog-content ui-widget-content" style="height: auto;"><iframe style="border:0 none" frameborder="0"/></div>');
        f.appendTo(document.body);
        var c = f.find("iframe"),
            g = e.url.indexOf("?") === -1 ? "?" : "&",
            b = e.url + g + "pfdlgcid=" + e.pfdlgcid,
            a = e.options.contentWidth || 640;
        pos = e.options.position || "center";
        c.width(a);
        c.on("load", function () {
            var j = $(this),
                k = j.contents().find("title");
            if (!j.data("initialized")) {
                PrimeFaces.cw("DynamicDialog", h, {
                    id: d,
                    position: pos,
                    sourceComponentId: e.sourceComponentId,
                    sourceWidget: e.sourceWidget,
                    onHide: function () {
                        var n = this, m = this.content.children("iframe");
                        if (m.get(0).contentWindow.PrimeFaces) {
                            this.destroyIntervalId = setInterval(function () {
                                if (m.get(0).contentWindow.PrimeFaces.ajax.Queue.isEmpty()) {
                                    clearInterval(n.destroyIntervalId);
                                    m.attr("src", "about:blank");
                                    n.jq.remove()
                                }
                            }, 10)
                        } else {
                            m.attr("src", "about:blank");
                            n.jq.remove()
                        }
                        PF[h] = undefined
                    },
                    modal: e.options.modal,
                    resizable: e.options.resizable,
                    draggable: e.options.draggable,
                    width: e.options.width,
                    height: e.options.height
                })
            }
            if (k.length > 0) {
                PF(h).titlebar.children("span.ui-dialog-title").html(k.text())
            }
            PF(h).show();
            var l = PrimeFaces.env.browser.webkit ? 5 : 20, i = e.options.contentHeight || j.get(0).contentWindow.document.body.scrollHeight + l;
            j.height(i);
            c.data("initialized", true)
        }).attr("src", b)
    };
}

function fixDataTableMultipleSelection() {
    console.log("Fix DataTable multiple selection \'onRowClick\'. Click on already selected row unselect it without need to press CNTRL key)");
    PrimeFaces.widget.DataTable.prototype.onRowClick = function (e, d, a) {
        if ($(e.target).is("td,span:not(.ui-c)")) {
            var g = $(d),
                c = g.hasClass("ui-state-highlight"),
                f = e.metaKey || e.ctrlKey,
                b = e.shiftKey;

            if (c) {
                this.unselectRow(g, a)
            } else {
                if (this.isSingleSelection() || (this.isMultipleSelection() && e && !f && !b && this.cfg.rowSelectMode === "new")) {
                    this.unselectAllRows()
                }
                if (this.isMultipleSelection() && e && e.shiftKey) {
                    this.selectRowsInRange(g)
                } else {
                    this.originRowIndex = g.index();
                    this.cursorIndex = null;
                    this.selectRow(g, a)
                }
            }
            if (this.cfg.disabledTextSelection) {
                PrimeFaces.clearSelection()
            }
        }
    };
}