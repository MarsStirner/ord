package ru.entity.model.document;

import org.apache.commons.beanutils.PropertyUtils;
import ru.entity.model.mapped.DeletableEntity;
import ru.entity.model.referenceBook.DocumentForm;
import ru.entity.model.referenceBook.Region;
import ru.entity.model.user.User;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "report_templates")
public class ReportTemplate extends DeletableEntity {

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte getType() {
        return type;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    @Transient
    public String getStartDateString() {
        System.out.println(startDate == null ? "" : new java.text.SimpleDateFormat("dd.MM.yyyy").format(startDate));
        return startDate == null ? "" : new java.text.SimpleDateFormat("dd.MM.yyyy").format(startDate);
    }

    public void setStartAlias(String startAlias) {
        this.startAlias = startAlias;
    }

    public String getStartAlias() {
        return startAlias;
    }

    public void setStartDescription(String startDescription) {
        this.startDescription = startDescription;
    }

    public String getStartDescription() {
        return startDescription;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    @Transient
    public String getEndDateString() {
        System.out.println(endDate == null ? "" : new java.text.SimpleDateFormat("dd.MM.yyyy").format(endDate));
        return endDate == null ? "" : new java.text.SimpleDateFormat("dd.MM.yyyy").format(endDate);
    }

    public void setEndAlias(String endAlias) {
        this.endAlias = endAlias;
    }

    public String getEndAlias() {
        return endAlias;
    }

    public void setEndDescription(String endDescription) {
        this.endDescription = endDescription;
    }

    public String getEndDescription() {
        return endDescription;
    }

    public Map<String, Object> getProperties() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("reportName", templateName);
        result.put(startAlias, startDate);

        if ((type == 1) || (type == 101)) {
            result.put(endAlias, endDate);
        }

        if ((type == 100) || (type == 101)) {
            result.put(userAlias, PropertyUtils.getProperty(user, userProperty).toString());
        }

        if (documentForm != null) {
            result.put(documentFormAlias, PropertyUtils.getProperty(documentForm, documentFormProperty).toString());
        }

        if (region != null) {
            result.put(regionAlias, PropertyUtils.getProperty(region, regionProperty).toString());
        }

        return result;
    }


    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }


    public void setUserAlias(String userAlias) {
        this.userAlias = userAlias;
    }

    public String getUserAlias() {
        return userAlias;
    }


    public void setUserDescription(String userDescription) {
        this.userDescription = userDescription;
    }

    public String getUserDescription() {
        return userDescription;
    }


    public void setUserGroup(String userGroup) {
        this.userGroup = userGroup;
    }

    public String getUserGroup() {
        return userGroup;
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


    public void setRegion(Region region) {
        this.region = region;
    }

    public Region getRegion() {
        return region;
    }


    public void setRegionAlias(String regionAlias) {
        this.regionAlias = regionAlias;
    }

    public String getRegionAlias() {
        return regionAlias;
    }


    public void setRegionDescription(String regionDescription) {
        this.regionDescription = regionDescription;
    }

    public String getRegionDescription() {
        return regionDescription;
    }


    public void setUserProperty(String userProperty) {
        this.userProperty = userProperty;
    }

    public String getUserProperty() {
        return userProperty;
    }


    public void setDocumentFormProperty(String documentFormProperty) {
        this.documentFormProperty = documentFormProperty;
    }

    public String getDocumentFormProperty() {
        return documentFormProperty;
    }


    public void setRegionProperty(String regionProperty) {
        this.regionProperty = regionProperty;
    }

    public String getRegionProperty() {
        return regionProperty;
    }


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
    private Date startDate;

    private String startAlias;

    private String startDescription;

    @Transient
    private Date endDate;

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


    private static final long serialVersionUID = -5877947826192366106L;
}