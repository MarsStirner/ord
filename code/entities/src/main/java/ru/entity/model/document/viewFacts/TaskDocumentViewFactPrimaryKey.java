package ru.entity.model.document.viewFacts;

import ru.entity.model.document.Task;
import ru.entity.model.user.User;

import java.io.Serializable;

/**
 * Author: Upatov Egor <br>
 * Date: 04.02.2015, 19:21 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class TaskDocumentViewFactPrimaryKey implements Serializable{
    private Task document;
    private User user;

    public TaskDocumentViewFactPrimaryKey() {
    }
}
