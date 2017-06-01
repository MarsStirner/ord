package ru.hitsl.sql.dao.interfaces;

import ru.entity.model.document.Numerator;
import ru.entity.model.enums.DocumentType;
import ru.entity.model.referenceBook.Contragent;
import ru.entity.model.referenceBook.DocumentForm;
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
     * Увеличить значение счетчика и сохранить его в БД после чего вернуть это значение
     * @param numerator счетчик, значение которого нужно увеличить
     * @return текущее состояние счетчика ПОСЛЕ увеличения
     */
    String incrementAndGet(Numerator numerator);
}
