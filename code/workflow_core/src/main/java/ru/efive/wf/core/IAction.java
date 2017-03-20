package ru.efive.wf.core;

import java.util.List;

import ru.entity.model.enums.DocumentAction;
import ru.efive.wf.core.data.EditableProperty;

public interface IAction {

    DocumentAction getAction();

    ActionResult run();

    void commit() throws ProcessingException;

    void rollback() throws ProcessingException;

    List<? extends IActivity> getPreActionActivities();

    List<? extends IActivity> getPostActionActivities();

    List<? extends IActivity> getLocalActivities();

    String getEvaluationMessage();

    boolean isHistoryAction();

    void setComment(String comment);

    List<? extends IActivity> getActivities();

    List<EditableProperty> getProperties();

    void addProperty(EditableProperty property) throws Exception;

    void addProperties(List<EditableProperty> properties) throws Exception;
}