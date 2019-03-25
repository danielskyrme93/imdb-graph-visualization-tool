package app.control;

import app.io.OMDbCommand;
import app.io.OMDbService;
import app.model.DataModel;
import app.model.Episode;
import app.model.Show;
import app.model.TitleData;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observer;


/**
 * Receives queries for the OMDb API and manipulates the data model
 *
 * @author Daniel Skyrme
 * @version %I%, %G%
 */
public class DataController {
    private DataModel dataModel;
    private TitleData titleData;
    private String apiKey;

    static boolean authenticateApiKey(String apiKey) {
        OMDbCommand apiKeyCommand = new OMDbCommand(apiKey, OMDbCommand.CommandType.API_KEY, null);
        JSONObject json = OMDbService.getJSONFromURL(apiKeyCommand);
        return json != null &&
                !((String) json.get("Response")).equalsIgnoreCase("False");
    }

    public DataController(ArrayList<Observer> views, String apiKey) {
        this.apiKey = apiKey;
        dataModel = new DataModel(views);
        // loads title data for use in autocomplete
        titleData = new TitleData();
        titleData.loadTitleData();
    }

    private Show jsonToShow(JSONObject json) {
        String id = (String) json.get("imdbID");
        String title = (String) json.get("Title");
        String yearsActive = ((String) json.get("Year")).replace("\\u", "-");
        String rating = ((String) json.get("imdbRating"));
        String numOfAnnouncedStr = (String) json.get("totalSeasons");
        String numOfVotesStr = (String) json.get("imdbVotes");
        String genreStr = (String) json.get("Genre");
        int numOfAnnounced = numOfAnnouncedStr.matches("[0-9]+") ? Integer.valueOf(numOfAnnouncedStr) : 0;
        Show toRtn = new Show(title, id, yearsActive, numOfAnnounced);
        toRtn.setRating(rating.matches("^([+-]?\\d*\\.?\\d*)$") ? Float.valueOf(rating) : Float.NaN);
        toRtn.setNumOfVotes(numOfVotesStr);
        toRtn.setGenres(genreStr);
        return toRtn;
    }

    public void updateShowById(String id) {
        Show updatedShow;
        if (id == null) {
            updatedShow = Show.getMessagedShow("Show not found");
        } else {
            OMDbCommand showCommand = new OMDbCommand(apiKey,
                    OMDbCommand.CommandType.SHOW_BY_ID,
                    new String[]{id});
            JSONObject json = OMDbService.getJSONFromURL(showCommand);
            if (json == null)
                return;
            updatedShow = jsonToShow(json);
            addEpisodesToShow(updatedShow, updatedShow.getAnnouncedSeasons());
        }
        dataModel.setShow(updatedShow);
    }

    public void updateShowByTitle(String title) {
        dataModel.clearAlternatives();
        if (title.equals(""))
            dataModel.setShow(Show.getMessagedShow(""));
        String id = searchForId(title);
        updateShowById(id);
    }

    public ArrayList<String> getAutoCompleteTitles(String input) {
        return titleData.getAutoCompleteTitles(input);
    }

    private String searchForId(String title) {
        ArrayList<String> words = new ArrayList<>(Arrays.asList(title.split(TitleData.TITLE_SPLIT_REGEX)));
        while (words.size() > 0) {
            StringBuilder guess = new StringBuilder();
            for (String w : words)
                guess.append(w).append(" ");
            guess = new StringBuilder(guess.substring(0, guess.length() - 1));
            OMDbCommand showCommand = new OMDbCommand(apiKey,
                    OMDbCommand.CommandType.SHOW_BY_TITLE,
                    new String[]{guess.toString()});
            JSONObject json = OMDbService.getJSONFromURL(showCommand);
            if (json == null || ((String) json.get("Response")).equalsIgnoreCase("False")) {
                words.remove(words.size() - 1);
            } else {
                JSONArray allMatches = json.getJSONArray("Search");
                JSONObject bestMatch = (JSONObject) allMatches.get(0);
                for (int i = 0; i < allMatches.length(); i++) {
                    JSONObject currentMatch = ((JSONObject) allMatches.get(i));
                    String currentTitle = (String) currentMatch.get("Title");
                    String currentId = (String) currentMatch.get("imdbID");
                    String currentYear = (String) currentMatch.get("Year");
                    dataModel.addAlternative(currentTitle + " (" + currentYear + ")", currentId);
                }
                return (String) bestMatch.get("imdbID");

            }
        }
        return null;
    }

    private void addEpisodesToShow(Show show, int numOfSeasons) {
        for (int i = 1; i <= numOfSeasons; i++) {
            String[] params ={show.getId(), String.valueOf(i)};
            OMDbCommand episodeCommand = new OMDbCommand(apiKey, OMDbCommand.CommandType.EPISODES_BY_TITLE_SEASON, params);
            JSONObject json = OMDbService.getJSONFromURL(episodeCommand);
            if (((String) json.get("Response")).equalsIgnoreCase("False"))
                continue;
            JSONArray jsonEps = (JSONArray) json.get("Episodes");
            for (int j = 0; j < jsonEps.length(); j++) {
                json = jsonEps.getJSONObject(j);
                String epTitle = (String) json.get("Title");
                String epNum = (String) json.get("Episode");
                String epId = (String) json.get("imdbID");
                String ratingAsStr = (String) json.get("imdbRating");
                if (ratingAsStr.matches("\\d+\\.\\d*")) {
                    float epRating = Float.valueOf((String) json.get("imdbRating"));
                    Episode ep = new Episode(epTitle, epId, i, Integer.valueOf(epNum), epRating);
                    show.addEpisode(show.getId(), ep);
                    ep.setReleaseDate((String) json.get("Released"));

                }
            }
        }
    }


}
