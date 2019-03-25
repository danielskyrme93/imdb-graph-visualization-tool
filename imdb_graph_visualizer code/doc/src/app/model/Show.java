package app.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

/**
 * Data stucture of TV Show.
 * @author Daniel Skyrme
 * @version %I%, %G%
 */
public class Show extends IMDbEntity {
    private final String yearsActive;
    private int announcedSeasons;
    private String numOfVotes;
    private String genres;

    public Show(String title, String id, String yearsActive, int announcedSeasons) {
        super(id, title);
        this.yearsActive = yearsActive;
        this.numOfVotes = "N/A";
        this.genres = "N/A";
        this.announcedSeasons = announcedSeasons;
    }


    public static Show getMessagedShow(String message) {
        return new Show(message, "", "", 0);
    }

    public int getAnnouncedSeasons() {
        return announcedSeasons;
    }

    public void setNumOfVotes(String numOfVotes) {
        this.numOfVotes = numOfVotes;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public void addEpisode(String showId, Episode e) {
        int seasonNum = e.getSeasonNum();
        if (!map.containsKey(seasonNum))
            map.put(seasonNum, new Season(showId, seasonNum));
        ((Season) map.get(seasonNum)).addEpisode(e);
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public ArrayList<Episode> getEpisodes() {
        ArrayList<Episode> toRtn = new ArrayList<>();
        for (IMDbEntity season : map.values()) {
            toRtn.addAll(((Season) season).getEpisodes());
        }
        return toRtn;
    }

    public int getNumOfSeasons() {
        if (map.isEmpty())
            return 0;
        return Collections.max(map.keySet());
    }

    public float getMinRating() {
        float minRating = 10;
        for (Episode ep : getEpisodes()) {
            if (ep.getRating() < minRating)
                minRating = ep.getRating();
        }
        return minRating;
    }

    private ArrayList<Episode> getBestEpisodes() {
        ArrayList<Episode> toRtn = new ArrayList<>();
        double bestRating = 0;
        for (IMDbEntity entity : map.values()) {
            Season season = (Season) entity;
            ArrayList<Episode> eps = season.getBestEpisodes();
            double currentRating = eps.get(0).getRating();
            if(currentRating >= bestRating) {
                if(currentRating > bestRating) {
                    bestRating = currentRating;
                    toRtn.clear();
                }
                toRtn.addAll(eps);
            }
        }
        return toRtn;
    }

    public String getTitle() {
        return title;
    }

    public String getYearsActive() {
        return yearsActive;
    }

    public String toString() {
        return title + "\t" + id + "\t" + getNumOfSeasons() + " seasons\t";
    }

    public Season getSeason(int seasonNum) {
        Season season = (Season) map.get(seasonNum);
        if (season == null)
            return Season.getEmptySeason();
        return season;
    }

    public String getId() {
        return id;
    }

    @Override
    public LinkedHashMap<String, Object> getInfoStringMap() {
        LinkedHashMap map = new LinkedHashMap();
        map.put("Title", title);
        map.put("Rating", rating);
        map.put("Num of Votes", numOfVotes);

        map.put("Total Seasons", map.keySet().size());
        map.put("Genre", genres);

        ArrayList<Episode> bestEps = getBestEpisodes();
        StringBuilder bestEpTitles = new StringBuilder();
        for (Episode ep : bestEps)
            bestEpTitles.append(ep.toString()).append(", ");
        int len = bestEpTitles.length();
        if(bestEpTitles.length() > 2)
            bestEpTitles = bestEpTitles.delete(len - 2, len);
        map.put("Best Episodes", bestEpTitles.toString());

        map.put("Link", IMDB_URL_PREFIX + id);
        return map;
    }
}