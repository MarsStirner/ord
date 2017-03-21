package ru.entity.model.crm;

import org.apache.commons.lang3.StringUtils;
import ru.entity.model.mapped.IdentifiedEntity;
import ru.entity.model.referenceBook.Contragent;
import ru.util.Descriptionable;

import javax.persistence.*;

/**
 * Контакты
 * (контактные лица различных организаций (предположение))
 */
@Entity
@Table(name = "contacts")
public class Contact extends IdentifiedEntity implements Descriptionable {
    /**
     * имя
     */
    @Column(name = "firstName")
    private String firstName;

    /**
     * фамилия
     */
    @Column(name = "lastName")
    private String lastName;

    /**
     * отчество
     */
    @Column(name = "middleName")
    private String middleName;

    /**
     * Организация
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contragent_id", nullable = true)
    private Contragent contragent;

    public Contact() {
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // GETTERS & SETTERS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Contragent getContragent() {
        return contragent;
    }

    public void setContragent(Contragent contragent) {
        this.contragent = contragent;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Interface Descriptionable
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public String getDescription() {
        final StringBuilder sb = new StringBuilder();
        if(StringUtils.isNotEmpty(lastName)){
            sb.append(lastName);
        }
        if(StringUtils.isNotEmpty(firstName)){
            if(sb.length()!=0){
                sb.append(' ');
            }
           sb.append(firstName);
        }
        if(StringUtils.isNotEmpty(middleName)){
            if(sb.length()!=0){
                sb.append(' ');
            }
            sb.append(middleName);
        }
        return sb.toString();
    }

    @Override
    /**
     * Иванов Иван Иванович - > Иванов И. И.
     */
    public String getDescriptionShort() {
        final StringBuilder sb = new StringBuilder();
        if(StringUtils.isNotEmpty(lastName)){
            sb.append(lastName);
        }
        if(StringUtils.isNotEmpty(firstName)){
            if(sb.length()!=0){
                sb.append(' ');
            }
            sb.append(firstName.charAt(0)).append('.');
        }
        if(StringUtils.isNotEmpty(middleName)){
            if(sb.length()!=0){
                sb.append(' ');
            }
            sb.append(middleName.charAt(0)).append('.');
        }
        return sb.toString();
    }



    private static final long serialVersionUID = 2676252421021362937L;
}
