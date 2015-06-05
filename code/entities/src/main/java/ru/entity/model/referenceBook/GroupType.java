package ru.entity.model.referenceBook;

import ru.entity.model.mapped.DictionaryEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "rbGroupType")
public class GroupType extends DictionaryEntity {
    public static final String RB_CODE_ROLE = "ROLE";
    public static final String RB_CODE_SERVICE = "SERVICE";
    public static final String RB_CODE_WORKGROUP = "WORKGROUP";
    public static final String RB_CODE_DIVISION = "DIVISION";
}