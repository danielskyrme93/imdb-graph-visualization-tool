package app.view;

import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.Toolkit;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.util.Duration;
import app.model.DataModel;
import app.model.Episode;
import app.model.Season;
import app.model.Show;
import app.util.TVChartBuilder;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The type Graph pane.
 * @author Daniel Skyrme
 * @version %I%, %G%
 */
class GraphPane extends VBox implements Observer {
    private static final float DELTA_PER_DRAG = 0.1f;
    private static final float MAXIMUM_SMALLEST_Y = 8.0f;
    private static final float MINIMUM_SMALLEST_Y = 1.0f;
    private static final double LABEL_MARGIN = 15;
    private static final int CHART_SYMBOL_ENTRANCE_DISPLACEMENT = 300;
    private static final double HOVER_NODE_SCALE = 1.6;
    private static final int CHART_LABEL_PADDING = 10;
    private final int gridsPerRow;
    private float graphLowerBound = 0;
    private double deltaOnCurrentDrag = 0f;
    private MainView mainView;
    private Label titleLbl;
    private GridPane gridLayout;
    private int numOfCharts;

    private LineChart[] allCharts;
    private XYChart.Series[] allSeries;
    private ImageCursor dotCursor;
    private ExecutorService ex;

    /**
     * Pane that shows graphs in a grid
     *
     * @author Daniel Skyrme
     * @version %I%, %G%
     */
    GraphPane(MainView mainView) {
        super();
        this.mainView = mainView;
        if(Screen.getPrimary().getVisualBounds().getWidth() < 1445)
            this.gridsPerRow = 2;
        else
            this.gridsPerRow = 3;
        ex = Executors.newSingleThreadExecutor();
        getStyleClass().add("ui-region");
        setMinHeight(Region.USE_PREF_SIZE);
        numOfCharts = 0;

        titleLbl = new Label("");
        titleLbl.setCursor(Cursor.HAND);
        titleLbl.setAlignment(Pos.CENTER);
        titleLbl.getStyleClass().add("ui-region");
        titleLbl.getStyleClass().add("show-title");
        setOnHoverHighlight(titleLbl);

        gridLayout = new GridPane();
        gridLayout.setAlignment(Pos.TOP_CENTER);
        gridLayout.setMinHeight(Screen.getPrimary().getVisualBounds().getHeight());
        gridLayout.getStyleClass().addAll("ui-region", "graph-region");
        gridLayout.setHgap(0);
        gridLayout.setVgap(0);

        setAlignment(Pos.CENTER);
        setFocusTraversable(false);
        setFillWidth(true);

        getChildren().addAll(titleLbl, gridLayout);

        Circle circle = new Circle(2.5, Color.WHITE);
        SnapshotParameters snapshotParameters = new SnapshotParameters();
        snapshotParameters.setFill(Color.TRANSPARENT);
        Image img = circle.snapshot(snapshotParameters, null);
        dotCursor = new ImageCursor(img, img.getWidth() / 2, img.getHeight() / 2);
        setOnMouseReleased(event -> deltaOnCurrentDrag = 0f);
    }

