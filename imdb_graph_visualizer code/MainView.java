package app.view;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import app.control.DataController;
import app.control.SettingsService;
import app.model.IMDbEntity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Observer;


/**
 * The frame containing the UI
 * @author Daniel Skyrme
 * @version %I%, %G%
 */
public class MainView extends Application {
    private Stage primaryStage;
    private BorderPane mainLayout;
    private TextField searchField;
    private ScrollPane graphScrollPane;
    private StackPane centerPane;
    private AutoBox autoBox;

    private ListSummaryView listSummaryView;

    private DataController iDataController;
    private boolean hasPopup = false;
    private StackPane totalLayout;
    private ArrayList<Observer> observers;

    @Override
    public void start(Stage pStage) {
        String apiKey = SettingsService.getAPIKey();
        hasPopup = false;
        // Setup scenes and stages
        primaryStage = pStage;
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setIconified(true);
        primaryStage.getIcons().add(new Image("file:res/IMGraph.png"));
        primaryStage.setTitle("IMDb Graph Visualizer");
        primaryStage.setWidth(Screen.getPrimary().getVisualBounds().getWidth());
        primaryStage.setHeight(Screen.getPrimary().getVisualBounds().getHeight());
        primaryStage.setMaximized(true);

        mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("frame-border");
        totalLayout = new StackPane();
        totalLayout.getChildren().add(mainLayout);

        Scene mainScene = new Scene(totalLayout, primaryStage.getWidth(), primaryStage.getHeight());
        mainScene.getStylesheets().add("file:" +"res/style/main_style.css");
        primaryStage.setScene(mainScene);


        // Setup GUI and event handlers
        observers = new ArrayList<>();
        initSearchBar();
        initDataArea();
        setActions();
        primaryStage.show();
        iDataController = new DataController(getObserverComponents(), apiKey);
        processSearchRequest("The Twilight Zone", true);
    }

    private void initSearchBar() {
        HeaderPane headerPane = new HeaderPane(this);
        searchField = headerPane.getSearchField();
        mainLayout.setTop(headerPane);
        headerPane.toFront();
        observers.add(headerPane);
    }

    private void initDataArea() {
        listSummaryView = new ListSummaryView(this);
        GraphPane graphPane = new GraphPane(this);
        observers.add(listSummaryView);
        observers.add(graphPane);

        mainLayout.setCenter(centerPane);
        mainLayout.setRight(listSummaryView);

        centerPane = new StackPane();

        graphScrollPane = new ScrollPane();
        graphScrollPane.fitToWidthProperty().setValue(true);
        graphScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        graphScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        graphScrollPane.setFocusTraversable(true);
        graphScrollPane.getStyleClass().add("ui-region");
        graphScrollPane.setContent(graphPane);

        primaryStage.widthProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() < 0.6 * Screen.getPrimary().getVisualBounds().getWidth()) {
                mainLayout.setCenter(null);
                listSummaryView.setPrefWidth(newValue.intValue());
            } else if (mainLayout.getCenter() == null) {
                mainLayout.setCenter(centerPane);
                listSummaryView.setPrefWidth(listSummaryView.minWidth);
            }
        });

        autoBox = new AutoBox();
        centerPane.setAlignment(Pos.TOP_LEFT);
        centerPane.getChildren().addAll(graphScrollPane, autoBox);
        autoBox.setActive(false);
    }

    private void setActions() {
        totalLayout.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE && hasPopup) {
                hidePopUpWindow();
            }
        });
        mainLayout.addEventFilter(MouseEvent.ANY, event -> {
            if (hasPopup) {
                if (event.getEventType().equals(MouseEvent.MOUSE_CLICKED))
                    hidePopUpWindow();
                event.consume();
            }
        });

        searchField.setOnAction(this::handleSearchAction);

        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DOWN) {
                autoBox.changeSelectedBy(1);
            } else if (event.getCode() == KeyCode.UP)
                autoBox.changeSelectedBy(-1);
        });
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals("")) {
                autoBox.setActive(false);
            } else {
                ArrayList<String> autcCompleteOptions = iDataController.getAutoCompleteTitles(newValue);
                autoBox.setItems(
                        FXCollections.observableList(autcCompleteOptions.subList(0, Math.min(12, autcCompleteOptions.size()))));
            }
        });

        searchField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && !searchField.getText().equals("")) {
                autoBox.setActive(true);
            } else if (listSummaryView.isFocused()) {
                autoBox.setActive(false);
            }
        });
        ListView<String> autoCompleteList = autoBox.getAutoCompleteList();
        autoCompleteList.setOnMouseClicked(event -> processSearchRequest(autoCompleteList.getSelectionModel().getSelectedItem(), true));

        centerPane.setOnMouseClicked(event -> {
            if (autoCompleteList.getSelectionModel().getSelectedItems().size() == 0) {
                autoBox.setActive(false);
            }
        });

    }

    void processSearchRequest(String searchTerm, boolean byTitle) {
        if (searchTerm != null) {
            autoBox.setActive(false);
            graphScrollPane.setVvalue(0);
            searchField.setText("");
            if (byTitle) {
                iDataController.updateShowByTitle(searchTerm.replace("[^\\w&^\\d]", ""));
            } else
                iDataController.updateShowById(searchTerm);
            graphScrollPane.requestFocus();
        }
    }

    private ArrayList<Observer> getObserverComponents() {
        return observers;
    }

    void showPopUpWindow(IMDbEntity entity) {
        Platform.runLater(() -> {
            BlurTransition blurTransition = new BlurTransition(mainLayout);
            blurTransition.play();
        });
        LinkedHashMap<String, Object> toShow = entity.getInfoStringMap();
        double popUpWidth = 0.4 * primaryStage.getWidth();
        double popUpHeight = 0.5 * primaryStage.getHeight();
        PopUpWindow wind = new PopUpWindow(toShow,
                popUpWidth);
        wind.setOpacity(0);
        Platform.runLater(() -> {
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(300), wind);
            fadeTransition.setInterpolator(Interpolator.EASE_OUT);
            fadeTransition.setFromValue(0);
            fadeTransition.setToValue(1);
            fadeTransition.play();
        });
        wind.setTranslateX(-listSummaryView.getWidth() / 2);
        totalLayout.getChildren().add(new Group(wind));
        hasPopup = true;
    }

    private void hidePopUpWindow() {
        mainLayout.setEffect(null);
        totalLayout.getChildren().clear();
        totalLayout.getChildren().add(mainLayout);
        hasPopup = false;
    }

    private void handleSearchAction(ActionEvent e) {
        String txt = autoBox.isSelected() ? autoBox.getAutoCompleteList().getSelectionModel().getSelectedItem() : searchField.getText();
        processSearchRequest(txt, true);
    }

    public static void showErrorDialogue(String toShow) {
        Alert dialog = new Alert(Alert.AlertType.ERROR);
        dialog.setContentText(toShow);
        dialog.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }

    class BlurTransition extends Transition {
        private static final double MAX_BLUR = 10;
        private static final double DURATION_MS = 300;
        private Node node;
        private BoxBlur blurEffect;

        BlurTransition(Node node) {
            this.node = node;
            blurEffect = new BoxBlur();
            blurEffect.setWidth(0);
            blurEffect.setHeight(0);
            setCycleDuration(Duration.millis(DURATION_MS));
            setInterpolator(Interpolator.EASE_OUT);
        }

        @Override
        protected void interpolate(double frac) {
            blurEffect.setWidth(frac * MAX_BLUR);
            blurEffect.setHeight(frac * MAX_BLUR);
            node.setEffect(blurEffect);
        }

    }

}