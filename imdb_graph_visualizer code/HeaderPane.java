package app.view;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import app.control.SettingsService;
import app.model.DataModel;
import app.model.TitleData;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;


/**
 * The header of the frame
 * Contains search field, alternatives box showing disambiguations, and buttons for updating api key and title data
 * @author Daniel Skyrme
 * @version %I%, %G%
 */
public class HeaderPane extends BorderPane implements Observer {
    private TextField searchField;
    private MainView parent;
    private HBox centerPane;
    private HBox rightRegion = null;
    private ChoiceBox<String> alternativesBox;

    HeaderPane(MainView parent) {
        super();
        this.parent = parent;

        setMinHeight(Screen.getPrimary().getVisualBounds().getHeight() / 20);
        getStyleClass().add("ui-region");
        initSearchRegion();

        initChoiceRegion();

        initMiscRegion();
    }

    private void initSearchRegion() {
        double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
        double fieldWidth = 0.14 * screenWidth;
        double searchLblWidth = 0.08 * screenWidth;
        HBox leftRegion = new HBox();

        leftRegion.setAlignment(Pos.CENTER_LEFT);
        leftRegion.getStyleClass().add("search-region");

        Label searchLbl = new Label("Enter TV Show: ");
        searchLbl.setPrefWidth(searchLblWidth);
        leftRegion.getChildren().add(searchLbl);

        searchField = new TextField();
        searchField.setPrefWidth(fieldWidth);
        leftRegion.getChildren().add(searchField);

        Button searchBtn = new Button("Search");
        searchBtn.setCursor(Cursor.HAND);
//        searchBtn.setPrefWidth(btnWidth);
//        searchBtn.setMinWidth(btnWidth);
        searchBtn.setFocusTraversable(false);
        searchBtn.setOnAction(e -> parent.processSearchRequest(searchField.getText(), true));
        leftRegion.getChildren().add(searchBtn);

        setLeft(leftRegion);
        leftRegion.toFront();
    }

    private void initChoiceRegion() {
        centerPane = new HBox();
        centerPane.getStyleClass().add("choice-region");
        centerPane.setAlignment(Pos.CENTER);
        centerPane.setPadding(new Insets(0,100,0,60));
        setCenter(centerPane);
    }

    private void updateChoiceRegion(DataModel dataModel, boolean hasResetChoices) {
        if(hasResetChoices) {
            ArrayList<String> alternatives = dataModel.getAlternatives();
            alternativesBox = new ChoiceBox<>(FXCollections.observableList(alternatives));
            alternativesBox.setPrefWidth(0.3 *Screen.getPrimary().getVisualBounds().getWidth());
            centerPane.getChildren().clear();
            centerPane.getChildren().add(alternativesBox);
            alternativesBox.getSelectionModel().select(0);
            alternativesBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if(oldValue == null || !oldValue.equals(newValue)) {
                    String id = dataModel.getId(newValue);
                    parent.processSearchRequest(id, false);
                }
            });
        }
    }

    private void initMiscRegion() {
        if(rightRegion == null) {
            rightRegion = new HBox();
            rightRegion.getStyleClass().add("misc-region");
            rightRegion.setSpacing(20);
            rightRegion.setAlignment(Pos.CENTER_RIGHT);
        }
        rightRegion.getChildren().clear();

        double buttonWidth = Screen.getPrimary().getVisualBounds().getWidth() / 12;
        Button apiButton = new Button("API Key");
        apiButton.setAlignment(Pos.CENTER_RIGHT);
        apiButton.setOnAction(event -> SettingsService.setAPIKey());
        apiButton.setFocusTraversable(false);
        apiButton.setPrefWidth(buttonWidth);
        rightRegion.getChildren().add(apiButton);

        Button updateTitlesButton = new Button("Update Titles");
        updateTitlesButton.setAlignment(Pos.CENTER_RIGHT);
        updateTitlesButton.setOnAction(event -> showUpdateFileContext());
        updateTitlesButton.setFocusTraversable(false);
        updateTitlesButton.setPrefWidth(buttonWidth);
        rightRegion.getChildren().add(updateTitlesButton);
        if(getRight() == null)
            setRight(rightRegion);
    }

    private void showUpdateFileContext() {
        ProgressBar progressBar = new ProgressBar();

        progressBar.setVisible(true);
        progressBar.setProgress(0);
        progressBar.getStyleClass().add("api-progress");
        Platform.runLater(() ->  {
            rightRegion.getChildren().clear();
            rightRegion.getChildren().add(progressBar);
            progressBar.setPrefWidth(Screen.getPrimary().getVisualBounds().getWidth() / 6);
        });
        new Thread(() -> {
            TitleData.updateFileTitleData(this, progressBar);
        }).start();
    }

    public void resetMisc() {
        Platform.runLater(() ->{
            Alert loadingCompleteAlert = new Alert(Alert.AlertType.INFORMATION);
            loadingCompleteAlert.setTitle("Title Loading Completed");
            loadingCompleteAlert.setHeaderText(null);
            loadingCompleteAlert.setContentText("Restart to view new titles");
            loadingCompleteAlert.showAndWait();
            initMiscRegion();
        });
    }

    TextField getSearchField() {
        return searchField;
    }

    @Override
    public void update(Observable o, Object arg) {
        DataModel dataModel = (DataModel) o;
        boolean toResetChoices = (boolean) arg;
        updateChoiceRegion(dataModel, toResetChoices);
    }
}
