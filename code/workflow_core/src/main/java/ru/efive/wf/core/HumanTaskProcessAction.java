package ru.efive.wf.core;

import org.apache.commons.beanutils.PropertyUtils;
import ru.entity.model.user.User;
import ru.entity.model.wf.HumanTask;
import ru.external.ProcessUser;

import java.io.Serializable;
import java.util.Objects;

public class HumanTaskProcessAction extends UserAction implements Serializable {

    private static final long serialVersionUID = 5604144630908029259L;
    private HumanTask task;
    private HumanTaskTreeStateResolver resolver;

    public HumanTaskProcessAction(Process process) {
        super(process);
    }

    public HumanTaskProcessAction(Process process, HumanTask task, HumanTaskTreeStateResolver resolver) {
        super(process);
        setTask(task);
        setResolver(resolver);
    }

    public HumanTask getTask() {
        return task;
    }

    public void setTask(HumanTask task) {
        this.task = task;
    }

    public HumanTaskTreeStateResolver getResolver() {
        return resolver;
    }

    public void setResolver(HumanTaskTreeStateResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public boolean isAvailable() {
        boolean result = false;
        try {
            ProcessUser currentUser = getProcess().getProcessUser();

            Object prop = PropertyUtils.getProperty(task, "executor");
            User user = (prop == null ? null : (User) prop);
            if (user != null && Objects.equals(user.getId(), currentUser.getId())) {
                return true;
            }
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }
}