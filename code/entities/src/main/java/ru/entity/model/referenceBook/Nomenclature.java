package ru.entity.model.referenceBook;

import ru.entity.model.mapped.DictionaryEntity;

import javax.persistence.Entity;
import javax.persistence.Table;


/**
 * Номенклатура документа
 */
@Entity
@Table(name = "rbNomenclature")
public class Nomenclature extends DictionaryEntity {
}
