package io.joranvanbelle.tiemeplan.ui.navigation;

import io.joranvanbelle.tiemeplan.ui.view.AboutView;
import io.joranvanbelle.tiemeplan.ui.view.ConstraintsView;
import io.joranvanbelle.tiemeplan.ui.view.CoursesView;
import io.joranvanbelle.tiemeplan.ui.view.NoSolutionView;
import io.joranvanbelle.tiemeplan.ui.view.ScenarioManagerView;
import io.joranvanbelle.tiemeplan.ui.view.SetupView;
import io.joranvanbelle.tiemeplan.ui.view.SolvingView;
import io.joranvanbelle.tiemeplan.ui.view.TimetableView;

import java.util.EnumMap;
import java.util.Map;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

public class NavigationService {
    private final BorderPane contentPane;
    private final Map<Screen, ViewProvider> providers = new EnumMap<>(Screen.class);
    private Screen currentScreen;

    public NavigationService(BorderPane contentPane) {
        this.contentPane = contentPane;
        registerDefaults();
    }

    private void registerDefaults() {
        providers.put(Screen.SETUP, new SetupView());
        providers.put(Screen.COURSES, new CoursesView());
        providers.put(Screen.CONSTRAINTS, new ConstraintsView());
        providers.put(Screen.SOLVING, new SolvingView());
        providers.put(Screen.TIMETABLE, new TimetableView());
        providers.put(Screen.NO_SOLUTION, new NoSolutionView());
        providers.put(Screen.SCENARIO_MANAGER, new ScenarioManagerView());
        providers.put(Screen.ABOUT, new AboutView());
    }

    public void navigateTo(Screen screen) {
        var provider = providers.get(screen);
        if (provider == null) {
            return;
        }
        Node view = provider.getView();
        contentPane.setCenter(view);
        currentScreen = screen;
    }

    public Screen getCurrentScreen() {
        return currentScreen;
    }
}
