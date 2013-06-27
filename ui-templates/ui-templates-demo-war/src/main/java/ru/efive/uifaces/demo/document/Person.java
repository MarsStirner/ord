package ru.efive.uifaces.demo.document;

import java.io.Serializable;

/**
 *
 * @author Denis Kotegov
 */
public class Person implements Serializable {

    private static final long serialVersionUID = 1L;

    private String shortName;

    private String name;

    private String title;

    private String department;

    private String phone;

    public Person() {

    }

    public Person(Person copyOf) {
        shortName = copyOf.shortName;
        name = copyOf.name;
        title = copyOf.title;
        department = copyOf.department;
        phone = copyOf.phone;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

}
