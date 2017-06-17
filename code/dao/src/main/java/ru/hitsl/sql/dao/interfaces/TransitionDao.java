package ru.hitsl.sql.dao.interfaces;

import ru.entity.model.referenceBook.DocumentType;
import ru.entity.model.workflow.Status;
import ru.entity.model.workflow.Transition;
import ru.hitsl.sql.dao.interfaces.mapped.CommonDao;

import java.util.List;

/**
 * Created by EUpatov on 14.06.2017.
 */
public interface TransitionDao extends CommonDao<Transition> {
    List<Transition> getAvailableTransitions(DocumentType documentType, Status status);
}
