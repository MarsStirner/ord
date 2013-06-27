package ru.efive.uifaces.demo.document;

import java.io.Serializable;

/**
 *
 * @author Denis Kotegov
 */
public class Organization implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    private String address;

    private String phone;

    public Organization() {
    }

    public Organization(Organization copyOf) {
        name = copyOf.name;
        address = copyOf.address;
        phone = copyOf.phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

}
