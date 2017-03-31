package ru.entity.model.referenceBook;

import ru.entity.model.mapped.ReferenceBookEntity;

import javax.persistence.Entity;
import javax.persistence.Table;


@Entity
@Table(name = "rbSenderType")
public class SenderType extends ReferenceBookEntity {
    public static final String PHYSICAL_ENTITY = "PHYSICAL_ENTITY";
    public static final String JURIDICAL_ENTITY = "JURIDICAL_ENTITY";
}