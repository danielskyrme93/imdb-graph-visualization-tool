package app.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

/**
 * The data structure Season.
 *
 * @author Daniel Skyrme
 * @version %I%, %G%
 */
public class Season extends IMDbEntity {
    private static final String SEASON_QUERY = "/episodes?season=";
    private int seasonNum;

    public Season(String showId, int seasonNum) {
        super(showId, "Season " + seasonNum);
        this.seasonNum = seasonNum;
    }


    static Season getEmptySeason() {
        return new Season("", 0);
    }

    ArrayList<Episode> getEpisodes() {
        ArrayList<Episode> toRtn = (ArrayList) new ArrayList<IMDbEntity>(map.values());
        return toRtn;
    }

    void addEpisode(Episode e) {
        map.put(e.getEpisodeNum(), e);
    }

    private int size() {
        return map.keySet().size();
    }

    public int getMaxEpisode() {
        if (map.isEmpty())
            return 0;
        return Collections.max(map.keySet());
    }

    public Float getAverageRating() {
        if (map.isEmpty())
            return null;
        float total = 0;
        for (IMDbEntity nxt : map.values()) {
            total += nxt.getRating();
        }
        float avg = total / map.keySet().size();
        return roundTo2dp(avg);
    }

    public Float getStandardDeviation() {
        if (map.isEmpty())
            return null;
        Float avg = getAverageRating();
        float sum = 0;
        for (IMDbEntity ep : map.values()) {
            sum += Math.pow(ep.getRating() - avg, 2);
        }
        return roundTo2dp(Float.valueOf("" + (Math.sqrt(sum / map.values().size()))));
    }

    public ArrayList<Episode> getBestEpisodes() {
        ArrayList<Episode> toRtn = new ArrayList<>();
        double bestRating = 0;
        for (IMDbEntity ep : map.values()) {
            double rating = ep.getRating();
            if (rating == bestRating)
                toRtn.add((Episode) ep);
            else if (rating > bestRating) {
                toRtn.clear();
                toRtn.add((Episode) ep);
                bestRating = rating;
            }
        }
        return toRtn;
    }

    private Float roundTo2dp(Float f) {
        return Math.round(100 * f) / 100f;
    }


    @Override
    public LinkedHashMap<String, Object> getInfoStringMap() {
        LinkedHashMap<String, Object> map = new LinkedHashMap();
        map.put("Season", seasonNum);
        map.put("Episodes", size());

        ArrayList<Episode> bestEps = getBestEpisodes();
        StringBuilder bestEpTitles = new StringBuilder();
        for (Episode ep : bestEps)
            bestEpTitles.append(ep.toString()).append(", ");
        int len = bestEpTitles.length();
        if(bestEpTitles.length() > 2)
            bestEpTitles = bestEpTitles.delete(len - 2, len);
        map.put("Best Episodes", bestEpTitles.toString());
        if (id != null && !id.isEmpty()) {
            IMDbEntity.linkProperty(map, "Link", IMDB_URL_PREFIX + id + SEASON_QUERY + seasonNum);
        }
        return map;
    }

}
