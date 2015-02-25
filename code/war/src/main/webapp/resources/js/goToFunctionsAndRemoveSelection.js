/**
 * Убирает выделение по двойному щелчку
 */
document.ondblclick = function DoubleClick(evt) {
    if (window.getSelection)
        window.getSelection().removeAllRanges();
    else if (document.selection)
        document.selection.empty();
};

function goToDocumentByUniqueId(parentId) {
    if (parentId != "") {
        var pos=parentId.indexOf('_');
        if(pos!=-1){
            var id=parentId.substring(pos+1,parentId.length);
            var docType = parentId.substring(0, pos);
            goToDocument(docType, id);
        }
    }
}

function goToDocument(docType, id) {
    if (id != "") {
        if (docType != "") {
            if(docType.indexOf('incoming')!=-1){
                componentType='in/in_document';
            }else if(docType.indexOf('outgoing')!=-1){
                componentType='out/out_document';
            }else if(docType.indexOf('internal')!=-1){
                componentType='internal/internal_document';
            }else if(docType.indexOf('request')!=-1){
                componentType='request/request_document';
            } else if(parentId.indexOf('task')!=-1){
                componentType='task/task';
            }
            window.open('/component/'+componentType+'.xhtml?docId=' + id,'_blank');
        }
    }
}

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
        window.open('/component/office/office_keeping_volume.xhtml?docId=' + id,'_blank');
    }
}
// К Делам
function goToOfficeKeepingFile(id) {
    if (id != 0) {
        window.open('/component/office/office_keeping_file.xhtml?docId=' + id,'_blank');
    }
}
// К номенклатурам дел
function goToNomenclature(id) {
    if (id != 0) {
        window.open('/component/admin/nomenclature.xhtml?docId=' + id,'_blank');
    }
}

// К бумажным копиям документа
function goToPaperCopyDocument(id) {
    if (id != "") {
        window.open('/component/paper_copy_document.xhtml?docId=' + id,'_blank');
    }
}
// К подразделениям
function goToDepartment(id) {
    if (id != 0) {
        window.open('/component/admin/department.xhtml?docId=' + id,'_blank');
    }
}
// К должностям
function goToPosition(id) {
    if (id != 0) {
        window.open('/component/admin/position.xhtml?docId=' + id,'_blank');
    }
}
//К контрагентам
function goToContragent(id) {
    if (id != 0) {
        window.open('/component/contragent/contragent.xhtml?docId=' + id,'_blank');
    }
}
//К типам контрагентов
function goToContragentType(id){
    if (id != 0) {
        window.open('/component/contragent/contragentType.xhtml?docId=' + id,'_blank');
    }
}
// К группам
function goToGroup(id) {
    if (id != 0) {
        window.open('/component/group.xhtml?docId=' + id,'_blank');
    }
}
// К внутренним документам
function goToInternalDocument(id) {
    if (id != 0) {
        window.open('/component/internal/internal_document.xhtml?docId=' + id, '_blank')
    }
}
// К исходящим докам
function goToOutgoingDocument(id) {
    if (id != 0) {
        window.open('/component/out/out_document.xhtml?docId=' + id, '_blank');
    }
}
//  К обращениям
function goToRequestDocument(id) {
    if (id != 0) {
        window.open('/component/request/request_document.xhtml?docId=' + id,'_blank');
    }
}
// К нумераторам
function goToNumerator(id) {
    if (id != 0) {
        window.open('/component/numerator.xhtml?docId=' + id,'_blank');
    }
}
// К пользователям
function goToUser(id) {
    if (id != 0) {
        window.open('/component/user.xhtml?docId=' + id,'_blank');
    }
}
// TODO
function goToRecordBook(id) {
    if (id != 0) {
        window.open('/component/record_book_document.xhtml?docId=' + id,'_blank');
    }
}
// К шаблонам печати
function goToReportTemplate(id) {
    if (id != 0) {
        window.open('/component/report/report_template.xhtml?docId=' + id,'_blank');
    }
}
// К ролям
function goToRole(id) {
    if (id != 0) {
        window.open('/component/role.xhtml?docId=' + id,'_blank');
    }
}
//К шаблонам согласований
function goToRouteTemplate(id) {
    if (id != 0) {
        window.open('/component/admin/route_template.xhtml?docId=' + id,'_blank');
    }
}
//  К сканам
function goToScanDocument(id) {
    if (id != 0) {
        window.open('/component/scan_document.xhtml?docId=' + id, '_blank');
    }
}
// К замещениям
function goToSubstitution(id) {
    if (id != 0) {
        window.open('/component/user_substitution.xhtml?docId=' + id, '_blank');
    }
}