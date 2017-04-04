package ru.entity.model.document;


import ru.entity.model.mapped.DeletableEntity;
import ru.entity.model.referenceBook.DocumentForm;
import ru.entity.model.referenceBook.Region;
import ru.entity.model.user.User;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "report_templates")
public class ReportTemplate extends DeletableEntity {
    /**
     * Отображаемое в списке название
     */
    private String displayName;
    /**
     * Название шаблона
     */
    private String templateName;
    /**
     * Тип отчета (0 - за дату,
     * 1 - за период,
     * 100 - за дату c с выбором пользователя,
     * 101 - за период с выбором пользователя )
     */
    private byte type;
    @Transient
    private LocalDateTime startDate;
    private String startAlias;
    private String startDescription;
    @Transient
    private LocalDateTime endDate;
    private String endAlias;
    private String endDescription;
    @Transient
    private User user;
    private String userAlias;
    private String userProperty;
    private String userDescription;
    private String userGroup;
    @Transient
    private DocumentForm documentForm;
    private String documentFormAlias;
    private String documentFormProperty;
    private String documentFormDescription;
    private String documentFormGroup;
    @Transient
    private Region region;
    private String regionAlias;
    private String regionProperty;
    private String regionDescription;

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

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    @Transient
    public String getStartDateString() {
        System.out.println(startDate == null ? "" : new java.text.SimpleDateFormat("dd.MM.yyyy").format(startDate));
        return startDate == null ? "" : new java.text.SimpleDateFormat("dd.MM.yyyy").format(startDate);
    }

    public String getStartAlias() {
        return startAlias;
    }

    public void setStartAlias(String startAlias) {
        this.startAlias = startAlias;
    }

    public String getStartDescription() {
        return startDescription;
    }

    public void setStartDescription(String startDescription) {
        this.startDescription = startDescription;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    @Transient
    public String getEndDateString() {
        System.out.println(endDate == null ? "" : new java.text.SimpleDateFormat("dd.MM.yyyy").format(endDate));
        return endDate == null ? "" : new java.text.SimpleDateFormat("dd.MM.yyyy").format(endDate);
    }

    public String getEndAlias() {
        return endAlias;
    }

    public void setEndAlias(String endAlias) {
        this.endAlias = endAlias;
    }

    public String getEndDescription() {
        return endDescription;
    }

    public void setEndDescription(String endDescription) {
        this.endDescription = endDescription;
    }

    public Map<String, Object> getProperties() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Map<String, Object> result = new HashMap<>();
        result.put("reportName", templateName);
        result.put(startAlias, startDate);

        if ((type == 1) || (type == 101)) {
            result.put(endAlias, endDate);
        }

        if ((type == 100) || (type == 101)) {
            result.put(userAlias, user.getId());
        }

        if (documentForm != null) {
            result.put(documentFormAlias, documentForm.getId());
        }

        if (region != null) {
            result.put(regionAlias, region.getId());
        }

        return result;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUserAlias() {
        return userAlias;
    }

    public void setUserAlias(String userAlias) {
        this.userAlias = userAlias;
    }

    public String getUserDescription() {
        return userDescription;
    }

    public void setUserDescription(String userDescription) {
        this.userDescription = userDescription;
    }

    public String getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(String userGroup) {
        this.userGroup = userGroup;
    }

    public DocumentForm getDocumentForm() {
        return documentForm;
    }

    public void setDocumentForm(DocumentForm documentForm) {
        this.documentForm = documentForm;
    }

    public String getDocumentFormAlias() {
        return documentFormAlias;
    }

    public void setDocumentFormAlias(String documentFormAlias) {
        this.documentFormAlias = documentFormAlias;
    }

    public String getDocumentFormDescription() {
        return documentFormDescription;
    }

    public void setDocumentFormDescription(String documentFormDescription) {
        this.documentFormDescription = documentFormDescription;
    }

    public String getDocumentFormGroup() {
        return documentFormGroup;
    }

    public void setDocumentFormGroup(String documentFormGroup) {
        this.documentFormGroup = documentFormGroup;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public String getRegionAlias() {
        return regionAlias;
    }

    public void setRegionAlias(String regionAlias) {
        this.regionAlias = regionAlias;
    }

    public String getRegionDescription() {
        return regionDescription;
    }

    public void setRegionDescription(String regionDescription) {
        this.regionDescription = regionDescription;
    }

    public String getUserProperty() {
        return userProperty;
    }

    public void setUserProperty(String userProperty) {
        this.userProperty = userProperty;
    }

    public String getDocumentFormProperty() {
        return documentFormProperty;
    }

    public void setDocumentFormProperty(String documentFormProperty) {
        this.documentFormProperty = documentFormProperty;
    }

    public String getRegionProperty() {
        return regionProperty;
    }

    public void setRegionProperty(String regionProperty) {
        this.regionProperty = regionProperty;
    }
}