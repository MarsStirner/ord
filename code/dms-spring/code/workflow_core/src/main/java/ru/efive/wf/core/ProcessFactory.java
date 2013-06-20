package ru.efive.wf.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;

import ru.efive.sql.entity.enums.RoleType;
import ru.efive.sql.entity.user.Group;
import ru.efive.sql.entity.user.Role;
import ru.efive.sql.entity.user.User;

import ru.efive.wf.core.activity.InvokeMethodActivity;
import ru.efive.wf.core.activity.ParametrizedPropertyLocalActivity;
import ru.efive.wf.core.activity.SendMailActivity;
import ru.efive.wf.core.activity.enums.EditablePropertyScope;
import ru.efive.wf.core.data.HumanTaskTree;
import ru.efive.wf.core.data.MailMessage;
import ru.efive.wf.core.data.impl.InputDateForm;
import ru.efive.wf.core.data.impl.InputReasonForm;
import ru.efive.wf.core.data.impl.SelectAccessLevelForm;
import ru.efive.wf.core.data.impl.SelectUserForm;
import ru.efive.wf.core.util.EngineHelper;

public final class ProcessFactory {
	private static String in_serverHost="http://ord.fccho-moscow.ru";
	//private static String in_serverHost="http://10.0.200.60";
	//private static String in_serverHost="http://10.0.1.91:9080/dms";

	public static <T extends ProcessedData> Process getProcessByType(T t) {
		Process process = null;
		try {
			System.out.println("process factory initialization");

			process = new Process();
			process.setProcessedData(t);
			Map<Integer, Status<T>> statuses = new HashMap<Integer, Status<T>>();

			Object prop = PropertyUtils.getProperty(t, "id");
			String id = prop == null ? null : prop.toString();
			if (id != null && !id.equals("")) {

				if (t.getType().equals("PaperCopyDocument")) {
					System.out.println("Initialization process for paper copy document");

					// Проект документа - Зарегистрирован
					Status<T> status = new Status<T>();
					status.setId(1);
					status.setName("Проект документа");
					status.setProcessedData(t);
					List<StatusChangeAction> fromStatusActions = new ArrayList<StatusChangeAction>();
					StatusChangeAction toStatusAction = new StatusChangeAction(process);
					toStatusAction.setId(1);
					toStatusAction.setName("Зарегистрировать");
					toStatusAction.setInitialStatus(status);					

					List<IActivity> activites = new ArrayList<IActivity>();
					InvokeMethodActivity activity = new InvokeMethodActivity();
					List<Serializable> list = new ArrayList<Serializable>();
					list.add(t);
					activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "setPaperCopyRegistrationNumber", list);
					activites.add(activity);
					toStatusAction.setPreActionActivities(activites);

					Status<T> toStatus = new Status<T>();
					toStatus.setId(2);
					toStatus.setName("Зарегистрирован");
					toStatus.setProcessedData(t);
					toStatusAction.setDestinationStatus(toStatus);

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					statuses.put(status.getId(), status);

					//Зарегистрирован - Архив
					status = toStatus;
					fromStatusActions = new ArrayList<StatusChangeAction>();
					toStatusAction = new StatusChangeAction(process);
					toStatusAction.setId(99);
					toStatusAction.setName("В архив");
					toStatusAction.setInitialStatus(status);

					activites = new ArrayList<IActivity>();
					activity = new InvokeMethodActivity();
					list = new ArrayList<Serializable>();
					list.add(t);
					activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "checkPaperCopyPropertiesForArchiving", list);
					activites.add(activity);
					toStatusAction.setPreActionActivities(activites);

					toStatus = new Status<T>();
					toStatus.setId(99);
					toStatus.setName("В архиве");
					toStatus.setProcessedData(t);
					toStatusAction.setDestinationStatus(toStatus);

					activites = new ArrayList<IActivity>();
					activity = new InvokeMethodActivity();
					list = new ArrayList<Serializable>();
					list.add(t);
					activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "setPaperCopyParentVolumeUnitsCount", list);
					activites.add(activity);
					toStatus.setPreStatusActivities(activites);

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					statuses.put(status.getId(), status);
					statuses.put(toStatus.getId(), toStatus);

					//Зарегистрирован - Отправлен
					status = statuses.get(2);
					fromStatusActions = statuses.get(2).getAvailableActions();
					toStatusAction = new StatusChangeAction(process);
					toStatusAction.setId(200);
					toStatusAction.setName("Отправить");
					toStatusAction.setInitialStatus(status);

					toStatus = new Status<T>();
					toStatus.setId(200);
					toStatus.setName("Отправлен");
					toStatus.setProcessedData(t);
					toStatusAction.setDestinationStatus(toStatus);

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					//statuses.put(toStatus.getId(), status);

					//Отправлен - Доставлен
					status = toStatus;
					fromStatusActions = new ArrayList<StatusChangeAction>();
					toStatusAction = new StatusChangeAction(process);
					toStatusAction.setId(210);
					toStatusAction.setName("Подтверждение о доставке документа получено");
					toStatusAction.setInitialStatus(status);

					toStatus = new Status<T>();
					toStatus.setId(210);
					toStatus.setName("Доставлен");
					toStatus.setProcessedData(t);
					toStatusAction.setDestinationStatus(toStatus);

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					statuses.put(status.getId(), status);

					statuses.put(toStatus.getId(), toStatus);

					//В архиве - изъят из архива
					status = statuses.get(99);
					fromStatusActions = new ArrayList<StatusChangeAction>();
					toStatusAction = new StatusChangeAction(process);
					toStatusAction.setId(100);
					toStatusAction.setName("Изъять из архива");
					toStatusAction.setInitialStatus(status);

					activites = new ArrayList<IActivity>();
					activity = new InvokeMethodActivity();
					list = new ArrayList<Serializable>();
					list.add(t);
					activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "checkPaperCopyPropertiesForUnfile", list);
					activites.add(activity);
					toStatusAction.setPreActionActivities(activites);

					toStatus = new Status<T>();
					toStatus.setId(110);
					toStatus.setName("Изъят из архива");
					toStatus.setProcessedData(t);
					toStatusAction.setDestinationStatus(toStatus);

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					//statuses.put(status.getId(), status);

					//Изъят из архива - В архиве
					status = toStatus;
					fromStatusActions = new ArrayList<StatusChangeAction>();
					toStatusAction = new StatusChangeAction(process);
					toStatusAction.setId(125);
					toStatusAction.setName("Вернуть в архив");
					toStatusAction.setInitialStatus(status);

					toStatus = statuses.get(99);
					toStatusAction.setDestinationStatus(toStatus);