    private void updateGUI(Show show) {
        if(!ex.isTerminated())
            ex.shutdownNow();
            ex = Executors.newSingleThreadExecutor();
        int numOfSeasons = show.getNumOfSeasons();
        numOfCharts = 0;
        String title = show.getTitle();
        String years = show.getYearsActive();
        titleLbl.setText(title + (years.equals("") ? "" : " (" + years + ")"));
        getTitleLabel().setOnMouseClicked(e -> mainView.showPopUpWindow(show));

        gridLayout.getChildren().clear();

        allSeries = new XYChart.Series[numOfSeasons];
        allCharts = new LineChart[numOfSeasons];
        graphLowerBound = (float) Math.floor( 2.0 * Math.max(show.getMinRating() - 0.5f, MINIMUM_SMALLEST_Y)) / 2;

        for (int i = 0; i < numOfSeasons; i++) {
            allSeries[i] = new XYChart.Series();

            Season season = show.getSeason(i + 1);

            allCharts[i] = makeLineChart("Season " + (i + 1), season.getMaxEpisode(), show.getSeason(i + 1));
            Float avg = season.getAverageRating();

            String avgPostfix = "";
            String sdPostfix = "";

            if (avg != null) {
                avgPostfix = "µ = " + avg;
                Float standardDeviation = season.getStandardDeviation();
                sdPostfix = ", σ = " + standardDeviation;
            }
            allCharts[i].getXAxis().setLabel(avgPostfix + sdPostfix);

            //noinspection unchecked
            allCharts[i].getData().add(allSeries[i]);
            addChart(allCharts[i]);
        }


        Platform.runLater(() -> {
            HashMap<Node, Episode> symbolToEpisode = new HashMap<>();
            HashMap<Integer, ArrayList<Node>> seasonToTitle = new HashMap<>();
            for (Episode episode : show.getEpisodes()) {
                    XYChart.Data<String, Number> data = new XYChart.Data<>(String.valueOf(episode.getEpisodeNum()), episode.getRating());

                    allSeries[episode.getSeasonNum() - 1].getData().add(data);
                    Node chartSymbol = data.getNode();
                    chartSymbol.setCacheHint(CacheHint.SPEED);
                    chartSymbol.setPickOnBounds(false);
                    chartSymbol.setVisible(false);
                    chartSymbol.getStyleClass().add("graph-node");
                    chartSymbol.toFront();
                    chartSymbol.setCursor(dotCursor);
                    symbolToEpisode.put(chartSymbol, episode);
                    seasonToTitle.computeIfAbsent(episode.getSeasonNum(), k -> new ArrayList<>());
                    seasonToTitle.get(episode.getSeasonNum()).add(chartSymbol);
            }
            Random rand = new Random();
            for (Node chartSymbol : symbolToEpisode.keySet()) {
                chartSymbol.setTranslateY(rand.nextBoolean() ? CHART_SYMBOL_ENTRANCE_DISPLACEMENT : -CHART_SYMBOL_ENTRANCE_DISPLACEMENT);
                chartSymbol.setOnMouseClicked(event -> {
                    removeChartSymbolHoverEffect(chartSymbol);
                    mainView.showPopUpWindow(symbolToEpisode.get(chartSymbol));
                });
                chartSymbol.setOnMouseEntered(event -> {
                    if(deltaOnCurrentDrag == 0f) {
                        hilightNode(chartSymbol, symbolToEpisode.get(chartSymbol).getTitle());
                    }
                });
                chartSymbol.setOnMouseExited(event -> removeChartSymbolHoverEffect(chartSymbol));
                chartSymbol.setOnMousePressed(event -> removeChartSymbolHoverEffect(chartSymbol));
            }
            for (int i = 1; i <= numOfSeasons; i++) {
                if (seasonToTitle.get(i) == null)
                    continue;
                for (Node chartSymbol : seasonToTitle.get(i)) {
                    if (chartSymbol == null)
                        continue;
                    ex.execute(() -> {
                        Platform.runLater(() -> {
                            applyGraphNodeInAnimation(chartSymbol, 1);
                        });
                        try {
                            Thread.sleep(7);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                }
                XYChart.Series<String, Number> bestFit = getLineOfBestFit(allSeries[i - 1]);
                if (bestFit == null)
                    continue;
                allCharts[i - 1].getData().add(bestFit);
                Node bestFitNode = bestFit.getNode();
                bestFitNode.setOpacity(0);
                bestFitNode.setPickOnBounds(false);
                bestFitNode.toBack();
                for (XYChart.Data<String, Number> data : bestFit.getData()) {
                    data.getNode().getStyleClass().add("trans-node");
                }
                bestFitNode.setOnMouseEntered(event -> {
                    bestFitNode.getStyleClass().add("best-fit-lit");
                });
                bestFitNode.setOnMouseExited(event -> {
                    bestFitNode.getStyleClass().removeAll("best-fit-lit");
                });

                ex.execute(() -> {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    bestFitNode.setCacheHint(CacheHint.QUALITY);
                    bestFitNode.setCache(true);
                    bestFitNode.setVisible(true);
                    Platform.runLater(() -> {
                        applyGraphNodeInAnimation(bestFitNode, 2);
                    });
                });
            }
        });

    }

    private void hilightNode(Node chartSymbol, String symbolTitle) {
        chartSymbol.setScaleX(HOVER_NODE_SCALE);
        chartSymbol.setScaleY(HOVER_NODE_SCALE);

        Pane pane = (StackPane) chartSymbol;
        pane.toFront();
        Label lbl = new Label(symbolTitle);
        lbl.setFont(Font.font("System", 9.5));
        lbl.setEffect(null);
        lbl.setMinWidth(Region.USE_PREF_SIZE);
        pane.getChildren().add(lbl);


        transformToFitParent(chartSymbol, lbl);
    }

    private void applyGraphNodeInAnimation(Node node, double durationMultiplier) {
        Random rand = new Random();
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(300 * durationMultiplier), node);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1.0);

        node.setCache(true);
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(300 * durationMultiplier + rand.nextInt(500) * durationMultiplier), node);
        translateTransition.setToY(0);
        translateTransition.setInterpolator(Interpolator.EASE_OUT);

        fadeTransition.play();
        node.setVisible(true);
        translateTransition.play();
    }

    private Label getTitleLabel() {
        return titleLbl;
    }

    private void addChart(LineChart chart) {
        GridPane.setConstraints(chart, numOfCharts % gridsPerRow, numOfCharts / gridsPerRow);
        gridLayout.getChildren().add(chart);
        numOfCharts++;
    }

    private void removeChartSymbolHoverEffect(Node chartSymbol) {
        Pane pane = (StackPane) chartSymbol;
        chartSymbol.getParent().setCursor(Cursor.CROSSHAIR);
        if (pane.getChildren() != null) {
            pane.getChildren().clear();
        }
        chartSymbol.setScaleX(1);
        chartSymbol.setScaleY(1);
    }

