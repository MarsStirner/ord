var e5ui_tabPanel = {
    selectTab: function(tab) {
        var tphp = tab.getAttribute('ID').replace(/:/g, '\\:');
        var tph = tphp.lastIndexOf("-");
        var tp = tphp.substr(0, tph-7);
        var tphpn = tphp.substr(tph+1, tphp.length-tph);
        var unsel = jQuery('#' + tp + '-header>.e5uiTabPanelHeaderPage.selected');
        unsel.removeClass('selected');
        jQuery('#' + tp + '-content>.e5uiTabPanelContentPage.selected').removeClass('selected');
        var sel = jQuery('#' + tphp);
        sel.addClass('selected');
        jQuery('#' + tp + '-content-' + tphpn).addClass('selected');
    }
}
