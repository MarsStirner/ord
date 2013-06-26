package ru.efive.uifaces.filter;

import javax.servlet.ServletException;

/**
 * The exception for {@link UploadFilter}
 *
 * @author Pavel Porubov
 */
public class UploadException extends ServletException {

    public UploadException(Throwable rootCause) {
        super(rootCause);
    }

    public UploadException(String message, Throwable rootCause) {
        super(message, rootCause);
    }

    public UploadException(String message) {
        super(message);
    }

    public UploadException() {
    }
}
