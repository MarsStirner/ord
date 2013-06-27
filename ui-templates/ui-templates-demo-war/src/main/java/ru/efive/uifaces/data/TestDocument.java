package ru.efive.uifaces.data;

import java.util.Date;

/**
 * Test implementation of document with integer id.
 * 
 * @author Ramil_Habirov
 */
public class TestDocument implements Document<Integer> {

    /**
     * id of document.
     */
    private Integer id;

    /**
     * Name of document.
     */
    private String name;

    /**
     * Description of document.
     */
    private String description;

    /**
     * Date of document;
     */
    private Date date;

    /**
     * Creates instance of TestDocument class.
     */
    public TestDocument() {
        this(null, null, null);
    }

    /**
     * Creates instance of TestDocument class.
     * 
     * @param id
     *            id of document.
     * @param name
     *            name of document.
     * @param description
     *            description of document.
     */
    public TestDocument(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    /**
     * Sets id of document.
     * 
     * @param id
     *            id of document.
     */
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public Integer getId() {
        return id;
    }

    /**
     * Sets name of document.
     * 
     * @param name
     *            name of document.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns name of document.
     * 
     * @return name of document.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets description of document.
     * 
     * @param description
     *            description of document.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns description of document.
     * 
     * @return description of document.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets date of document.
     * 
     * @param date
     *            date of document.
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Returns date of document.
     * 
     * @return date of document.
     */
    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.getClass().getName());
        stringBuilder.append("[id = ").append(getId()).append(", ");
        stringBuilder.append("name = ").append(getName()).append(", ");
        stringBuilder.append("description = ").append(getDescription())
                .append(", ");
        stringBuilder.append("date = ").append(getDate()).append("]");
        return stringBuilder.toString();
    }
}
