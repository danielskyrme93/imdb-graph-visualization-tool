package app.control;

import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.FlowPane;
import app.view.MainView;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.Preferences;

/**
 * Mediates updates and retrievals from the Java preferences data
 * @author Daniel Skyrme
 * @version %I%, %G%
 */
public class SettingsService {
    private static final String OMDB_LINK = "http://www.omdbapi.com/apikey.aspx";
    private static Preferences preferences = Preferences.userRoot().node(System.getProperty("user.dir") + "\\res");
    private static String NO_KEYWORD = "key not found";

    private static String promptForKey() {
        AtomicReference<String> toRtn = new AtomicReference<>();
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("IMDb Visualization");
        dialog.setHeaderText("Key Configuration");
        dialog.setContentText("Please enter your API key: ");
        FlowPane fp = new FlowPane();
        Hyperlink hl = new Hyperlink(OMDB_LINK);
        hl.setOnMouseClicked(event -> {
            try {
                URI uri = new URI(OMDB_LINK);
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(uri);
                }
            } catch (URISyntaxException | IOException e) {
                e.printStackTrace();
            }
        });
        fp.getChildren().add(hl);
        dialog.getDialogPane().setExpandableContent(fp);
        dialog.getDialogPane().setExpanded(true);
        Optional<String> inputVal = dialog.showAndWait();
        inputVal.ifPresent(toRtn::set);
        if (!DataController.authenticateApiKey(toRtn.get())) {
            MainView.showErrorDialogue("Invalid API Key");
            toRtn.set(NO_KEYWORD);
        }
        return toRtn.get();
    }

    public static String getAPIKey() {
        String apiKey = preferences.get("api_key", NO_KEYWORD);
        while (apiKey.equals(NO_KEYWORD)) {
            apiKey = promptForKey();
        }
        preferences.put("api_key", apiKey);
        return apiKey;
    }

    public static void setAPIKey() {
        String apiKey = NO_KEYWORD;
        preferences.put("api_key", NO_KEYWORD);
        while (apiKey.equals(NO_KEYWORD)) {
            apiKey = promptForKey();
        }
    }

    private static void clearPreferences() {
        String apiKey = preferences.get("api_key", NO_KEYWORD);
        preferences.put("api_key", NO_KEYWORD);
        System.out.println("Preferences cleared");
        System.out.println("Code was " + apiKey);
    }

    public static void main(String[] args) {
        clearPreferences();
    }

}
