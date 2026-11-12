package io.joranvanbelle.tiemeplan.ui.navigation;

public enum Screen {
    SETUP("Setup"),
    COURSES("Courses"),
    CONSTRAINTS("Constraints"),
    TIMETABLE("Timetable"),
    SCENARIO("Scenario"),
    ABOUT("About");

    private final String title;

    Screen(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
