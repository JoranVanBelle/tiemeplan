package io.joranvanbelle.tiemeplan.ui.component;

import io.joranvanbelle.tiemeplan.viewmodel.ValidationIssue;
import io.joranvanbelle.tiemeplan.viewmodel.ValidationSeverity;

import java.util.List;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ValidationBanner extends VBox {
    private Consumer<ValidationIssue> onIssueClick;

    public ValidationBanner() {
        setSpacing(8);
        setPadding(new Insets(12));
        getStyleClass().add("validation-banner");
    }

    public void setOnIssueClick(Consumer<ValidationIssue> onIssueClick) {
        this.onIssueClick = onIssueClick;
    }

    public void setIssues(List<ValidationIssue> issues) {
        getChildren().clear();
        if (issues == null || issues.isEmpty()) {
            setVisible(false);
            setManaged(false);
            return;
        }
        setVisible(true);
        setManaged(true);

        var errors = issues.stream().filter(i -> i.severity() == ValidationSeverity.ERROR).toList();
        var warnings = issues.stream().filter(i -> i.severity() == ValidationSeverity.WARNING).toList();

        if (!errors.isEmpty()) {
            Label header = new Label(errors.size() + " error(s) found");
            header.getStyleClass().add("validation-header-error");
            getChildren().add(header);
            for (ValidationIssue issue : errors) {
                getChildren().add(createIssueRow(issue, "validation-item-error"));
            }
        }
        if (!warnings.isEmpty()) {
            Label header = new Label(warnings.size() + " warning(s)");
            header.getStyleClass().add("validation-header-warning");
            getChildren().add(header);
            for (ValidationIssue issue : warnings) {
                getChildren().add(createIssueRow(issue, "validation-item-warning"));
            }
        }
    }

    private HBox createIssueRow(ValidationIssue issue, String styleClass) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("validation-item");
        row.getStyleClass().add(styleClass);
        Label code = new Label(issue.code());
        code.getStyleClass().add("validation-code");
        Label message = new Label(issue.message());
        message.getStyleClass().add("validation-message");
        message.setWrapText(true);
        row.getChildren().addAll(code, message);
        if (onIssueClick != null) {
            row.setFocusTraversable(true);
            row.setAccessibleRole(AccessibleRole.BUTTON);
            row.setAccessibleText(issue.code() + ": " + issue.message());
            row.setOnMouseClicked(e -> onIssueClick.accept(issue));
            row.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) {
                    onIssueClick.accept(issue);
                    e.consume();
                }
            });
            row.setCursor(javafx.scene.Cursor.HAND);
        }
        return row;
    }
}
