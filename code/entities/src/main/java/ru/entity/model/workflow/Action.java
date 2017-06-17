package ru.entity.model.workflow;

import ru.entity.model.mapped.MappedEnum;
import ru.entity.model.mapped.ReferenceBookEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Роль пользователя системы
 */
@Entity
@Table(name = "wf_action")
public class Action extends ReferenceBookEntity {
    @MappedEnum("CANCEL")
    public static Action CANCEL;
    @MappedEnum("CREATED")
    public static Action CREATED;
    @MappedEnum("CUSTOM_REGISTER")
    public static Action CUSTOM_REGISTER;
    @MappedEnum("EXECUTE")
    public static Action EXECUTE;
    @MappedEnum("FROM_ARCHIVE")
    public static Action FROM_ARCHIVE;
    @MappedEnum("REGISTER")
    public static Action REGISTER;
    @MappedEnum("RETURN_TO_ARCHIVE")
    public static Action RETURN_TO_ARCHIVE;
    @MappedEnum("TO_AGREEMENT")
    public static Action TO_AGREEMENT;
    @MappedEnum("TO_ARCHIVE")
    public static Action TO_ARCHIVE;
    @MappedEnum("TO_CONSIDERATION")
    public static Action TO_CONSIDERATION;
    @MappedEnum("TO_EXECUTION")
    public static Action TO_EXECUTION;
    @MappedEnum("TO_OTHER_ARCHIVE")
    public static Action TO_OTHER_ARCHIVE;

}