package ru.entity.model.document.viewFacts;

import ru.entity.model.mapped.DocumentEntity;
import ru.entity.model.user.User;

import java.io.Serializable;

/**
 * Author: Upatov Egor <br>
 * Date: 02.02.2015, 15:50 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class DocumentViewFactPrimaryKey<T extends DocumentEntity> implements Serializable {
    private T document;
    private User user;

}