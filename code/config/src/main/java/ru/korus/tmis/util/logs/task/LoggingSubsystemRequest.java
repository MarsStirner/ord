package ru.korus.tmis.util.logs.task;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Layout;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import ru.korus.tmis.util.logs.appender.RestfullLoggingSubsystemAppender;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;

/**
 * Author: Upatov Egor <br>
 * Date: 20.12.13, 13:26 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class LoggingSubsystemRequest implements Runnable {

    private static URI uri;
    private static Layout<ILoggingEvent> layout;
    private static Header[] headers;
    private static int timeout;
    private static String charset;
    private static RestfullLoggingSubsystemAppender parent;

    private final int number;
    private final ILoggingEvent event;

    public LoggingSubsystemRequest(final ILoggingEvent event, final int number) {
        this.event = event;
        this.number = number;
    }

    @Override
    public void run() {
        final HttpClient httpClient = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), timeout);
        HttpConnectionParams.setSoTimeout(httpClient.getParams(), timeout);
        if (event != null && event.getMessage() != null && event.getMessage().length() > 1) {
            final HttpPost request = new HttpPost(uri);
            final String data = layout.doLayout(event);
            try {
                request.setHeaders(headers);
                request.setEntity(new StringEntity(data, charset));
                final HttpResponse response = httpClient.execute(request);
                if(200 != response.getStatusLine().getStatusCode()){
                    parent.addError(String.format("#%d Message:\"%s\" return \"%s\" ResponseContent:\"%s\"",
                            number,
                            data,
                            response.toString(),
                            convertStreamToString(response.getEntity().getContent()) )
                    );
                }
            } catch (UnsupportedEncodingException e) {
                parent.addError(String.format("#%d UnsupportedEncodingException for message:\"%s\"", number, data), e);
                parent.submitError();
            } catch (ClientProtocolException e) {
                parent.addError(String.format("#%d ClientProtocolException for message:\"%s\"", number, data), e);
                parent.submitError();
            } catch (IOException e) {
                parent.addError(String.format("#%d IOException for message:\"%s\"", number, data), e);
                parent.submitError();
            } catch (Exception e) {
                parent.addError(String.format("#%d Unknown Exception for message:\"%s\"", number, data), e);
                parent.submitError();
            } finally {
                httpClient.getConnectionManager().shutdown();
            }
        }
    }

    public static void setUri(final URI uri) {
        LoggingSubsystemRequest.uri = uri;
    }

    public static void setLayout(final Layout<ILoggingEvent> layout) {
        LoggingSubsystemRequest.layout = layout;
    }

    public static void setHeaders(final Header[] headers) {
        LoggingSubsystemRequest.headers = headers;
    }

    public static void setTimeout(int timeout) {
        LoggingSubsystemRequest.timeout = timeout;
    }

    public static void setCharset(String charset) {
        LoggingSubsystemRequest.charset = charset;
    }

    public static void setParent(RestfullLoggingSubsystemAppender parent) {
        LoggingSubsystemRequest.parent = parent;
    }

    private static String convertStreamToString(final java.io.InputStream is) {
        final java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
