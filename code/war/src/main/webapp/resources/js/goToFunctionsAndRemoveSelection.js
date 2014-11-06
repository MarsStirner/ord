/**
 * Created by EUpatov on 05.11.2014.
 * Убирает выделение по двойному щелчку
 */
document.ondblclick = function DoubleClick(evt) {
    if (window.getSelection)
        window.getSelection().removeAllRanges();
    else if (document.selection)
        document.selection.empty();
};
/*
 * Переходы на различные документы
 */
// К поручениям
function goToTask(id) {
    if (id != 0) {
        window.open('#{facesContext.externalContext.requestContextPath}/component/task/task.xhtml?docId=' + id,'_blank');
    }
}