package ru.efive.uifaces.renderkit.html_basic;

import ru.efive.uifaces.renderkit.html_basic.base.HtmlAttributeValue;
import javax.el.ValueExpression;
import javax.faces.component.behavior.ClientBehavior;
import javax.faces.component.behavior.ClientBehaviorContext;
import javax.faces.event.ActionEvent;
import ru.efive.uifaces.filter.UploadInfo;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlAttribute;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlElement;
import ru.efive.uifaces.component.html.FileUpload;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.UIComponent;
import javax.faces.component.behavior.AjaxBehavior;
import javax.faces.context.FacesContext;
import javax.faces.render.FacesRenderer;
import ru.efive.uifaces.component.ComponentFamily;
import ru.efive.uifaces.filter.UploadHandler;
import ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlBasicRenderer;

import static java.lang.String.format;
import static ru.efive.uifaces.util.JSFUtils.getFacesContext;
import static javax.faces.context.PartialViewContext.PARTIAL_EXECUTE_PARAM_NAME;
import static javax.faces.context.PartialViewContext.ALL_PARTIAL_PHASE_CLIENT_IDS;
import static ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter.writeStyle;
import static ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter.writeStyleClass;
import static ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter.findEnclosingForm;

/**
 * The renderer for output {@link FileUpload}.
 * <br>This renderer forms only a part of resulting component's html representation.
 * Significant part of the component is built on client by routines of {@code fileUpload.js}.
 *
 * @author Pavel Porubov
 */
@FacesRenderer(
        renderKitId = "HTML_BASIC",
        rendererType = FileUploadRenderer.RENDERER,
        componentFamily = ComponentFamily.FILE_UPLOAD)
@ResourceDependencies({
        @ResourceDependency(name = "jsf.js", target = "head", library = "javax.faces"),
        @ResourceDependency(name = "jquery.js", target = "head", library = "e5ui/js"),
        @ResourceDependency(name = "ajaxStatus.js", target = "head", library = "e5ui/js"),
        @ResourceDependency(name = "fileUpload.js", target = "head", library = "e5ui/js"),
        @ResourceDependency(name = "fileUpload.i18n.js", target = "head", library = "e5ui/js"),
        @ResourceDependency(name = "fileUpload.css", target = "head", library = "e5ui/css")})
public class FileUploadRenderer extends HtmlBasicRenderer {

    /** <code>Renderer</code> type for component */
    public static final String RENDERER = "ru.efive.uifaces.FileUpload";

    private static final String WRONG_COMPONENT = "Can not render component of class %s";

    /** The surrounding div's class. */
    public static final String FILE_UPLOAD_CLASS = "e5uiFileUpload";
    /** The button's class. */
    public static final String FILE_UPLOAD_BUTTON_CLASS = "e5uiFileUploadButton";
    public static final String FILE_UPLOAD_BUTTON_SELECT_CLASS = "select";
    public static final String FILE_UPLOAD_BUTTON_UPLOAD_CLASS = "upload";
    /** The button's text class. */
    public static final String FILE_UPLOAD_BUTTON_TEXT_CLASS = "e5uiFileUploadBtnText";
    /** The selected files table's class. */
    public static final String FILE_UPLOAD_LIST_CLASS = "e5uiFileUploadList";

    private static final String FRAME_ID_SUFFIX = "-e5uiFileUpload";
    private static final String BTNSEL_ID_SUFFIX = "-e5uiFileUpload-btnsel";
    private static final String BTNUPL_ID_SUFFIX = "-e5uiFileUpload-btnupl";
    private static final String LIST_ID_SUFFIX = "-e5uiFileUpload-list";

    private static final String MAKEUP = "e5ui_fileUpload.makeUp({id: \"%s\", lang: \"%s\", form: \"%s\", maxCount: %s, ajax: %s%s});";
    private static final String ON_UPLOADING = ", onuploading: function(params){%s}";
    private static final String ON_UPLOADED = ", onuploaded: function(params){%s}";
    private static final String AJAX_CALL = "function(){var event = {type: \"" + FileUpload.DEFAULT_EVENT_NAME + "\"};%s;}";

