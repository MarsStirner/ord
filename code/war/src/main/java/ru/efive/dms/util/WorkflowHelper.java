package ru.efive.dms.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.efive.dms.uifaces.beans.utils.SessionManagementBean;
import ru.efive.wf.core.IActivity;
import ru.efive.wf.core.NoStatusAction;
import ru.efive.wf.core.activity.SendMailActivity;
import ru.efive.wf.core.data.EditableProperty;
import ru.efive.wf.core.data.MailMessage;
import ru.efive.wf.core.util.EngineHelper;
import ru.entity.model.document.*;
import ru.entity.model.enums.DocumentAction;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.enums.RoleType;
import ru.entity.model.referenceBook.Role;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.interfaces.OfficeKeepingFileDao;
import ru.hitsl.sql.dao.interfaces.document.*;
import ru.hitsl.sql.dao.interfaces.referencebook.RoleDao;
import ru.hitsl.sql.dao.util.DocumentSearchMapKeys;
import ru.util.ApplicationHelper;

import javax.faces.context.FacesContext;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class WorkflowHelper {
    private static final Logger taskLogger = LoggerFactory.getLogger("TASK");
    private static final int LEFT_PAD_COUNT = 5;
    private static final char LEFT_PAD_CHAR = '0';

    @Autowired
    @Qualifier("roleDao")
    private RoleDao roleDao;






    public boolean changeTaskExecutionDateAction(NoStatusAction changeDateAction, Task task) {
        boolean result = false;
        try {
            LocalDateTime choosenDate = null;
            for (EditableProperty property : changeDateAction.getProperties()) {
                if (property.getName().equals("executionDate")) {
                    choosenDate = (LocalDateTime) property.getValue();
                }
            }
            if (choosenDate != null) {
                final String delegateReason = "Делегирован %s " + new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm").format(LocalDateTime.now());
                task.setWFResultDescription(String.format(delegateReason, task.getExecutors().iterator().next().getDescription()));
                task.setExecutionDate(choosenDate);
                result = true;
            } else {
                System.out.println("No date fo change execution date");
            }
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    public boolean doTaskDelegateAction(NoStatusAction delegateAction, Task task) {
        boolean result = false;
        try {
            User selectedUser = null;
            for (EditableProperty property : delegateAction.getProperties()) {
                if (property.getName().equals(EngineHelper.PROP_DELEGATION_USER)) {
                    selectedUser = (User) property.getValue();
                }
            }
            if (selectedUser != null) {
                List<IActivity> activities = delegateAction.getPreActionActivities();
                for (IActivity activity : activities) {
                    if (activity instanceof SendMailActivity) {
                        SendMailActivity sendMailActivity = (SendMailActivity) activity;
                        MailMessage message = sendMailActivity.getMessage();
                        List<String> sendTo = new ArrayList<>();
                        sendTo.add(selectedUser.getEmail());
                        message.setSendTo(sendTo);
                        sendMailActivity.setMessage(message);
                    }
                }
                final String delegateReason = "Делегирован %s " + new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm").format(LocalDateTime.now());
                task.setWFResultDescription(String.format(delegateReason, task.getExecutors().iterator().next().getDescription()));
                final HashSet<User> users = new HashSet<>(1);
                users.add(selectedUser);
                task.setExecutors(users);

                result = true;
            } else {
                System.out.println("Not found selected user for delegate action");
            }
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }




}