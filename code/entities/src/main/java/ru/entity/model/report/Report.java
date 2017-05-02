package ru.entity.model.report;


import ru.entity.model.mapped.DeletableEntity;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "report")
public class Report extends DeletableEntity {

    /**
     * Отображаемое в списке название
     */
    @Column(name = "displayName", nullable = false)
    private String displayName;
    /**
     * Название шаблона
     */
    @Column(name = "templateName", nullable = false)
    private String templateName;

    /**
     * Параметры шаблона
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "report")
    private Set<ReportParameter> parameters;

    public Report() {
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public Set<ReportParameter> getParameters() {
        return parameters;
    }

    public void setParameters(Set<ReportParameter> parameters) {
        this.parameters = parameters;
    }
}