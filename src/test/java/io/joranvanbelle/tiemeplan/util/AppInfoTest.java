package io.joranvanbelle.tiemeplan.util;

import java.time.Year;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AppInfoTest {

    @Test
    void versionIsFilteredFromBuild() {
        assertThat(AppInfo.getVersion()).isNotEqualTo("dev").matches("\\d+\\.\\d+\\.\\d+(-\\S+)?");
    }

    @Test
    void nameAndDescriptionComeFromBuild() {
        assertThat(AppInfo.getName()).isEqualTo("Tiemeplan");
        assertThat(AppInfo.getDescription()).isEqualTo("Create time plans for schools using constraints");
    }

    @Test
    void licenseIsApacheTwo() {
        assertThat(AppInfo.getLicense()).isEqualTo("Apache License 2.0");
        assertThat(AppInfo.getLicenseUrl()).isEqualTo("https://www.apache.org/licenses/LICENSE-2.0");
    }

    @Test
    void supportUrlsAreConfigured() {
        assertThat(AppInfo.getIssuesUrl()).contains("github.com");
        assertThat(AppInfo.getUrl()).contains("github.com");
    }

    @Test
    void vendorIsConfigured() {
        assertThat(AppInfo.getVendor()).isEqualTo("Joran Van Belle");
    }

    @Test
    void javaVersionMatchesRuntime() {
        assertThat(AppInfo.getJavaVersion()).isEqualTo(Runtime.version().feature());
    }

    @Test
    void javaFxVersionIsReported() {
        assertThat(AppInfo.getJavaFxVersion()).isNotBlank();
    }

    @Test
    void platformDescribesOperatingSystem() {
        assertThat(AppInfo.getPlatform()).contains(System.getProperty("os.name"));
    }

    @Test
    void copyrightYearIsCurrentYear() {
        assertThat(AppInfo.getCopyrightYear()).isEqualTo(Year.now().getValue());
    }

    @Test
    void diagnosticsSummarizesRuntime() {
        String diagnostics = AppInfo.diagnostics();
        assertThat(diagnostics).contains(AppInfo.getName() + " " + AppInfo.getVersion());
        assertThat(diagnostics).contains("Java " + AppInfo.getJavaVersion());
        assertThat(diagnostics).contains("JavaFX " + AppInfo.getJavaFxVersion());
        assertThat(diagnostics).contains("Platform " + AppInfo.getPlatform());
    }
}
