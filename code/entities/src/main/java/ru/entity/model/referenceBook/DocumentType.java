package ru.entity.model.referenceBook;

import ru.entity.model.mapped.MappedEnum;
import ru.entity.model.mapped.ReferenceBookEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Author: Upatov Egor <br>
 * Date: 02.06.2015, 15:19 <br>
 * Company: Korus Consulting IT <br>
 * Description: Справочник типов документов<br>
 */
@Entity
@Table(name = "rbDocumentType")
public class DocumentType extends ReferenceBookEntity {
    @MappedEnum("INCOMING")
    public static DocumentType INCOMING;
    @MappedEnum("OUTGOING")
    public static DocumentType OUTGOING;
    @MappedEnum("INTERNAL")
    public static DocumentType INTERNAL;
    @MappedEnum("TASK")
    public static DocumentType TASK;
    @MappedEnum("REQUEST")
    public static DocumentType REQUEST;
}
