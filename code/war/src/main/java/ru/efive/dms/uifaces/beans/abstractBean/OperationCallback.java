package ru.efive.dms.uifaces.beans.abstractBean;

import ru.entity.model.mapped.IdentifiedEntity;

public interface OperationCallback<I extends IdentifiedEntity> extends BeforeOperationCallback<I>, AfterOperationCallback<I>{}
