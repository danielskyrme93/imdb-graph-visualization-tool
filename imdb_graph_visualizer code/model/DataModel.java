package app.model;


import java.util.*;

/**
 * Contains the data of current show displayed and alternatives associated with it
 * @author Daniel Skyrme
 * @version %I%, %G%
 */
public class DataModel extends Observable {
    private Show show;
    private LinkedHashMap<String, String> alternativeMap;
    private boolean hasCleared;

    public DataModel(ArrayList<Observer> views) {
        show = null;
        alternativeMap = new LinkedHashMap<>();
        if (views != null)
            for (Observer o : views)
                addObserver(o);
    }

    public Show getShow() {
        return show;
    }

    public void setShow(Show show) {
        this.show = show;
        setChanged();
        notifyObservers(hasCleared);
        hasCleared = false;
    }

    public void clearAlternatives() {
        alternativeMap.clear();
        hasCleared = true;
    }

    public void addAlternative(String titleYear, String id) {
        if(alternativeMap.containsKey(titleYear))
            return;
        alternativeMap.put(titleYear, id);
    }

    public String getId(String key) {
        return alternativeMap.get(key);
    }

    public ArrayList<String> getAlternatives() {
        return new ArrayList<>(alternativeMap.keySet());
    }
}
