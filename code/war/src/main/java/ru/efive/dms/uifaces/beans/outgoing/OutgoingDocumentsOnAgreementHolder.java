package ru.efive.dms.uifaces.beans.outgoing;

import com.github.javaplugs.jsf.SpringScopeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.documents.LazyDataModelForOutgoingDocument;
import ru.entity.model.document.OutgoingDocument;

import javax.annotation.PostConstruct;
import org.springframework.stereotype.Controller;
import java.io.Serializable;

@Controller("outDocumentsOnAgreement")
@SpringScopeView
public class OutgoingDocumentsOnAgreementHolder extends AbstractDocumentLazyDataModelBean<OutgoingDocument> implements Serializable {

    @Autowired
    @Qualifier("outgoingDocumentLDM")
    protected LazyDataModelForOutgoingDocument lazyModel;

    @PostConstruct
    public void init() {
        lazyModel.addFilter("statusId", "3");
    }
}