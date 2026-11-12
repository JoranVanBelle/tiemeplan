package io.joranvanbelle.tiemeplan.ui.view;

import io.joranvanbelle.tiemeplan.ui.navigation.ViewProvider;
import io.joranvanbelle.tiemeplan.util.AppInfo;

import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class AboutView implements ViewProvider {
    private final Consumer<String> linkOpener;

    public AboutView(Consumer<String> linkOpener) {
        this.linkOpener = linkOpener;
    }

    @Override
    public Node getView() {
        VBox root = new VBox(24);
        root.getStyleClass().add("screen");
        root.setPadding(new Insets(32));

        root.getChildren().addAll(createIdentityHeader(), createSeparator(), createSystemSection(),
                createSeparator(), createLicenseSection(), createSeparator(), createFooter());
        return root;
    }

    private Separator createSeparator() {
        Separator separator = new Separator();
        separator.getStyleClass().add("about-separator");
        return separator;
    }

    private HBox createIdentityHeader() {
        VBox header = new VBox(6);
        header.getStyleClass().add("screen-header");

        Label logo = new Label("T");
        logo.getStyleClass().add("about-logo");
        logo.setAccessibleText("Tiemeplan logo");

        Label appName = new Label(AppInfo.getName());
        appName.getStyleClass().add("screen-title");
        Label versionPill = new Label("v " + AppInfo.getVersion());
        versionPill.getStyleClass().add("version-pill");
        versionPill.setAccessibleText("Application version " + AppInfo.getVersion());
        HBox nameRow = new HBox(10, appName, versionPill);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        Label appDescription = new Label(AppInfo.getDescription());
        appDescription.getStyleClass().add("screen-subtitle");

        header.getChildren().addAll(nameRow, appDescription);
        HBox.setHgrow(header, Priority.ALWAYS);

        HBox identity = new HBox(16, logo, header);
        identity.setAlignment(Pos.CENTER_LEFT);
        return identity;
    }

    private Node createSystemSection() {
        VBox section = new VBox(10);

        Label sectionTitle = new Label("System");
        sectionTitle.getStyleClass().add("about-section-title");

        GridPane rows = new GridPane();
        rows.setHgap(12);
        rows.setVgap(6);
        addSystemRow(rows, 0, "Java", String.valueOf(AppInfo.getJavaVersion()));
        addSystemRow(rows, 1, "JavaFX", AppInfo.getJavaFxVersion());
        addSystemRow(rows, 2, "Platform", AppInfo.getPlatform());

        section.getChildren().addAll(sectionTitle, rows);
        return section;
    }

    private Node createLicenseSection() {
        VBox section = new VBox(10);

        Label sectionTitle = new Label("License");
        sectionTitle.getStyleClass().add("about-section-title");

        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        Label licenseLabel = new Label("Released under the");
        licenseLabel.getStyleClass().add("about-row-value");
        Hyperlink licenseLink = new Hyperlink(AppInfo.getLicense());
        licenseLink.getStyleClass().add("about-link");
        licenseLink.setOnAction(event -> openLink(AppInfo.getLicenseUrl()));
        row.getChildren().addAll(licenseLabel, licenseLink);

        section.getChildren().addAll(sectionTitle, row);
        return section;
    }

    private Node createFooter() {
        VBox footer = new VBox(10);

        Label copyright = new Label("© " + AppInfo.getCopyrightYear() + " " + AppInfo.getVendor());
        copyright.getStyleClass().add("about-copyright");

        HBox actions = new HBox(16);
        actions.setAlignment(Pos.CENTER_LEFT);
        Hyperlink reportIssue = new Hyperlink("Report an Issue");
        reportIssue.getStyleClass().add("about-link");
        reportIssue.setOnAction(event -> openLink(AppInfo.getIssuesUrl()));
        Button copyDiagnostics = new Button("Copy Diagnostics");
        copyDiagnostics.getStyleClass().add("ghost-button");
        copyDiagnostics.setOnAction(event -> copyDiagnostics());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        actions.getChildren().addAll(reportIssue, spacer, copyDiagnostics);

        footer.getChildren().addAll(copyright, actions);
        return footer;
    }

    private void addSystemRow(GridPane rows, int rowIndex, String label, String value) {
        Label rowLabel = new Label(label);
        rowLabel.getStyleClass().add("about-row-label");
        Label rowValue = new Label(value);
        rowValue.getStyleClass().add("about-row-value");
        rows.addRow(rowIndex, rowLabel, rowValue);
    }

    private void openLink(String url) {
        if (url != null && !url.isBlank()) {
            linkOpener.accept(url);
        }
    }

    private void copyDiagnostics() {
        ClipboardContent content = new ClipboardContent();
        content.putString(AppInfo.diagnostics());
        Clipboard.getSystemClipboard().setContent(content);
    }
}
