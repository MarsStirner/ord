package ru.efive.uifaces.beans;

import java.io.Serializable;
import java.util.Iterator;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import ru.efive.uifaces.dao.TestDocumentDao;
import ru.efive.uifaces.data.TestDocument;

/**
 * <code>TestDocumentBean</code> class is a test document implementation.
 * 
 * @author Ramil_Habirov
 */
@ManagedBean
@RequestScoped
public class TestDocumentBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id request parameter name.
     */
    private static final String ID_REQUEST_PARAMETER_NAME = "id";

    /**
     * Current document.
     */
    private TestDocument currentDocument;

    @ManagedProperty(value = "#{modalStateBean}")
    private ModalStateBean modalStateBean;

    /**
     * Initializing current document.
     */
    @PostConstruct
    public void initCurrentDocument() {
        currentDocument = null;
        Integer id = getRequestedId();
        if (id != null) {
            try {
                currentDocument = TestDocumentDao.get(id);
            } catch (Exception e) {
                addErrorMessage(e.getMessage());
            }
        }
        if (currentDocument == null) {
            currentDocument = new TestDocument();
        }
    }

    /**
     * Returns id request parameter name.
     * 
     * @return id request parameter name.
     */
    public String getIdRequestParameterName() {
        return ID_REQUEST_PARAMETER_NAME;
    }

    /**
     * Returns current document.
     * 
     * @return current document.
     */
    public TestDocument getCurrentDocument() {
        return currentDocument;
    }

    public void setModalStateBean(ModalStateBean modalStateBean) {
        this.modalStateBean = modalStateBean;
    }

    public ModalStateBean getModalStateBean() {
        return modalStateBean;
    }

    /**
     * Saves current document.
     */
    public String saveCurrentDocument() {
        System.out.println("saveCurrentDocument...");
        try {
            if (currentDocument.getId() != null) {
                TestDocumentDao.update(currentDocument);
            } else {
                TestDocumentDao.save(currentDocument);
            }
            clearMessages();
        } catch (Exception e) {
            addErrorMessage(e.getMessage());
        }
        getModalStateBean().setRendered(false);
        return "document";
    }

    /**
     * Adds error message.
     * 
     * @param errorMessage
     *            error message.
     */
    private void addErrorMessage(String errorMessage) {
        FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, errorMessage,
                        null));
    }

    /**
     * Removes all messages.
     */
    private void clearMessages() {
        Iterator<FacesMessage> messageIterator = FacesContext
                .getCurrentInstance().getMessages();
        while (messageIterator.hasNext()) {
            FacesMessage message = messageIterator.next();
            message.setSummary(null);
            message.setDetail(null);
        }
    }

    /**
     * Returns requested id.
     * 
     * @return requested id.
     */
    private Integer getRequestedId() {
        HttpServletRequest httpServletRequest = (HttpServletRequest) FacesContext
                .getCurrentInstance().getExternalContext().getRequest();
        String idParameter = httpServletRequest
                .getParameter(getIdRequestParameterName());
        Integer id = null;
        if (idParameter != null) {
            try {
                id = Integer.parseInt(idParameter);
            } catch (NumberFormatException e) {
                // Do nothing.
            }
        }
        return id;
    }
}
