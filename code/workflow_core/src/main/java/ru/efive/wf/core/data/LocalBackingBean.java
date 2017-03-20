package ru.efive.wf.core.data;

import java.util.List;

import ru.efive.wf.core.ActionResult;

public interface LocalBackingBean {

    String getForm();

    List<EditableProperty> getProperties();

    ActionResult initialize();
}