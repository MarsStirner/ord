package ru.efive.wf.core.activity;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

import ru.efive.wf.core.LocalActivity;
import ru.efive.wf.core.activity.enums.EditablePropertyScope;
import ru.efive.wf.core.data.EditableProperty;
import ru.efive.wf.core.data.LocalBackingBean;
import ru.efive.wf.core.util.EngineHelper;

public class ParametrizedPropertyLocalActivity extends ParametrizedFieldActivity implements LocalActivity {

    @Override
    public LocalBackingBean getDocument() {
        return document;
    }

    public void setDocument(LocalBackingBean document) {
        this.document = document;
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
            for (EditableProperty property : getDocument().getProperties()) {
                if (property.getName() == null || property.getName().equals("")) {
                    setResult(EngineHelper.EXCEPTION_WRONG_NAME + ": " + property.getName());
                    return false;
                } else {
                    if (property.getScope().equals(EditablePropertyScope.LOCAL)) {
                        getParentAction().addProperty(property);
                        result = true;
                    } else if (property.getScope().equals(EditablePropertyScope.GLOBAL)) {
                        if (beanProperties.keySet().contains(property.getName())) {
                            PropertyDescriptor pd = beanProperties.get(property.getName());
                            pd.getWriteMethod().invoke(getProcessedData(), new Object[]{property.getValue()});
                            getParentAction().addProperty(property);
                            result = true;
                        } else {
                            setResult(EngineHelper.PROPERTY_NOT_FOUND);
                            System.out.println(EngineHelper.PROPERTY_NOT_FOUND + ": " + property.getName());
                            return false;
                        }
                    } else {
                        setResult(EngineHelper.EXCEPTION_WRONG_SCOPE);
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            result = false;
            setResult(EngineHelper.DEFAULT_ERROR_MESSAGE);
            e.printStackTrace();
        }
        return result;
    }


    private LocalBackingBean document;
}