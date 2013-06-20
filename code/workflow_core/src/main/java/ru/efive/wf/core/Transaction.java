package ru.efive.wf.core;

import java.util.List;

public class Transaction {

    protected Transaction() {

    }

    protected Transaction(IAction action) {
        this.action = action;
    }

    public IAction getAction() {
        return action;
    }

    protected void setAction(IAction action) {
        this.action = action;
    }


    //TODO: commit action result
    public void commit() throws ProcessingException {

    }

    //TODO: rollback action
    public void rollback() throws ProcessingException {

    }


    private IAction action;
    private List<IActivity> processedActivities;
}