					activites = new ArrayList<IActivity>();
					activity = new InvokeMethodActivity();
					list = new ArrayList<Serializable>();
					list.add(t);
					activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "setPaperCopyCollectorToEmpty", list);
					activites.add(activity);
					toStatusAction.setPreActionActivities(activites);

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					statuses.put(status.getId(), status);

					//В архиве - документ уничтожен
					status = statuses.get(99);
					fromStatusActions = status.getAvailableActions();//new ArrayList<StatusChangeAction>();
					toStatusAction = new StatusChangeAction(process);
					toStatusAction.setId(115);
					toStatusAction.setName("Уничтожить документ");
					toStatusAction.setInitialStatus(status);

					toStatus = new Status<T>();
					toStatus.setId(120);
					toStatus.setName("Документ уничтожен");
					toStatus.setProcessedData(t);
					toStatusAction.setDestinationStatus(toStatus);

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					statuses.put(status.getId(), status);
					statuses.put(toStatus.getId(), toStatus);

					//В архиве - Направлен в другой архив
					status = statuses.get(99);
					fromStatusActions =status.getAvailableActions();//new ArrayList<StatusChangeAction>();
					toStatusAction = new StatusChangeAction(process);
					toStatusAction.setId(135);
					toStatusAction.setName("Направить в другой архив");
					toStatusAction.setInitialStatus(status);

					toStatus = new Status<T>();
					toStatus.setId(130);
					toStatus.setName("Направлен в другой архив");
					toStatus.setProcessedData(t);
					toStatusAction.setDestinationStatus(toStatus);

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					//statuses.put(status.getId(), status);
					statuses.put(toStatus.getId(), toStatus);


					System.out.println("new process initialization");

					Status<T> currentStatus = statuses.get(t.getStatusId());
					if (currentStatus != null) {
						process.setCurrentStatus(currentStatus);
					}
				}else if (t.getType().equals("IncomingDocument")) {
					System.out.println("Initialization process for incoming document");

					prop = PropertyUtils.getProperty(t, "executionDate");
					Date executionDate = (prop == null ? null : (Date) prop);

					// На регистрации - Зарегистрирован
					Status<T> status = new Status<T>();
					status.setId(1);
					status.setName("На регистрации");
					status.setProcessedData(t);
					List<StatusChangeAction> fromStatusActions = new ArrayList<StatusChangeAction>();
					StatusChangeAction toStatusAction = new StatusChangeAction(process){
						@Override
						public boolean isAvailable() {
							boolean result = false;
							try {
								ProcessedData data = getProcess().getProcessedData();
								ProcessUser user = getProcess().getProcessUser();

								for(Role role:user.getRoles()){
									if((role.getRoleType().equals(RoleType.ENTERPRISE_ADMINISTRATION))){result=true;break;}
									if((role.getRoleType().equals(RoleType.ADMINISTRATOR))){result=true;break;}
									if((role.getRoleType().equals(RoleType.OFFICE_MANAGER))){result=true;break;}
								}							
							}
							catch (Exception e) {
								result = false;
								e.printStackTrace();
							}
							return result;
						}
					};

					toStatusAction.setId(1);
					toStatusAction.setName("Зарегистрировать");
					toStatusAction.setInitialStatus(status);

					List<IActivity> activites = new ArrayList<IActivity>();
					InvokeMethodActivity activity = new InvokeMethodActivity();
					List<Serializable> list = new ArrayList<Serializable>();
					list.add(t);
					activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "setIncomingRegistrationNumber", list);
					activites.add(activity);
					toStatusAction.setPreActionActivities(activites);

					Status<T> toStatus = new Status<T>();
					toStatus.setId(2);
					toStatus.setName("Зарегистрирован");
					toStatus.setProcessedData(t);
					toStatusAction.setDestinationStatus(toStatus);

					activites = new ArrayList<IActivity>();
					SendMailActivity mailActivity=null ;
					prop = PropertyUtils.getProperty(t, "registrationNumber");
					String docNumber = (prop == null ? "" : (String) prop);

					//1-mail
					List<String> sendTo = new ArrayList<String>();
					prop = PropertyUtils.getProperty(t, "executors");
					List<User> executors = (prop == null ? null : (List<User>) prop);
					if(executors!=null){
						for(User user:executors){
							if((user.getEmail()!=null)&&(!user.getEmail().isEmpty())){sendTo.add(user.getEmail());}
						}
						if(sendTo.size()>0){
							mailActivity = new SendMailActivity();
							MailMessage message1 = new MailMessage(sendTo, null, "Вы назнaчены ответственным по документу @DocumentNumber" + docNumber,
									new StringBuilder("Документ перешел в статус \"" + toStatusAction.getDestinationStatus().getName() + "\"\n\n").
									append("<a href=\""+in_serverHost+"/component/in_document.xhtml?docId=").
									append(id).append("\" >Ссылка на документ</a>").toString());
							message1.setContentType("text/html");
							mailActivity.setMessage(message1);
						}
					}
					if(mailActivity!=null){activites.add(mailActivity);mailActivity=null;}

					//2-mail
					sendTo.clear();
					Set<String> recipients=new HashSet<String>();
					activites = new ArrayList<IActivity>();
					prop = PropertyUtils.getProperty(t, "recipientUsers");
					List<User> recipientUsers = (prop == null ? null : (List<User>) prop);
					if(recipientUsers!=null){
						for(User user:recipientUsers){
							if((user.getEmail()!=null)&&(!user.getEmail().isEmpty())){recipients.add(user.getEmail());}
						}
					}

					prop = PropertyUtils.getProperty(t, "recipientGroups");
					Set<Group> recipientGroups = (prop == null ? null : (Set<Group>) prop);
					if(recipientGroups!=null){
						for(Group group:recipientGroups){
							if(group!=null){
								Set<User> members=group.getMembers();
								for(User user:members){
									if((user.getEmail()!=null)&&(!user.getEmail().isEmpty())){recipients.add(user.getEmail());}
								}
							}
						}
					}

					if(recipients.size()>0){
						mailActivity = new SendMailActivity();
						MailMessage message2 = new MailMessage(new ArrayList(recipients), null, "Вам адресован документ @DocumentNumber" + docNumber,
								new StringBuilder("Документ перешел в статус \"" + toStatusAction.getDestinationStatus().getName() + "\"\n\n").
								append("<a href=\""+in_serverHost+"/component/in_document.xhtml?docId=").
								append(id).append("\" >Ссылка на документ</a>").toString());
						message2.setContentType("text/html");
						mailActivity.setMessage(message2);
						activites.add(mailActivity);
						toStatus.setPreStatusActivities(activites);
					}
					if(mailActivity!=null){activites.add(mailActivity);mailActivity=null;}
					if(activites.size()>0){toStatus.setPreStatusActivities(activites);}

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					statuses.put(status.getId(), status);

					//На рассмотрении - На исполнении
					status = toStatus;
					fromStatusActions = new ArrayList<StatusChangeAction>();
					toStatusAction = new StatusChangeAction(process);
					toStatusAction.setId(2);
					toStatusAction.setName("Направить на исполнение");
					toStatusAction.setCommentNecessary(true);
					toStatusAction.setInitialStatus(status);

					toStatus = new Status<T>();
					toStatus.setId(80);
					toStatus.setName("На исполнении");
					toStatus.setProcessedData(t);
					toStatusAction.setDestinationStatus(toStatus);

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					statuses.put(status.getId(), status);


					//На исполнении - Исполнен
					status = toStatus;
					fromStatusActions = new ArrayList<StatusChangeAction>();
					toStatusAction = new StatusChangeAction(process);
					toStatusAction.setId(80);
					toStatusAction.setName("Исполнен");
					toStatusAction.setCommentNecessary(true);
					toStatusAction.setInitialStatus(status);

					toStatus = new Status<T>();
					toStatus.setId(90);
					toStatus.setName("Исполнен");
					toStatus.setProcessedData(t);
					toStatusAction.setDestinationStatus(toStatus);

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					statuses.put(status.getId(), status);

					//Исполнен - Архив
					status = toStatus;
					fromStatusActions = new ArrayList<StatusChangeAction>();
					toStatusAction = new StatusChangeAction(process);
					toStatusAction.setId(90);
					toStatusAction.setName("В архив");
					toStatusAction.setInitialStatus(status);

					activites = new ArrayList<IActivity>();
					activity = new InvokeMethodActivity();
					list = new ArrayList<Serializable>();
					list.add(t);
					activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "checkIncomingPropertiesForArchiving", list);
					activites.add(activity);
					toStatusAction.setPreActionActivities(activites);

					toStatus = new Status<T>();
					toStatus.setId(100);
					toStatus.setName("В архиве");
					toStatus.setProcessedData(t);
					toStatusAction.setDestinationStatus(toStatus);

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					statuses.put(status.getId(), status);
					statuses.put(toStatus.getId(), toStatus);

					//if((t.getStatusId()>=2)&&(t.getStatusId()<100)){

					List<NoStatusAction> noStatusActions = new ArrayList<NoStatusAction>();

					NoStatusAction changeDateAction = new NoStatusAction(process) {
						@Override
						public boolean isAvailable() {
							boolean result = false;
							try {
								if (getProcess().getProcessedData().getStatusId() >= 2 && getProcess().getProcessedData().getStatusId() < 100) return true;
							}
							catch (Exception e) {
								result = false;
								e.printStackTrace();
							}
							return result;
						}
					};
					changeDateAction.setId(1002);
					changeDateAction.setName("Изменить срок исполнения");

					activites = new ArrayList<IActivity>();
					ParametrizedPropertyLocalActivity localActivity2 = new ParametrizedPropertyLocalActivity();
					localActivity2.setParentAction(changeDateAction);
					InputDateForm dateForm = new InputDateForm();
					dateForm.setBeanName(process.getProcessedData().getBeanName());
					dateForm.setActionDateField("executionDate");
					dateForm.setActionDate(executionDate);
					dateForm.setScope(EditablePropertyScope.GLOBAL);
					dateForm.setActionCommentaryField("WFResultDescription");
					dateForm.setActionCommentary("Срок исполнения изменен");
					localActivity2.setDocument(dateForm);
					activites.add(localActivity2);
					changeDateAction.setLocalActivities(activites);

					noStatusActions.add(changeDateAction);

					//Изменить уровень допуска
					NoStatusAction changeAccessLevelAction = new NoStatusAction(process) {
						@Override
						public boolean isAvailable() {
							boolean result = true;
							try {
								if (getProcess().getProcessedData().getStatusId() == 1) return false;
							}
							catch (Exception e) {
								result = false;
								e.printStackTrace();
							}
							return result;
						}
					};
					changeAccessLevelAction.setId(1002);
					changeAccessLevelAction.setName("Изменить уровень допуска");

					activites = new ArrayList<IActivity>();

					localActivity2 = new ParametrizedPropertyLocalActivity();
					localActivity2.setParentAction(changeAccessLevelAction);
					SelectAccessLevelForm accessLevelForm = new SelectAccessLevelForm();
					accessLevelForm.setBeanName(process.getProcessedData().getBeanName());
					accessLevelForm.setSelectedAccessLevelField("userAccessLevel");
					accessLevelForm.setScope(EditablePropertyScope.GLOBAL);					
					localActivity2.setDocument(accessLevelForm);
					activites.add(localActivity2);

					changeAccessLevelAction.setLocalActivities(activites);

					noStatusActions.add(changeAccessLevelAction);

					process.setNoStatusActions(noStatusActions);
					//}

					System.out.println("new process initialization");

					Status<T> currentStatus = statuses.get(t.getStatusId());
					if (currentStatus != null) {
						process.setCurrentStatus(currentStatus);
					}

				}
				else if (t.getType().equals("OutgoingDocument")){
					System.out.println("Initialization process for outgoing document");

					// Проект документа - На исполнении
					Status<T> status = new Status<T>();
					status.setId(1);
					status.setName("Проект документа");
					status.setProcessedData(t);
					List<StatusChangeAction> fromStatusActions = new ArrayList<StatusChangeAction>();
					StatusChangeAction toStatusAction = new StatusChangeAction(process){
						@Override
						public boolean isAvailable() {
							boolean result = false;
							try {
								ProcessedData data = getProcess().getProcessedData();
								ProcessUser user = getProcess().getProcessUser();

								for(Role role:user.getRoles()){
									if((role.getRoleType().equals(RoleType.ENTERPRISE_ADMINISTRATION))){result=true;break;}
									if((role.getRoleType().equals(RoleType.ADMINISTRATOR))){result=true;break;}
									if((role.getRoleType().equals(RoleType.OFFICE_MANAGER))){result=true;break;}
								}		
							}
							catch (Exception e) {
								result = false;
								e.printStackTrace();
							}
							return result;
						}
					};

					toStatusAction.setId(80);
					toStatusAction.setName("Зарегистрировать");
					toStatusAction.setInitialStatus(status);

					List<IActivity> activites = new ArrayList<IActivity>();
					InvokeMethodActivity activity = new InvokeMethodActivity();
					List<Serializable> list = new ArrayList<Serializable>();
					list.add(t);
					activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "setOutgoingRegistrationNumber", list);
					activites.add(activity);

					toStatusAction.setPreActionActivities(activites);

					Status<T> toStatus = new Status<T>();
					toStatus.setId(80);
					toStatus.setName("Зарегистрирован");
					toStatus.setProcessedData(t);
					toStatusAction.setDestinationStatus(toStatus);

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					statuses.put(status.getId(), status);
					statuses.put(toStatus.getId(), toStatus);

					// Проект документа - На рассмотрении
					status = statuses.get(1);
					fromStatusActions = statuses.get(1).getAvailableActions();
					toStatusAction = new StatusChangeAction(process);

					activites = new ArrayList<IActivity>();
					activity = new InvokeMethodActivity();
					toStatusAction.setId(2);
					toStatusAction.setName("Направить на рассмотрение");
					toStatusAction.setInitialStatus(status);
					toStatusAction.setPreActionActivities(activites);

					toStatus = new Status<T>();
					toStatus.setId(2);
					toStatus.setName("На рассмотрении");
					toStatus.setProcessedData(t);
					toStatusAction.setDestinationStatus(toStatus);

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					statuses.put(status.getId(), status);
					statuses.put(toStatus.getId(), toStatus);

					// Проект документа - Согласование
					status = statuses.get(1);
					fromStatusActions = statuses.get(1).getAvailableActions();
					toStatusAction = new StatusChangeAction(process);
					toStatusAction.setId(3);
					toStatusAction.setName("Направить на согласование");
					toStatusAction.setInitialStatus(status);

					Status<T> agreeStatus = new Status<T>();
					agreeStatus.setId(3);
					agreeStatus.setName("Согласование");
					agreeStatus.setProcessedData(t);
					agreeStatus.setAgreementEnabled(true);

					//List<String> sendTo=new ArrayList<String>();
					activites = new ArrayList<IActivity>();
					Object agreementTree = PropertyUtils.getProperty(t, "agreementTree");
					if (agreementTree != null) {
						HumanTaskTree tree = (HumanTaskTree) agreementTree;
						//List<String> sendTo = EngineHelper.doGenerateAgreementPrimaryNotificationList(tree);
						//List<String> blindCopyTo = new ArrayList<String>();
						InvokeMethodActivity addAgreementUsersActivity=null;
						Set<User> executors=EngineHelper.doGenerateAgreementPrimaryExecutors(tree);
						if(executors.size()>0){
							addAgreementUsersActivity=new InvokeMethodActivity();
							list=new ArrayList<Serializable>();
							list.add(t);
							list.add(new ArrayList(executors));
							addAgreementUsersActivity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "addToDocumentAgreementUsers", list);
							activites.add(addAgreementUsersActivity);
						}

						List<String> sendTo=new ArrayList<String>();
						for(User executor: executors){
							if((executor.getEmail()!=null)&&(!executor.getEmail().isEmpty())){
								sendTo.add(executor.getEmail());
							}
						}
						MailMessage message = new MailMessage(sendTo, null, "Новый запрос на согласование",
								new StringBuilder("Новый запрос на согласование\n\n").
								append("<a href=\""+in_serverHost+"/component/out_document.xhtml?docId=").
								append(process.getProcessedData().getId()).append("\" >Ссылка на документ</a>").toString());
						//message.setBlindCopyTo(blindCopyTo);
						message.setContentType("text/html");
						SendMailActivity mailActivity = new SendMailActivity();
						mailActivity.setMessage(message);
						if (sendTo.size() > 0) activites.add(mailActivity);
						agreeStatus.setPreStatusActivities(activites);
					}
					toStatusAction.setDestinationStatus(agreeStatus);

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					statuses.put(status.getId(), status);
					statuses.put(agreeStatus.getId(), agreeStatus);

					//На рассмотрении - Зарегистрирован
					status = statuses.get(2);
					fromStatusActions = statuses.get(2).getAvailableActions();
					toStatusAction = new StatusChangeAction(process){
						@Override
						public boolean isAvailable() {
							boolean result = false;
							try {
								ProcessedData data = getProcess().getProcessedData();
								ProcessUser user = getProcess().getProcessUser();

								for(Role role:user.getRoles()){
									if((role.getRoleType().equals(RoleType.ENTERPRISE_ADMINISTRATION))){result=true;break;}
									if((role.getRoleType().equals(RoleType.ADMINISTRATOR))){result=true;break;}
									if((role.getRoleType().equals(RoleType.OFFICE_MANAGER))){result=true;break;}
								}		
							}
							catch (Exception e) {
								result = false;
								e.printStackTrace();
							}
							return result;
						}
					};
					
					toStatusAction.setId(81);
					toStatusAction.setName("Зарегистрировать");
					toStatusAction.setCommentNecessary(true);
					toStatusAction.setInitialStatus(status);

					toStatus = statuses.get(80);
					toStatusAction.setDestinationStatus(toStatus);

					activites = new ArrayList<IActivity>();
					activity = new InvokeMethodActivity();
					list = new ArrayList<Serializable>();
					list.add(t);
					activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "setOutgoingRegistrationNumber", list);
					activites.add(activity);
					toStatusAction.setPreActionActivities(activites);

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					statuses.put(status.getId(), status);

					activites = new ArrayList<IActivity>();
					prop = PropertyUtils.getProperty(t, "registrationNumber");
					String docNumber = (prop == null ? "" : (String) prop);

					activites = new ArrayList<IActivity>();
					/*//1-mail
					prop = PropertyUtils.getProperty(t, "executor");
					User executor = (prop == null ? null : (User) prop);
					if(executor!=null){
						List<String> sendTo=new ArrayList<String>();
						if((executor.getEmail()!=null)&&(!executor.getEmail().isEmpty())){sendTo.add(executor.getEmail());}
						if(sendTo.size()>0){
							SendMailActivity mailActivity = new SendMailActivity();
							String subject="Вы назнaчены ответственным по документу @DocumentNumber";
							StringBuffer body=new StringBuffer();
							body.append("Документ перешел в статус \"" + toStatusAction.getDestinationStatus().getName() + "\"\n\n");
							MailMessage message = new MailMessage(sendTo, null, subject,body.
									append("<a href=\""+in_serverHost+"/component/task.xhtml?docId=").
									append(id).append("\" >Ссылка на документ</a>").toString());
							message.setContentType("text/html");
							mailActivity.setMessage(message);
							activites.add(mailActivity);
						}
					}
					 */
					//2-mail
					prop = PropertyUtils.getProperty(t, "recipientPersons");
					Set<User> recipientUsers = (prop == null ? null : (Set<User>) prop);
					if(recipientUsers!=null){
						List<String> sendTo=new ArrayList<String>();
						for(User user:recipientUsers){
							if((user.getEmail()!=null)&&(!user.getEmail().isEmpty())){sendTo.add(user.getEmail());}
						}
						if(sendTo.size()>0){
							SendMailActivity mailActivity = new SendMailActivity();
							String subject="Вам адресован документ @DocumentNumber";
							StringBuffer body=new StringBuffer();
							body.append("Документ перешел в статус \"" + toStatusAction.getDestinationStatus().getName() + "\"\n\n");
							MailMessage message = new MailMessage(sendTo, null, subject,body.
									append("<a href=\""+in_serverHost+"/component/out_document.xhtml?docId=").
									append(id).append("\" >Ссылка на документ</a>").toString());
							message.setContentType("text/html");
							mailActivity.setMessage(message);
							activites.add(mailActivity);
						}
					}
					if(activites.size()>0){toStatus.setPreStatusActivities(activites);}

					statuses.put(toStatus.getId(), toStatus);

					// На согласовании - Зарегистрирован
					fromStatusActions = new ArrayList<StatusChangeAction>();
					toStatusAction = new StatusChangeAction(process) {
						@Override
						public boolean isAvailable() {
							boolean result = false;
							try {
								ProcessedData data = getProcess().getProcessedData();
								ProcessUser user = getProcess().getProcessUser();

								boolean isAdmin = false;
								for (int i = 0; i < user.getRoles().size(); i++) {
									if((user.getRoles().iterator().next().getRoleType().equals(RoleType.ENTERPRISE_ADMINISTRATION))) {
										isAdmin = true;
									}else if((user.getRoles().iterator().next().getRoleType().equals(RoleType.ADMINISTRATOR))) {
										isAdmin = true;
									}
								}
								if (isAdmin) {
									result = true;
								}
							/*	else {
									Object prop = PropertyUtils.getProperty(data, "author");
									User author = (prop == null ? null : (User) prop);
									if (author != null && author.getId() == user.getId())
										result = true;
								}*/
							}
							catch (Exception e) {
								result = false;
								e.printStackTrace();
							}
							return result;
						}
					};
					toStatus = statuses.get(80);

					toStatusAction.setId(82);
					toStatusAction.setName("Зарегистрировать");
					toStatusAction.setInitialStatus(agreeStatus);
					toStatusAction.setDestinationStatus(toStatus);

					activites = new ArrayList<IActivity>();
					activity = new InvokeMethodActivity();
					list = new ArrayList<Serializable>();
					list.add(t);
					activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "setOutgoingRegistrationNumber", list);
					activites.add(activity);

					toStatusAction.setPreActionActivities(activites);

					fromStatusActions.add(toStatusAction);
					agreeStatus.setAvailableActions(fromStatusActions);

					statuses.put(agreeStatus.getId(), agreeStatus);

					//Зарегистрирован - Исполнен
					status = toStatus;
					fromStatusActions = new ArrayList<StatusChangeAction>();
					toStatusAction = new StatusChangeAction(process);
					toStatusAction.setId(90);
					toStatusAction.setName("Исполнен");
					toStatusAction.setCommentNecessary(true);
					toStatusAction.setInitialStatus(status);

					toStatus = new Status<T>();
					toStatus.setId(90);
					toStatus.setName("Исполнен");
					toStatus.setProcessedData(t);
					toStatusAction.setDestinationStatus(toStatus);

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					statuses.put(status.getId(), status);

					//Исполнен - В архив
					status=toStatus;
					fromStatusActions = new ArrayList<StatusChangeAction>();
					toStatusAction = new StatusChangeAction(process);
					toStatusAction.setId(99);
					toStatusAction.setName("В архив");
					toStatusAction.setInitialStatus(status);

					activites = new ArrayList<IActivity>();
					activity = new InvokeMethodActivity();
					list = new ArrayList<Serializable>();
					list.add(t);
					activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "checkOutgoingPropertiesForArchiving", list);
					activites.add(activity);
					toStatusAction.setPreActionActivities(activites);

					toStatus = new Status<T>();
					toStatus.setId(100);
					toStatus.setName("В архиве");
					toStatus.setProcessedData(t);
					toStatusAction.setDestinationStatus(toStatus);

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					statuses.put(status.getId(), status);
					statuses.put(toStatus.getId(), toStatus);

					//Изменить уровень допуска
					List<NoStatusAction> noStatusActions = new ArrayList<NoStatusAction>();					
					ParametrizedPropertyLocalActivity localActivity2 = new ParametrizedPropertyLocalActivity();										
					NoStatusAction changeAccessLevelAction = new NoStatusAction(process) {
						@Override
						public boolean isAvailable() {
							boolean result = true;
							try {
								if (getProcess().getProcessedData().getStatusId() == 1) return false;
							}
							catch (Exception e) {
								result = false;
								e.printStackTrace();
							}
							return result;
						}
					};
					changeAccessLevelAction.setId(1002);
					changeAccessLevelAction.setName("Изменить уровень допуска");

					activites = new ArrayList<IActivity>();

					localActivity2 = new ParametrizedPropertyLocalActivity();
					localActivity2.setParentAction(changeAccessLevelAction);
					SelectAccessLevelForm accessLevelForm = new SelectAccessLevelForm();
					accessLevelForm.setBeanName(process.getProcessedData().getBeanName());
					accessLevelForm.setSelectedAccessLevelField("userAccessLevel");
					accessLevelForm.setScope(EditablePropertyScope.GLOBAL);
					localActivity2.setDocument(accessLevelForm);
					activites.add(localActivity2);

					changeAccessLevelAction.setLocalActivities(activites);

					noStatusActions.add(changeAccessLevelAction);

					process.setNoStatusActions(noStatusActions);
					System.out.println("new process initialization");

					Status<T> currentStatus = statuses.get(t.getStatusId());
					if (currentStatus != null) {
						process.setCurrentStatus(currentStatus);
					}

				}
				else if (t.getType().equals("Task")) {
					System.out.println("Initialization process for task");
					prop = PropertyUtils.getProperty(t, "author");
					User author = (prop == null ? null : (User) prop);
					prop = PropertyUtils.getProperty(t, "executor");
					User executor = (prop == null ? null : (User) prop);

					prop = PropertyUtils.getProperty(t, "form");
					Object form = (prop == null ? null : (Object) prop);

					prop=null;
					if(form!=null){prop=PropertyUtils.getProperty(form, "description");}
					String formDescription = (prop == null ? "" : (String) prop);

					prop = PropertyUtils.getProperty(t, "executionDate");
					Date executionDate = (prop == null ? null : (Date) prop);

					// Черновик - На исполнении
					Status<T> status = new Status<T>();
					status.setId(1);
					status.setName("Черновик");
					status.setProcessedData(t);
					List<StatusChangeAction> fromStatusActions = new ArrayList<StatusChangeAction>();
					StatusChangeAction toStatusAction = new StatusChangeAction(process);

					toStatusAction.setId(1);
					toStatusAction.setName("Направить на исполнение");
					toStatusAction.setInitialStatus(status);

					Status<T> toStatus = new Status<T>();
					toStatus.setId(2);
					toStatus.setName("На исполнении");
					toStatus.setProcessedData(t);
					toStatusAction.setDestinationStatus(toStatus);

					List<IActivity> activites = new ArrayList<IActivity>();
					InvokeMethodActivity activity = new InvokeMethodActivity();
					List<Serializable> list = new ArrayList<Serializable>();
					list.add(t);
					activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "setTaskRegistrationNumber", list);
					activites.add(activity);
					toStatusAction.setPreActionActivities(activites);

					activites = new ArrayList<IActivity>();
					if(executor!=null){
						List<String> sendTo=new ArrayList<String>();
						if((executor.getEmail()!=null)&&(!executor.getEmail().isEmpty())){sendTo.add(executor.getEmail());}
						if(sendTo.size()>0){
							SendMailActivity mailActivity = new SendMailActivity();
							String subject="Поступило новое поручение @DocumentNumber";
							StringBuffer body=new StringBuffer();
							if(formDescription.equals("resolution")){
								subject="Поступила новая резолюция @DocumentNumber";
								body=body.append("К вам поступила новая резолюция на исполнение \n\n");
							}else if(formDescription.equals("exercise")){
								prop = PropertyUtils.getProperty(t, "exerciseType");
								Object  exerciseType=(prop == null ? null : (Object) prop);

								prop=null;
								if(form!=null){prop=PropertyUtils.getProperty(exerciseType, "value");}
								String exerciseTypeValue = (prop == null ? "" : (String) prop).toLowerCase();

								if(exerciseType.equals("обращение")||exerciseType.equals("заявление")){
									subject="Поступило новое"+exerciseTypeValue+" @DocumentNumber";
									body=body.append("К вам поступила новое "+exerciseTypeValue+" на исполнение \n\n");
								}else{
									subject="Поступила новая "+exerciseTypeValue+" @DocumentNumber";
									body=body.append("К вам поступила новая"+exerciseTypeValue+" на исполнение \n\n");
								}
							}else{
								body=body.append("К вам поступило новое поручение на исполнение \n\n");
							}

							MailMessage message = new MailMessage(sendTo, null, subject,body.
									append("<a href=\""+in_serverHost+"/component/task.xhtml?docId=").
									append(id).append("\" >Ссылка на документ</a>").toString());
							message.setContentType("text/html");
							mailActivity.setMessage(message);
							activites.add(mailActivity);
						}
					}
					if(activites.size()>0){toStatus.setPreStatusActivities(activites);}

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					statuses.put(status.getId(), status);

					// На рассмотрении - Исполнен
					status = toStatus;
					fromStatusActions = new ArrayList<StatusChangeAction>();
					toStatusAction = new StatusChangeAction(process);
					toStatusAction.setId(2);
					toStatusAction.setName("Исполненo");
					toStatusAction.setInitialStatus(status);

					toStatus = new Status<T>();
					toStatus.setId(3);
					toStatus.setName("Исполненo");
					toStatus.setProcessedData(t);
					toStatusAction.setDestinationStatus(toStatus);

					activites = new ArrayList<IActivity>();
					if(author!=null){
						List<String> sendTo=new ArrayList<String>();
						if((author.getEmail()!=null)&&(!author.getEmail().isEmpty())){sendTo.add(author.getEmail());}
						if(sendTo.size()>0){
							SendMailActivity mailActivity = new SendMailActivity();
							String docNumber=(String)PropertyUtils.getProperty(t, "taskNumber");
							String statusName=toStatusAction.getDestinationStatus().getName();
							String subject="Ваше поручение "+docNumber + " " + statusName;
							StringBuffer body=new StringBuffer();
							if(formDescription.equals("resolution")){
								subject="Ваша резолюция "+docNumber + " " + statusName;
								body=body.append("Резолюция перешла в статус \"" + statusName + "\"\n\n");
							}else if(formDescription.equals("exercise")){
								prop = PropertyUtils.getProperty(t, "exerciseType");
								Object  exerciseType=(prop == null ? null : (Object) prop);

								prop=null;
								if(form!=null){prop=PropertyUtils.getProperty(exerciseType, "value");}
								String exerciseTypeValue = (prop == null ? "" : (String) prop).toLowerCase();

								if(exerciseType.equals("обращение")||exerciseType.equals("заявление")){
									subject="Ваше "+exerciseTypeValue+" "+docNumber + " " + statusName;
									body=body.append("Ваше "+exerciseTypeValue+" перешло в статус "+statusName+" \n\n");
								}else{
									subject="Ваша "+exerciseTypeValue+" "+docNumber + " " + statusName;
									body=body.append("Ваша "+exerciseTypeValue+" перешла в статус "+statusName+" \n\n");
								}
							}else{
								body=body.append("Поручение перешло в статус "+status+"\n\n");
							}

							MailMessage message = new MailMessage(sendTo, null, subject,body.
									append("<a href=\""+in_serverHost+"/component/task.xhtml?docId=").
									append(id).append("\" >Ссылка на документ</a>").toString());
							message.setContentType("text/html");
							mailActivity.setMessage(message);
							activites.add(mailActivity);
						}
					}
					if(activites.size()>0){toStatus.setPreStatusActivities(activites);}

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					statuses.put(status.getId(), status);
					statuses.put(toStatus.getId(), toStatus);

					if(formDescription.equals("exercise")||formDescription.equals("task")){
						//На рассмотрении - Отказ
						status = statuses.get(2);
						fromStatusActions = statuses.get(2).getAvailableActions();//new ArrayList<StatusChangeAction>();
						toStatusAction = new StatusChangeAction(process);
						toStatusAction.setId(25);
						toStatusAction.setName("Отказаться");
						toStatusAction.setInitialStatus(status);

						toStatus = new Status<T>();
						toStatus.setId(4);
						toStatus.setName("Отказ");
						toStatus.setProcessedData(t);
						toStatusAction.setDestinationStatus(toStatus);

						activites = new ArrayList<IActivity>();
						ParametrizedPropertyLocalActivity localActivity = new ParametrizedPropertyLocalActivity();
						localActivity.setParentAction(toStatusAction);
						InputReasonForm reasonForm = new InputReasonForm();
						reasonForm.setBeanName("task");
						reasonForm.setActionCommentaryField("WFResultDescription");
						reasonForm.setScope(EditablePropertyScope.LOCAL);
						localActivity.setDocument(reasonForm);
						activites.add(localActivity);
						toStatusAction.setLocalActivities(activites);

						activites = new ArrayList<IActivity>();
						if(author!=null){
							List<String> sendTo=new ArrayList<String>();
							if((author.getEmail()!=null)&&(!author.getEmail().isEmpty())){sendTo.add(author.getEmail());}
							if(sendTo.size()>0){
								SendMailActivity mailActivity= new SendMailActivity();
								String docNumber=(String)PropertyUtils.getProperty(t, "taskNumber");
								String statusName=toStatusAction.getDestinationStatus().getName();
								String subject="Ваше поручение "+docNumber + " " + statusName;
								StringBuffer body=new StringBuffer();
								if(formDescription.equals("resolution")){
									subject="Ваша резолюция "+docNumber + " " + statusName;
									body=body.append("Резолюция перешла в статус \"" + statusName + "\"\n\n");
								}else if(formDescription.equals("exercise")){
									prop = PropertyUtils.getProperty(t, "exerciseType");
									Object  exerciseType=(prop == null ? null : (Object) prop);

									prop=null;
									if(form!=null){prop=PropertyUtils.getProperty(exerciseType, "value");}
									String exerciseTypeValue = (prop == null ? "" : (String) prop).toLowerCase();

									if(exerciseType.equals("обращение")||exerciseType.equals("заявление")){
										subject="Ваше "+exerciseTypeValue+" "+docNumber + " " + statusName;
										body=body.append("Ваше "+exerciseTypeValue+" перешло в статус "+statusName+" \n\n");
									}else{
										subject="Ваша "+exerciseTypeValue+" "+docNumber + " " + statusName;
										body=body.append("Ваша "+exerciseTypeValue+" перешла в статус "+statusName+" \n\n");
									}
								}else{
									body=body.append("Поручение перешло в статус "+status+"\n\n");
								}

								MailMessage message = new MailMessage(sendTo, null, subject,body.
										append("<a href=\""+in_serverHost+"/component/task.xhtml?docId=").
										append(id).append("\" >Ссылка на документ</a>").toString());
								message.setContentType("text/html");
								mailActivity.setMessage(message);
								activites.add(mailActivity);
							}
						}
						if(activites.size()>0){toStatus.setPreStatusActivities(activites);}

						fromStatusActions.add(toStatusAction);
						status.setAvailableActions(fromStatusActions);

						statuses.put(status.getId(), status);
						statuses.put(toStatus.getId(), toStatus);
					}

					if(formDescription.equals("task") && t.getStatusId()==2){

						List<NoStatusAction> noStatusActions = new ArrayList<NoStatusAction>();
						NoStatusAction delegateAction = new NoStatusAction(process);/*  {

							@Override
							public boolean isAvailable() {
								boolean result = false;
								try {
									ProcessedData data = getProcess().getProcessedData();
									ProcessUser currentUser = getProcess().getProcessUser();

									if (data.getStatusId() == 2) return true;

								}
								catch (Exception e) {
									result = false;
									e.printStackTrace();
								}
								return result;
							}
						};*/

						delegateAction.setId(1001);
						delegateAction.setName("Делегировать");

						activites = new ArrayList<IActivity>();
						ParametrizedPropertyLocalActivity localActivity = new ParametrizedPropertyLocalActivity();
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
						list.add(t);
						activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "doTaskDelegateAction", list);
						activites.add(activity);

						if(executor!=null){
							List<String> sendTo=new ArrayList<String>();
							if((executor.getEmail()!=null)&&(!executor.getEmail().isEmpty())){sendTo.add(executor.getEmail());}
							if(sendTo.size()>0){
								SendMailActivity mailActivity = new SendMailActivity();
								String subject="Поступило новое поручение @DocumentNumber";
								StringBuffer body=new StringBuffer();
								if(formDescription.equals("resolution")){
									subject="Поступила новая резолюция @DocumentNumber";
									body=body.append("К вам поступила новая резолюция на исполнение \n\n");
								}else if(formDescription.equals("exercise")){
									prop = PropertyUtils.getProperty(t, "exerciseType");
									Object  exerciseType=(prop == null ? null : (Object) prop);

									prop=null;
									if(form!=null){prop=PropertyUtils.getProperty(exerciseType, "value");}
									String exerciseTypeValue = (prop == null ? "" : (String) prop).toLowerCase();

									if(exerciseType.equals("обращение")||exerciseType.equals("заявление")){
										subject="Поступило новое"+exerciseTypeValue+" @DocumentNumber";
										body=body.append("К вам поступила новое "+exerciseTypeValue+" на исполнение \n\n");
									}else{
										subject="Поступила новая "+exerciseTypeValue+" @DocumentNumber";
										body=body.append("К вам поступила новая"+exerciseTypeValue+" на исполнение \n\n");
									}
								}else{
									body=body.append("К вам поступило новое поручение на исполнение \n\n");
								}

								MailMessage message = new MailMessage(sendTo, null, subject,body.
										append("<a href=\""+in_serverHost+"/component/task.xhtml?docId=").
										append(id).append("\" >Ссылка на документ</a>").toString());
								message.setContentType("text/html");
								mailActivity.setMessage(message);
								activites.add(mailActivity);
							}

							if(activites.size()>0){delegateAction.setPreActionActivities(activites);}
							noStatusActions.add(delegateAction);
						}

						if(formDescription.equals("task")){

							NoStatusAction changeDateAction = new NoStatusAction(process);
							changeDateAction.setId(1002);
							changeDateAction.setName("Изменить срок исполнения");

							activites = new ArrayList<IActivity>();
							ParametrizedPropertyLocalActivity localActivity2 = new ParametrizedPropertyLocalActivity();
							localActivity2.setParentAction(changeDateAction);
							InputDateForm dateForm = new InputDateForm();
							dateForm.setBeanName(process.getProcessedData().getBeanName());
							dateForm.setActionDateField("controlDate");
							dateForm.setActionDate(executionDate);
							dateForm.setScope(EditablePropertyScope.GLOBAL);
							dateForm.setActionCommentaryField("WFResultDescription");
							dateForm.setActionCommentary("Срок исполнения изменен");
							//localActivity.setDocument(dateForm);
							//dateForm.setScope(EditablePropertyScope.LOCAL);
							localActivity2.setDocument(dateForm);
							activites.add(localActivity2);
							changeDateAction.setLocalActivities(activites);

							//if(activites.size()>0){toStatus.setPreStatusActivities(activites);}

							//if(activites.size()>0){changeDateAction.setPreActionActivities(activites);}
							noStatusActions.add(changeDateAction);

							process.setNoStatusActions(noStatusActions);
						}
						//if(activites.size()>0){toStatus.setPreStatusActivities(activites);}

						process.setNoStatusActions(noStatusActions);
					}

					System.out.println("new process initialization");

					Status<T> currentStatus = statuses.get(t.getStatusId());
					if (currentStatus != null) {
						process.setCurrentStatus(currentStatus);
					}
				}
				else if (t.getType().equals("InternalDocument")){
					System.out.println("Initialization process for internal document");
					List<StatusChangeAction> fromBranchingStatusActions = new ArrayList<StatusChangeAction>();

					// Черновик - На рассмотрении
					Status<T> status = new Status<T>();
					status.setId(1);
					status.setName("Черновик");
					status.setProcessedData(t);
					List<StatusChangeAction> fromStatusActions = new ArrayList<StatusChangeAction>();
					StatusChangeAction toStatusAction = new StatusChangeAction(process);

					List<Serializable> list = new ArrayList<Serializable>();

					toStatusAction.setId(1);
					toStatusAction.setName("Направить на рассмотрение");
					toStatusAction.setInitialStatus(status);

					List<IActivity> activites = new ArrayList<IActivity>();
					InvokeMethodActivity  activity = new InvokeMethodActivity();
					list.add(t);
					activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "checkInternalDocumentPropertiesForReview", list);
					activites.add(activity);
					toStatusAction.setPreActionActivities(activites);

					Status<T> toStatus = new Status<T>();
					toStatus.setId(2);
					toStatus.setName("На рассмотрении");
					toStatus.setProcessedData(t);
					toStatusAction.setDestinationStatus(toStatus);

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					statuses.put(status.getId(), status);

					// На рассмотрении - Зарегистрирован
					status=toStatus;
					fromStatusActions = new ArrayList<StatusChangeAction>();
					toStatusAction = new StatusChangeAction(process){
						@Override
						public boolean isAvailable() {
							boolean result = false;
							try {
								ProcessedData data = getProcess().getProcessedData();
								ProcessUser user = getProcess().getProcessUser();

								for(Role role:user.getRoles()){
									if((role.getRoleType().equals(RoleType.ENTERPRISE_ADMINISTRATION))){result=true;break;}
									if((role.getRoleType().equals(RoleType.ADMINISTRATOR))){result=true;break;}
									if((role.getRoleType().equals(RoleType.OFFICE_MANAGER))){result=true;break;}
								}		
							}
							catch (Exception e) {
								result = false;
								e.printStackTrace();
							}
							return result;
						}
					};

					toStatusAction.setId(5);
					toStatusAction.setName("Зарегистрировать");
					toStatusAction.setInitialStatus(status);

					activites = new ArrayList<IActivity>();
					activity = new InvokeMethodActivity();
					list = new ArrayList<Serializable>();
					list.add(t);
					activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "setInternalRegistrationNumber", list);
					activites.add(activity);
					toStatusAction.setPreActionActivities(activites);

					toStatus = new Status<T>();
					toStatus.setId(5);
					toStatus.setName("Зарегистрирован");
					toStatus.setProcessedData(t);
					toStatusAction.setDestinationStatus(toStatus);

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					statuses.put(status.getId(), status);
					//statuses.put(toStatus.getId(), toStatus);

					// Черновик - Зарегистрирован
					status=statuses.get(1);
					fromStatusActions = statuses.get(1).getAvailableActions();
					toStatusAction = new StatusChangeAction(process){
						@Override
						public boolean isAvailable() {
							boolean result = false;
							try {
								ProcessedData data = getProcess().getProcessedData();
								ProcessUser user = getProcess().getProcessUser();

								for(Role role:user.getRoles()){
									if((role.getRoleType().equals(RoleType.ENTERPRISE_ADMINISTRATION))){result=true;break;}
									if((role.getRoleType().equals(RoleType.ADMINISTRATOR))){result=true;break;}
									if((role.getRoleType().equals(RoleType.OFFICE_MANAGER))){result=true;break;}
								}		
							}
							catch (Exception e) {
								result = false;
								e.printStackTrace();
							}
							return result;
						}
					};

					toStatusAction.setId(55);
					toStatusAction.setName("Зарегистрировать");
					toStatusAction.setInitialStatus(status);

					activites = new ArrayList<IActivity>();
					activity = new InvokeMethodActivity();
					list = new ArrayList<Serializable>();
					list.add(t);
					activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "setInternalRegistrationNumber", list);
					activites.add(activity);
					toStatusAction.setPreActionActivities(activites);

					//toStatus = new Status<T>();
					//toStatus.setId(5);
					//toStatus.setName("Зарегистрирован");
					//toStatus.setProcessedData(t);
					//toStatus=statuses.get(5);
					//toStatus.setProcessedData(t);
					toStatusAction.setDestinationStatus(toStatus);

					prop = PropertyUtils.getProperty(t, "registrationNumber");
					String docNumber = (prop == null ? "" : (String) prop);

					//1-mail
					Set<String> recipients=new HashSet<String>();
					activites = new ArrayList<IActivity>();
					prop = PropertyUtils.getProperty(t, "recipientUsers");
					List<User> recipientUsers = (prop == null ? null : (List<User>) prop);
					if(recipientUsers!=null){
						for(User user:recipientUsers){
							if((user.getEmail()!=null)&&(!user.getEmail().isEmpty())){recipients.add(user.getEmail());}
						}
					}

					prop = PropertyUtils.getProperty(t, "recipientGroups");
					Set<Group> recipientGroups = (prop == null ? null : (Set<Group>) prop);
					if(recipientGroups!=null){
						for(Group group:recipientGroups){
							if(group!=null){
								Set<User> members=group.getMembers();
								for(User user:members){
									if((user.getEmail()!=null)&&(!user.getEmail().isEmpty())){recipients.add(user.getEmail());}
								}
							}
						}
					}

					if(recipients.size()>0){
						SendMailActivity mailActivity = new SendMailActivity();
						mailActivity = new SendMailActivity();
						MailMessage message2 = new MailMessage(new ArrayList(recipients), null, "Вам адресован документ @DocumentNumber" + docNumber,
								new StringBuilder("Документ перешел в статус \"" + toStatusAction.getDestinationStatus().getName() + "\"\n\n").
								append("<a href=\""+in_serverHost+"/component/internal_document.xhtml?docId=").
								append(id).append("\" >Ссылка на документ</a>").toString());
						message2.setContentType("text/html");
						mailActivity.setMessage(message2);
						activites.add(mailActivity);
						toStatus.setPreStatusActivities(activites);
					}

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					statuses.put(toStatus.getId(), toStatus);

					// Проект документа - Согласование
					status = statuses.get(1);
					fromStatusActions = statuses.get(1).getAvailableActions();
					toStatusAction = new StatusChangeAction(process);
					toStatusAction.setId(3);
					toStatusAction.setName("Направить на согласование");
					toStatusAction.setInitialStatus(status);

					Status<T> agreeStatus = new Status<T>();
					agreeStatus.setId(3);
					agreeStatus.setName("Согласование");
					agreeStatus.setProcessedData(t);
					agreeStatus.setAgreementEnabled(true);

					//List<String> sendTo=new ArrayList<String>();
					activites = new ArrayList<IActivity>();
					Object agreementTree = PropertyUtils.getProperty(t, "agreementTree");
					if (agreementTree != null) {
						HumanTaskTree tree = (HumanTaskTree) agreementTree;
						//List<String> sendTo = EngineHelper.doGenerateAgreementPrimaryNotificationList(tree);
						//List<String> blindCopyTo = new ArrayList<String>();
						InvokeMethodActivity addAgreementUsersActivity=null;
						Set<User> executors=EngineHelper.doGenerateAgreementPrimaryExecutors(tree);
						if(executors.size()>0){
							addAgreementUsersActivity=new InvokeMethodActivity();
							list=new ArrayList<Serializable>();
							list.add(t);
							list.add(new ArrayList(executors));
							addAgreementUsersActivity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "addToDocumentAgreementUsers", list);
							activites.add(addAgreementUsersActivity);
						}

						List<String> sendTo=new ArrayList<String>();
						for(User executor: executors){
							if((executor.getEmail()!=null)&&(!executor.getEmail().isEmpty())){
								sendTo.add(executor.getEmail());
							}
						}
						MailMessage message = new MailMessage(sendTo, null, "Новый запрос на согласование",
								new StringBuilder("Новый запрос на согласование\n\n").
								append("<a href=\""+in_serverHost+"/component/internal_document.xhtml?docId=").
								append(process.getProcessedData().getId()).append("\" >Ссылка на документ</a>").toString());
						//message.setBlindCopyTo(blindCopyTo);
						message.setContentType("text/html");
						SendMailActivity mailActivity = new SendMailActivity();
						mailActivity.setMessage(message);
						if (sendTo.size() > 0) activites.add(mailActivity);
						agreeStatus.setPreStatusActivities(activites);
					}
					toStatusAction.setDestinationStatus(agreeStatus);

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					statuses.put(status.getId(), status);

					// На согласовании - Зарегистрирован
					fromStatusActions = new ArrayList<StatusChangeAction>();
					toStatusAction = new StatusChangeAction(process) {
						@Override
						public boolean isAvailable() {
							boolean result = false;
							try {
								ProcessedData data = getProcess().getProcessedData();
								ProcessUser user = getProcess().getProcessUser();

								boolean isAdmin = false;
								for (int i = 0; i < user.getRoles().size(); i++) {
									if (user.getRoles().iterator().next().getRoleType().equals(RoleType.ENTERPRISE_ADMINISTRATION)) {
										isAdmin = true;
									}
								}
								if (isAdmin) {
									result = true;
								}
								/*else {
									Object prop = PropertyUtils.getProperty(data, "initiator");
									User author = (prop == null ? null : (User) prop);
									if (author != null && author.getId() == user.getId())
										result = true;
								}*/
							}
							catch (Exception e) {
								result = false;
								e.printStackTrace();
							}
							return result;
						}
					};

					toStatusAction.setId(6);
					toStatusAction.setName("Зарегистрировать");
					toStatusAction.setInitialStatus(agreeStatus);
					toStatusAction.setDestinationStatus(toStatus);

					activites = new ArrayList<IActivity>();
					activity = new InvokeMethodActivity();
					list = new ArrayList<Serializable>();
					list.add(t);
					activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "setInternalRegistrationNumber", list);
					activites.add(activity);

					toStatusAction.setPreActionActivities(activites);

					fromStatusActions.add(toStatusAction);
					agreeStatus.setAvailableActions(fromStatusActions);

					statuses.put(agreeStatus.getId(), agreeStatus);

					//Зарегистрирован - На исполнении
					status = toStatus;
					fromStatusActions = new ArrayList<StatusChangeAction>();
					toStatusAction = new StatusChangeAction(process);
					toStatusAction.setId(80);
					toStatusAction.setName("Направить на исполнение");
					toStatusAction.setCommentNecessary(true);
					toStatusAction.setInitialStatus(status);

					toStatus = new Status<T>();
					toStatus.setId(80);
					toStatus.setName("На исполнении");
					toStatus.setProcessedData(t);
					toStatusAction.setDestinationStatus(toStatus);

					activites = new ArrayList<IActivity>();
					//1-mail
					prop = PropertyUtils.getProperty(t, "responsible");
					User executor = (prop == null ? null : (User) prop);
					if(executor!=null){
						List<String> sendTo=new ArrayList<String>();
						if((executor.getEmail()!=null)&&(!executor.getEmail().isEmpty())){sendTo.add(executor.getEmail());}
						if(sendTo.size()>0){
							SendMailActivity mailActivity = new SendMailActivity();
							String subject="Вы назнaчены ответственным по документу @DocumentNumber";
							StringBuffer body=new StringBuffer();
							body.append("Документ перешел в статус \"" + toStatusAction.getDestinationStatus().getName() + "\"\n\n");
							MailMessage message = new MailMessage(sendTo, null, subject,body.
									append("<a href=\""+in_serverHost+"/component/internal_document.xhtml?docId=").
									append(id).append("\" >Ссылка на документ</a>").toString());
							message.setContentType("text/html");
							mailActivity.setMessage(message);
							activites.add(mailActivity);
							toStatus.setPreStatusActivities(activites);
						}
					}

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					statuses.put(status.getId(), status);

					//На исполнении - Исполнен
					status = toStatus;
					fromStatusActions = new ArrayList<StatusChangeAction>();
					toStatusAction = new StatusChangeAction(process);
					toStatusAction.setId(80);
					toStatusAction.setName("Исполнен");
					toStatusAction.setCommentNecessary(true);
					toStatusAction.setInitialStatus(status);

					toStatus = new Status<T>();
					toStatus.setId(90);
					toStatus.setName("Исполнен");
					toStatus.setProcessedData(t);
					toStatusAction.setDestinationStatus(toStatus);

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					statuses.put(status.getId(), status);

					//Исполнен - Архив
					status = toStatus;
					fromStatusActions = new ArrayList<StatusChangeAction>();
					toStatusAction = new StatusChangeAction(process);
					toStatusAction.setId(90);
					toStatusAction.setName("В архив");
					toStatusAction.setInitialStatus(status);

					activites = new ArrayList<IActivity>();
					activity = new InvokeMethodActivity();
					list = new ArrayList<Serializable>();
					list.add(t);
					activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "checkInternalPropertiesForArchiving", list);
					activites.add(activity);
					toStatusAction.setPreActionActivities(activites);

					toStatus = new Status<T>();
					toStatus.setId(100);
					toStatus.setName("Архив");
					toStatus.setProcessedData(t);
					toStatusAction.setDestinationStatus(toStatus);

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					statuses.put(status.getId(), status);
					statuses.put(toStatus.getId(), toStatus);

					//На рассмотрении - Отказ
					status = statuses.get(2);
					fromStatusActions = statuses.get(2).getAvailableActions();
					toStatusAction = new StatusChangeAction(process);
					toStatusAction.setId(150);
					toStatusAction.setName("Отказать");
					toStatusAction.setInitialStatus(status);

					toStatus = new Status<T>();
					toStatus.setId(150);
					toStatus.setName("Отказ");
					toStatus.setProcessedData(t);
					toStatusAction.setDestinationStatus(toStatus);

					activites = new ArrayList<IActivity>();
					ParametrizedPropertyLocalActivity localActivity = new ParametrizedPropertyLocalActivity();
					localActivity.setParentAction(toStatusAction);
					InputReasonForm reasonForm = new InputReasonForm();
					reasonForm.setBeanName("internal_doc");
					reasonForm.setActionCommentaryField("WFResultDescription");
					reasonForm.setScope(EditablePropertyScope.LOCAL);
					localActivity.setDocument(reasonForm);
					activites.add(localActivity);
					toStatusAction.setLocalActivities(activites);

					prop = PropertyUtils.getProperty(t, "initiator");
					User initiator = (prop == null ? null : (User) prop);
					if(initiator!=null){
						List<String> sendTo=new ArrayList<String>();
						if((initiator.getEmail()!=null)&&(!initiator.getEmail().isEmpty())){sendTo.add(initiator.getEmail());}
						if(sendTo.size()>0){
							activites = new ArrayList<IActivity>();
							SendMailActivity mailActivity = new SendMailActivity();
							String subject="Ваш документ перешел перешел в статус \"" + toStatusAction.getDestinationStatus().getName()+"\"";
							StringBuffer body=new StringBuffer();
							body.append("Документ перешел в статус \"" + toStatusAction.getDestinationStatus().getName() + "\"\n\n");
							MailMessage message = new MailMessage(sendTo, null, subject,body.
									append("<a href=\""+in_serverHost+"/component/internal_document.xhtml?docId=").
									append(id).append("\" >Ссылка на документ</a>").toString());
							message.setContentType("text/html");
							mailActivity.setMessage(message);
							activites.add(mailActivity);
							toStatus.setPreStatusActivities(activites);
						}
					}

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					statuses.put(status.getId(), status);
					statuses.put(toStatus.getId(), toStatus);

					//Изменить уровень допуска
					List<NoStatusAction> noStatusActions = new ArrayList<NoStatusAction>();					
					ParametrizedPropertyLocalActivity localActivity2 = new ParametrizedPropertyLocalActivity();										
					NoStatusAction changeAccessLevelAction = new NoStatusAction(process) {
						@Override
						public boolean isAvailable() {
							boolean result = true;
							try {
								if (getProcess().getProcessedData().getStatusId() == 1) return false;
							}
							catch (Exception e) {
								result = false;
								e.printStackTrace();
							}
							return result;
						}
					};
					changeAccessLevelAction.setId(1002);
					changeAccessLevelAction.setName("Изменить уровень допуска");

					activites = new ArrayList<IActivity>();

					localActivity2 = new ParametrizedPropertyLocalActivity();
					localActivity2.setParentAction(changeAccessLevelAction);
					SelectAccessLevelForm accessLevelForm = new SelectAccessLevelForm();
					accessLevelForm.setBeanName(process.getProcessedData().getBeanName());
					accessLevelForm.setSelectedAccessLevelField("userAccessLevel");
					accessLevelForm.setScope(EditablePropertyScope.GLOBAL);
					localActivity2.setDocument(accessLevelForm);
					activites.add(localActivity2);

					changeAccessLevelAction.setLocalActivities(activites);

					noStatusActions.add(changeAccessLevelAction);

					process.setNoStatusActions(noStatusActions);
					
					System.out.println("new process initialization");

					Status<T> currentStatus = statuses.get(t.getStatusId());
					if (currentStatus != null) {
						process.setCurrentStatus(currentStatus);
					}

				}
				else if (t.getType().equals("RequestDocument")){
					System.out.println("Initialization process for request document");

					// Черновик - На рассмотрении
					Status<T> status = new Status<T>();
					status.setId(1);
					status.setName("Черновик");
					status.setProcessedData(t);
					List<StatusChangeAction> fromStatusActions = new ArrayList<StatusChangeAction>();
					StatusChangeAction toStatusAction = new StatusChangeAction(process);

					toStatusAction.setId(1);
					toStatusAction.setName("Зарегистрировать");
					toStatusAction.setInitialStatus(status);

					List<IActivity> activites = new ArrayList<IActivity>();
					InvokeMethodActivity activity = new InvokeMethodActivity();
					List<Serializable> list = new ArrayList<Serializable>();
					list.add(t);
					activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "setRequestRegistrationNumber", list);
					activites.add(activity);
					toStatusAction.setPreActionActivities(activites);

					Status<T> toStatus = new Status<T>();
					toStatus.setId(2);
					toStatus.setName("На рассмотрении");
					toStatus.setProcessedData(t);
					toStatusAction.setDestinationStatus(toStatus);

					activites = new ArrayList<IActivity>();
					SendMailActivity mailActivity=null ;
					prop = PropertyUtils.getProperty(t, "registrationNumber");
					String docNumber = (prop == null ? "" : (String) prop);

					//2-mail
					List<String> sendTo=new ArrayList<String>();
					Set<String> recipients=new HashSet<String>();
					activites = new ArrayList<IActivity>();
					prop = PropertyUtils.getProperty(t, "recipientUsers");
					List<User> recipientUsers = (prop == null ? null : (List<User>) prop);
					if(recipientUsers!=null){
						for(User user:recipientUsers){
							if((user.getEmail()!=null)&&(!user.getEmail().isEmpty())){recipients.add(user.getEmail());}
						}
					}

					prop = PropertyUtils.getProperty(t, "recipientGroups");
					Set<Group> recipientGroups = (prop == null ? null : (Set<Group>) prop);
					if(recipientGroups!=null){
						for(Group group:recipientGroups){
							if(group!=null){
								Set<User> members=group.getMembers();
								for(User user:members){
									if((user.getEmail()!=null)&&(!user.getEmail().isEmpty())){recipients.add(user.getEmail());}
								}
							}
						}
					}

					if(recipients.size()>0){
						mailActivity = new SendMailActivity();
						MailMessage message2 = new MailMessage(new ArrayList(recipients), null, "Вам адресован документ @DocumentNumber" + docNumber,
								new StringBuilder("Документ перешел в статус \"" + toStatusAction.getDestinationStatus().getName() + "\"\n\n").
								append("<a href=\""+in_serverHost+"/component/request_document.xhtml?docId=").
								append(id).append("\" >Ссылка на документ</a>").toString());
						message2.setContentType("text/html");
						mailActivity.setMessage(message2);
						activites.add(mailActivity);
						toStatus.setPreStatusActivities(activites);
					}
					if(mailActivity!=null){activites.add(mailActivity);mailActivity=null;}
					if(activites.size()>0){toStatus.setPreStatusActivities(activites);}

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					statuses.put(status.getId(), status);


					//На рассмотрении - На исполнении
					status = toStatus;
					fromStatusActions = new ArrayList<StatusChangeAction>();
					toStatusAction = new StatusChangeAction(process);
					toStatusAction.setId(2);
					toStatusAction.setName("Направить на исполнение");
					toStatusAction.setCommentNecessary(true);
					toStatusAction.setInitialStatus(status);

					toStatus = new Status<T>();
					toStatus.setId(80);
					toStatus.setName("На исполнении");
					toStatus.setProcessedData(t);
					toStatusAction.setDestinationStatus(toStatus);

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					sendTo.clear();
					mailActivity=null;
					prop = PropertyUtils.getProperty(t, "executor");
					User responsible = (prop == null ? null : (User) prop);
					if(responsible!=null){
						mailActivity = new SendMailActivity();
						sendTo.add(responsible.getEmail());
						MailMessage message1 = new MailMessage(sendTo, null, "Вы назнaчены ответственным по документу @DocumentNumber" + docNumber,
								new StringBuilder("Документ перешел в статус \"" + toStatusAction.getDestinationStatus().getName() + "\"\n\n").
								append("<a href=\""+in_serverHost+"/component/request_document.xhtml?docId=").
								append(id).append("\" >Ссылка на документ</a>").toString());
						message1.setContentType("text/html");
						mailActivity.setMessage(message1);
					}
					if(mailActivity!=null){activites.add(mailActivity);mailActivity=null;}
					if(activites.size()>0){toStatus.setPreStatusActivities(activites);}

					statuses.put(status.getId(), status);

					//На исполнении - Исполнен
					status = toStatus;
					fromStatusActions = new ArrayList<StatusChangeAction>();
					toStatusAction = new StatusChangeAction(process);
					toStatusAction.setId(80);
					toStatusAction.setName("Исполнен");
					toStatusAction.setCommentNecessary(true);
					toStatusAction.setInitialStatus(status);

					toStatus = new Status<T>();
					toStatus.setId(90);
					toStatus.setName("Исполнен");
					toStatus.setProcessedData(t);
					toStatusAction.setDestinationStatus(toStatus);

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					statuses.put(status.getId(), status);

					//Исполнен - Архив
					status = toStatus;
					fromStatusActions = new ArrayList<StatusChangeAction>();
					toStatusAction = new StatusChangeAction(process);
					toStatusAction.setId(90);
					toStatusAction.setName("В архив");
					toStatusAction.setInitialStatus(status);

					toStatus = new Status<T>();
					toStatus.setId(100);
					toStatus.setName("Архив");
					toStatus.setProcessedData(t);
					toStatusAction.setDestinationStatus(toStatus);

					activites = new ArrayList<IActivity>();
					activity = new InvokeMethodActivity();
					list = new ArrayList<Serializable>();
					list.add(t);
					activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "checkRequestPropertiesForArchiving", list);
					activites.add(activity);
					toStatusAction.setPreActionActivities(activites);

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					statuses.put(status.getId(), status);
					statuses.put(toStatus.getId(), toStatus);

					System.out.println("new process initialization");

					Status<T> currentStatus = statuses.get(t.getStatusId());
					if (currentStatus != null) {
						process.setCurrentStatus(currentStatus);
					}

				}
				else if (t.getType().equals("OfficeKeepingFile")) {
					System.out.println("Initialization process for office keeping file");

					// На регистрации - Зарегистрирован
					Status<T> status = new Status<T>();
					status.setId(1);
					status.setName("Проект");
					status.setProcessedData(t);
					List<StatusChangeAction> fromStatusActions = new ArrayList<StatusChangeAction>();
					StatusChangeAction toStatusAction = new StatusChangeAction(process);

					toStatusAction.setId(1);
					toStatusAction.setName("Зарегистрировать");
					toStatusAction.setInitialStatus(status);

					List<IActivity> activites = new ArrayList<IActivity>();
					InvokeMethodActivity activity = new InvokeMethodActivity();
					List<Serializable> list = new ArrayList<Serializable>();
					list.add(t);
					activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "setOfficeKeepingFileRegistrationNumber", list);
					activites.add(activity);
					toStatusAction.setPreActionActivities(activites);

					Status<T> toStatus = new Status<T>();
					toStatus.setId(2);
					toStatus.setName("Зарегистрировано");
					toStatus.setProcessedData(t);
					toStatusAction.setDestinationStatus(toStatus);

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					statuses.put(status.getId(), status);

					//На рассмотрении - На исполнении
					/*
					status = toStatus;
					fromStatusActions = new ArrayList<StatusChangeAction>();
					toStatusAction = new StatusChangeAction(process);
					toStatusAction.setId(2);
					toStatusAction.setName("Направить на исполнение");
					toStatusAction.setCommentNecessary(true);
					toStatusAction.setInitialStatus(status);

					toStatus = new Status<T>();
					toStatus.setId(80);
					toStatus.setName("На исполнении");
					toStatus.setProcessedData(t);
					toStatusAction.setDestinationStatus(toStatus);

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					statuses.put(status.getId(), status);*/
					statuses.put(toStatus.getId(), toStatus);

					Status<T> currentStatus = statuses.get(t.getStatusId());
					if (currentStatus != null) {
						process.setCurrentStatus(currentStatus);
					}

					System.out.println("new process initialization");

				}
				else if (t.getType().equals("OfficeKeepingVolume")) {
					System.out.println("Initialization process for office keeping volume");

					// Проект тома - Открыт
					Status<T> status = new Status<T>();
					status.setId(1);
					status.setName("Проект тома");
					status.setProcessedData(t);
					List<StatusChangeAction> fromStatusActions = new ArrayList<StatusChangeAction>();
					StatusChangeAction toStatusAction = new StatusChangeAction(process);

					toStatusAction.setId(1);
					toStatusAction.setName("Зарегистрировать");
					toStatusAction.setInitialStatus(status);

					List<IActivity> activites = new ArrayList<IActivity>();
					InvokeMethodActivity activity = new InvokeMethodActivity();
					List<Serializable> list = new ArrayList<Serializable>();
					list.add(t);
					activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "setOfficeKeepingVolumeRegistrationNumber", list);
					activites.add(activity);
					toStatusAction.setPreActionActivities(activites);

					Status<T> toStatus = new Status<T>();
					toStatus.setId(2);
					toStatus.setName("Открыт");
					toStatus.setProcessedData(t);
					toStatusAction.setDestinationStatus(toStatus);

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					statuses.put(status.getId(), status);

					// Открыт - Закрыт
					status = toStatus;
					fromStatusActions = new ArrayList<StatusChangeAction>();
					toStatusAction = new StatusChangeAction(process);

					toStatusAction.setId(10);
					toStatusAction.setName("Закрыть");
					toStatusAction.setInitialStatus(status);

					toStatus = new Status<T>();
					toStatus.setId(10);
					toStatus.setName("Закрыт");
					toStatus.setProcessedData(t);
					toStatusAction.setDestinationStatus(toStatus);

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					statuses.put(status.getId(), status);

					//Закрыт - изъят
					status = toStatus;
					fromStatusActions = new ArrayList<StatusChangeAction>();
					toStatusAction = new StatusChangeAction(process);
					toStatusAction.setId(100);
					toStatusAction.setName("Изъять");
					toStatusAction.setInitialStatus(status);

					activites = new ArrayList<IActivity>();
					activity = new InvokeMethodActivity();
					list = new ArrayList<Serializable>();
					list.add(t);
					activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "checkOfficeKeepingVolumePropertiesForUnfile", list);
					activites.add(activity);
					toStatusAction.setPreActionActivities(activites);

					toStatus = new Status<T>();
					toStatus.setId(110);
					toStatus.setName("Изъят");
					toStatus.setProcessedData(t);
					toStatusAction.setDestinationStatus(toStatus);

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					statuses.put(status.getId(), status);

					//Изъят из архива - Зарегистрирован
					status = toStatus;
					fromStatusActions = new ArrayList<StatusChangeAction>();
					toStatusAction = new StatusChangeAction(process);
					toStatusAction.setId(110);
					toStatusAction.setName("Вернуть");
					toStatusAction.setInitialStatus(status);

					activites = new ArrayList<IActivity>();
					activity = new InvokeMethodActivity();
					list = new ArrayList<Serializable>();
					list.add(t);
					activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "setOfficeKeepingVolumeCollectorToEmpty", list);
					activites.add(activity);
					toStatusAction.setPreActionActivities(activites);

					toStatus = statuses.get(10);
					//toStatus.setId(100);
					//toStatus.setName("В архиве");
					//toStatus.setProcessedData(t);
					toStatusAction.setDestinationStatus(toStatus);

					fromStatusActions.add(toStatusAction);
					status.setAvailableActions(fromStatusActions);

					statuses.put(status.getId(), status);

					statuses.put(toStatus.getId(), toStatus);

					Status<T> currentStatus = statuses.get(t.getStatusId());
					if (currentStatus != null) {
						process.setCurrentStatus(currentStatus);
					}

					System.out.println("new process initialization");

				}
			}
			else {
				System.out.println("Document id is null or equal blank string. Abort.");
			}
		}
		catch (Exception e) {
			System.out.println("exception in initialization process");
			process = null;
			e.printStackTrace();
		}
		return process;
	}

}