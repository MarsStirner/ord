package ru.entity.model.workflow;

import ru.entity.model.mapped.ReferenceBookEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Роль пользователя системы
 */
@Entity
@Table(name = "wf_action")
public class Action extends ReferenceBookEntity{
}