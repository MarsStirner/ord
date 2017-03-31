package ru.efive.dms.uifaces.beans.outgoing;

import com.github.javaplugs.jsf.SpringScopeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.documents.LazyDataModelForOutgoingDocument;
import ru.entity.model.document.OutgoingDocument;

import org.springframework.stereotype.Controller;
import java.io.Serializable;

@Controller("outDocumentsOnConsideration")
@SpringScopeView
public class OutgoingDocumentsOnConsiderationHolder extends AbstractDocumentLazyDataModelBean<OutgoingDocument> implements Serializable {

    @Autowired
    @Qualifier("outgoingDocumentLDM")
    private LazyDataModelForOutgoingDocument outgoingDocuments;
}