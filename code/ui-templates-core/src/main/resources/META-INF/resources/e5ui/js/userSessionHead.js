var e5ui_updateSession = {
    list: [],

    update: function(id) {
        jsf.ajax.request(document.getElementById(id), null, {
            render : id
        });
    }
};
