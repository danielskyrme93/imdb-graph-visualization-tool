package app.view;

import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The window displayed showing show, season and episode data
 *
 * @author Daniel Skyrme
 * @version %I%, %G%
 */
public class PopUpWindow extends VBox {
    private static final int BOX_PADDING = 60;
    private static final String LINK_REGEX = "https://[A-Za-z0-9]+[.[A-Za-z0-9]+]+[/[A-Za-z0-9]+]+[/]?[?[A-Za-z0-9=]+]*";

    /**
     * Instantiates a new Pop up window.
     *
     * @param toShow the to show
     * @param width  the width
     */
    PopUpWindow(LinkedHashMap<String, Object> toShow, double width) {
        super();
        setPadding(new Insets(BOX_PADDING));
        getStyleClass().add("pop-vbox");
        for (Map.Entry<String, Object> entry : toShow.entrySet()) {
            String k = entry.getKey();
            String v = String.valueOf(entry.getValue());

            HBox txtLine = new HBox();

            TextField headLbl = new TextField(k + ": ");
            configLabel(headLbl);
            txtLine.getChildren().add(headLbl);

            TextField infoLbl = new TextField(v);
            infoLbl.setMinWidth(infoLbl.getText().length() * 11);
            configLabel(infoLbl);

            txtLine.getChildren().add(infoLbl);

            getChildren().add(new Group(txtLine));
        }

    }

    private static void configLabel(TextField lbl) {
        lbl.getStyleClass().add("pop-text");
        lbl.setBackground(null);
        lbl.getStyleClass().add("popup-rect");
        lbl.getStyleClass().add("pop-lbl");
        lbl.setEditable(false);
        lbl.setStyle("-fx-text-fill: white;");
        if (lbl.getText().matches(LINK_REGEX)) {
            lbl.setCursor(Cursor.HAND);
            lbl.setOnMouseEntered(event -> lbl.setStyle("-fx-text-fill: \"#89cff0\";"));
            lbl.setOnMouseExited(e -> lbl.setStyle("-fx-text-fill: white;"));
            lbl.setOnMouseClicked(event -> {
                try {
                    URI uri = new URI(lbl.getText());
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(uri);
                    }
                } catch (URISyntaxException | IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static void main(String[] args) {
        MainView.main(null);
    }
}
