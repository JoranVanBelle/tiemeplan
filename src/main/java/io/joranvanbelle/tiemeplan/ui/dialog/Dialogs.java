package io.joranvanbelle.tiemeplan.ui.dialog;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

public final class Dialogs {

    private Dialogs() {
    }

    public static void info(String title, String message) {
        info(null, title, message);
    }

    public static void info(Window owner, String title, String message) {
        show(Alert.AlertType.INFORMATION, owner, title, null, message);
    }

    public static void warning(String title, String message) {
        warning(null, title, message);
    }

    public static void warning(Window owner, String title, String message) {
        show(Alert.AlertType.WARNING, owner, title, null, message);
    }

    public static void error(String title, String message) {
        error(null, title, message);
    }

    public static void error(Window owner, String title, String message) {
        show(Alert.AlertType.ERROR, owner, title, title, message);
    }

    public static boolean confirm(String title, String header, String message) {
        return confirm(null, title, header, message);
    }

    public static boolean confirm(Window owner, String title, String header, String message) {
        Alert alert = create(Alert.AlertType.CONFIRMATION, owner, title, header, message);
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

    public static String messageOf(Throwable error) {
        if (error == null) {
            return "Unknown error.";
        }
        String message = error.getMessage();
        return message == null || message.isBlank() ? "Unknown error." : message;
    }

    private static void show(Alert.AlertType type, Window owner, String title, String header, String message) {
        create(type, owner, title, header, message).showAndWait();
    }

    private static Alert create(Alert.AlertType type, Window owner, String title, String header, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message == null ? "" : message);
        Window resolvedOwner = owner != null ? owner : defaultOwner();
        if (resolvedOwner != null) {
            alert.initOwner(resolvedOwner);
        }
        return alert;
    }

    private static Window defaultOwner() {
        return Window.getWindows().stream()
                .filter(Window::isShowing)
                .filter(Window::isFocused)
                .findFirst()
                .orElseGet(() -> Window.getWindows().stream()
                        .filter(Window::isShowing)
                        .findFirst()
                        .orElse(null));
    }
}
