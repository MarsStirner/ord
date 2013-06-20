package ru.efive.wf.core.activity;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;

import ru.efive.wf.core.IActivity;
import ru.efive.wf.core.ProcessedData;

public class InvokeMethodActivity implements IActivity {
	
	public InvokeMethodActivity() {
		
	}
	
	@Override
	public <T extends ProcessedData> boolean initialize(T t) {
		boolean result = false;
		try {
			processedData = t;
			class_ = t.getClass();
			result = true;
		}
		catch (Exception e) {
			result = false;
			e.printStackTrace();
		}
		return result;
	}
	
	//TODO: remove, only for test
	public void setInvokeInformation(String className, String methodName, List<Serializable> parameters) {
		this.className = className;
		this.methodName = methodName;
		this.parameters = parameters;
	}
	
	@Override
	public boolean execute() {
		boolean result = false;
		try {
			Class<?> c = Class.forName(className);
			Object t = c.newInstance();
			
			Class [] list = new Class[parameters.size()];
			int i = 0;
			for (Object parameter: parameters) {
				list [i] = parameter.getClass();
				i++;
			}
			Method method = c.getMethod(methodName, list);
			if (method != null) {
				method.setAccessible(true);
				Object in_result = method.invoke(t, parameters.toArray());											
				if (in_result.getClass().getName().equals("java.lang.Boolean")) {
					result = ((Boolean) in_result);
				} else {
					result = true;
				}
			}
			else {
				System.out.println("Unable to get method");
			}
		}
		catch (Exception e) {
			result = false;
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public boolean dispose() {
		return true;
	}
	
	@Override
	public String getResult() {
		return resultMessage;
	}
	
	
	Class<? extends ProcessedData> class_;
	private ProcessedData processedData;
	
	private String className;
	private String methodName;
	private List<Serializable> parameters = new ArrayList<Serializable>();
	
	
	private String resultMessage;
}