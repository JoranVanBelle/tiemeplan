package io.joranvanbelle.tiemeplan.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThemeServiceTest {

    @Test
    void defaultThemeIsLight() {
        assertThat(new ThemeService().getCurrentTheme()).isEqualTo(ThemeService.Theme.LIGHT);
    }

    @Test
    void toggleThemeSwitchesBetweenLightAndDark() {
        ThemeService themeService = new ThemeService();
        themeService.toggleTheme();
        assertThat(themeService.getCurrentTheme()).isEqualTo(ThemeService.Theme.DARK);
        themeService.toggleTheme();
        assertThat(themeService.getCurrentTheme()).isEqualTo(ThemeService.Theme.LIGHT);
    }

    @Test
    void applyPersistsThemeWithoutScene() {
        ThemeService themeService = new ThemeService();
        themeService.apply(ThemeService.Theme.DARK);
        assertThat(themeService.getCurrentTheme()).isEqualTo(ThemeService.Theme.DARK);
    }
}
