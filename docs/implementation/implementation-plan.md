# Implementation Plan (Milestones)

**Project:** Tiemeplan  
**Last Updated:** 2026-10-22  
**Status:** Planning only (no implementation yet)  
**Constraint:** Replace `Tiemeplan.main` to launch JavaFX app; use MVVM with JavaFX properties/bindings; add FXML support; support light/dark; cancellable solver; JSON/YAML save/load; exports PDF/CSV/PNG; jpackage + JReleaser.

## Milestone Overview

| Milestone | Goal | Verifiable | Dependencies |
|---|---|---|---|
| M0: Project scaffolding | Add deps/plugins, package structure, entry point | `mvn compile` succeeds | None |
| M1: Domain/UI mapping & models | UI models/mappers, ViewModels skeleton | Unit tests pass | M0 |
| M2: Solver service with cancellation | Cooperative cancellation in Solver + SolverService | Unit tests for cancel | M0, existing Solver |
| M3: UI shell & navigation/theming | App shell, navigation, light/dark, CSS | App launches (smoke) | M0,M1 |
| M4: Configuration screens | Setup, Courses (reorder), Constraints | Manual UI flow works | M1,M3 |
| M5: Solve flow & results | Solving overlay, Timetable, NoSolution | End-to-end small scenario works | M2,M4 |
| M6: Scenario persistence | JSON/YAML save/load, schema versioning | Roundtrip tests pass | M1,M4 |
| M7: Exports | PDF/CSV/PNG export | Files generated correctly | M5 |
| M8: Packaging & release | jpackage + JReleaser, CI workflow | Artifacts built per platform (plan only) | M0,M5,M6,M7 |

## Detailed Milestones

### M0: Project scaffolding
**Tasks:**
- Update `pom.xml`: add `javafx-fxml`, `javafx-swing`, Jackson (databind/yaml/jsr310), OpenPDF, commons-csv. Add plugins: `javafx-maven-plugin`, `jpackage-maven-plugin` (or exec), `jreleaser-maven-plugin`. Set versions in properties.
- Create packages: `app/`, `ui/`, `ui/view`, `ui/controller`, `ui/component`, `ui/navigation`, `viewmodel/`, `service/`, `model/`, `io/`, `util/`.
- Replace `Tiemeplan.main` to launch `app/TiemeplanApp extends Application`.
- Add `resources/` structure: `fxml/`, `css/` (styles-base.css, styles-light.css, styles-dark.css), `icons/`.

**Verification:** `mvn compile` passes; app class exists.

**Risks:** JavaFX version compatibility with Java 26; plugin versions.

### M1: Domain/UI mapping & models
**Tasks:**
- Create UI DTOs/mappers between domain (`Course`, `Classroom`, `Timeslot`, `Teacher`, `Subject`, `StudentGroup`) and UI models.
- Create ViewModels: `ScenarioViewModel`, `CourseListViewModel` (with `ObservableList` and reorder support), `ClassroomViewModel`, etc.
- Validation logic in ViewModels.

**Verification:** Unit tests for mappers/validation pass. No JavaFX toolkit needed.

**Dependencies:** M0

### M2: Solver service with cancellation
**Tasks:**
- Modify `Solver` to accept `CancellationToken` (cooperative). Check token in recursive calls/loops; return early on cancel.
- Add `BacktrackResult.Cancelled` (or equivalent) to represent cancellation (separate from `NoSolutionFound`).
- Create `service/SolverService` using `javafx.concurrent.Service<Task<BacktrackResult>>`.
- Wire cancellation from Task to token.

**Verification:** Unit tests: cancel mid-solve returns cancelled state; algorithm stops (no unnecessary work after cancel). Existing solver tests still pass.

**Dependencies:** M0, existing solver. **Blocking:** Requires changing core solver API.

### M3: UI shell & navigation/theming
**Tasks:**
- `app/TiemeplanApp`: load theme, set scene, init navigation.
- `NavigationService`, base views/controllers.
- `ThemeService` with light/dark, persist preference (location TBD).
- CSS with variables and theme files.

**Verification:** App launches, can switch themes, navigate between placeholder screens.

**Dependencies:** M0,M1

### M4: Configuration screens
**Tasks:**
- Setup screen: classrooms/timeslots/teachers/subjects/groups
- Courses Editor: ReorderableTableView with drag-and-drop, edit dialogs
- Constraints Overview: validation summary, enable/disable Solve

**Verification:** Can build a scenario, reorder courses, validation works.

**Dependencies:** M1,M3

### M5: Solve flow & results
**Tasks:**
- Solving screen with LoadingOverlay and Cancel
- TimetableView with TimetableGrid, filters, layout toggle
- NoSolution screen using `previousState()`
- Wire SolverService

**Verification:** End-to-end solve small scenario; cancel works; failure case shows details.

**Dependencies:** M2,M4

### M6: Scenario persistence (JSON/YAML)
**Tasks:**
- `io/ScenarioSerializer` for JSON and YAML (Jackson). Schema versioning.
- `service/ScenarioService` save/load to user config dir (location TBD).
- ScenarioManager UI with file picker and list.

**Verification:** Roundtrip tests; load/save both formats; version mismatch handled.

**Dependencies:** M1,M4

### M7: Exports (PDF/CSV/PNG)
**Tasks:**
- `service/ExportService`: PDF via OpenPDF, CSV via commons-csv, PNG via JavaFX snapshot + SwingFXUtils
- Export buttons in TimetableView

**Verification:** Files generated and readable; PNG matches visible grid.

**Dependencies:** M5

### M8: Packaging & release
**Tasks:**
- Configure `jpackage-maven-plugin` for app-images/installers (Windows/macOS/Linux)
- Configure `jreleaser-maven-plugin` and `jreleaser.yml` for GitHub Releases
- GitHub Actions workflow matrix (build, test headless, package, release)
- Icons/metadata

**Verification:** Plan artifacts; CI workflow documented (no release until approved)

**Dependencies:** M0,M5,M6,M7

## Critical Path
M0 → M1 → M2 → M4 → M5, with M3 in parallel early. M6/M7 after M5. M8 last.

## Unresolved Dependencies/Decisions
See API mapping doc for backend details and decisions requiring approval (scenario storage location, theme persistence, solver cancelled state shape, schema versioning, signing/notarization).
