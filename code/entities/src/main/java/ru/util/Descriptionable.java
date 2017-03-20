package ru.util;

/**
 * Author: Upatov Egor <br>
 * Date: 09.02.2015, 14:26 <br>
 * Company: Korus Consulting IT <br>
 * Description: Итерфейс определяющий два метода getDescription и getDescriptionShort<br>
 */
public interface Descriptionable {

    /**
     * Получить полное описание сущности
     * @return  строка с полным описанием сущности
     */
    String getDescription();

    /**
     * Получить краткое описание сущности
     * @return  строка с кратким описанием сущности
     */
    String getDescriptionShort();
}
