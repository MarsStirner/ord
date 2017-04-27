package ru.entity.model.report;

import ru.entity.model.mapped.IdentifiedEntity;
import ru.util.JsonObjectConverter;

import javax.json.JsonObject;
import javax.persistence.*;

@Entity
@Table(name = "report_parameter")
public class ReportParameter extends IdentifiedEntity{

    /**
     * Ссылка на использующий отчет
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="report_id", nullable = false)
    private Report report;

    /**
     * Имя параметра в отчете
     */
    @Column(name="name", nullable = false)
    private String name;

    /**
     * Признак обязательности
     */
    @Column(name="required", nullable = false)
    private boolean required;

    /**
     * Название параметра
     */
    @Column(name="label")
    private String label;

    /**
     * Поле для сортировки
     */
    @Column(name="sort", nullable = false)
    private int sort;

    /**
     * Шаблон отрисовки
     */
    @Column(name="template")
    private String template;

    /**
     * Свойства для отрисовки
     */
    @Column(name="settings")
    @Convert(converter = JsonObjectConverter.class)
    private JsonObject settings;

    public ReportParameter() {
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public JsonObject getSettings() {
        return settings;
    }

    public void setSettings(JsonObject settings) {
        this.settings = settings;
    }

    @Override
    public String toString() {
        return "ReportParameter{" +
                "report=" + report +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", required=" + required +
                ", label='" + label + '\'' +
                ", sort=" + sort +
                ", template='" + template + '\'' +
                ", settings=" + settings +
                '}';
    }
}
