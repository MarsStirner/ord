package ru.efive.dms.uifaces.beans;

import com.github.javaplugs.jsf.SpringScopeView;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.entity.model.document.Numerator;

import org.springframework.stereotype.Controller;

@Controller("numerators")
@SpringScopeView
public class NumeratorsHolder extends AbstractDocumentLazyDataModelBean<Numerator> {
    //TODO
}