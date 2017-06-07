package ru.efive.dms.uifaces.beans.workflow;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractLoggableBean;
import ru.efive.dms.util.message.MessageHolder;
import ru.efive.dms.util.message.MessageUtils;
import ru.entity.model.document.IncomingDocument;
import ru.entity.model.document.InternalDocument;
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
            log.info("[fillDocumentNumber] entering synchronized block: semaphore[queue={}]", semaphore.getQueueLength());
            final Numerator numerator = numeratorDao.findBestNumerator(doc.getType(), doc.getForm(), doc.getController(), getContragent(doc));
            if (numerator == null) {
                log.error("[fillDocumentNumber] No numerator found!");
                MessageUtils.addMessage(MessageHolder.MSG_NO_NUMERATOR_FOUND);
                return false;
            }
            log.info("[fillDocumentNumber] Used numerator: {}", numerator);
            doc.setRegistrationDate(LocalDateTime.now());
            doc.setRegistrationNumber(numeratorDao.incrementAndGet(numerator));
            doc.setNumerator(numerator);
            return true;
        } catch (Exception e) {
            log.error("[fillDocumentNumber] End-> Exception: {} ", e.getMessage(), e);
            return false;
        } finally {
            log.info("[fillDocumentNumber] leave synchronized block: semaphore[queue={}]", semaphore.availablePermits(), semaphore.getQueueLength());
            semaphore.release();
        }
    }

    public boolean freeIfLast(DocumentEntity doc) {
        if (StringUtils.isEmpty(doc.getRegistrationNumber())
                || doc.getNumerator() == null
                || doc.getRegistrationDate() == null
                || (doc instanceof InternalDocument && ((InternalDocument) doc).isClosePeriodRegistrationFlag())) {
            return false;
        }
        //XXX: semaphore usage
        try {
            semaphore.acquire();
            log.info("[freeIfLast] entering synchronized block: semaphore[queue={}]", semaphore.getQueueLength());
            if (isLastDocumentInNumerator(doc, doc.getNumerator())) {
                numeratorDao.decrementAndGet(doc.getNumerator());
            }
            return true;
        } catch (Exception e) {
            log.error("[freeIfLast] End-> Exception: {} ", e.getMessage(), e);
            return false;
        } finally {
            log.info("[freeIfLast] leave synchronized block: semaphore[queue={}]", semaphore.availablePermits(), semaphore.getQueueLength());
            semaphore.release();
        }
    }

    private boolean isLastDocumentInNumerator(DocumentEntity doc, Numerator numerator) {
        final String previousNumber = numerator.getPrefix() + (numerator.getCurrent() -1);
        return StringUtils.equals(previousNumber, doc.getRegistrationNumber());

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