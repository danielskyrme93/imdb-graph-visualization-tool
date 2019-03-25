package app.view;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import app.model.DataModel;
import app.model.Episode;
import app.model.IMDbEntity;
import app.model.Show;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

/**
 * Shows a list of episodes as a sidebar for the given show
 * @author Daniel Skyrme
 * @version %I%, %G%
 */
public class ListSummaryView extends VBox implements Observer {
    private static final int EPISODE_TITLE_LIMIT = 40;
    public final double minWidth = 0.18 * Screen.getPrimary().getVisualBounds().getWidth();
    private MainView parent;
    private TreeView<String> treeView;
    private HashMap<TreeItem<String>, IMDbEntity> treeItemToEpisode;

    ListSummaryView(MainView parent) {
        super();

        this.parent = parent;
        treeItemToEpisode = new HashMap<>();

        setFocusTraversable(false);

        setPrefWidth(minWidth);
        getStyleClass().add("data-summary-region");

        treeView = new TreeView<>();
        setVgrow(treeView, Priority.ALWAYS);
        TreeItem<String> root = new TreeItem<>();
        root.setExpanded(true);

        treeView.setRoot(root);
        treeView.setShowRoot(false);
        getChildren().addAll(treeView);
    }

    private void updateGUI(DataModel dataModel, boolean hasResetChoices) {
        getChildren().clear();
        Show show = dataModel.getShow();


        treeItemToEpisode.clear();
        int numOfSeasons = show.getNumOfSeasons();
        TreeItem<String> root = new TreeItem<>();
        treeView.setRoot(root);
        TreeItem[] treeSrcs = new TreeItem[numOfSeasons];
        for (int i = 0; i < numOfSeasons; i++) {
            TreeItem<String> seasonTreeItem = new TreeItem<>("Season " + (i + 1));
            seasonTreeItem.setExpanded(false);

            root.getChildren().add(seasonTreeItem);
            treeSrcs[i] = seasonTreeItem;
            treeItemToEpisode.put(seasonTreeItem, dataModel.getShow().getSeason(i+1));
        }

        for (Episode episode : show.getEpisodes()) {
            if (episode.getSeasonNum() > treeSrcs.length) {
                continue;
            }
            String title = episode.getTitle();
            DecimalFormat df = new DecimalFormat("##.#");
            if (title.length() >= EPISODE_TITLE_LIMIT) {
                title = title.substring(0, EPISODE_TITLE_LIMIT) + "...";
            }
            TreeItem<String> item = new TreeItem<>(title
                    + " - " + df.format(episode.getRating()));
            treeSrcs[episode.getSeasonNum() - 1].getChildren().add(item);
            treeItemToEpisode.put(item, episode);
        }
        treeView.setOnMouseClicked(event -> {
            if(treeView.getSelectionModel().getSelectedItem() != null && event.getClickCount() == 2)
                parent.showPopUpWindow(treeItemToEpisode.get(treeView.getSelectionModel().getSelectedItem()));
            event.consume();
        });
        getChildren().addAll(treeView);
    }


    @Override
    public void update(Observable o, Object arg) {
        DataModel dataModel = (DataModel) o;
        boolean toResetChoices = (boolean) arg;
        updateGUI(dataModel, toResetChoices);
    }
}
