package ru.korus.tmis.util.logs.filter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * Author:      Dmitriy E. Nosov <br>
 * Date:        25.01.13, 14:34 <br>
 * Company:     Korus Consulting IT<br>
 * Description: Фильтр эксепшенов для аппедреров логбека <br>
 */
public class ExceptionFilter extends Filter<ILoggingEvent> {

    private Class<?> exceptionClass;

    @Override
    public FilterReply decide(final ILoggingEvent event) {
        final IThrowableProxy throwableProxy = event.getThrowableProxy();
        if (throwableProxy == null) {
            return FilterReply.DENY;
        }

        if (!(throwableProxy instanceof ThrowableProxy)) {
            return FilterReply.DENY;
        }

        final ThrowableProxy throwableProxyImpl = (ThrowableProxy) throwableProxy;
        final Throwable throwable = throwableProxyImpl.getThrowable();
        if (exceptionClass.isInstance(throwable)) {
            return FilterReply.ACCEPT;
        }

        return FilterReply.DENY;
    }

    public void setExceptionClassName(final String exceptionClassName) {
        try {
            exceptionClass = Class.forName(exceptionClassName);
        } catch (final ClassNotFoundException e) {
            throw new IllegalArgumentException("Class is unavailable: " + exceptionClassName, e);
        }
    }
}