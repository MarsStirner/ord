package ru.efive.dms.uifaces.beans.task;

import com.github.javaplugs.jsf.SpringScopeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.tasks.LazyDataModelForPersonalDraftsTask;
import ru.entity.model.document.Task;

import org.springframework.stereotype.Controller;
import java.io.Serializable;

@Controller("personal_tasks")
@SpringScopeView
public class PersonalTaskListHolder extends AbstractDocumentLazyDataModelBean<Task> implements Serializable {

    private static final long serialVersionUID = 853542007446781235L;
    @Autowired
    @Qualifier("personalTaskLDM")
    LazyDataModelForPersonalDraftsTask ldm;

}
