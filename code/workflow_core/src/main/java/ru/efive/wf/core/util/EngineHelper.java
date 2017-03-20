package ru.efive.wf.core.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import ru.entity.model.user.User;
import ru.entity.model.enums.DocumentStatus;
import ru.efive.wf.core.HumanTaskProcessAction;
import ru.efive.wf.core.HumanTaskTreeStateResolver;
import ru.efive.wf.core.IActivity;
import ru.efive.wf.core.activity.SendMailActivity;
import ru.efive.wf.core.data.EditableProperty;
import ru.entity.model.wf.HumanTask;
import ru.entity.model.wf.HumanTaskTree;
import ru.entity.model.wf.HumanTaskTreeNode;
import ru.efive.wf.core.data.MailMessage;

public final class EngineHelper {

    public static boolean doHumanTaskAgreeAction(HumanTaskTreeStateResolver resolver, HumanTask task) {
        boolean result = false;
        try {
            Date currentDate = Calendar.getInstance(new Locale("ru", "RU")).getTime();
            List<HumanTask> currentTaskList = resolver.getCurrentTaskList();
            for (HumanTask currentTask : currentTaskList) {
                if (currentTask.getExecutor().equals(task.getExecutor()) && currentTask.getStatusId() == DocumentStatus.DRAFT.getId()) {
                    currentTask.setStatusId(DocumentStatus.ON_EXECUTION_2.getId());
                    currentTask.setExecuted(currentDate);
                }
            }
            result = true;
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    public static boolean doHumanTaskDeclineAction(HumanTaskProcessAction declineAction, HumanTask task) {
        boolean result = false;
        try {
            Date currentDate = Calendar.getInstance(new Locale("ru", "RU")).getTime();
            List<HumanTask> currentTaskList = declineAction.getResolver().getCurrentTaskList();
            String declineReason = "";
            for (EditableProperty property : declineAction.getProperties()) {
                if (property.getName().equals(PROP_WF_RESULT_DESCRIPTION)) {
                    declineReason = property.getValue().toString();
                }
            }
            for (HumanTask currentTask : currentTaskList) {
                if (currentTask.getExecutor().equals(task.getExecutor()) && currentTask.getStatusId() == 1) {
                    currentTask.setStatusId(-1);
                    currentTask.setCommentary(declineReason);
                    currentTask.setExecuted(currentDate);
                }
            }
            result = true;
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    public static boolean doHumanTaskDelegateAction(HumanTaskProcessAction delegateAction, HumanTask task) {
        boolean result = false;
        try {
            Date currentDate = Calendar.getInstance(new Locale("ru", "RU")).getTime();
            List<HumanTask> currentTaskList = delegateAction.getResolver().getCurrentTaskList();
            User selectedUser = null;
            for (EditableProperty property : delegateAction.getProperties()) {
                if (property.getName().equals(PROP_DELEGATION_USER)) {
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
                String delegateReasin = "Делегирован " + task.getExecutor().getDescriptionShort() + " " + new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm").format(currentDate);
                for (HumanTask currentTask : currentTaskList) {
                    if (currentTask.getExecutor().equals(task.getExecutor()) && currentTask.getStatusId() == 1) {
                        currentTask.setCommentary(delegateReasin);
                        currentTask.setExecutor(selectedUser);
                    }
                }
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

    // Получение и регистрация в контексте действия первичного списка рассылки о согласовании
    public static List<String> doGenerateAgreementPrimaryNotificationList(HumanTaskTree tree) {
        List<String> result = new ArrayList<>();
        try {
            Set<String> mails = new HashSet<>();
            for (HumanTaskTreeNode rootNode : tree.getRootNodeList()) {
                for (HumanTask task : rootNode.getTaskList()) {
                    mails.add(task.getExecutor().getEmail());
                }
            }
            result.addAll(mails);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // Получение и регистрация в контексте действия первичного списка пользователей о согласовании
    public static Set<User> doGenerateAgreementPrimaryExecutors(HumanTaskTree tree) {
        Set<User> result = new HashSet<>();
        try {
            Set<User> users = new HashSet<>();
            for (HumanTaskTreeNode rootNode : tree.getRootNodeList()) {
                for (HumanTask task : rootNode.getTaskList()) {
                    users.add(task.getExecutor());
                }
            }
            result.addAll(users);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // Получение и регистрация в контексте действия вторичного списка рассылки о согласовании (на выполнении согласования)
    public static List<String> doGenerateAgreementSecondaryNotificationList(HumanTaskTreeStateResolver resolver, HumanTask task) {
        List<String> result = new ArrayList<>();
        try {
            List<HumanTask> processedTaskList = new ArrayList<>();
            List<HumanTask> currentTaskList = resolver.getCurrentTaskList();
            for (HumanTask currentTask : currentTaskList) {
                if (currentTask.getExecutor().equals(task.getExecutor()) && currentTask.getStatusId() == 1) {
                    processedTaskList.add(currentTask);
                }
            }
            Set<String> mails = new HashSet<>();
            for (HumanTask processedTask : processedTaskList) {
                HumanTaskTreeNode node = processedTask.getParentNode();
                if (node != null) {
                    if (node.getChildNodeList().size() > 0) {
                        List<HumanTask> parallelTasks = node.getTaskList();
                        boolean notifyNext = true;
                        for (HumanTask parallelTask : parallelTasks) {
                            if (parallelTask.getId() != processedTask.getId() && parallelTask.getStatusId() == 1) {
                                notifyNext = false;
                            }
                        }
                        if (notifyNext) {
                            List<HumanTaskTreeNode> childNodes = node.getChildNodeList();
                            for (HumanTaskTreeNode childNode : childNodes) {
                                for (HumanTask childTask : childNode.getTaskList()) {
                                    mails.add(childTask.getExecutor().getEmail());
                                }
                            }
                        }
                    }
                }
            }
            result.addAll(mails);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    public static final String DEFAULT_ERROR_MESSAGE = "Внутренняя ошибка. Пожалуйста, повторите позднее.";
    public static final String PROCESSOR_NO_ACTIONS = "Нет доступных действий";
    public static final String ACTION_NOT_SELECTED = "Не выбрано действие";
    public static final String PROPERTY_NOT_FOUND = "Не найдено свойство объекта";
    public static final String WRONG_PROCESSED_DATA = "Не удается инициализировать объект";
    public static final String WRONG_SESSION_BEAN = "Не удается инициализировать session management bean";

    public static final String EXCEPTION_WRONG_NAME = "Недопустимое наименование переменной";
    public static final String EXCEPTION_WRONG_SCOPE = "Не указана область видимости переменной";
    public static final String EXCEPTION_PROCESSING_LOCAL_ACTIVITY = "Ошибка при выполнении локальной activity";
    public static final String EXCEPTION_PROCESSING_HISTORY = "Ошибка при формировании записи в истории";
    public static final String WRONG_LOCAL_ACTIVITY_CLASS = "Некорректно сконфигурированный процесс. Локальные activity должны наследоваться от LocalActivity";

    public static final String DEFAULT_PROCESSED_MESSAGE = "Действие успешно выполнено";

    public static final String PROP_WF_RESULT_DESCRIPTION = "WFResultDescription";
    public static final String PROP_DELEGATION_USER = "DelegationUser";
}