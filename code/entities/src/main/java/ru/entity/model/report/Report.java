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

    /**
     * Тип отчета (0 - за дату,
     * 1 - за период,
     * 100 - за дату c с выбором пользователя,
     * 101 - за период с выбором пользователя )
     * <p>
     * private byte type;
     *
     * @Transient private LocalDateTime startDate;
     * private String startAlias;
     * private String startDescription;
     * @Transient private LocalDateTime endDate;
     * private String endAlias;
     * private String endDescription;
     * @Transient private User user;
     * private String userAlias;
     * private String userProperty;
     * private String userDescription;
     * private String userGroup;
     * @Transient private DocumentForm documentForm;
     * private String documentFormAlias;
     * private String documentFormProperty;
     * private String documentFormDescription;
     * private String documentFormGroup;
     * @Transient private Region region;
     * private String regionAlias;
     * private String regionProperty;
     * private String regionDescription;
     */
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