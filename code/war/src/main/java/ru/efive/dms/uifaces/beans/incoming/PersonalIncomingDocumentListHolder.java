package ru.efive.dms.uifaces.beans.incoming;

import com.github.javaplugs.jsf.SpringScopeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.documents.LazyDataModelForPersonalDraftsIncomingDocument;
import ru.entity.model.document.IncomingDocument;

import org.springframework.stereotype.Controller;
import java.io.Serializable;

@Controller("personal_in_documents")
@SpringScopeView
public class PersonalIncomingDocumentListHolder extends AbstractDocumentLazyDataModelBean<IncomingDocument> implements Serializable {
    @Autowired
    @Qualifier("personalIncomingDocumentLDM")
    LazyDataModelForPersonalDraftsIncomingDocument lazyModel;


}