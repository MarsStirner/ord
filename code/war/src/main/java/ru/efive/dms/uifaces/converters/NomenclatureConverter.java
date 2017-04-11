package ru.efive.dms.uifaces.converters;

import org.springframework.beans.factory.annotation.Autowired;
import ru.efive.dms.uifaces.beans.annotations.FacesConverter;
import ru.entity.model.referenceBook.Nomenclature;
import ru.hitsl.sql.dao.interfaces.referencebook.NomenclatureDao;

@FacesConverter("NomenclatureConverter")
public class NomenclatureConverter extends AbstractReferenceBookConverter<Nomenclature> {

    @Autowired
    public NomenclatureConverter(NomenclatureDao nomenclatureDao) {
        super(nomenclatureDao);
    }
}