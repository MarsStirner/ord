package ru.efive.wf.core;

import java.io.Serializable;

import org.apache.commons.beanutils.PropertyUtils;

import ru.entity.model.user.User;
import ru.entity.model.wf.HumanTask;
import ru.external.ProcessUser;

public class HumanTaskProcessAction extends UserAction implements Serializable {

    public HumanTaskProcessAction(Process process) {
        super(process);
    }

    public HumanTaskProcessAction(Process process, HumanTask task, HumanTaskTreeStateResolver resolver) {
        super(process);
        setTask(task);
        setResolver(resolver);
    }

    public void setTask(HumanTask task) {
        this.task = task;
    }

    public HumanTask getTask() {
        return task;
    }

    public void setResolver(HumanTaskTreeStateResolver resolver) {
        this.resolver = resolver;
    }

    public HumanTaskTreeStateResolver getResolver() {
        return resolver;
    }

    @Override
    public boolean isAvailable() {
        boolean result = false;
        try {
            ProcessUser currentUser = getProcess().getProcessUser();

            Object prop = PropertyUtils.getProperty(task, "executor");
            User user = (prop == null ? null : (User) prop);
            if (user != null && user.getId() == currentUser.getId()) {
                return true;
            }
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }


    private HumanTask task;
    private HumanTaskTreeStateResolver resolver;


    private static final long serialVersionUID = 5604144630908029259L;
}