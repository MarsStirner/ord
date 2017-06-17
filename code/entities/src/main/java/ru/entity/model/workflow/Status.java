package ru.entity.model.workflow;

import ru.entity.model.mapped.MappedEnum;
import ru.entity.model.mapped.ReferenceBookEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Роль пользователя системы
 */
@Entity
@Table(name = "rb_status")
public class Status extends ReferenceBookEntity {

    @MappedEnum("CANCELED")
    public final static Status CANCELED = null;
    @MappedEnum("DRAFT")
    public final static Status DRAFT = null;
    @MappedEnum("EXECUTED")
    public final static Status EXECUTED = null;
    @MappedEnum("IN_ARCHIVE")
    public final static Status IN_ARCHIVE = null;
    @MappedEnum("ON_AGREEMENT")
    public final static Status ON_AGREEMENT = null;
    @MappedEnum("ON_CONSIDERATION")
    public final static Status ON_CONSIDERATION = null;
    @MappedEnum("ON_EXECUTION")
    public final static Status ON_EXECUTION = null;
    @MappedEnum("OUT_ARCHIVE")
    public final static Status OUT_ARCHIVE = null;
    @MappedEnum("REGISTERED")
    public final static Status REGISTERED = null;

}