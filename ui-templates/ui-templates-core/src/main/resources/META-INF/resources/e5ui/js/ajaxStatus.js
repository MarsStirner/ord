var e5ui_ajaxStatus = {
    status: 'success',
    eventBegin: {},
    eventSuccess: {},
    eventError: {},
    updateStatus: function(status, data) {
        if (status != e5ui_ajaxStatus.status && (status == 'processing' || status == 'error' || (status == 'success' && e5ui_ajaxStatus.status != 'error')))
            e5ui_ajaxStatus.status = status;
        else return;
        jQuery('.e5uiAjaxStatusIndicatorSuccess').css('display', status == 'success' ? 'block' : 'none');
        jQuery('.e5uiAjaxStatusIndicatorProcessing').css('display', status == 'processing' ? 'block' : 'none');
        jQuery('.e5uiAjaxStatusIndicatorError').css('display', status == 'error' ? 'block' : 'none');
        var evc = status == 'success' ? e5ui_ajaxStatus.eventSuccess : status == 'processing' ? e5ui_ajaxStatus.eventBegin : e5ui_ajaxStatus.eventError;
        for (var evn in evc) evc[evn](data);
    },
    jsfAjaxHandler: function(data) {
        if (data.type != undefined) {
            if (data.type == 'error') e5ui_ajaxStatus.updateStatus('error', data);
            else if (data.type == 'event') {
                if (data.status != undefined && data.status == 'begin') e5ui_ajaxStatus.updateStatus('processing', data);
                else e5ui_ajaxStatus.updateStatus('success', data);
            }
        }
    }
};
jsf.ajax.addOnEvent(e5ui_ajaxStatus.jsfAjaxHandler);
jsf.ajax.addOnError(e5ui_ajaxStatus.jsfAjaxHandler);
