package app.view;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Auto-complete box that is shown when typing in search field
 * @author Daniel Skyrme
 * @version %I%, %G%
 */
class AutoBox extends VBox {
    private static final int DEFAULT_CELL_SIZE = 40;
    private static final int MAX_WIDTH = 1400;
    private static final int MAX_NUM_SHOWN = 12;
    private ListView<String> autoCompleteList;
    private boolean isActive;
    private int selectedIndex;

    AutoBox() {
        super();
        autoCompleteList = new ListView<>();
        autoCompleteList.getStyleClass().add("auto-region");
        autoCompleteList.setFocusTraversable(true);
        setMinHeight(Region.USE_PREF_SIZE);
        setMaxWidth(MAX_WIDTH);
        setAlignment(Pos.TOP_LEFT);
        getChildren().add(autoCompleteList);
        setActive(true);
        autoCompleteList.setFixedCellSize(DEFAULT_CELL_SIZE);
    }

    ListView<String> getAutoCompleteList() {
        return autoCompleteList;
    }

    void changeSelectedBy(int i) {
        int nxtIndex = selectedIndex + i;
        if (nxtIndex > -2 && nxtIndex <= autoCompleteList.getItems().size()) {
            selectedIndex = nxtIndex;
            autoCompleteList.getSelectionModel().clearSelection();
            if (nxtIndex >= 0 && nxtIndex < autoCompleteList.getItems().size())
                autoCompleteList.getSelectionModel().select(selectedIndex);
        }
    }

    void setItems(ObservableList<String> list) {
        selectedIndex = -1;
        autoCompleteList.setItems(list);
        autoCompleteList.setPrefHeight(Math.min(list.size(), MAX_NUM_SHOWN) * DEFAULT_CELL_SIZE);
        if (autoCompleteList.getItems().isEmpty())
            setActive(false);
        else {
            setActive(true);
        }
    }

    boolean isSelected() {
        return autoCompleteList.getSelectionModel().getSelectedItems().size() > 0;
    }

    void setActive(boolean isActive) {
        if (this.isActive != isActive) {
            this.isActive = isActive;
            setVisible(isActive);
            setPickOnBounds(isActive);
        }
    }
}
