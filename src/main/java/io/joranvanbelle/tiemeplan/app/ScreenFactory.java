package io.joranvanbelle.tiemeplan.app;

import io.joranvanbelle.tiemeplan.backtrack.BacktrackResult;
import io.joranvanbelle.tiemeplan.service.ScenarioPersistenceService;
import io.joranvanbelle.tiemeplan.service.SolverService;
import io.joranvanbelle.tiemeplan.service.export.ExportService;
import io.joranvanbelle.tiemeplan.service.export.ExportTheme;
import io.joranvanbelle.tiemeplan.ui.navigation.NavigationService;
import io.joranvanbelle.tiemeplan.ui.navigation.Screen;
import io.joranvanbelle.tiemeplan.ui.navigation.ViewProvider;
import io.joranvanbelle.tiemeplan.ui.view.AboutView;
import io.joranvanbelle.tiemeplan.ui.view.ConstraintsView;
import io.joranvanbelle.tiemeplan.ui.view.CoursesView;
import io.joranvanbelle.tiemeplan.ui.view.ScenarioManagerView;
import io.joranvanbelle.tiemeplan.ui.view.SetupView;
import io.joranvanbelle.tiemeplan.ui.view.TimetableView;
import io.joranvanbelle.tiemeplan.viewmodel.ScenarioViewModel;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ScreenFactory {
    private final NavigationService navigationService;
    private final ScenarioViewModel scenarioViewModel;
    private final Supplier<BacktrackResult> resultSupplier;
    private final Consumer<BacktrackResult> resultConsumer;
    private final SolverService solverService;
    private final ExportService exportService;
    private final ScenarioPersistenceService persistenceService;
    private final Supplier<ExportTheme> exportThemeSupplier;
    private final Consumer<String> linkOpener;

    public ScreenFactory(
            NavigationService navigationService,
            ScenarioViewModel scenarioViewModel,
            Supplier<BacktrackResult> resultSupplier,
            Consumer<BacktrackResult> resultConsumer,
            SolverService solverService,
            ExportService exportService,
            ScenarioPersistenceService persistenceService,
            Supplier<ExportTheme> exportThemeSupplier,
            Consumer<String> linkOpener) {
        this.navigationService = navigationService;
        this.scenarioViewModel = scenarioViewModel;
        this.resultSupplier = resultSupplier;
        this.resultConsumer = resultConsumer;
        this.solverService = solverService;
        this.exportService = exportService;
        this.persistenceService = persistenceService;
        this.exportThemeSupplier = exportThemeSupplier;
        this.linkOpener = linkOpener;
    }

    public Map<Screen, ViewProvider> createProviders() {
        Map<Screen, ViewProvider> providers = new EnumMap<>(Screen.class);
        providers.put(Screen.SETUP, new SetupView(scenarioViewModel));
        providers.put(Screen.COURSES, new CoursesView(scenarioViewModel));
        providers.put(Screen.CONSTRAINTS, new ConstraintsView(scenarioViewModel, navigationService, solverService,
                resultConsumer));
        providers.put(Screen.TIMETABLE, new TimetableView(navigationService, scenarioViewModel, resultSupplier,
                exportService, exportThemeSupplier));
        providers.put(Screen.SCENARIO, new ScenarioManagerView(scenarioViewModel, persistenceService));
        providers.put(Screen.ABOUT, new AboutView(linkOpener));
        return providers;
    }
}