    /** {@inheritDoc} */
    @Override
    protected void encodeBegin(AdvancedResponseWriter writer) throws IOException {
        UIComponent component = writer.getComponent();
        if (!component.isRendered()) {
            return;
        }

        if (component instanceof FileUpload) {
            FileUpload fileUpload = (FileUpload) component;
            String fuId = fileUpload.getClientId(),
                    fuIdEx = fuId + FRAME_ID_SUFFIX;

            //frame
            writer.startElement(HtmlElement.DIV);
            writer.writeAttribute(HtmlAttribute.ID, fuIdEx, null);
            writeStyleClass(fileUpload, writer, FILE_UPLOAD_CLASS);
            writeStyle(fileUpload, writer);

            //"select" button
            writer.startElement(HtmlElement.DIV);
            writer.writeAttribute(HtmlAttribute.ID, fuId + BTNSEL_ID_SUFFIX, null);
            writeStyleClass(null, writer, FILE_UPLOAD_BUTTON_CLASS, FILE_UPLOAD_BUTTON_SELECT_CLASS);
            writer.startElement(HtmlElement.SPAN);
            writeStyleClass(null, writer, FILE_UPLOAD_BUTTON_TEXT_CLASS, FILE_UPLOAD_BUTTON_SELECT_CLASS);
            writer.endElement(HtmlElement.SPAN);
            writer.endElement(HtmlElement.DIV);

            //list
            writer.startElement(HtmlElement.TABLE);
            writer.writeAttribute(HtmlAttribute.ID, fuId + LIST_ID_SUFFIX, null);
            writeStyleClass(null, writer, FILE_UPLOAD_LIST_CLASS);
            writer.endElement(HtmlElement.TABLE);

            boolean separate = fileUpload.getActionBehavior() == FileUpload.ActionBehavior.separate;
            if (separate) {
                //"upload" button
                writer.startElement(HtmlElement.DIV);
                writer.writeAttribute(HtmlAttribute.ID, fuId + BTNUPL_ID_SUFFIX, null);
                writeStyleClass(null, writer, FILE_UPLOAD_BUTTON_CLASS, FILE_UPLOAD_BUTTON_UPLOAD_CLASS);
                writer.startElement(HtmlElement.SPAN);
                writeStyleClass(null, writer, FILE_UPLOAD_BUTTON_TEXT_CLASS, FILE_UPLOAD_BUTTON_UPLOAD_CLASS);
                writer.endElement(HtmlElement.SPAN);
                writer.endElement(HtmlElement.DIV);
            }

            writer.endElement(HtmlElement.DIV); //frame

            //makeup
            String ab = "null";
            if (separate) {
                Map<String, List<ClientBehavior>> cbm = fileUpload.getClientBehaviors();
                if (cbm != null && !cbm.isEmpty()) {
                    List<ClientBehavior> cbl = cbm.get(FileUpload.DEFAULT_EVENT_NAME);
                    if (cbl != null && !cbl.isEmpty()) {
                        ClientBehaviorContext cbCtx =
                                ClientBehaviorContext.createClientBehaviorContext(
                                writer.getContext(), fileUpload, FileUpload.DEFAULT_EVENT_NAME,
                                null, null);
                        for (ClientBehavior cb : cbl) {
                            if (cb instanceof AjaxBehavior) {
                                String cbs = cb.getScript(cbCtx);
                                if (cbs != null && !cbs.isEmpty()) {
                                    ab = format(AJAX_CALL, cbs);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            writer.startElement(HtmlElement.SCRIPT);
            writer.writeAttribute(HtmlAttribute.TYPE, HtmlAttributeValue.TYPE_TEXT_JAVASCRIPT, null);
            int maxFilesCount = 1;
            if (fileUpload.isMultiple()) {
                Integer mfc = fileUpload.getMaxFilesCount();
                maxFilesCount = (mfc != null) ? mfc : 0;
            }
            String onUploading = (String) fileUpload.getAttributes().get(FileUpload.PropertyKeys.onuploading.name());
            if (onUploading != null && !onUploading.isEmpty()) {
                onUploading = format(ON_UPLOADING, onUploading);
            } else {
                onUploading = "";
            }
            String onUploaded = (String) fileUpload.getAttributes().get(FileUpload.PropertyKeys.onuploaded.name());
            if (onUploaded != null && !onUploaded.isEmpty()) {
                onUploaded = format(ON_UPLOADED, onUploaded);
            } else {
                onUploaded = "";
            }
            writer.writeText(format(MAKEUP,
                    fileUpload.getClientId(),
                    writer.getContext().getViewRoot().getLocale().getLanguage(),
                    separate ? findEnclosingForm(fileUpload).getClientId() : "",
                    maxFilesCount,
                    ab,
                    onUploading + onUploaded),
                    null);
            writer.endElement(HtmlElement.SCRIPT);

            //save uploadHandler expression for UploadFilter
            Map<String, Object> sessionMap = writer.getContext().getExternalContext().getSessionMap();
            final ValueExpression uploadHandlerExpression =
                    fileUpload.getValueExpression(FileUpload.PropertyKeys.uploadHandler.name());
            if (uploadHandlerExpression != null) {
                sessionMap.put(fuIdEx, new UploadHandler() {

                    @Override
                    public void handleUpload(UploadInfo uploadInfo) {
                        FacesContext facesContext = getFacesContext(uploadInfo.getRequest(), uploadInfo.getResponse());
                        UploadHandler uploadHandler =
                                (UploadHandler) uploadHandlerExpression.getValue(facesContext.getELContext());
                        uploadHandler.handleUpload(uploadInfo);
                    }
                });
            } else {
                sessionMap.put(fuIdEx, "");
            }
        } else {
            throw new IllegalArgumentException(format(WRONG_COMPONENT, component.getClass().getName()));
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void encodeChildren(AdvancedResponseWriter writer,
            List<UIComponent> childrens) throws IOException {
        return;
    }

    /** {@inheritDoc} */
    @Override
    protected void encodeEnd(AdvancedResponseWriter writer) throws IOException {
        return;
    }

    /** {@inheritDoc} */
    @Override
    public void decode(FacesContext context, UIComponent component) {
        super.decode(context, component);
        if (component instanceof FileUpload) {
            FileUpload fileUpload = (FileUpload) component;
            Map<String, String> requestParameterMap = context.getExternalContext().getRequestParameterMap();
            String execute = requestParameterMap.get(PARTIAL_EXECUTE_PARAM_NAME);
            if (execute != null && !execute.isEmpty()) {
                boolean event = execute.contains(ALL_PARTIAL_PHASE_CLIENT_IDS);
                if (!event) {
                    String fuId = fileUpload.getClientId();
                    for (String ex : execute.split("\\s+")) {
                        if (fuId.equals(ex)) {
                            event = true;
                            break;
                        }
                    }
                }
                if (event) {
                    ActionEvent actionEvent = new ActionEvent(fileUpload);
                    fileUpload.queueEvent(actionEvent);
                }
            }
        } else {
            throw new IllegalArgumentException(format(WRONG_COMPONENT, component.getClass().getName()));
        }
    }
}
