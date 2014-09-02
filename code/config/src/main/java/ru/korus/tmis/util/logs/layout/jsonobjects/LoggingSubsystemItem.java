package ru.korus.tmis.util.logs.layout.jsonobjects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 17.12.13, 18:31 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class LoggingSubsystemItem {
    private String level;
    private Map<String, String> owner = new HashMap<String, String>();
    private Map<String, String> data = new HashMap<String, String>();
    private List<String> tags = new ArrayList<String>(5);

    public LoggingSubsystemItem() {
    }

    public void addToTags(final String tag){
        tags.add(tag);
    }

    public void addToOwner(final String key, final String value){
        owner.put(key, value);
    }

    public void addToData(final String key, final String value){
        data.put(key, value);
    }

    //GET & SET

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level.toLowerCase();
    }

    public Map<String, String> getOwner() {
        return owner;
    }

    public void setOwner(Map<String, String> owner) {
        this.owner = owner;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
