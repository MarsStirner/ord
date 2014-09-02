package ru.korus.tmis.util.logs.filter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.AbstractMatcherFilter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Upatov Egor <br>
 * Date: 18.12.13, 13:32 <br>
 * Company: Korus Consulting IT <br>
 * Description: фильтр основанный на маркерах логирования.
 * Если в установленном списке нет маркера логируемого события,
 * или у события не выставлен маркер - в аппендер не добавлять <br>
 */
public class MarkerFilter extends AbstractMatcherFilter<ILoggingEvent> {

    //Маркеры для сравнения
    private List<Marker> markerList = new ArrayList<Marker>(5);

    @Override
    public void start() {
        if (!markerList.isEmpty())  {
            super.start();
        } else {
            addError("MarkerFilter: No one marker is set.");
            stop();
        }
    }

    @Override
    public FilterReply decide(final ILoggingEvent event) {
        final Marker marker = event.getMarker();
        if (!isStarted()){
            return FilterReply.NEUTRAL;
        }
        if(marker == null){
            return onMismatch;
        }
        if (markerList.contains(marker)){
            return onMatch;
        }
        return onMismatch;
    }

    public void setMarker(final String markerString) {
        if(null != markerString && !markerString.isEmpty()) {
           markerList.add(MarkerFactory.getMarker(markerString));
        }  else {
            addWarn(String.format("MarkerFilter: Incorrect markerString \"%s\"", markerString));
        }
    }
}
