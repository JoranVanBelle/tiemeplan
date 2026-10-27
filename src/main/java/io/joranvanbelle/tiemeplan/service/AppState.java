package io.joranvanbelle.tiemeplan.service;

import io.joranvanbelle.tiemeplan.backtrack.BacktrackResult;
import io.joranvanbelle.tiemeplan.viewmodel.ScenarioViewModel;

public class AppState {
    private static final AppState INSTANCE = new AppState();

    private final ScenarioViewModel scenarioViewModel = new ScenarioViewModel();
    private final SolverService solverService = new SolverService();
    private BacktrackResult lastResult;

    private AppState() {
    }

    public static AppState getInstance() {
        return INSTANCE;
    }

    public ScenarioViewModel getScenarioViewModel() {
        return scenarioViewModel;
    }

    public SolverService getSolverService() {
        return solverService;
    }

    public BacktrackResult getLastResult() {
        return lastResult;
    }

    public void setLastResult(BacktrackResult lastResult) {
        this.lastResult = lastResult;
    }
}
