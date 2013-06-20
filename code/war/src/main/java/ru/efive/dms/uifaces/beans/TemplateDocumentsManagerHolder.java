package ru.efive.dms.uifaces.beans;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import ru.efive.dms.dao.IncomingDocumentDAOImpl;
import ru.efive.dms.dao.InternalDocumentDAOImpl;
import ru.efive.dms.dao.NumeratorDAOImpl;
import ru.efive.dms.dao.OutgoingDocumentDAOImpl;
import ru.efive.dms.dao.RequestDocumentDAOImpl;
import ru.efive.dms.data.IncomingDocument;
import ru.efive.dms.data.InternalDocument;
import ru.efive.dms.data.Numerator;
import ru.efive.dms.data.OutgoingDocument;
import ru.efive.dms.data.RequestDocument;
import ru.efive.dms.util.ApplicationHelper;

@Named("templateDocumentsManager")
@SessionScoped
public class TemplateDocumentsManagerHolder implements Serializable {

    public List<String> getTemplateDocumentDescriptionById(String key) {
        System.out.println("<<key>>" + key);
        List<String> result = new ArrayList<String>();
        if (!key.isEmpty()) {
            Numerator numerator = sessionManagement.getDAO(NumeratorDAOImpl.class, ApplicationHelper.NUMERATOR_DAO).findDocumentById(key);
            if (numerator != null) {
                StringBuffer in_description = new StringBuffer("");
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

                if (numerator.getDocumentTypeKey().equals("incoming")) {
                    IncomingDocument in_doc = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, ApplicationHelper.INCOMING_DOCUMENT_FORM_DAO).findDocumentByNumeratorId(key);
                    in_description = new StringBuffer("Шаблон входщяего документа от " + sdf.format(in_doc.getCreationDate()));

                } else if (numerator.getDocumentTypeKey().equals("outgoing")) {
                    OutgoingDocument out_doc = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, ApplicationHelper.OUTGOING_DOCUMENT_FORM_DAO).findDocumentByNumeratorId(key);
                    in_description = new StringBuffer("Шаблон исходящего документа от " + sdf.format(out_doc.getCreationDate()));

                } else if (numerator.getDocumentTypeKey().equals("internal")) {
                    InternalDocument internal_doc = sessionManagement.getDAO(InternalDocumentDAOImpl.class, ApplicationHelper.INTERNAL_DOCUMENT_FORM_DAO).findDocumentByNumeratorId(key);
                    in_description = new StringBuffer("Шаблон внутреннего документа от " + sdf.format(internal_doc.getCreationDate()));

                } else if (numerator.getDocumentTypeKey().equals("request")) {
                    RequestDocument request_doc = sessionManagement.getDAO(RequestDocumentDAOImpl.class, ApplicationHelper.REQUEST_DOCUMENT_FORM_DAO).findDocumentByNumeratorId(key);
                    in_description = new StringBuffer("Шаблон обращения граждан от " + sdf.format(request_doc.getCreationDate()));

                }
                result.add(in_description.toString());
            }
        }
        return result;
    }

    @Inject
    @Named("sessionManagement")
    SessionManagementBean sessionManagement = new SessionManagementBean();
}