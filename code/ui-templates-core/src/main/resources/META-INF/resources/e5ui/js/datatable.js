var e5ui_dataTable = {

    processSublevels: function (jqRow, all) {
        var hide = jqRow.hasClass("collapsed");
        var level = jqRow.attr("e5ui-level");
        var rows = jqRow.nextAll("tr");
        var ri = 0;
        var row;
        while (ri < rows.length) {
            row = jQuery(rows.get(ri));
            if (row.attr("e5ui-level") <= level) {
                if (all) {
                    hide = row.hasClass("collapsed");
                    level = row.attr("e5ui-level");
                    ri++;
                    continue;
                } else break;
            }
            if (hide) {
                row.hide();
                ri++;
            } else {
                row.show();
                if (row.hasClass("collapsed")) {
                    var curLevel = row.attr("e5ui-level");
                    do row = jQuery(rows.get(++ri));
                    while (row && row.attr("e5ui-level") > curLevel);
                } else ri++;
            }
        }
    },

    toggleRowGroup: function(cr) {
        if (!cr || !cr.tagName) return;
        var tr;
        if (cr.tagName == "TD") tr = jQuery(cr).parent();
        else if (cr.tagName == "TR") tr = jQuery(cr);
        else return;
        tr.toggleClass("collapsed");
        e5ui_dataTable.processSublevels(tr);
    },

    switchRowGroups: function(tbl, grp) {
        if (!tbl) return;
        var tgl = typeof grp === "undefined";
        var rows = jQuery(tbl).find("tbody tr");
        if (!rows.length) return;
        var fr = jQuery(rows.get(0));
        for (var ri = 0; ri < rows.length; ri++) {
            var row = jQuery(rows.get(ri));
            if (row.find("td.e5ui-row-marker").length) {
                if (tgl) row.toggleClass("collapsed");
                else if (grp) row.removeClass("collapsed");
                else row.addClass("collapsed");
            }
        }
        e5ui_dataTable.processSublevels(fr, true);
    },

    findTable: function(tblId) {
        if (typeof tblId === "string") {
            var tbls = jQuery("table.e5ui-dataTable");
            for (var ti = 0; ti < tbls.length; ti++) {
                var tbl = tbls.get(ti);
                if (tbl.id.lastIndexOf(tblId) == (tbl.id.length - tblId.length)) return tbl;
            }
        }
        return null;
    },

    collapseRowGroups: function(tblId) {
        e5ui_dataTable.switchRowGroups(e5ui_dataTable.findTable(tblId), false);
    },

    expandRowGroups: function(tblId) {
        e5ui_dataTable.switchRowGroups(e5ui_dataTable.findTable(tblId), true);
    },

    toggleRowGroups: function(tblId) {
        e5ui_dataTable.switchRowGroups(e5ui_dataTable.findTable(tblId));
    }
}
