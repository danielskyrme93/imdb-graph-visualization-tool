package app.io;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Encapsulates information to be used by OMDb service
 *
 * @author Daniel Skyrme
 * @version %I%, %G%
 */
public class OMDbCommand {
    public enum CommandType{API_KEY, SHOW_BY_TITLE, SHOW_BY_ID, EPISODES_BY_TITLE_SEASON};
    private static String API_KEY_QUERY = "i=tt0303461";
    private static String SHOW_BY_TITLE_QUERY = "type=series&plot=full&s=<>";
    private static String SHOW_BY_ID_QUERY = "type=series&plot=full&i=<>";
    private static String EPISODES_BY_TITLE_SEASON_QUERY= "type=episode&plot=full&i=<>&season=<>";
    static final String IMDB_URL_PREIX = "http://www.omdbapi.com/?";

    private static final String CHARSET = "UTF-8";
    private String query;

    public OMDbCommand(String apiKey, CommandType commandType, String[] parameters) {
        this.query = "apikey=" + apiKey + "&";
        switch (commandType) {
            case API_KEY:
                this.query += API_KEY_QUERY;
                break;
            case SHOW_BY_TITLE:
                this.query += SHOW_BY_TITLE_QUERY;
                break;
            case SHOW_BY_ID:
                this.query += SHOW_BY_ID_QUERY;
                break;
            case EPISODES_BY_TITLE_SEASON:
                this.query += EPISODES_BY_TITLE_SEASON_QUERY;
                break;
        }

        if(parameters != null) {
            try {
                for(String param: parameters)
                    this.query = this.query.replaceFirst("<>", URLEncoder.encode(param, CHARSET));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    URL getURL() {
        String asStr = IMDB_URL_PREIX + query;
        URL url = null;
        try {
            url = new URL(asStr);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }
}
