package ru.efive.dms.uifaces.beans.abstractBean;

import java.io.Serializable;

/**
 * Author: Upatov Egor <br>
 * Date: 22.06.2015, 14:22 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public enum State implements Serializable {
    VIEW,
    EDIT,
    CREATE,
    ERROR
}
