package io.joranvanbelle.tiemeplan.ui.navigation;

import io.joranvanbelle.tiemeplan.ui.view.AboutView;
import io.joranvanbelle.tiemeplan.ui.view.ConstraintsView;
import io.joranvanbelle.tiemeplan.ui.view.CoursesView;
import io.joranvanbelle.tiemeplan.ui.view.NoSolutionView;
import io.joranvanbelle.tiemeplan.ui.view.ScenarioManagerView;
import io.joranvanbelle.tiemeplan.ui.view.SetupView;
import io.joranvanbelle.tiemeplan.ui.view.SolvingView;
import io.joranvanbelle.tiemeplan.ui.view.TimetableView;
import io.joranvanbelle.tiemeplan.viewmodel.ScenarioViewModel;

import java.util.EnumMap;
import java.util.Map;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

public class NavigationService {
    private final BorderPane contentPane;
    private final ScenarioViewModel scenarioViewModel;
    private final Map<Screen, ViewProvider> providers = new EnumMap<>(Screen.class);
    private Screen currentScreen;

    public NavigationService(BorderPane contentPane) {
        this(contentPane, new ScenarioViewModel());
    }

    public NavigationService(BorderPane contentPane, ScenarioViewModel scenarioViewModel) {
        this.contentPane = contentPane;
        this.scenarioViewModel = scenarioViewModel;
        registerDefaults();
    }

    private void registerDefaults() {
        providers.put(Screen.SETUP, new SetupView(scenarioViewModel));
        providers.put(Screen.COURSES, new CoursesView(scenarioViewModel));
        providers.put(Screen.CONSTRAINTS, new ConstraintsView(scenarioViewModel, this));
        providers.put(Screen.TIMETABLE, new TimetableView(this));
        providers.put(Screen.SOLVING, new SolvingView());
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
