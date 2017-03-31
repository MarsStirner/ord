package ru.efive.wf.core;

import org.apache.commons.beanutils.PropertyUtils;
import ru.efive.wf.core.activity.InvokeMethodActivity;
import ru.efive.wf.core.activity.ParametrizedPropertyLocalActivity;
import ru.efive.wf.core.activity.SendMailActivity;
import ru.efive.wf.core.activity.SetPropertyActivity;
import ru.efive.wf.core.activity.enums.EditablePropertyScope;
import ru.efive.wf.core.data.MailMessage;
import ru.efive.wf.core.data.impl.InputReasonForm;
import ru.efive.wf.core.data.impl.SelectUserForm;
import ru.efive.wf.core.util.EngineHelper;
import ru.entity.model.enums.DocumentAction;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.user.User;
import ru.entity.model.wf.HumanTask;
import ru.external.ProcessUser;
import ru.external.ProcessedData;

import java.io.Serializable;
import java.util.*;

public class HumanTaskActionGenerator {

    //private static String in_serverHost = "http://10.0.1.91:9080/dms";
    private static String in_serverHost = "http://ord.fccho-moscow.ru";
    private Process process;
    //private static String in_serverHost="http://10.0.200.60";
    private HumanTaskTreeStateResolver resolver;

    public HumanTaskActionGenerator(Process process, HumanTaskTreeStateResolver resolver) {
        this.process = process;
        this.resolver = resolver;
    }

