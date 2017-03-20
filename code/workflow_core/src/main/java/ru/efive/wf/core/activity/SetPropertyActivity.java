package ru.efive.wf.core.activity;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

import ru.efive.wf.core.IActivity;
import ru.external.ProcessedData;

public class SetPropertyActivity implements IActivity {

    public SetPropertyActivity() {

    }

    @Override
    public <T extends ProcessedData> boolean initialize(T t) {
        boolean result = false;
        try {
            processedData = t;
            class_ = t.getClass();
            result = true;
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    //TODO: remove, only for test
    public void setPropertyChanges(Map<String, Object> propertyChanges) {
        this.propertyChanges = propertyChanges;
    }

    @Override
    public boolean execute() {
        boolean result = false;
        try {
            BeanInfo info = Introspector.getBeanInfo(class_);
            PropertyDescriptor[] pds = info.getPropertyDescriptors();
            Map<String, PropertyDescriptor> beanProperties = new HashMap<>();
            for (PropertyDescriptor pd : pds) {
                beanProperties.put(pd.getName(), pd);
            }
            for (String propertyName : propertyChanges.keySet()) {
                if (beanProperties.keySet().contains(propertyName)) {
                    PropertyDescriptor pd = beanProperties.get(propertyName);
                    pd.getWriteMethod().invoke(processedData, new Object[]{propertyChanges.get(propertyName)});
                } else {
                    System.out.println("Bean property not found");
                }
            }

            result = true;
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public boolean dispose() {
        return true;
    }

    public String getResult() {
        return resultMessage;
    }


    Class<? extends ProcessedData> class_;
    private ProcessedData processedData;

    private Map<String, Object> propertyChanges;

    private String resultMessage;
}