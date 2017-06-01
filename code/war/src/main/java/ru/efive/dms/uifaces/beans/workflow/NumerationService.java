package ru.efive.dms.uifaces.beans.workflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractLoggableBean;
import ru.entity.model.document.IncomingDocument;
import ru.entity.model.document.Numerator;
import ru.entity.model.document.RequestDocument;
import ru.entity.model.mapped.DocumentEntity;
import ru.entity.model.referenceBook.Contragent;
import ru.hitsl.sql.dao.interfaces.NumeratorDao;

import java.time.LocalDateTime;
import java.util.concurrent.Semaphore;


@Component("numerationService")
public class NumerationService extends AbstractLoggableBean {
    private static final Semaphore semaphore = new Semaphore(1);

    @Autowired
    @Qualifier("numeratorDao")
    private NumeratorDao numeratorDao;

    //TODO return flag + Message
    public boolean fillDocumentNumber(DocumentEntity doc) {
        //XXX: semaphore usage
        try {
            semaphore.acquire();
            log.info("entering synchronized block: semaphore[queue={}]", semaphore.getQueueLength());
            final Numerator numerator = numeratorDao.findBestNumerator(doc.getType(), doc.getForm(), doc.getController(), getContragent(doc));
            if(numerator == null){
                log.error("No numerator found!");
                doc.setWFResultDescription("Не найдено подходящего нумератора");
                return false;
            }
            log.info("Used numerator: {}", numerator);
            doc.setRegistrationDate(LocalDateTime.now());
            doc.setRegistrationNumber(numeratorDao.incrementAndGet(numerator));
            doc.setNumerator(numerator);
            return true;
        } catch (Exception e) {
            log.error("End-> Exception: {} ", e.getMessage(), e);
            return false;
        } finally {
            log.info("leave synchronized block: semaphore[queue={}]", semaphore.availablePermits(), semaphore.getQueueLength());
            semaphore.release();
        }
    }

    private Contragent getContragent(DocumentEntity doc) {
        if (doc instanceof IncomingDocument) {
            return ((IncomingDocument) doc).getContragent();
        } else if (doc instanceof RequestDocument) {
            return ((RequestDocument) doc).getContragent();
        } else {
            return null;
        }
    }

}