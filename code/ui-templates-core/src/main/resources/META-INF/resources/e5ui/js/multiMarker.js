var e5ui_multiMarker = {
    remove: function(multiMarkerId) {
        var wi = e5ui_util.firstWithIdLike('body', multiMarkerId + '-e5uiMultiMarker$');
        if (!wi) return;
        wi.parentNode.removeChild(wi);
    }
};
