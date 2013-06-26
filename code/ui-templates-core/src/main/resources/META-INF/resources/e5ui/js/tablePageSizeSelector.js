var e5ui_tablePageSizeSelector = {
    addChangeEvent: function(id, hiddenId, buttonId) {
        jQuery(document.getElementById(id)).change(function() {
            jQuery(document.getElementById(hiddenId)).val(jQuery(document.getElementById(id)).val());
            jQuery(document.getElementById(buttonId)).click();
        });
    },
    makeUp: function(selectorId, pageSizesList, pageSize) {
        var pageSizeSelector = jQuery(document.getElementById(selectorId + ":currentPageSizeToSelect"));
        var pageSizes = eval("new Array(" + pageSizesList + ")");
        var contains = false;
        for (var i = 0; i < pageSizes.length; i++) {
            pageSizeSelector.append("<option>" + pageSizes[i] + "</option>");
            if (pageSize == pageSizes[i]) contains = true;
        }
        if (!contains) pageSizeSelector.append("<option>" + pageSize + "</option>");
        pageSizeSelector.val(pageSize);
        e5ui_tablePageSizeSelector.addChangeEvent(selectorId + ":currentPageSizeToSelect", selectorId + ":currentPageSizeToSelectHidden", selectorId + ":selectPageSize");
    }
};
