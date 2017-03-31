package ru.efive.dms.uifaces.beans.incoming;

import com.github.javaplugs.jsf.SpringScopeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.documents.LazyDataModelForIncomingDocument;
import ru.entity.model.document.IncomingDocument;

import org.springframework.stereotype.Controller;
import java.io.Serializable;


@Controller("in_documents")
@SpringScopeView
public class IncomingDocumentListHolder extends AbstractDocumentLazyDataModelBean<IncomingDocument> implements Serializable {
    @Autowired
    public void setLazyModel( @Qualifier("incomingDocumentLDM") LazyDataModelForIncomingDocument ldm) {
        super.setLazyModel(ldm);
    }

}