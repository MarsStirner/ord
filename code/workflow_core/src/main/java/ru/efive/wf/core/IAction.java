package ru.efive.wf.core;

import java.util.List;

import ru.entity.model.enums.DocumentAction;
import ru.efive.wf.core.data.EditableProperty;

public interface IAction {

    public DocumentAction getAction();

    public ActionResult run();

    public void commit() throws ProcessingException;

    public void rollback() throws ProcessingException;

    public List<? extends IActivity> getPreActionActivities();

    public List<? extends IActivity> getPostActionActivities();

    public List<? extends IActivity> getLocalActivities();

    public String getEvaluationMessage();

    public boolean isHistoryAction();

    public void setComment(String comment);

    public List<? extends IActivity> getActivities();

    public List<EditableProperty> getProperties();

    public void addProperty(EditableProperty property) throws Exception;

    public void addProperties(List<EditableProperty> properties) throws Exception;
}