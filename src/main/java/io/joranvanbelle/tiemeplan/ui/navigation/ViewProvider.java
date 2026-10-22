package io.joranvanbelle.tiemeplan.ui.navigation;

import javafx.scene.Node;

public interface ViewProvider {
    String getTitle();

    Node getView();
}
