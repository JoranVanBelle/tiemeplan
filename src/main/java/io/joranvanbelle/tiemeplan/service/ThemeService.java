package io.joranvanbelle.tiemeplan.service;

import java.util.prefs.Preferences;

import javafx.scene.Scene;

public class ThemeService {
    public enum Theme {
        LIGHT,
        DARK
    }

    private static final String PREF_KEY = "theme";
    private static final String LIGHT_CSS = "/css/theme-light.css";
    private static final String DARK_CSS = "/css/theme-dark.css";
    private static final String BASE_CSS = "/css/styles.css";

    private final Preferences preferences = Preferences.userNodeForPackage(ThemeService.class);
    private Theme currentTheme = Theme.LIGHT;
    private Scene scene;

    public Theme getCurrentTheme() {
        return currentTheme;
    }

    public void initialize(Scene scene) {
        this.scene = scene;
        apply(loadTheme());
    }

    public void toggleTheme() {
        apply(currentTheme == Theme.LIGHT ? Theme.DARK : Theme.LIGHT);
    }

    public void apply(Theme theme) {
        if (scene == null) {
            currentTheme = theme;
            saveTheme(theme);
            return;
        }
        scene.getStylesheets().clear();
        var base = ThemeService.class.getResource(BASE_CSS);
        if (base != null) {
            scene.getStylesheets().add(base.toExternalForm());
        }
        var themeCss = theme == Theme.DARK ? ThemeService.class.getResource(DARK_CSS)
                : ThemeService.class.getResource(LIGHT_CSS);
        if (themeCss != null) {
            scene.getStylesheets().add(themeCss.toExternalForm());
        }
        scene.getRoot().getStyleClass().removeAll("theme-light", "theme-dark");
        scene.getRoot().getStyleClass().add(theme == Theme.DARK ? "theme-dark" : "theme-light");
        currentTheme = theme;
        saveTheme(theme);
    }

    private Theme loadTheme() {
        var value = preferences.get(PREF_KEY, Theme.LIGHT.name());
        try {
            return Theme.valueOf(value);
        } catch (Exception e) {
            return Theme.LIGHT;
        }
    }

    private void saveTheme(Theme theme) {
        preferences.put(PREF_KEY, theme.name());
    }
}
