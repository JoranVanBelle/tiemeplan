module io.joranvanbelle.tiemeplan {
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires java.desktop;
    requires java.prefs;
    requires tools.jackson.databind;
    requires tools.jackson.dataformat.yaml;
    requires com.fasterxml.jackson.annotation;
    requires com.github.librepdf.openpdf;
    requires com.opencsv;
    requires org.apache.commons.lang3;

    exports io.joranvanbelle.tiemeplan;
    exports io.joranvanbelle.tiemeplan.app;
    exports io.joranvanbelle.tiemeplan.backtrack;
    exports io.joranvanbelle.tiemeplan.variables;
    exports io.joranvanbelle.tiemeplan.model;
    exports io.joranvanbelle.tiemeplan.viewmodel;
    exports io.joranvanbelle.tiemeplan.service;
    exports io.joranvanbelle.tiemeplan.service.export;
    exports io.joranvanbelle.tiemeplan.util;
    exports io.joranvanbelle.tiemeplan.ui.navigation;

    opens io.joranvanbelle.tiemeplan.model to tools.jackson.databind, tools.jackson.dataformat.yaml;
    opens io.joranvanbelle.tiemeplan.app to javafx.fxml;
    opens io.joranvanbelle.tiemeplan.ui.view to javafx.fxml;
    opens io.joranvanbelle.tiemeplan.ui.navigation to javafx.fxml;
}
