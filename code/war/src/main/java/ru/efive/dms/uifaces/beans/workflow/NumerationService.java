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
import ru.entity.model.document.OutgoingDocument;
import ru.entity.model.document.RequestDocument;
import ru.entity.model.mapped.DocumentEntity;
import ru.entity.model.numerator.Numerator;
import ru.entity.model.numerator.NumeratorUsage;
import ru.entity.model.referenceBook.Contragent;
import ru.hitsl.sql.dao.interfaces.NumeratorDao;

import java.util.concurrent.Semaphore;


@Component("numerationService")
public class NumerationService extends AbstractLoggableBean {
    private static final Semaphore semaphore = new Semaphore(1);

    @Autowired
    @Qualifier("numeratorDao")
    private NumeratorDao numeratorDao;

    public boolean freeNumeration(DocumentEntity doc) {
        if(doc.getRegistrationDate() == null || StringUtils.isEmpty(doc.getRegistrationNumber())){
            return false;
        }
        //XXX: semaphore usage
        try {
            semaphore.acquire();
            log.info("[freeNumeration] entering synchronized block: semaphore[queue={}]", semaphore.getQueueLength());
            final NumeratorUsage usage = numeratorDao.getUsage(doc.getUniqueId());
            if (usage != null) {
                log.error("[freeNumeration] Document use numerator: {}", usage);
                numeratorDao.deleteUsage(usage);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("[freeNumeration] End-> Exception: {} ", e.getMessage(), e);
            return false;
        } finally {
            log.info("[freeNumeration] leave synchronized block: semaphore[queue={}]", semaphore.availablePermits(), semaphore.getQueueLength());
            semaphore.release();
        }
    }

    //TODO return flag + Message
    public boolean registerDocument(DocumentEntity doc) {
        //XXX: semaphore usage
        try {
            semaphore.acquire();
            log.info("[registerDocument] entering synchronized block: semaphore[queue={}]", semaphore.getQueueLength());
            final NumeratorUsage usage = numeratorDao.getUsage(doc.getUniqueId());
            if (usage != null) {
                log.error("[registerDocument] Document already use numerator! {}", usage);
                MessageUtils.addMessage(MessageHolder.MSG_NUMERATOR_ALREADY_IN_USE);
                return false;
            }
            log.debug("No previous Numerator usage found");
            final Numerator numerator = numeratorDao.findBestNumerator(doc.getType(), doc.getForm(), doc.getController(), getContragent(doc));
            if (numerator == null) {
                log.error("[registerDocument] No numerator found!");
                MessageUtils.addMessage(MessageHolder.MSG_NO_NUMERATOR_FOUND);
                return false;
            }
            log.info("[registerDocument] Used numerator: {}", numerator);

            final NumeratorUsage acquiredUsage = numeratorDao.createUsage(doc, numerator);
            doc.setRegistrationDate(acquiredUsage.getRegistrationDate());
            doc.setRegistrationNumber(numerator.getPrefix() + acquiredUsage.getNumber());
            return true;
        } catch (Exception e) {
            log.error("[registerDocument] End-> Exception: {} ", e.getMessage(), e);
            return false;
        } finally {
            log.info("[registerDocument] leave synchronized block: semaphore[queue={}]", semaphore.availablePermits(), semaphore.getQueueLength());
            semaphore.release();
        }
    }

    private Contragent getContragent(DocumentEntity doc) {
        if (doc instanceof IncomingDocument) {
            return ((IncomingDocument) doc).getContragent();
        } else if (doc instanceof RequestDocument) {
            return ((RequestDocument) doc).getContragent();
        } else if (doc instanceof OutgoingDocument) {
            return ((OutgoingDocument) doc).getContragent();
        } else {
            return null;
        }
    }



}