    public List<IAction> generateActionsFromTask(HumanTask task) {
        List<IAction> result = new ArrayList<>();
        try {
            if (task != null && task.getExecutor() != null && task.getExecutor().getId() != 0 && Objects.equals(process.getProcessUser().getId(), task.getExecutor().getId())) {
                /**
                 * compose agree action
                 */
                HumanTaskProcessAction agreeAction = new HumanTaskProcessAction(process, task, resolver);
                agreeAction.setAction(DocumentAction.AGREE);
                agreeAction.setHistoryAction(false);

                List<IActivity> activites = new ArrayList<>();
                InvokeMethodActivity activity = new InvokeMethodActivity();
                List<Serializable> list = new ArrayList<>();
                list.add(resolver);
                list.add(task);
                activity.setInvokeInformation("ru.efive.wf.core.util.EngineHelper", "doHumanTaskAgreeAction", list);
                activites.add(activity);

                SendMailActivity mailActivity = new SendMailActivity();
                List<String> sendTo = new ArrayList<>();
                sendTo.add(task.getExecutor().getEmail());
                List<String> blindCopyTo = new ArrayList<>();
                blindCopyTo.add("alexeyvagizov@gmail.com");
                blindCopyTo.add("nkochubey@inbox.ru");
                MailMessage message = new MailMessage(sendTo, null, "Согласовано",
                        "Согласовано\n" + task.getExecutor().getDescription() + "\n\n" + "<a href=\"" + in_serverHost + "/component/out/out_document.xhtml?docId=" +
                                process.getProcessedData().getId() + "\" >Ссылка на документ</a>");
                message.setBlindCopyTo(blindCopyTo);
                message.setContentType("text/html");
                mailActivity.setMessage(message);
                //activites.add(mailActivity);

                agreeAction.setPreActionActivities(activites);

                activites = new ArrayList<>();
                mailActivity = new SendMailActivity();
                sendTo = EngineHelper.doGenerateAgreementSecondaryNotificationList(resolver, task);
                sendTo.add(task.getExecutor().getEmail());
                message = new MailMessage(sendTo, null, "Новый запрос на согласование",
                        "Новый запрос на согласование\n\n" + "<a href=\"" + in_serverHost + "/component/out/out_document.xhtml?docId=" +
                                process.getProcessedData().getId() + "\" >Ссылка на документ</a>");
                message.setBlindCopyTo(blindCopyTo);
                message.setContentType("text/html");
                mailActivity.setMessage(message);
                //if (sendTo.size() > 0) activites.add(mailActivity);

                agreeAction.setPostActionActivities(activites);

                result.add(agreeAction);

                /**
                 * compose decline action
                 */
                HumanTaskProcessAction declineAction = new HumanTaskProcessAction(process, task, resolver);
                declineAction.setAction(DocumentAction.NOT_AGREE);
                declineAction.setHistoryAction(false);

                activites = new ArrayList<>();
                ParametrizedPropertyLocalActivity localActivity = new ParametrizedPropertyLocalActivity();
                localActivity.setParentAction(declineAction);
                InputReasonForm reasonForm = new InputReasonForm();
                reasonForm.setBeanName(process.getProcessedData().getBeanName());
                reasonForm.setActionCommentaryField(EngineHelper.PROP_WF_RESULT_DESCRIPTION);
                reasonForm.setScope(EditablePropertyScope.LOCAL);
                localActivity.setDocument(reasonForm);
                activites.add(localActivity);
                declineAction.setLocalActivities(activites);

                activites = new ArrayList<>();
                activity = new InvokeMethodActivity();
                list = new ArrayList<>();
                list.add(declineAction);
                list.add(task);
                activity.setInvokeInformation("ru.efive.wf.core.util.EngineHelper", "doHumanTaskDeclineAction", list);
                activites.add(activity);

                mailActivity = new SendMailActivity();
                sendTo = new ArrayList<>();
                sendTo.add(task.getExecutor().getEmail());
                blindCopyTo = new ArrayList<>();
                blindCopyTo.add("alexeyvagizov@gmail.com");
                blindCopyTo.add("nkochubey@inbox.ru");
                message = new MailMessage(sendTo, null, "Отказ в согласовании ",
                        "Отказано в согласовании\n" + task.getExecutor().getDescription() + "\n\n" + "<a href=\"" + in_serverHost + "/component/out/out_document.xhtml?docId=" +
                                process.getProcessedData().getId() + "\" >Ссылка на документ</a>");
                message.setBlindCopyTo(blindCopyTo);
                message.setContentType("text/html");
                mailActivity.setMessage(message);
                activites.add(mailActivity);

                declineAction.setPreActionActivities(activites);

                result.add(declineAction);

                /**
                 * compose delegate action
                 */
                HumanTaskProcessAction delegateAction = new HumanTaskProcessAction(process, task, resolver);
                delegateAction.setAction(DocumentAction.DELEGATE_10002);
                delegateAction.setHistoryAction(false);

                activites = new ArrayList<>();
                localActivity = new ParametrizedPropertyLocalActivity();
                localActivity.setParentAction(delegateAction);
                SelectUserForm selectUserForm = new SelectUserForm();
                selectUserForm.setBeanName(process.getProcessedData().getBeanName());
                selectUserForm.setUserListBeanName("userList");
                selectUserForm.setSelectedUserField(EngineHelper.PROP_DELEGATION_USER);
                selectUserForm.setScope(EditablePropertyScope.LOCAL);
                localActivity.setDocument(selectUserForm);
                activites.add(localActivity);
                delegateAction.setLocalActivities(activites);

                activites = new ArrayList<>();
                activity = new InvokeMethodActivity();
                list = new ArrayList<>();
                list.add(delegateAction);
                list.add(task);
                activity.setInvokeInformation("ru.efive.wf.core.util.EngineHelper", "doHumanTaskDelegateAction", list);
                activites.add(activity);

                mailActivity = new SendMailActivity();
                sendTo = new ArrayList<>();
                List<String> copyTo = new ArrayList<>();
                copyTo.add(task.getExecutor().getEmail());
                blindCopyTo = new ArrayList<>();
                blindCopyTo.add("alexeyvagizov@gmail.com");
                blindCopyTo.add("nkochubey@inbox.ru");
                message = new MailMessage(sendTo, copyTo, "Делегирован запрос на согласование ",
                        "Делегирован запрос на согласование\n" + task.getExecutor().getDescription() + "\n\n" + "<a href=\"" + in_serverHost + "/component/out/out_document.xhtml?docId=" +
                                process.getProcessedData().getId() + "\" >Ссылка на документ</a>");
                message.setBlindCopyTo(blindCopyTo);
                message.setContentType("text/html");
                mailActivity.setMessage(message);
                activites.add(mailActivity);

                delegateAction.setPreActionActivities(activites);

                result.add(delegateAction);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public StatusChangeAction generateProjectStateReturnAction() {
        StatusChangeAction result = null;
        try {
            /**
             * compose return action
             */
            result = new StatusChangeAction(process) {
                @Override
                public boolean isAvailable() {
                    boolean result = false;
                    try {
                        ProcessedData data = getProcess().getProcessedData();
                        ProcessUser currentUser = getProcess().getProcessUser();

                        Object prop = PropertyUtils.getProperty(data, "author");
                        User user = (prop == null ? null : (User) prop);
                        if (user != null && Objects.equals(user.getId(), currentUser.getId())) {
                            return true;
                        }
                        prop = PropertyUtils.getProperty(data, "initiator");
                        user = (prop == null ? null : (User) prop);
                        if (user != null && Objects.equals(user.getId(), currentUser.getId())) {
                            return true;
                        }
                        prop = PropertyUtils.getProperty(data, "executor");
                        user = (prop == null ? null : (User) prop);
                        if (user != null && Objects.equals(user.getId(), currentUser.getId())) {
                            return true;
                        }
                        prop = PropertyUtils.getProperty(data, "responsible");
                        user = (prop == null ? null : (User) prop);
                        if (user != null && Objects.equals(user.getId(), currentUser.getId())) {
                            return true;
                        }
                    } catch (Exception e) {
                        result = false;
                        e.printStackTrace();
                    }
                    return result;
                }
            };
            result.setAction(DocumentAction.RETURN_TO_EDITING);

            Status<ProcessedData> status = new Status<>();
            status.setStatus(DocumentStatus.DOC_PROJECT_12);
            status.setProcessedData(process.getProcessedData());

            result.setDestinationStatus(status);

            status = new Status<>();
            status.setStatus(DocumentStatus.AGREEMENT_14);
            status.setProcessedData(process.getProcessedData());
            result.setInitialStatus(status);

            List<IActivity> activities = new ArrayList<>();
            SetPropertyActivity activitySetProp = new SetPropertyActivity();
            Map<String, Object> propertyChanges = new HashMap<>();
            propertyChanges.put("agreementTree", null);
            activitySetProp.setPropertyChanges(propertyChanges);
            activities.add(activitySetProp);
            result.setPreActionActivities(activities);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}