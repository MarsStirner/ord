package ru.korus.tmis.util.logs.layout;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.LayoutBase;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gson.Gson;
import ru.korus.tmis.util.logs.layout.jsonobjects.LoggingSubsystemItem;

/**
 * Author: Upatov Egor <br>
 * Date: 16.12.13, 18:50 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class JSONLayout extends LayoutBase<ILoggingEvent> {
    // Версия ядра
    private String coreVersion;
    // Список тегов
    private List<String> tagList = new ArrayList<String>(5);

    private Gson gson = new Gson();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS z");

    private String ipAddress = "";

    @Override
    public void start() {
        super.start();
        //проверка установки Версии ядра
        if (coreVersion != null && !coreVersion.isEmpty()) {
            addInfo(String.format("JSONLayout: coreVersion set to \"%s\"", coreVersion));
        } else {
            addError("JSONLayout: coreVersion is not set or is empty.");
            //Надо проверять при попытке обратиться флажок started у Layout...
            //что-то в коде не нашел где у них это делается.. собственно смысл флажка в чем тогда?
            stop();
        }
        //проверка установки списка тегов
        if (!tagList.isEmpty()) {
            StringBuilder sb = new StringBuilder("[");
            final Iterator<String> tagIterator = tagList.iterator();
            while (tagIterator.hasNext()) {
                sb.append('\"').append(tagIterator.next()).append('\"');
                if (tagIterator.hasNext()) {
                    sb.append(", ");
                }
            }
            sb.append(']');
            addInfo(String.format("JSONLayout: TAG list set to %s", sb.toString()));
        } else {
            addError("JSONLayout: TAG list is empty.");
            //Надо проверять при попытке обратиться флажок started у Layout...
            //что-то в коде не нашел где у них это делается.. собственно смысл флажка в чем тогда?
            stop();
        }
        try {
            ipAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            ipAddress = "unknown";
        }
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        final LoggingSubsystemItem item = new LoggingSubsystemItem();
        item.setLevel(event.getLevel().levelStr);
        item.addToOwner("coreVersion", coreVersion);
        item.addToOwner("LoggerName", event.getLoggerName());
        item.addToOwner("ip", ipAddress);
        item.setTags(tagList);
        item.addToData("Number", event.getLoggerContextVO().getName() );
        item.addToData("Timestamp", dateFormat.format(event.getTimeStamp()));
        item.addToData("ThreadName", event.getThreadName());
        item.addToData("FormattedMessage", event.getFormattedMessage());

        return gson.toJson(item);
    }


    public String getCoreVersion() {
        return coreVersion;
    }

    public void setCoreVersion(final String coreVersion) {
        this.coreVersion = coreVersion;
    }

    public void addTag(final String tag) {
        tagList.add(tag);
    }
}
