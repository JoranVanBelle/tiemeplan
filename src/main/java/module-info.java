module io.joranvanbelle.tiemeplan {
    requires javafx.base;
    requires javafx.graphics;
    requires tools.jackson.databind;
    requires tools.jackson.dataformat.yaml;
    requires com.fasterxml.jackson.annotation;

    exports io.joranvanbelle.tiemeplan;
    exports io.joranvanbelle.tiemeplan.app;
    exports io.joranvanbelle.tiemeplan.backtrack;
    exports io.joranvanbelle.tiemeplan.variables;
    exports io.joranvanbelle.tiemeplan.model;
    exports io.joranvanbelle.tiemeplan.viewmodel;

    opens io.joranvanbelle.tiemeplan.model to tools.jackson.databind, tools.jackson.dataformat.yaml;
}
