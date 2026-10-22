package io.joranvanbelle.tiemeplan.app;

import javafx.application.Application;
import javafx.stage.Stage;

public class TiemeplanApp extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Tiemeplan");
        stage.setMinWidth(900);
        stage.setMinHeight(650);
        stage.show();
    }
}
