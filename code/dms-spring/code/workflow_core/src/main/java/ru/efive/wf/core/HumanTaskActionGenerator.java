package ru.efive.wf.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;

import ru.efive.sql.entity.user.User;
import ru.efive.wf.core.activity.InvokeMethodActivity;
import ru.efive.wf.core.activity.ParametrizedPropertyLocalActivity;
import ru.efive.wf.core.activity.SendMailActivity;
import ru.efive.wf.core.activity.SetPropertyActivity;
import ru.efive.wf.core.activity.enums.EditablePropertyScope;
import ru.efive.wf.core.data.HumanTask;
import ru.efive.wf.core.data.MailMessage;
import ru.efive.wf.core.data.impl.InputReasonForm;
import ru.efive.wf.core.data.impl.SelectUserForm;
import ru.efive.wf.core.util.EngineHelper;

public class HumanTaskActionGenerator {
	
	public HumanTaskActionGenerator(Process process, HumanTaskTreeStateResolver resolver) {
		this.process = process;
		this.resolver = resolver;
	}
	
	//private static String in_serverHost = "http://10.0.1.91:9080/dms";
	private static String in_serverHost = "http://ord.fccho-moscow.ru";
	//private static String in_serverHost="http://10.0.200.60";
	
	public List<IAction> generateActionsFromTask(HumanTask task) {
		List<IAction> result = new ArrayList<IAction>();
		try {
			if (task != null && task.getExecutor() != null && task.getExecutor().getId() != 0 && process.getProcessUser().getId() == task.getExecutor().getId()) {
				/**
				 * compose agree action
				 */
				HumanTaskProcessAction agreeAction = new HumanTaskProcessAction(process, task, resolver);
				agreeAction.setId(10000);
				agreeAction.setName("Согласовано");
				agreeAction.setHistoryAction(false);
				
				List<IActivity> activites = new ArrayList<IActivity>();
				InvokeMethodActivity activity = new InvokeMethodActivity();
				List<Serializable> list = new ArrayList<Serializable>();
				list.add(resolver);
				list.add(task);
				activity.setInvokeInformation("ru.efive.wf.core.util.EngineHelper", "doHumanTaskAgreeAction", list);
				activites.add(activity);
				
				SendMailActivity mailActivity = new SendMailActivity();
				List<String> sendTo = new ArrayList<String>();
				sendTo.add(task.getExecutor().getEmail());
				List<String> blindCopyTo = new ArrayList<String>();
				blindCopyTo.add("alexeyvagizov@gmail.com");
				blindCopyTo.add("nkochubey@inbox.ru");
				MailMessage message = new MailMessage(sendTo, null, "Согласовано", 
						new StringBuilder("Согласовано\n").append(task.getExecutor().getDescription()).append("\n\n").
						append("<a href=\""+in_serverHost+"/component/out_document.xhtml?docId=").
						append(process.getProcessedData().getId()).append("\" >Ссылка на документ</a>").toString());
				message.setBlindCopyTo(blindCopyTo);
				message.setContentType("text/html");
				mailActivity.setMessage(message);
				//activites.add(mailActivity);
				
				agreeAction.setPreActionActivities(activites);
				
				activites = new ArrayList<IActivity>();
				mailActivity = new SendMailActivity();
				sendTo = EngineHelper.doGenerateAgreementSecondaryNotificationList(resolver, task);
				sendTo.add(task.getExecutor().getEmail());
				message = new MailMessage(sendTo, null, "Новый запрос на согласование", 
						new StringBuilder("Новый запрос на согласование\n\n").
						append("<a href=\""+in_serverHost+"/component/out_document.xhtml?docId=").
						append(process.getProcessedData().getId()).append("\" >Ссылка на документ</a>").toString());
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
				declineAction.setId(10001);
				declineAction.setName("Не согласовано");
				declineAction.setHistoryAction(false);
				
				activites = new ArrayList<IActivity>();
				ParametrizedPropertyLocalActivity localActivity = new ParametrizedPropertyLocalActivity();
				localActivity.setParentAction(declineAction);
				InputReasonForm reasonForm = new InputReasonForm();
				reasonForm.setBeanName(process.getProcessedData().getBeanName());
				reasonForm.setActionCommentaryField(EngineHelper.PROP_WF_RESULT_DESCRIPTION);
				reasonForm.setScope(EditablePropertyScope.LOCAL);
				localActivity.setDocument(reasonForm);
				activites.add(localActivity);
				declineAction.setLocalActivities(activites);
				
				activites = new ArrayList<IActivity>();
				activity = new InvokeMethodActivity();
				list = new ArrayList<Serializable>();
				list.add(declineAction);
				list.add(task);
				activity.setInvokeInformation("ru.efive.wf.core.util.EngineHelper", "doHumanTaskDeclineAction", list);
				activites.add(activity);
				
				mailActivity = new SendMailActivity();
				sendTo = new ArrayList<String>();
				sendTo.add(task.getExecutor().getEmail());
				blindCopyTo = new ArrayList<String>();
				blindCopyTo.add("alexeyvagizov@gmail.com");
				blindCopyTo.add("nkochubey@inbox.ru");
				message = new MailMessage(sendTo, null, "Отказ в согласовании ", 
						new StringBuilder("Отказано в согласовании\n").append(task.getExecutor().getDescription()).append("\n\n").
						append("<a href=\""+in_serverHost+"/component/out_document.xhtml?docId=").
						append(process.getProcessedData().getId()).append("\" >Ссылка на документ</a>").toString());
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
				delegateAction.setId(10002);
				delegateAction.setName("Делегировать");
				delegateAction.setHistoryAction(false);
				
				activites = new ArrayList<IActivity>();
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
				
				activites = new ArrayList<IActivity>();
				activity = new InvokeMethodActivity();
				list = new ArrayList<Serializable>();
				list.add(delegateAction);
				list.add(task);
				activity.setInvokeInformation("ru.efive.wf.core.util.EngineHelper", "doHumanTaskDelegateAction", list);
				activites.add(activity);
				
				mailActivity = new SendMailActivity();
				sendTo = new ArrayList<String>();
				List<String> copyTo = new ArrayList<String>();
				copyTo.add(task.getExecutor().getEmail());
				blindCopyTo = new ArrayList<String>();
				blindCopyTo.add("alexeyvagizov@gmail.com");
				blindCopyTo.add("nkochubey@inbox.ru");
				message = new MailMessage(sendTo, copyTo, "Делегирован запрос на согласование ", 
						new StringBuilder("Делегирован запрос на согласование\n").append(task.getExecutor().getDescription()).append("\n\n").
						append("<a href=\""+in_serverHost+"/component/out_document.xhtml?docId=").
						append(process.getProcessedData().getId()).append("\" >Ссылка на документ</a>").toString());
				message.setBlindCopyTo(blindCopyTo);
				message.setContentType("text/html");
				mailActivity.setMessage(message);
				activites.add(mailActivity);
				
				delegateAction.setPreActionActivities(activites);
				
				result.add(delegateAction);
			}
		}
		catch (Exception e) {
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
						if (user != null && user.getId() == currentUser.getId()) {
							return true;
						}
						prop = PropertyUtils.getProperty(data, "initiator");
						user = (prop == null ? null : (User) prop);
						if (user != null && user.getId() == currentUser.getId()) {
							return true;
						}
						prop = PropertyUtils.getProperty(data, "executor");
						user = (prop == null ? null : (User) prop);
						if (user != null && user.getId() == currentUser.getId()) {
							return true;
						}
						prop = PropertyUtils.getProperty(data, "responsible");
						user = (prop == null ? null : (User) prop);
						if (user != null && user.getId() == currentUser.getId()) {
							return true;
						}
					}
					catch (Exception e) {
						result = false;
						e.printStackTrace();
					}
					return result;
				}
			};
			result.setId(-10000);
			result.setName("Вернуть на редактирование");
			
			Status<ProcessedData> status = new Status<ProcessedData>();
			status.setId(1);
			status.setName("Проект документа");
			status.setProcessedData(process.getProcessedData());
			
			result.setDestinationStatus(status);
			
			status = new Status<ProcessedData>();
			status.setId(3);
			status.setName("Согласование");
			status.setProcessedData(process.getProcessedData());
			result.setInitialStatus(status);
			
			List<IActivity> activities = new ArrayList<IActivity>();
			SetPropertyActivity activitySetProp = new SetPropertyActivity();
			Map<String, Object> propertyChanges = new HashMap<String, Object>();
			propertyChanges.put("agreementTree", null);
			activitySetProp.setPropertyChanges(propertyChanges);
			activities.add(activitySetProp);
			result.setPreActionActivities(activities);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
	private Process process;
	private HumanTaskTreeStateResolver resolver;
}