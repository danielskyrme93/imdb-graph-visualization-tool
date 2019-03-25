package app.model;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Is the parent class of all IMDb elements (show, season, episode).
 * May hold other IMDb Entities (Composite) or not (Leaf).
 * @author Daniel Skyrme
 * @version %I%, %G%
 */
public class IMDbEntity {
    static final String IMDB_URL_PREFIX = "https://www.imdb.com/title/";
    String id;
    String title;
    Float rating;
    HashMap<Integer, IMDbEntity> map;

    IMDbEntity(String id, String title) {
        this.id = id;
        this.title = title;
        this.rating = Float.NaN;
        this.map = new HashMap<>();
    }


    static void linkProperty(LinkedHashMap<String, Object> map, String name, Object val) {
        String assignedVal = val == null ? "N/A" : String.valueOf(val);
        map.put(name, assignedVal);
    }


    public Float getRating() {
        return rating;
    }

    public LinkedHashMap<String, Object> getInfoStringMap() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        linkProperty(map, "Info", null);
        return map;
    }
}
