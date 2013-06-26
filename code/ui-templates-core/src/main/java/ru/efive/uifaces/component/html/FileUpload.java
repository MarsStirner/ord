package ru.efive.uifaces.component.html;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import javax.faces.component.FacesComponent;
import javax.faces.component.UICommand;
import javax.faces.component.behavior.ClientBehaviorHolder;
import ru.efive.uifaces.component.ComponentFamily;
import ru.efive.uifaces.filter.UploadHandler;
import ru.efive.uifaces.renderkit.html_basic.FileUploadRenderer;

/**
 * The component uploads selected by user files. Uploading can be processed with enclosing form or separately.
 *
 * @author Pavel Porubov
 */
@FacesComponent(FileUpload.COMPONENT)
public class FileUpload extends UICommand implements ClientBehaviorHolder {

    /** Component name */
    public static final String COMPONENT = "ru.efive.uifaces.FileUpload";

    /** Default constructor. */
    public FileUpload() {
        setRendererType(FileUploadRenderer.RENDERER);
    }

    /** {@inheritDoc} */
    @Override
    public String getFamily() {
        return ComponentFamily.FILE_UPLOAD;
    }

    /**
     * Component properties.
     * <br>There are following properties:
     * <ul>
     * <li>{@code multiple} - determines if user can select many files, one-by-one.
     * Default is false.</li>
     * <li>{@code maxFilesCount} - determines maximum allowed number of files than used can select.
     * Affects only if {@code multiple} property is {@code true}.
     * The value less than or equal to zero means unlimited.
     * Default is unlimited.</li>
     * <li>{@code actionBehavior} - determines the components's behavior when it is activated.</li>
     * <li>{@code uploadHandler} - stores upload handler which is invoked when upload event occurs.</li>
     * </ul>
     * @see ActionBehavior
     */
    public enum PropertyKeys {
        multiple,
        maxFilesCount,
        actionBehavior,
        uploadHandler,
        onuploading, onuploaded
    }

    /**
     * Returns the value of {@code multiple} property.
     * @return value of {@code multiple} property
     * @see PropertyKeys
     */
    public boolean isMultiple() {
        return (java.lang.Boolean) getStateHelper().eval(PropertyKeys.multiple, false);

    }

    /**
     * Sets the value of property {@code multiple}.
     * @param multiple value of {@code multiple} property
     * @see PropertyKeys
     */
    public void setMultiple(boolean multiple) {
        getStateHelper().put(PropertyKeys.multiple, multiple);
    }

    /**
     * Returns the value of {@code maxFilesCount} property.
     * @return value of {@code maxFilesCount} property
     * @see PropertyKeys
     */
    public Integer getMaxFilesCount() {
        return (Integer) getStateHelper().eval(PropertyKeys.maxFilesCount);
    }

    /**
     * Sets the value of property {@code maxFilesCount}.
     * @param maxFilesCount value of {@code maxFilesCount} property
     * @see PropertyKeys
     */
    public void setMaxFilesCount(Integer maxFilesCount) {
        if (maxFilesCount != null && maxFilesCount > 0) {
            getStateHelper().put(PropertyKeys.maxFilesCount, maxFilesCount);
        } else {
            getStateHelper().remove(PropertyKeys.maxFilesCount);
        }
    }

    /**
     * The variants of components's behavior.
     * <br>There are following values:
     * <ul>
     * <li>{@code form} - the uploader is rendered as list of file inputs.
     * Selected files is submitted with enclosing form.</li>
     * <li>{@code separate} - the uploader is rendered to use special frame to collect and submit selected files.</li>
     * </ul>
     * The default behavior is {@code form}.
     */
    public enum ActionBehavior {
        form, separate
    }

    /**
     * Returns the value of {@code actionBehavior} property.
     * @return value of {@code actionBehavior} property
     * @see PropertyKeys
     * @see ActionBehavior
     */
    public ActionBehavior getActionBehavior() {
        return (ActionBehavior) getStateHelper().eval(PropertyKeys.actionBehavior, ActionBehavior.form);
    }

    /**
     * Sets the value of property {@code actionBehavior}.
     * @param actionBehavior value of {@code actionBehavior} property
     * @see PropertyKeys
     * @see ActionBehavior
     */
    public void setActionBehavior(ActionBehavior actionBehavior) {
        getStateHelper().put(PropertyKeys.actionBehavior, actionBehavior);
    }

    /**
     * Returns the value of {@code uploadHandler} property.
     * @return value of {@code uploadHandler} property
     * @see PropertyKeys
     */
    public UploadHandler getUploadHandler() {
        return (UploadHandler) getStateHelper().eval(PropertyKeys.uploadHandler);
    }

    /**
     * Sets the value of property {@code uploadHandler}.
     * @param uploadHandler value of {@code uploadHandler} property
     * @see PropertyKeys
     */
    public void setUploadHandler(UploadHandler uploadHandler) {
        getStateHelper().put(PropertyKeys.uploadHandler, uploadHandler);
    }

    /** The name of default and only event of the component. */
    public static final String DEFAULT_EVENT_NAME = "upload";

    private static final Collection<String> EVENT_NAMES = Collections.unmodifiableCollection(
            Arrays.asList(DEFAULT_EVENT_NAME));

    /** {@inheritDoc} */
    @Override
    public Collection<String> getEventNames() {
        return EVENT_NAMES;
    }

    /** {@inheritDoc} */
    @Override
    public String getDefaultEventName() {
        return DEFAULT_EVENT_NAME;
    }
}
