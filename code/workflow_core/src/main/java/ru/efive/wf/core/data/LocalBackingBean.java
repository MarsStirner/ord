package ru.efive.wf.core.data;

import ru.efive.wf.core.ActionResult;

import java.util.List;

public interface LocalBackingBean {

    String getForm();

    List<EditableProperty> getProperties();

    ActionResult initialize();
}