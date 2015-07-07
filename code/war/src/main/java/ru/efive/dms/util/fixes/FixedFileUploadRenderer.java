package ru.efive.dms.util.fixes;

import org.primefaces.component.fileupload.FileUploadRenderer;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
/**
 * Author: Upatov Egor <br>
 * Date: 01.07.2015, 17:29 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class FixedFileUploadRenderer  extends FileUploadRenderer {

    @Override
    public void decode(FacesContext context, UIComponent component) {
        if (context.getExternalContext().getRequestContentType().toLowerCase().startsWith("multipart/")) {
            super.decode(context, component);
        }
    }

}