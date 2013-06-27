package ru.efive.uifaces.demo.document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Denis Kotegov
 */
public class Contract implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private Date createDate;

    private Person author;

    private Person curator;

    private List<Organization> contractors;

    private String registrationNumber;

    private String contractKind;

    private Date signDate;

    private Date validThruDate;

    private String activityKind;

    private String businessPlanItem;

    private String bddsItem;

    private String projectName;

    private String currency;

    private Double sum;

    private Double sumWithVat;

    private String paymentTerms;

    public Contract() {
    }

    public Contract(Contract copyOf) {
        activityKind = copyOf.activityKind;
        author = new Person(copyOf.author);
        bddsItem = copyOf.bddsItem;
        businessPlanItem = copyOf.businessPlanItem;
        contractKind = copyOf.contractKind;
        contractors = new ArrayList<Organization>(copyOf.contractors.size());
        for (Organization organization : copyOf.contractors) {
            contractors.add(new Organization(organization));
        }
        createDate = copyOf.createDate;
        curator = new Person(copyOf.curator);
        currency = copyOf.currency;
        id = copyOf.id;
        paymentTerms = copyOf.paymentTerms;
        projectName = copyOf.projectName;
        registrationNumber = copyOf.registrationNumber;
        signDate = copyOf.signDate;
        sum = copyOf.sum;
        sumWithVat = copyOf.sumWithVat;
        validThruDate = copyOf.validThruDate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getActivityKind() {
        return activityKind;
    }

    public void setActivityKind(String activityKind) {
        this.activityKind = activityKind;
    }

    public Person getAuthor() {
        return author;
    }

    public void setAuthor(Person author) {
        this.author = author;
    }

    public String getBddsItem() {
        return bddsItem;
    }

    public void setBddsItem(String bddsItem) {
        this.bddsItem = bddsItem;
    }

    public String getBusinessPlanItem() {
        return businessPlanItem;
    }

    public void setBusinessPlanItem(String businessPlanItem) {
        this.businessPlanItem = businessPlanItem;
    }

    public String getContractKind() {
        return contractKind;
    }

    public void setContractKind(String contractKind) {
        this.contractKind = contractKind;
    }

    public List<Organization> getContractors() {
        return contractors;
    }

    public void setContractors(List<Organization> contractors) {
        this.contractors = contractors;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Person getCurator() {
        return curator;
    }

    public void setCurator(Person curator) {
        this.curator = curator;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(String paymentTerms) {
        this.paymentTerms = paymentTerms;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public Date getSignDate() {
        return signDate;
    }

    public void setSignDate(Date signDate) {
        this.signDate = signDate;
    }

    public Double getSum() {
        return sum;
    }

    public void setSum(Double sum) {
        this.sum = sum;
    }

    public Double getSumWithVat() {
        return sumWithVat;
    }

    public void setSumWithVat(Double sumWithVat) {
        this.sumWithVat = sumWithVat;
    }

    public Date getValidThruDate() {
        return validThruDate;
    }

    public void setValidThruDate(Date validThruDate) {
        this.validThruDate = validThruDate;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Contract other = (Contract) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }


}
