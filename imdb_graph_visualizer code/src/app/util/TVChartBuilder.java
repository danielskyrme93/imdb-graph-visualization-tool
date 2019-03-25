package app.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.layout.Region;

import java.util.ArrayList;

/**
 * Builds JavaFx charts to be shown in Graph Panel
 *
 * @author Daniel Skyrme
 * @version %I%, %G%
 */
public class TVChartBuilder {
    private CategoryAxis xAxis;
    private NumberAxis yAxis;

    public void makeXAxis(int numOfEpisodes) {
        ObservableList<String> list = FXCollections.observableList(new ArrayList<>());
        for (int i = 1; i <= numOfEpisodes; i++) {
            list.add(Integer.toString(i));
        }
        xAxis = new CategoryAxis(list);
        xAxis.setLabel("Episode");
        xAxis.getStyleClass().add("graph-axis");
    }

    public void makeYAxis(float graphLowerBound) {
        yAxis = new NumberAxis("", graphLowerBound, 10, 1);
        yAxis.setMinorTickCount(0);
        yAxis.getStyleClass().add("graph-axis");
    }

    public LineChart<String, Number> getChart() {
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setLegendVisible(false);
        lineChart.setFocusTraversable(true);
        lineChart.getStyleClass().addAll("ui-region", "graph-region");
        lineChart.setAnimated(false);
        lineChart.setCursor(Cursor.CROSSHAIR);
        lineChart.setPrefWidth(Region.USE_COMPUTED_SIZE);
        lineChart.setPrefHeight(Region.USE_COMPUTED_SIZE);
        lineChart.setCache(true);
        return lineChart;
    }

}
