package ru.efive.dms.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ApplicationHelper {

	public static final Locale getLocale() {
		return locale;
	}

	public static final String getFullTimeFormat() {
		return "dd.MM.yyyy HH:mm:ss z";
	}

	public synchronized static String getStatus(String type, int id) {
		String result = "";
		if (type.equals("PaperCopyDocument")) {			
			if (id == 1) {
				result = "Проект";
			} else if (id == 2) {
				result = "Зарегистрирован";
			} else if (id == 99) {
				result = "В архиве";		
			} else if (id == 110) {
				result = "Изъят из архива";
			} else if (id == 120) {
				result = "Оригинал уничтожен";	
			} else if (id == 130) {
				result = "Направлен в другой архив";	
			} else if (id == 200) {
				result = "Отправлен";
			} else if (id == 210) {
				result = "Доставлен";
			}
		} else if  (type.equals("IncomingDocument")) {
			if (id == 1) {
				result = "На регистрации";
			} else if (id == 2) {
				result = "Зарегистрирован";
			} else if (id == 80) {
				result = "На исполнении";
			} else if (id == 90) {
				result = "Исполнен";
			} else if (id == 100) {
				result = "В архиве";
			} else if (id == 110) {
				result = "Изъят из архива";
			} else if (id == 120) {
				result = "Оригинал уничтожен";	
			} else if (id == 130) {
				result = "Направлен в другой архив";
			}
		} else if (type.equals("InternalDocument")) {
			if (id == 1) {
				result = "Проект документа";
			} else if (id == 2) {
				result = "На рассмотрении";
			} else if (id == 3) {
				result = "Согласование";
			} else if (id == 5) {
				result = "Зарегистрирован";
			} else if (id == 80) {
				result = "На исполнении";
			} else if (id == 90) {
				result = "Исполнен";
			} else if (id == 100) {
				result = "В архиве";
			} else if (id == 110) {
				result = "Изъят из архива";
			} else if (id == 130) {
				result = "Направлен в другой архив";	
			} else if (id == 150) {
				result = "Отказ";	
			}

		} else if (type.equals("OutgoingDocument")) {
			if (id == 1) {
				result = "Проект документа";
			} else if (id == 2) {
				result = "На рассмотрении";
			} else if (id == 3) {
				result = "Согласование";
			} else if (id == 80) {
				result = "Зарегистрирован";
			} else if (id == 90) {
				result = "Исполнен";		
			} else if (id == 100) {
				result = "В архиве";			
			}
		} if (type.equals("RequestDocument")) {
			if (id == 1) {
				result = "На регистрации";
			} else if (id == 2) {
				result = "Зарегистрирован";
			} else if (id == 80) {
				result = "На исполнении";
			} else if (id == 90) {
				result = "Исполнен";			
			} else if (id == 100) {
				result = "В архиве";			
			}
		} else if (type.equals("Task")) {
			if (id == 1) {
				result = "Черновик";
			} else if (id == 2) {
				result = "На исполнении";
			} else if (id == 3) {
				result = "Исполненo";
			} else if (id == 4) {
				result = "Отказ";
			}
		} else if (type.equals("OfficeKeepingFile")) {
			if (id == 1) {
				result = "Проект дела";
			} else if (id == 2) {
				result = "Зарегистрировано";			
			}
		} else if (type.equals("OfficeKeepingVolume")) {
			if (id == 1) {
				result = "Проект тома";
			} else if (id == 2) {
				result = "Открыт";
			} else if (id == 10) {
				result = "Закрыт";	
			} else if (id == 110) {
				result = "Изъят";			
			}
		} 
		return result;
	}

	public synchronized static String getActionName(String type, int id) {
		String result = "";
		if (type.equals("PaperCopyDocument")) {
			if (id == 1) {
				result = "Зарегистрировать";
			} else if (id == 99) {
				result = "В архив";
			} else if (id == 100) {
				result = "Изъять из архива";
			} else if (id == 110) {
				result = "Вернуть в архив";
			} else if (id == 120) {
				result = "Уничтожить оригинал документа";	
			} else if (id == 130) {
				result = "Направить в другой архив";	
			} else if (id == 200) {
				result = "Документ отправлен адресату";
			} else if (id == 210) {
				result = "Подтверждение о доставке документа получено";			
			} else if (id == 0) {
				result = "Документ создан";		
			}
		}else if (type.equals("IncomingDocument")) {
			if (id == 1) {
				result = "Зарегистрировать";
			} else if (id == 2) {
				result = "Направить на исполнение";
			} else if (id == 80) {
				result = "Исполнен";
			} else if (id == 90) {
				result = "В архив";
			} else if (id == 100) {
				result = "Изъять из архива";
			} else if (id == 110) {
				result = "Вернуть в архив";
			} else if (id == 125) {
				result = "Уничтожить оригинал документа";	
			} else if (id == 135) {
				result = "Направить в другой архив";	
			} else if (id == 0) {
				result = "Документ создан";
			} else if (id == 1001) {
				result = "Изменить срок исполнения";
			} else if (id == 1002) {
				result = "Изменить уровень допуска";
			}
		} else if (type.equals("InternalDocument")) {
			if (id == 1) {
				result = "Направить на рассмотрение";
			} else if (id == 3) {
				result = "Направить на согласование";
			} else if (id == 5) {
				result = "Зарегистрировать";
			} else if (id == 6) {
				result = "Зарегистрировать";
			} else if (id == 55) {
				result = "Зарегистрировать";
			} else if (id == 80) {
				result = "Направить на исполнение";
			} else if (id == 90) {
				result = "В архив";
			} else if (id == 100) {
				result = "Изъять из архива";
			} else if (id == 110) {
				result = "Вернуть в архив";
			} else if (id == 135) {
				result = "Направить в другой архив";	
			} else if (id == 150) {
				result = "Отказаться";	
			} else if (id == 0) {
				result = "Документ создан";
			} else if (id == -10000) {
				result = "Вернуть на редактирование";	
			} else if (id == 1002) {
				result = "Изменить уровень допуска";
			}
		} else if (type.equals("OutgoingDocument")) {
			if (id == 2) {
				result = "Направить на рассмотрение";			
			} else if (id == 3) {
				result = "Направить на согласование";
			} else if (id == 80) {
				result = "Зарегистрировать";
			} else if (id == 81) {
				result = "Зарегистрировать";
			} else if (id == 82) {
				result = "Зарегистрировать";
			} else if (id == 90) {
				result = "Исполнен";			
			} else if (id == 99) {
				result = "В архив";
			} else if (id == 0) {
				result = "Документ создан";
			} else if (id == -10000) {
				result = "Вернуть на редактирование";	
			} else if (id == 1002) {
				result = "Изменить уровень допуска";
			}
		} if (type.equals("RequestDocument")) {
			if (id == 1) {
				result = "Зарегистрировать";
			} else if (id == 2) {
				result = "Направить на исполнение";
			} else if (id == 80) {
				result = "Исполнен";
			} else if (id == 90) {
				result = "В архив";			
			} else if (id == 100) {
				result = "Изъять из архива";
			} else if (id == 110) {
				result = "Вернуть в архив";
			} else if (id == 135) {
				result = "Направить в другой архив";	
			} else if (id == 0) {
				result = "Документ создан";
			} 
		} else if (type.equals("Task")) {
			if (id == 1) {
				result = "Направить на исполнение";
			} else if (id == 2) {
				result = "Исполненo";
			} else if (id == 25) {
				result = "Отказать";
			} else if (id == 0) {
				result = "Документ создан";
			}
		} else if (type.equals("OfficeKeepingFile")) {
			if (id == 1) {
				result = "Зарегистрировать";
			} else if (id == 2) {
				result = "";
			} 
		} else if (type.equals("OfficeKeepingVolume")) {
			if (id == 1) {
				result = "Зарегистрировать";
			} else if (id == 10) {
				result = "Закрыть";	
			} else if (id == 100) {
				result = "Изъять";
			} else if (id == 110) {
				result = "Вернуть";
			}
		}
		return result;
	}

	public synchronized static List<Integer> getStatusIdListByStrKey(String type, String strKey) {
		List<Integer> result=new ArrayList<Integer>();
		strKey=strKey.toLowerCase();
		if(type.equals("IncomingDocument")) {
			if("на регистрации".indexOf(strKey)>=0){result.add(1);}
			if("зарегистрирован".indexOf(strKey)>=0){result.add(2);}		
			if("на исполнении".indexOf(strKey)>=0){result.add(80);}
			if("исполнен".indexOf(strKey)>=0){result.add(90);}
			if("в архиве".indexOf(strKey)>=0){result.add(100);}
			if("изъят из архива".indexOf(strKey)>=0){result.add(110);}
			if("оригинал уничтожен".indexOf(strKey)>=0){result.add(120);}
			if("направлен в другой архив".indexOf(strKey)>=0){result.add(130);}
		}else if (type.equals("InternalDocument")) {
			if("проект документа".indexOf(strKey)>=0){result.add(1);}
			if("на рассмотрении".indexOf(strKey)>=0){result.add(2);}		
			if("согласование".indexOf(strKey)>=0){result.add(3);}
			if("зарегистрирован".indexOf(strKey)>=0){System.out.println("зарег");result.add(5);}
			if("на исполнении".indexOf(strKey)>=0){result.add(80);}
			if("исполнен".indexOf(strKey)>=0){result.add(90);}
			if("в архиве".indexOf(strKey)>=0){result.add(100);}
			if("изъят из архива".indexOf(strKey)>=0){result.add(110);}
			if("отказ".indexOf(strKey)>=0){result.add(150);}
			if("направлен в другой архив".indexOf(strKey)>=0){result.add(130);}
		}else if (type.equals("OutgoingDocument")) {		
			if("проект документа".indexOf(strKey)>=0){result.add(1);}
			if("на рассмотрении".indexOf(strKey)>=0){result.add(2);}		
			if("согласование".indexOf(strKey)>=0){result.add(3);}			
			if("зарегистрирован".indexOf(strKey)>=0){result.add(80);}
			if("исполнен".indexOf(strKey)>=0){result.add(90);}
			if("в архиве".indexOf(strKey)>=0){result.add(100);}			
		}else if (type.equals("RequestDocument")) {	
			if("на регистрации".indexOf(strKey)>=0){result.add(1);}
			if("зарегистрирован".indexOf(strKey)>=0){result.add(2);}					
			if("на исполнении".indexOf(strKey)>=0){result.add(80);}
			if("исполнен".indexOf(strKey)>=0){result.add(90);}
			if("в архиве".indexOf(strKey)>=0){result.add(100);}			
		} 
		return result;
	}

	private static Locale locale = new Locale("ru", "RU");

	public static String STORE_NAME = "E5 DMS";
	public static String NAMESPACE = "http://www.efive.ru/model/dictionary/1.0";
	public static String NAMESPACE_PREFIX = "e5-dms";
	public static String TYPE_FILE = "File";

	public static String USER_DAO = "userDao";
	public static String GROUP_DAO = "groupDao";
	public static String ROLE_DAO = "roleDao";
	public static String CONTRAGENT_DAO = "contragentDao";
	public static String GROUP_TYPE_DAO = "groupTypeDao";

	public static String DOCUMENT_FORM_DAO = "documentFormDao";
	public static String ACCESS_LEVEL_DAO = "accessLevelDao";
	public static String USER_ACCESS_LEVEL_DAO = "userAccessLevelDao";
	public static String PAPER_COPY_DOCUMENT_FORM_DAO = "paperCopyDao";
	public static String INCOMING_DOCUMENT_FORM_DAO = "incomingDao";
	public static String NUMERATOR_DAO = "numeratorDao";	
	public static String OUTGOING_DOCUMENT_FORM_DAO = "outgoingDao";
	public static String REQUEST_DOCUMENT_FORM_DAO = "requestDao";
	public static String INTERNAL_DOCUMENT_FORM_DAO = "internalDao";
	public static String TASK_DAO = "taskDao";
	public static String NOMENCLATURE_DAO = "nomenclatureDao";
	public static String DELIVERY_TYPE_DAO = "deliveryTypeDao";
	public static String SENDER_TYPE_DAO = "senderTypeDao";
	public static String REGION_DAO = "regionDao";
	public static String REPORT_DAO = "reportDao";
	public static String OFFICE_KEEPING_RECORD_DAO = "officeKeepingRecordDao";
	public static String OFFICE_KEEPING_FILE_DAO = "officeKeepingFileDao";
	public static String OFFICE_KEEPING_VOLUME_DAO = "officeKeepingVolumeDao";

}