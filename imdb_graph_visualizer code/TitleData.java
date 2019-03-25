package app.model;

import javafx.scene.control.ProgressBar;
import app.view.HeaderPane;
import app.view.MainView;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Retrieves title data for auto-complete
 *
 * @author Daniel Skyrme
 * @version %I%, %G%
 */
public class TitleData {
    private static final String RESOURCE_FOLDER_NAME = "res";
    private static final String TEMP_FILE_NAME = "TEMP_FILE_NAME";
    private static final String MOST_POPULAR_FILE_NAME = "popular_titles.txt";
    private static final String MOST_VOTED_FILE_NAME = "most_voted.txt";
    private static final int MAX_VOTED_ENTRIES = 3000;
    private static final int MAX_POPULAR_ENTRIES = 600;
    private static final String MOST_POPULAR_URL_AS_STR = "https://www.imdb.com/search/title?title_type=tv_series&countries=us&ref_=adv_nxt&start=";
    private static final String MOST_VOTED_URL_AS_STR = "https://www.imdb.com/search/title?title_type=tv_series&countries=us&ref_=adv_nxt&sort=num_votes,desc&start=";
    private static final String MATCHING_HTML_LINE_IDENTIFIER = "ref_=adv_li_tt";
    public static final String TITLE_SPLIT_REGEX = "[^\\w]*[\\s]+[^\\w]*"; // regex used to split title keywords
    private static String[] TOO_FREQUENT_IN_TITLES_KEYWORDS = {"the", "of", "and"}; // frequent words unhelpful to search
    private TreeMap<String, ArrayList<String>> titleKeywordToMatches; // map from keywords to titles containing it
    private HashSet<String> titles;
    private TreeSet<String> keywordsInTitles;

    public TitleData() {
        titles = new HashSet<>();
        titleKeywordToMatches = new TreeMap<>();
        keywordsInTitles = new TreeSet<>();
    }

    public static void updateFileTitleData(HeaderPane panel, ProgressBar progressBar) {
        updateFile(0, progressBar, MOST_POPULAR_URL_AS_STR, MAX_POPULAR_ENTRIES, MOST_POPULAR_FILE_NAME);
        updateFile(MAX_POPULAR_ENTRIES, progressBar, MOST_VOTED_URL_AS_STR, MAX_VOTED_ENTRIES, MOST_VOTED_FILE_NAME);
        panel.resetMisc();
    }

    private static void updateFile(int baseProgress, ProgressBar progressBar, String baseURL, int numOfTitles, String writeTo) {
        Path tempPath = Paths.get(RESOURCE_FOLDER_NAME + "/" + TEMP_FILE_NAME);
        Path writePath = Paths.get(RESOURCE_FOLDER_NAME + "/" + writeTo);
        try {
            PrintWriter writer = new PrintWriter(tempPath.toString());
            for(int i = 1; i <= numOfTitles; i+= 50 ) {
                URL url = new URL(baseURL + i);
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                String line;
                boolean marked = false;
                while ((line = reader.readLine()) != null) {
                    if (marked || line.contains(MATCHING_HTML_LINE_IDENTIFIER)) {
                        if(line.contains(">")) {
                            String toWrite = line.split(">")[1].replace("</a>", "")
                                    .replaceAll("\n", "")
                                    .replaceAll("</a", "")+ "\n";
                            writer.write(toWrite);
                        }
                        marked = !marked;
                    }
                }
                progressBar.setProgress(((double) i + baseProgress) / (MAX_POPULAR_ENTRIES + MAX_VOTED_ENTRIES));
            }
            writer.close();
            Files.copy(tempPath, writePath, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadTitleData() {
        loadTitleData(MOST_POPULAR_FILE_NAME);
        loadTitleData(MOST_VOTED_FILE_NAME);
    }

    private void loadTitleData(String txtFileName) {
        try {
            String relativePath = "./" + RESOURCE_FOLDER_NAME + "/" + txtFileName;
            String absPath = Paths.get(relativePath).toAbsolutePath().toString();
            BufferedReader titleReader = new BufferedReader(new FileReader(absPath));
            int count = 0;
            String line;

            int numOfTitlesToUse = 0;
            switch (txtFileName) {
                case MOST_POPULAR_FILE_NAME:
                    numOfTitlesToUse = MAX_POPULAR_ENTRIES;
                    break;
                case MOST_VOTED_FILE_NAME:
                    numOfTitlesToUse = MAX_VOTED_ENTRIES;
                    break;
            }

            while (count < numOfTitlesToUse && (line = titleReader.readLine()) != null) {
                if (!titles.contains(line)) {
                    titles.add(line);
                    count++;
                    overloop:
                    for (String word : line.split(TITLE_SPLIT_REGEX)) {
                        String wordAsLowerCase = word.toLowerCase();
                        for (String nonMatch : TOO_FREQUENT_IN_TITLES_KEYWORDS)
                            if (nonMatch.equalsIgnoreCase(wordAsLowerCase))
                                continue overloop;
                        if (!titleKeywordToMatches.containsKey(wordAsLowerCase)) {
                            ArrayList<String> newList = new ArrayList<>();
                            keywordsInTitles.add(wordAsLowerCase);
                            newList.add(line);
                            titleKeywordToMatches.put(wordAsLowerCase, newList);
                        } else {
                            titleKeywordToMatches.get(wordAsLowerCase).add(line);
                        }
                    }
                }
            }
        } catch (IOException e) {
            MainView.showErrorDialogue("Title data not found");
        }

    }

    public ArrayList<String> getAutoCompleteTitles(String input) {
        String inputAsLowerCase = input.toLowerCase();
        ArrayList<String> toRtn = new ArrayList<>();
        if (inputAsLowerCase.length() < 3)
            return toRtn;
        String[] words = inputAsLowerCase.split(TITLE_SPLIT_REGEX);
        for (String word : words) {
            if (titleKeywordToMatches.containsKey(word)) {
                ArrayList<String> listOf = titleKeywordToMatches.get(word);
                for (String w : listOf)
                    if (!toRtn.contains(w))
                        toRtn.add(w);
            }
        }
        boolean hasPartial = inputAsLowerCase.charAt(inputAsLowerCase.length() - 1) != ' ';
        String partial = words[words.length - 1];
        if (hasPartial && partial.length() >= 2) {
            String upperBoundOnMatch = getUpperBoundOnMatch(partial);
            SortedSet<String> matchingWords;
            if (upperBoundOnMatch.equals(""))
                matchingWords = keywordsInTitles.tailSet(partial);
            else
                matchingWords = keywordsInTitles.subSet(partial, upperBoundOnMatch);
            for (String word : matchingWords)
                for (String title : titleKeywordToMatches.get(word))
                    if (!toRtn.contains(title))
                        toRtn.add(title);
        }
        return toRtn;
    }

    private static String getUpperBoundOnMatch(String partial) {
        if (partial.equals(""))
            return "";
        String prefix = partial.substring(0, partial.length() - 1);
        char lastChar = partial.charAt(partial.length() - 1);
        if (lastChar == 'z')
            return getUpperBoundOnMatch(prefix);
        return prefix + ++lastChar;
    }

}