    private LineChart<String, Number> makeLineChart(String title, int numOfEpisodes, Season season) {
        TVChartBuilder builder = new TVChartBuilder();
        builder.makeXAxis(numOfEpisodes);
        builder.makeYAxis(graphLowerBound);
        LineChart<String, Number> lineChart = builder.getChart();
        lineChart.setOnMouseDragged(event -> {
            double newY = event.getY();
            float diff = DELTA_PER_DRAG;
            if(newY < deltaOnCurrentDrag)
                diff = -DELTA_PER_DRAG;
            adjustGraphZooms(diff);
            deltaOnCurrentDrag = event.getY();
        });
        lineChart.setTitle(title);
        Label titleLbl = (Label) lineChart.getChildrenUnmodifiable().get(0);
        titleLbl.setTextFill(Paint.valueOf("white"));
        titleLbl.setOnMouseEntered(e -> titleLbl.setTextFill(Paint.valueOf("#89cff0")));
        titleLbl.setOnMouseExited(e -> titleLbl.setTextFill(Paint.valueOf("#FFFFFF")));
        titleLbl.setOnMouseClicked(e -> mainView.showPopUpWindow(season));
        titleLbl.setCursor(Cursor.HAND);
        lineChart.setOnMouseDragExited(event -> deltaOnCurrentDrag = 0);
        lineChart.setOnMouseDragReleased(event -> deltaOnCurrentDrag = 0);
        return lineChart;
    }

    private void adjustGraphZooms(float deltaY) {
        float nextY = graphLowerBound + deltaY;
        if(nextY >= MAXIMUM_SMALLEST_Y || nextY <= MINIMUM_SMALLEST_Y || deltaY == 0)
            return;
        for(LineChart chart: allCharts) {
            ((NumberAxis)chart.getYAxis()).setLowerBound(nextY);
        }
        graphLowerBound = nextY;
    }

    private void transformToFitParent(Node node, Label lbl) {
        // gets values to calculate label translate
        Node parent = node.getParent().getParent();
        double xTranslate = 0;
        double yTranslate = -LABEL_MARGIN;

        double parentWidth = 0.95 * parent.getBoundsInLocal().getMaxX() - parent.getBoundsInLocal().getMinX();

        String txt = lbl.getText();
        FontLoader fontLoader = Toolkit.getToolkit().getFontLoader();

        Bounds bounds = node.getParent().sceneToLocal(node.localToScene(lbl.getBoundsInParent()));
        double relativeX = bounds.getMinX();
        double relativeY = bounds.getMinY();
        double lblWidth = fontLoader.computeStringWidth(txt, lbl.getFont());
        if (relativeX + 0.75 * lblWidth > parentWidth - CHART_LABEL_PADDING) {
            xTranslate = parentWidth - CHART_LABEL_PADDING - relativeX - 0.5 * lblWidth;
        } else if (relativeX < 0.75 * lblWidth + CHART_LABEL_PADDING) {
            xTranslate = 0.5 * lblWidth + CHART_LABEL_PADDING - relativeX;
        }
        lbl.setTranslateX(xTranslate);

        if (relativeY - 2 * LABEL_MARGIN < 0) {
            yTranslate = LABEL_MARGIN;
        }
        lbl.setTranslateY(yTranslate);

    }

    private void setOnHoverHighlight(Label node) {
        node.setOnMouseEntered(event -> node.setTextFill(Paint.valueOf("#89cff0")));
        node.setOnMouseExited(e -> node.setTextFill(Paint.valueOf("white")));
    }

    private static XYChart.Series<String, Number> getLineOfBestFit(XYChart.Series<String, Number> inSeries) {
        ObservableList<XYChart.Data<String, Number>> dataList = inSeries.getData();
        if (dataList.isEmpty()) {
            return null;
        }
        int numOfPts = dataList.size();
        int maxX = 0;
        for (XYChart.Data<String, Number> data : dataList) {
            int x = Integer.valueOf(data.getXValue());
            if (x > maxX)
                maxX = x;
        }


        float[] X = new float[numOfPts];
        float[] Y = new float[numOfPts];

        float xMean = 0;
        float yMean = 0;

        for (int i = 0; i < numOfPts; i++) {
            XYChart.Data<String, Number> current = dataList.get(i);
            X[i] = Float.valueOf(current.getXValue());
            Y[i] = current.getYValue().floatValue();
            xMean += X[i];
            yMean += Y[i];
        }
        xMean /= numOfPts;
        yMean /= numOfPts;
        float numerator = 0;
        float denominator = 0;
        for (int i = 0; i < numOfPts; i++) {
            numerator += (X[i] - xMean) * (Y[i] - yMean);
            denominator += (X[i] - xMean) * (X[i] - xMean);
        }
        float m = numerator / denominator;
        float c = yMean - m * xMean;

        XYChart.Series toRtn = new XYChart.Series();

        float y1 = m + c;
        XYChart.Data data = new XYChart.Data<>("1", y1);
        toRtn.getData().add(data);

        float y2 = m * maxX + c;
        data = new XYChart.Data("" + maxX, y2);
        toRtn.getData().add(data);
        return toRtn;
    }

    @Override
    public void update(Observable o, Object arg) {
        DataModel dataModel = (DataModel) o;
        updateGUI(dataModel.getShow());
    }
}
