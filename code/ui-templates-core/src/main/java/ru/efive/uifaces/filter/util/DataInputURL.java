package ru.efive.uifaces.filter.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.URLStreamHandler;
import ru.efive.uifaces.filter.util.org.apache.tools.ant.util.ReaderInputStream;

/**
 *
 * @author Pavel Porubov
 */
public class DataInputURL {

    private static URL make(String s, URLStreamHandler h) throws MalformedURLException,
            UnsupportedEncodingException {
        return new URL(null, "DataInput://" + URLEncoder.encode(s, "UTF-8"), h);
    }

    public static URL make(final InputStream is) throws MalformedURLException, UnsupportedEncodingException {
        return make(String.valueOf(is.hashCode()), new URLStreamHandler() {

            @Override
            protected URLConnection openConnection(URL u) throws IOException {
                return new URLConnection(u) {

                    @Override
                    public InputStream getInputStream() throws IOException {
                        return is;
                    }

                    @Override
                    public void connect() throws IOException {
                    }
                };
            }
        });
    }

    public static URL make(final String str) throws MalformedURLException, UnsupportedEncodingException {
        return make(String.valueOf(str.hashCode()), new URLStreamHandler() {

            long lastModified = System.currentTimeMillis();

            @Override
            protected URLConnection openConnection(URL u) throws IOException {
                return new URLConnection(u) {

                    private InputStream is = new ReaderInputStream(new StringReader(str), "UTF-8");

                    @Override
                    public InputStream getInputStream() throws IOException {
                        return is;
                    }

                    @Override
                    public void connect() throws IOException {
                    }

                    @Override
                    public long getLastModified() {
                        return lastModified;
                    }
                };
            }
        });
    }
}
