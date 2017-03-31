package ru.entity.model.referenceBook;

import ru.entity.model.mapped.ReferenceBookEntity;

import javax.persistence.Entity;
import javax.persistence.Table;


/**
 * Номенклатура документа
 */
@Entity
@Table(name = "rbNomenclature")
public class Nomenclature extends ReferenceBookEntity {
}
