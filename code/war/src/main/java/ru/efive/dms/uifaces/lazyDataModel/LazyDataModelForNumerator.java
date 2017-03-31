package ru.efive.dms.uifaces.lazyDataModel;

import com.github.javaplugs.jsf.SpringScopeView;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * Author: Upatov Egor <br>
 * Date: 20.04.2015, 18:05 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */

@Component("numeratorLDM")
@SpringScopeView
public class LazyDataModelForNumerator implements Serializable {
}
// extends AbstractFilterableLazyDataModel<Numerator> {
//
//
//    @Autowired
//    public LazyDataModelForNumerator(final NumeratorDaoImpl dao) {
//        super(dao);
//    }
//
//
//}
