package ru.korus.tmis.util.logs.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import ru.korus.tmis.util.logs.task.LoggingSubsystemRequest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Author: Upatov Egor <br>
 * Date: 16.12.13, 13:37 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class RestfullLoggingSubsystemAppender extends AppenderBase<ILoggingEvent> {

    //Адрес сервиса куда отправляем запросы на логирование
    private String url;

    //Адрес для записей новых событий в подсистему журналирования
    private URI entryURI;
    //Адрес для получения списка логируемых уровней
    private URI levelURI;
    //Адрес для проверки доступности сервиса
    private URI stateURI;

    //Время последнего опроса на доступность подсистемы журналирования
    private long lastStateCheck;
    //Флажок доступности сервиса подсистемы журналирования
    private boolean serviceAvailable = false;
    //Интервал сканирования доступности подсистемы журналирования
    private long checkInterval = 30000;

    //Указанная кодировка
    private String charset = "UTF-8";
    //Заголовок с типом содержимого
    private String contentType = "application/json";

    //таймаут соединения & ожидания ответа
    private Integer timeout = 3000;

    //Внтутренний список заголовков запроса
    private Header[] headers = new Header[2];


    //Layout возвращает строку в нужном формате
    //Layout для подсистемы журналирования: в данном случае необходимо получить строковое отображение JSON-овского объекта
    private Layout<ILoggingEvent> layout;

    //Размер пула потоков
    private static final int POOL_SIZE = 8;
    //Пул потоков
    private ExecutorService pool;

    private int counter = 0;


    private long lastErrorTime;
    private int errorCount = 0;


    @Override
    public void start() {
        super.start();
        checkUrl();
        checkContentType();
        checkCharset();
        LoggingSubsystemRequest.setHeaders(headers);
        checkLayout();
        checkTimeout();
        LoggingSubsystemRequest.setParent(this);
        pool = Executors.newFixedThreadPool(POOL_SIZE);
    }

    @Override
    public void stop() {
        serviceAvailable = false;
        pool.shutdown();
        //super.stop();
    }

    private void checkTimeout() {
        if (timeout != null && timeout > 0) {
            addInfo(String.format("LoggingSubsystemAppender: Timeout is set to %d milliseconds", timeout));
        } else {
            addWarn("LoggingSubsystemAppender: Timeout is not set. Set it to 3000 milliseconds");
        }
        LoggingSubsystemRequest.setTimeout(timeout);
    }

    /**
     * Проверка заполненности charset
     */
    private void checkCharset() {
        if (charset != null && !charset.isEmpty()) {
            addInfo(String.format("LoggingSubsystemAppender: Charset is set to \"%s\"", charset));
        } else {
            addWarn(String.format("LoggingSubsystemAppender: Charset is not present. Set it to \"%s\"", charset));
        }
        headers[0] = new BasicHeader("charset", charset);
        LoggingSubsystemRequest.setCharset(charset);
    }


    /**
     * Проверка Layout-а
     */
    private void checkLayout() {
        if (layout != null && layout.isStarted()) {
            addInfo(String.format("LoggingSubsystemAppender: layout is initialized and has type \"%s\"", layout.getClass().getName()));
            LoggingSubsystemRequest.setLayout(layout);
        } else {
            addError("LoggingSubsystem: layout is not initialized. Appender not started");
            super.stop();
        }
    }


    /**
     * Проверка заполненности ContentType
     */
    private void checkContentType() {
        if (contentType != null && !contentType.isEmpty()) {
            addInfo(String.format("LoggingSubsystemAppender: Content-Type=\"%s\"", contentType));
        } else {
            addWarn(String.format("LoggingSubsystemAppender: Content-Type is not present. Set it to \"%s\"", contentType));
        }
        headers[1] = new BasicHeader("content-type", contentType);
    }

    /**
     * Проверка заполненности URL
     */
    private void checkUrl() {
        if (url != null && !url.isEmpty()) {
            addInfo(String.format("LoggingSubsystemAppender: URL=\"%s\"", url));
            addInfo(String.format("LoggingSubsystemAppender: LevelURL=\"%s\"", levelURI.toString()));
            addInfo(String.format("LoggingSubsystemAppender: StateURL=\"%s\"", stateURI.toString()));
            addInfo(String.format("LoggingSubsystemAppender: EntryURL=\"%s\"", entryURI.toString()));
            HttpClient httpClient = new DefaultHttpClient();
            final HttpGet stateRequest = new HttpGet(stateURI);
            try {
                final HttpResponse stateResponse = httpClient.execute(stateRequest);
                addInfo(String.format("LoggingSubsystem: checkState return \"%s\"", convertStreamToString(stateResponse.getEntity().getContent())));
                lastStateCheck = System.currentTimeMillis();
                serviceAvailable = true;
                addError(String.format("STATUS LINE %d %s", stateResponse.getStatusLine().getStatusCode(), stateResponse.getStatusLine().getReasonPhrase()));
            } catch (IOException e) {
                addError("LoggingSubsystemAppender: State request IO exception. Appender not started.", e);
                stop();
            } finally {
                httpClient.getConnectionManager().shutdown();
            }
            httpClient = new DefaultHttpClient();
            final HttpGet levelRequest = new HttpGet(levelURI);
            try {
                final HttpResponse levelResponse = httpClient.execute(levelRequest);
                addInfo(String.format("LoggingSubsystem: checkLevels return \"%s\"", convertStreamToString(levelResponse.getEntity().getContent())));
            } catch (IOException e) {
                addError("LoggingSubsystemAppender: Level request IO exception. Appender not started.", e);
                stop();
            } finally {
                httpClient.getConnectionManager().shutdown();
            }
        } else {
            addError("LoggingSubsystemAppender: URL is not present. Appender not started.");
            stop();
        }
        LoggingSubsystemRequest.setUri(entryURI);
    }


    @Override
    protected void append(final ILoggingEvent event) {
        if (serviceAvailable) {
            counter++;
            pool.execute(new LoggingSubsystemRequest(event, counter));
        } else {
            if (System.currentTimeMillis() - lastStateCheck > checkInterval) {
                start();
            }
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        try {
            this.entryURI = new URI(url.concat("/api/entry/"));
            this.levelURI = new URI(url.concat("/api/level/"));
            this.stateURI = new URI(url.concat("/api/"));
        } catch (URISyntaxException e) {
            addError("LoggingSubsystemAppender: URL can't parse to URI", e);
        }
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Layout getLayout() {
        return layout;
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public long getCheckInterval() {
        return checkInterval;
    }

    public void setCheckInterval(long checkInterval) {
        this.checkInterval = checkInterval * 1000;
    }

    private static String convertStreamToString(final java.io.InputStream is) {
        final java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }


    public synchronized void submitError() {
        if (System.currentTimeMillis() - lastErrorTime > checkInterval) {
            errorCount++;
        } else {
            errorCount = 1;
        }
        lastErrorTime = System.currentTimeMillis();
        if (errorCount > 10) {
            errorCount = 0;
            stop();
        }
    }
}
