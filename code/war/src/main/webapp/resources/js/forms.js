$(document).ready(function () {
    update_content();
});
var resizeTimer = null;
$(window).bind('resize', function () {
    if (resizeTimer) clearTimeout(resizeTimer);
    resizeTimer = setTimeout(update_content, 100);
});
function update_content() {
    var height = document.body.clientHeight - $("#view_header").height() - $("#footer").height() - $("#searchbar").height() - $("#table_paging").height() - 26;
    $("#table_wrap").height(height);
    $("#table_inner").height(height);
    $("#split").height(document.body.clientHeight);
    $(".e5ui-menu").height(document.body.clientHeight - 30);
}
;