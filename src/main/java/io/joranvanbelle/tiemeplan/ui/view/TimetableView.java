package io.joranvanbelle.tiemeplan.ui.view;

import io.joranvanbelle.tiemeplan.ui.navigation.ViewProvider;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class TimetableView implements ViewProvider {
    @Override
    public String getTitle() {
        return "Timetable";
    }

    @Override
    public Node getView() {
        VBox root = new VBox(24);
        root.getStyleClass().add("screen");
        root.setPadding(new Insets(32));

        VBox header = new VBox(6);
        header.getStyleClass().add("screen-header");
        Label titleLabel = new Label("Timetable");
        titleLabel.getStyleClass().add("screen-title");
        Label subtitleLabel = new Label("Manage and configure your Timetable workflow with a clean, modern interface.");
        subtitleLabel.getStyleClass().add("screen-subtitle");
        header.getChildren().addAll(titleLabel, subtitleLabel);

        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        Label cardTitle = new Label("Getting Started");
        cardTitle.getStyleClass().add("card-title");
        Label cardSubtitle = new Label(
                "This section will be implemented in the upcoming milestone with full functionality.");
        cardSubtitle.getStyleClass().add("card-subtitle");
        cardSubtitle.setWrapText(true);
        HBox badge = new HBox();
        badge.setAlignment(Pos.CENTER_LEFT);
        Label badgeLabel = new Label("Coming Soon");
        badgeLabel.getStyleClass().addAll("badge", "badge-accent");
        badge.getChildren().add(badgeLabel);
        card.getChildren().addAll(cardTitle, cardSubtitle, badge);

        root.getChildren().addAll(header, card);
        return root;
    }
}
