package com.nhom5.eventticket.ui;

import javafx.scene.control.ConstrainedColumnResizeBase;
import javafx.scene.control.TableView;
import javafx.util.Callback;

/** JavaFX constrained policy using the viewport width (excluding borders and scrollbar). */
@SuppressWarnings({"rawtypes", "unchecked"}) // TableView's resize-policy API uses raw ResizeFeatures.
final class ProportionalTableResizePolicy extends ConstrainedColumnResizeBase
        implements Callback<TableView.ResizeFeatures, Boolean> {
    private final double[] weights;

    ProportionalTableResizePolicy(double... weights) {
        this.weights = weights.clone();
    }

    @Override
    public Boolean call(TableView.ResizeFeatures features) {
        // Keep the requested proportions when a header divider is dragged too.
        if (features.getColumn() != null) return false;
        javafx.collections.ObservableList<javafx.scene.control.TableColumn> columns =
                features.getTable().getVisibleLeafColumns();
        double width = features.getContentWidth();
        if (width <= 0 || columns.size() != weights.length) return false;
        double total = 0;
        double previousEdge = 0;
        for (int i = 0; i < columns.size(); i++) {
            total += weights[i];
            double edge = i == columns.size() - 1 ? width : Math.round(width * total);
            features.setColumnWidth(columns.get(i), edge - previousEdge);
            previousEdge = edge;
        }
        return true;
    }
}
