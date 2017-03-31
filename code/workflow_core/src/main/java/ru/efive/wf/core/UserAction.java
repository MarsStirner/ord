package ru.efive.wf.core;

import ru.efive.wf.core.data.EditableProperty;
import ru.efive.wf.core.util.EngineHelper;
import ru.entity.model.enums.DocumentAction;

import java.util.ArrayList;
import java.util.List;

public abstract class UserAction implements IAction {
    protected boolean autoCommit = true;
    protected boolean historyAction = true;
    protected Transaction transaction;
    private DocumentAction action;
    private List<IActivity> localActivities = new ArrayList<>();
    private String evaluationMessage;
    private boolean isCommentNecessary = false;
    private String comment;
    private List<IActivity> preActionActivities = new ArrayList<>();
    private List<IActivity> postActionActivities = new ArrayList<>();
    private List<IActivity> activities;
    private Process process;
    private List<EditableProperty> properties = new ArrayList<>();

    public UserAction(Process process) {
        this.process = process;
    }

    public DocumentAction getAction() {
        return this.action;
    }

    public void setAction(DocumentAction action) {
        this.action = action;
    }

    public void setAutoCommit(boolean isAutoCommit) {
        this.autoCommit = isAutoCommit;
    }

    public boolean isAvailable() {
        return true;
    }

    @Override
    public ActionResult run() {
        ActionResult tresult = new ActionResult();
        if (autoCommit) transaction = new Transaction(this);
        //TODO: implement transaction management

        tresult.setProcessed(runPreAction() && runPostAction());
        tresult.setProcessedData(process.getProcessedData());

        return tresult;
    }

    @Override
    public List<IActivity> getPreActionActivities() {
        return preActionActivities;
    }

    public void setPreActionActivities(List<IActivity> preActionActivities) {
        this.preActionActivities = preActionActivities;
    }

    @Override
    public List<IActivity> getPostActionActivities() {
        return postActionActivities;
    }

    public void setPostActionActivities(List<IActivity> postActionActivities) {
        this.postActionActivities = postActionActivities;
    }

    @Override
    public List<IActivity> getLocalActivities() {
        return localActivities;
    }

    public void setLocalActivities(List<IActivity> localActivities) {
        this.localActivities = localActivities;
    }

    public String getEvaluationMessage() {
        return evaluationMessage;
    }

    public void setEvaluationMessage(String evaluationMessage) {
        this.evaluationMessage = evaluationMessage;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isCommentNecessary() {
        return isCommentNecessary;
    }

    public void setCommentNecessary(boolean isCommentNecessary) {
        this.isCommentNecessary = isCommentNecessary;
    }

    public boolean isHistoryAction() {
        return historyAction;
    }

    public void setHistoryAction(boolean historyAction) {
        this.historyAction = historyAction;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }


    //TODO: unimplemented
    @Override
    public List<IActivity> getActivities() {
        return activities;
    }


    protected boolean runPreAction() {
        boolean result = false;
        try {
            if (preActionActivities.size() > 0) {
                int i = 0;
                result = true;
                while (i < preActionActivities.size() && result) {
                    IActivity activity = preActionActivities.get(i);
                    System.out.println("processing activity " + i);
                    result = activity.initialize(getProcess().getProcessedData()) && activity.execute() && activity.dispose();
                    i++;
                }
            } else {
                result = true;
            }
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    protected boolean runPostAction() {
        boolean result = false;
        try {
            if (postActionActivities.size() > 0) {
                int i = 0;
                result = true;
                while (i < postActionActivities.size() && result) {
                    IActivity activity = postActionActivities.get(i);
                    System.out.println("processing activity " + i);
                    result = activity.initialize(getProcess().getProcessedData()) && activity.execute() && activity.dispose();
                    i++;
                }
            } else {
                result = true;
            }
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }


    @Override
    public void commit() throws ProcessingException {
        transaction.commit();
    }

    @Override
    public void rollback() throws ProcessingException {
        transaction.rollback();
    }

    public List<EditableProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<EditableProperty> properties) {
        this.properties = properties;
    }

    public void addProperty(EditableProperty property) throws Exception {
        if (property.getName() == null) {
            throw new Exception(EngineHelper.EXCEPTION_WRONG_NAME);
        }
        properties.add(property);
    }

    public void addProperties(List<EditableProperty> properties) throws Exception {
        for (EditableProperty property : properties) {
            if (property == null) {
                throw new Exception(EngineHelper.EXCEPTION_WRONG_NAME);
            }
            this.properties.add(property);
        }
    }
}