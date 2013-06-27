var e5ui_pager = {
    addEnterEvent: function(id, hiddenId, buttonId) {
        jQuery(document.getElementById(id)).keypress(function(event) {
            if (event.which == 13) {
                jQuery(document.getElementById(hiddenId)).val(jQuery(document.getElementById(id)).val());
                jQuery(document.getElementById(buttonId)).click();
                return false;
            } else {
                return true;
            }
        });
    }
};
	