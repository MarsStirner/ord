package ru.hitsl.sql.dao.interfaces;

import ru.entity.model.numerator.Numerator;

import ru.entity.model.mapped.DocumentEntity;
import ru.entity.model.numerator.NumeratorUsage;
import ru.entity.model.referenceBook.Contragent;
import ru.entity.model.referenceBook.DocumentForm;
import ru.entity.model.referenceBook.DocumentType;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.interfaces.mapped.CommonDao;

public interface NumeratorDao  extends CommonDao<Numerator> {
    /**
     * Найти наиболее приоритетный из нумераторов удовлетворяющих условиям
     * @param type Тип документа
     * @param form Вид документа
     * @param controller Руководитель
     * @param contragent Контрагент
     * @return наиболее приоритетный из подходящих нумераторов
     */
    Numerator findBestNumerator(DocumentType type, DocumentForm form, User controller, Contragent contragent);

    /**
     * Получить использование нумераторов этим документом
     * @param documentId уникальный идентифкатор документ-а
     * @return NumeratorUsage этого документа / null если отсутсвтует
     */
    NumeratorUsage getUsage(String documentId);

    NumeratorUsage createUsage(DocumentEntity doc, Numerator numerator);

    void deleteUsage(NumeratorUsage usage);
}
