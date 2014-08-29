package ru.efive.sql.entity.user;

import ru.efive.sql.entity.DictionaryEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Author: Upatov Egor <br>
 * Date: 19.08.2014, 16:24 <br>
 * Company: Korus Consulting IT <br>
 * Description: Справочник подразделений <br>
 */
@Entity
@Table(name = "dms_system_user_department")
public class Department extends DictionaryEntity {
}
