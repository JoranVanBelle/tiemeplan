package io.joranvanbelle.tiemeplan.ui.view;

import io.joranvanbelle.tiemeplan.util.AppInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AboutViewTest {

    @BeforeAll
    static void startToolkit() throws Exception {
        Assumptions.assumeTrue(System.getenv("DISPLAY") != null, "JavaFX needs a display");
        CountDownLatch started = new CountDownLatch(1);
        try {
            Platform.startup(started::countDown);
        } catch (IllegalStateException alreadyStarted) {
            started.countDown();
        } catch (Throwable unavailable) {
            Assumptions.assumeTrue(false, "JavaFX toolkit unavailable: " + unavailable);
        }
        Assumptions.assumeTrue(started.await(10, TimeUnit.SECONDS), "JavaFX toolkit did not start");
    }

    @Test
    void showsVersionPillAndRuntimeDetails() throws Exception {
        runOnFxThread(() -> {
            AboutView view = new AboutView(url -> {
            });
            Node root = view.getView();

            Label versionPill = findBy(root, node -> node.getStyleClass().contains("version-pill"));
            assertThat(versionPill.getText()).isEqualTo("v " + AppInfo.getVersion());

            assertThat(rowValue(root, "Java").getText()).isEqualTo(String.valueOf(AppInfo.getJavaVersion()));
            assertThat(rowValue(root, "JavaFX").getText()).isEqualTo(AppInfo.getJavaFxVersion());
            assertThat(rowValue(root, "Platform").getText()).isEqualTo(AppInfo.getPlatform());

            Hyperlink licenseLink = findBy(root, node -> node instanceof Hyperlink h
                    && h.getText().equals(AppInfo.getLicense()));
            assertThat(licenseLink.getText()).isEqualTo("Apache License 2.0");
        });
    }

    @Test
    void doesNotContainThemeControls() throws Exception {
        runOnFxThread(() -> {
            AboutView view = new AboutView(url -> {
            });
            Node root = view.getView();

            assertThat(allNodes(root)).noneMatch(node -> node instanceof ToggleButton);
            assertThat(allNodes(root)).noneMatch(node -> node instanceof Label l && l.getText().equals("Appearance"));
        });
    }

    @Test
    void reportIssueLinkOpensConfiguredUrl() throws Exception {
        runOnFxThread(() -> {
            List<String> opened = new ArrayList<>();
            AboutView view = new AboutView(opened::add);
            Node root = view.getView();

            Hyperlink reportIssue = findBy(root, node -> node instanceof Hyperlink h
                    && h.getText().equals("Report an Issue"));
            reportIssue.fire();
            assertThat(opened).containsExactly(AppInfo.getIssuesUrl());
        });
    }

    @Test
    void copyDiagnosticsButtonIsAvailable() throws Exception {
        runOnFxThread(() -> {
            AboutView view = new AboutView(url -> {
            });
            Node root = view.getView();

            Button copyDiagnostics = findBy(root, node -> node instanceof Button b
                    && b.getText().equals("Copy Diagnostics"));
            copyDiagnostics.fire();
        });
    }

    private static void runOnFxThread(Runnable action) throws Exception {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable error) {
                failure.set(error);
            } finally {
                done.countDown();
            }
        });
        assertThat(done.await(10, TimeUnit.SECONDS)).isTrue();
        assertThat(failure.get()).isNull();
    }

    @SuppressWarnings("unchecked")
    private static <T extends Node> T findBy(Node root, Predicate<Node> condition) {
        return (T) allNodes(root).stream().filter(condition).findFirst().orElseThrow();
    }

    private static Label rowValue(Node root, String rowLabelText) {
        Label rowLabel = findBy(root, node -> node instanceof Label l && l.getText().equals(rowLabelText));
        List<Node> siblings = rowLabel.getParent().getChildrenUnmodifiable();
        return (Label) siblings.get(siblings.indexOf(rowLabel) + 1);
    }

    private static List<Node> allNodes(Node root) {
        List<Node> nodes = new ArrayList<>();
        collect(root, nodes);
        return nodes;
    }

    private static void collect(Node node, List<Node> nodes) {
        nodes.add(node);
        if (node instanceof Parent parent) {
            parent.getChildrenUnmodifiable().forEach(child -> collect(child, nodes));
        }
    }
}
