package io.joranvanbelle.tiemeplan.ui.component;

import java.util.function.Function;
import javafx.beans.property.IntegerProperty;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;

public class IntegerSpinnerTableCell<T> extends TableCell<T, Integer> {

    private final Spinner<Integer> spinner;
    private final Function<T, IntegerProperty> property;

    public IntegerSpinnerTableCell(
            int min,
            int max,
            int defaultValue,
            int prefWidth,
            Function<T, IntegerProperty> property) {
        this.property = property;
        this.spinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, defaultValue));
        spinner.setEditable(true);
        spinner.setPrefWidth(prefWidth);
        spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            T item = currentItem();
            if (newValue != null && item != null && isEditing()) {
                property.apply(item).set(newValue);
            }
        });
    }

    @Override
    protected void updateItem(Integer item, boolean empty) {
        super.updateItem(item, empty);
        T rowItem = currentItem();
        if (empty || rowItem == null) {
            setText(null);
            setGraphic(null);
            return;
        }
        if (isEditing()) {
            setText(null);
            spinner.getValueFactory().setValue(property.apply(rowItem).get());
            setGraphic(spinner);
        } else {
            setGraphic(null);
            setText(String.valueOf(property.apply(rowItem).get()));
        }
    }

    @Override
    public void startEdit() {
        super.startEdit();
        updateItem(getItem(), isEmpty());
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        updateItem(getItem(), isEmpty());
    }

    private T currentItem() {
        return getTableRow() == null ? null : getTableRow().getItem();
    }
}
