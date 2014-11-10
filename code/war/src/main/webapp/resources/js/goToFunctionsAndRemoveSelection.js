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
        window.open('/component/task/task.xhtml?docId=' + id,'_blank');
    }
}
 // К входящим документам
function goToIncomingDocument(id) {
    if (id != 0) {
        window.open('/component/in/in_document.xhtml?docId=' + id,'_blank');
    }
}
// К томам дел
function goToOfficeKeepingVolume(id) {
    if (id != "" && id != 0) {
        window.open('/component/office/office_keeping_file.xhtml?docId=' + id,'_blank');
    }
}

// К бумажным копиям документа
function goToPaperCopyDocument(id) {
    if (id != "") {
        window.open('/component/paper_copy_document.xhtml?docId=' + id,'_blank');
    }
}