package ru.efive.wf.core;

import java.util.ArrayList;
import java.util.List;

import ru.efive.sql.entity.enums.DocumentAction;
import ru.efive.wf.core.data.EditableProperty;
import ru.efive.wf.core.util.EngineHelper;

public abstract class UserAction implements IAction {
    private DocumentAction action;
    protected boolean autoCommit = true;
    protected boolean historyAction = true;
    protected Transaction transaction;
    private List<IActivity> localActivities = new ArrayList<IActivity>();
    private String evaluationMessage;
    private boolean isCommentNecessary = false;
    private String comment;
    private List<IActivity> preActionActivities = new ArrayList<IActivity>();
    private List<IActivity> postActionActivities = new ArrayList<IActivity>();
    private List<IActivity> activities;
    private Process process;
    private List<EditableProperty> properties = new ArrayList<EditableProperty>();

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


    public void setPreActionActivities(List<IActivity> preActionActivities) {
        this.preActionActivities = preActionActivities;
    }

    @Override
    public List<IActivity> getPreActionActivities() {
        return preActionActivities;
    }


    public void setPostActionActivities(List<IActivity> postActionActivities) {
        this.postActionActivities = postActionActivities;
    }

    @Override
    public List<IActivity> getPostActionActivities() {
        return postActionActivities;
    }

    public void setLocalActivities(List<IActivity> localActivities) {
        this.localActivities = localActivities;
    }

    @Override
    public List<IActivity> getLocalActivities() {
        return localActivities;
    }

    public String getEvaluationMessage() {
        return evaluationMessage;
    }

    public void setEvaluationMessage(String evaluationMessage) {
        this.evaluationMessage = evaluationMessage;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public void setCommentNecessary(boolean isCommentNecessary) {
        this.isCommentNecessary = isCommentNecessary;
    }

    public boolean isCommentNecessary() {
        return isCommentNecessary;
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

    public void setProperties(List<EditableProperty> properties) {
        this.properties = properties;
    }

    public List<EditableProperty> getProperties() {
        return properties;
    }

    public void addProperty(EditableProperty property) throws Exception {
        if (property.getName() == null) {
            throw new Exception(EngineHelper.EXCEPTION_WRONG_NAME);
        }
        properties.add(property);
    }

    public void addProperties(List<EditableProperty> properties) throws Exception {
        for (int i = 0; i < properties.size(); i++) {
            if (properties.get(i) == null) {
                throw new Exception(EngineHelper.EXCEPTION_WRONG_NAME);
            }
            this.properties.add(properties.get(i));
        }
    }
}