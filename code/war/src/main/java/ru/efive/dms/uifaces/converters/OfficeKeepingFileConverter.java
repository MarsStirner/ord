package ru.efive.dms.uifaces.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.FacesConverter;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.entity.model.document.OfficeKeepingFile;
import ru.hitsl.sql.dao.interfaces.OfficeKeepingFileDao;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import java.util.List;


@FacesConverter("OfficeKeepingFileConverter")
public class OfficeKeepingFileConverter implements Converter {

    @Autowired
    @Qualifier("officeKeepingFileDao")
    private OfficeKeepingFileDao officeKeepingFileDao;

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Object result = null;
        try {
            List<OfficeKeepingFile> list = officeKeepingFileDao.getItems();
            if (list.size() != 0) {
                for (OfficeKeepingFile in_record : list) {
                    if (in_record != null) {
                        String in_recordCheckString = in_record.getFileIndex() + " - " + in_record.getShortDescription();
                        if (in_recordCheckString.equals(value)) {
                            result = in_record;
                            break;
                        }
                    }

                }
            } else {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CONVERTER_ERROR);
                System.out.println("Не найдена номенклатура дел");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value != null) {
            OfficeKeepingFile in_record = (OfficeKeepingFile) value;
            return in_record.getFileIndex() + " - " + in_record.getShortDescription();
        } else {
            return "";
        }
    }
}

