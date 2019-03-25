package app.model;

import java.util.LinkedHashMap;

/**
 * Data structure of episode
 *
 * @author Daniel Skyrme
 * @version %I%, %G%
 */
public class Episode extends IMDbEntity implements Comparable {
    private String releaseDate;
    private int seasonNum;
    private int episodeNum;


    public Episode(String title, String id, int seasonNum, int episodeNum, Float rating) {
        super(id, title);
        this.seasonNum = seasonNum;
        this.episodeNum = episodeNum;
        this.rating = rating;
        this.releaseDate = null;
    }

    @Override
    public int compareTo(Object o) {
        Episode other = (Episode) o;
        int seasonDiff = other.seasonNum - seasonNum;
        if (seasonDiff < 0) {
            return 1;
        } else if (seasonDiff > 0) {
            return -1;
        } else {
            int episodeDiff = other.episodeNum - episodeNum;
            if (episodeDiff < 0) {
                return 1;
            } else if (episodeDiff > 0) {
                return -1;
            }
        }
        return 0;
    }

    public Float getRating() {
        return rating;
    }


    public String getTitle() {
        return title;
    }

    public int getSeasonNum() {
        return seasonNum;
    }

    public int getEpisodeNum() {
        return episodeNum;
    }


    public void setReleaseDate(String date) {
        releaseDate = date;
    }

    @Override
    public LinkedHashMap<String, Object> getInfoStringMap() {
        LinkedHashMap map = new LinkedHashMap();
        IMDbEntity.linkProperty(map, "Title", title);
        IMDbEntity.linkProperty(map, "Season", seasonNum);
        IMDbEntity.linkProperty(map, "Episode", episodeNum);
        IMDbEntity.linkProperty(map, "Rating", rating);
        IMDbEntity.linkProperty(map, "Released", releaseDate);
        if (title != null)
            IMDbEntity.linkProperty(map, "Link", IMDB_URL_PREFIX + id);
        return map;
    }

    public String toString() {
        return title;

    }
}