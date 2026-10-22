# Implementation Plan (Milestones)

**Project:** Tiemeplan  
**Last Updated:** 2026-10-22  
**Status:** In progress — M0, M1, M2, M3 completed, M4 next  
**Constraint:** Replace `Tiemeplan.main` to launch JavaFX app; use MVVM with JavaFX properties/bindings; add FXML support; support light/dark; cancellable solver; JSON/YAML save/load; exports PDF/CSV/PNG; jpackage + JReleaser.

## Milestone Overview

| Milestone | Goal | Verifiable | Dependencies | Status |
|---|---|---|---|---|
| M0: Project scaffolding | Add deps/plugins, package structure, entry point | `mvn compile` succeeds | None | **Completed** |
| M1: Domain/UI mapping & models | UI models/mappers, ViewModels skeleton | Unit tests pass | M0 | **Completed** |
| M2: Solver service with cancellation | Cooperative cancellation in Solver + SolverService | Unit tests for cancel | M0, existing Solver | **Completed** |
| M3: UI shell & navigation/theming | App shell, navigation, light/dark, CSS | App launches (smoke) | M0,M1 | **Completed** |
| M4: Configuration screens | Setup, Courses (reorder), Constraints | Manual UI flow works | M1,M3 | Pending |
| M5: Solve flow & results | Solving overlay, Timetable, NoSolution | End-to-end small scenario works | M2,M4 | Pending |
| M6: Scenario persistence | JSON/YAML save/load, schema versioning | Roundtrip tests pass | M1,M4 | Pending |
| M7: Exports | PDF/CSV/PNG export | Files generated correctly | M5 | Pending |
| M8: Packaging & release | jpackage + JReleaser, CI workflow | Artifacts built per platform (plan only) | M0,M5,M6,M7 | Pending |

## Detailed Milestones

### M0: Project scaffolding — **Completed**
**Tasks:**
- Update `pom.xml`: add `javafx-fxml`, `javafx-swing`, Jackson 3 (`tools.jackson.core:jackson-databind`, `tools.jackson.dataformat:jackson-dataformat-yaml`), OpenPDF, OpenCSV. Add plugins: `javafx-maven-plugin`, `jpackage-maven-plugin`, `jreleaser-maven-plugin`. Pin `maven-compiler-plugin` and `maven-surefire-plugin`; set UTF-8 encoding.
- Create packages: `app/`, `ui/`, `ui/view`, `ui/controller`, `ui/component`, `ui/navigation`, `viewmodel/`, `service/`, `model/`, `io/`, `util/`.
- Add `Tiemeplan` launcher (non-`Application` class) and `app/TiemeplanApp extends Application`.
- Add `resources/` structure: `fxml/`, `css/`, `icons/` (empty until M3).

**Verification (done):**
- `mvn clean verify` → BUILD SUCCESS.
- `mvn clean test` → **11 tests, 0 failures**.
- `mvn javafx:run` and `java -cp <classes+deps> io.joranvanbelle.tiemeplan.Tiemeplan` launch without exceptions.
- Test runner defect fixed: Maven previously used `surefire 2.12.4` and had no `junit-jupiter-engine`, so **0 tests actually ran**. Now JUnit Platform executes.
- Stale `SolverTest` fixture corrected (`room2` capacity 5) because it contradicted its own "no solution" intent; assertion unchanged.

**Known issues carried forward:**
- IDE launch: `module-info.java` has been added and module-path launches are verified (`mvn javafx:run` and direct `--module` launch). IntelliJ must still be configured to run on the module path; re-verify before M4. Do not run JavaFX from the classpath (causes the Linux GTK `_runLoop` NPE).
- `java -jar target/tiemeplan-0.1.0.jar` does not work yet (no manifest/JavaFX bundling); jpackage delivers this in M8.
- Direct module launch prints a JDK warning about restricted native access in `javafx.graphics`; harmless for now, but consider `--enable-native-access=javafx.graphics` in run/package configuration.

### M1: Domain/UI mapping & models — **Completed**
**Goal:** Provide a stable, testable presentation layer that maps to the untouched domain (`variables/`, `backtrack/`) and can be serialized later by M6. No screens, no FXML, no solver changes.

**Source of truth:** Domain records `Teacher(String value)`, `Subject(String value)`, `StudentGroup(String value, int size)`, `Classroom(String name, int capacity, List<Timeslot> occupiedTimeslots)`, `Timeslot(DayOfWeek, LocalTime)`, `Course(Teacher, Subject, List<StudentGroup>)`; `Course.numberOfParticipant()`; `Classroom.canHoldStudentGroup(int)`.

**Deliverables (done):**
1. Dedicated serializable DTOs in `model/`: `ScenarioDto` (`schemaVersion`, ordered `courses`, plus `classrooms`, `timeslots`, `teachers`, `subjects`, `studentGroups`), `ClassroomDto`, `CourseDto`, `TimeslotDto`, `TeacherDto`, `SubjectDto`, `StudentGroupDto`. All use Jackson 3 `@JsonProperty` with friendly names (`name` instead of `value`); list-containing DTOs make lists null-safe and immutable in compact constructors. `CURRENT_SCHEMA_VERSION = 1`.
2. `model/ScenarioMapper`: DTO ↔ domain for all types plus list helpers, preserving course order exactly.
3. Editable item view models in `viewmodel/`: `ClassroomViewModel` (name, capacity, occupied timeslots), `CourseViewModel` (teacher, subject, groups, `participantCount` binding), `TimeslotViewModel`, `StudentGroupViewModel`. Teachers and subjects are plain `ObservableList<String>` in `ScenarioViewModel`; no separate VM class is needed. JavaFX properties only (no Controls), so tests run headless.
4. `ScenarioViewModel`: aggregates `ObservableList`s with extractors, supports `moveCourse`/`moveCourseUp`/`moveCourseDown`, `toDto()`/`loadFrom(ScenarioDto)`, and `toDomain*()` accessors.
5. Validation: `ValidationSeverity`, `ValidationIssue(code, severity, message)`, and `ScenarioValidator`.

**Validation rules implemented (errors):**
- No timeslots, classrooms, or courses.
- Blank or duplicate names per category (trimmed, case-insensitive).
- `capacity <= 0`; `StudentGroup.size <= 0`.
- Course missing teacher/subject or with zero student groups.
- Course participant count exceeds every classroom capacity.
- Classroom-unavailable timeslot not in the scenario timeslot list.
- Duplicate or incomplete timeslots.

**Warnings implemented:** unused teachers, subjects, and student groups.

**Tests (16 new, 27 total):**
- `ScenarioMapperTest`: mapper round-trip and null-safe DTO lists.
- `ScenarioDtoJsonTest`: Jackson 3 JSON serialization uses friendly names, round-trips `DayOfWeek`/`LocalTime`, and deserializes records.
- `ScenarioViewModelTest`: reorder and boundary behavior, out-of-bounds rejection, participant-count binding updates, DTO round-trip preserving course order.
- `ScenarioValidatorTest`: positive case plus negative/warning case per rule.
- Plain JUnit 6 + AssertJ, no TestFX.

**Verification (done):**
- `mvn clean verify` → BUILD SUCCESS, **27 tests, 0 failures**.
- `module-info.java` now exports `model` and `viewmodel`, requires Jackson 3, and opens `model` to `tools.jackson.databind`/`tools.jackson.dataformat.yaml` for record (de)serialization.
- `mvn javafx:run` launches; direct module launch (`--module io.joranvanbelle.tiemeplan/...Tiemeplan`) stays running without exceptions.
- Spotless formatting/import ordering applied.

**Resolved decisions:**
1. Serialization shape (for M6): dedicated DTOs with friendly JSON names and `schemaVersion`; M6 serializes `ScenarioDto`.
2. Use dedicated DTOs rather than reusing domain records as the serialized shape. Domain records remain the payload for the solver.
3. Name uniqueness is trimmed and case-insensitive.
4. Timeslots stay free-form for now. The template generator is deferred; see backlog below.
5. Course order is semantically meaningful (solver order) and is preserved by DTOs and reordering.
6. `module-info.java` adopted; module-path launch verified. IDE module-path configuration still to be re-checked before M4.

**Deferred: timeslot template generator (backlog before/with M4)**
- Analyze typical school structures (days, period count, start time, period length, breaks) and decide between fixed presets and a configurable generator.
- UX: preview generated timeslots, then replace or append; skip duplicates by `(dayOfWeek, startTime)`.
- Keep timeslots as `LocalTime` with no timezone; generate one entry per day/period; no domain changes required.
- Define validation interaction (warnings for partial or overlapping templates) and document the chosen model before implementation.

**Dependencies:** completed. Unblocks M2 (consumes domain lists), M3 (ViewModels for screens), M4 (bindings/reorder), M6 (serialize `ScenarioDto`).

### M2: Solver service with cancellation — **Completed**
**Tasks (done):**
- Added `util/CancellationToken` with cooperative cancellation.
- Extended `BacktrackResult` with `Cancelled(ClassSchedule previousState)`.
- Modified `Solver` to accept `CancellationToken` (optional for backward compatibility), checking it at recursion entry and in loops; returns `Cancelled` when canceled and propagates cancellation up the backtrack stack.
- Created `service/SolverService` extending `javafx.concurrent.Service<BacktrackResult>` with setters/reset and a `Task` that runs `Solver.solve(...)` with a token.
- Added `SolverCancellationTest` for early cancellation.

**Verification (done):**
- `mvn clean verify` → BUILD SUCCESS, **28 tests, 0 failures** (added cancellation test).
- Existing `SolverTest` (11 tests) still pass with unchanged assertions.
- `module-info.java` exports `service` and `util`.

**Dependencies:** M0, existing solver. Unblocks M5 (Solve flow can use `SolverService` and handle `Cancelled`).

### M3: UI shell & navigation/theming — **Completed**
**Tasks (done):**
- Added `javafx.fxml` and `java.prefs` requirements; opened UI packages to `javafx.fxml`. Added placeholder `ui`/`ui.controller` markers.
- Created `service/ThemeService` with light/dark themes, CSS variables, and `Preferences` persistence.
- Created `ui/navigation/Screen`, `ViewProvider`, `NavigationService` with placeholder views for Setup/Courses/Constraints/Solving/Timetable/NoSolution/ScenarioManager/About.
- Rebuilt `app/TiemeplanApp` as a shell: `BorderPane` root with toolbar (nav buttons, theme toggle), content pane, navigation wired, minimum size 900×650.
- Added CSS: `resources/css/styles.css`, `theme-light.css`, `theme-dark.css`.

**Verification (done):**
- `mvn clean compile` and `mvn test` succeed; **28 tests, 0 failures**.
- `mvn javafx:run` launches the shell with toolbar and placeholder screens; theme toggle and navigation work.

**Dependencies:** M0,M1. Unblocks M4 (configuration screens can be built on the shell and use ViewModels).

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
- `service/ExportService`: PDF via OpenPDF, CSV via OpenCSV, PNG via JavaFX snapshot + SwingFXUtils
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
M0 (completed) → M1 (completed) → M2 (completed) → M3 (completed) → M4 → M5. M6/M7 after M5. M8 last.

## Unresolved Dependencies/Decisions
See API mapping doc for storage and release details requiring approval (scenario storage location, theme persistence, solver cancelled state shape, signing/notarization).

Additional open items:
- **IDE module-path configuration (verify before M4):** use the module path in IntelliJ; classpath execution triggers the Linux GTK `_runLoop` NPE.
- **Timeslot template generator (before/with M4):** analyze presets/configurable generation, preview/replace/append UX, and duplicate handling; timeslots remain free-form until then.
- **JavaFX native-access warning:** consider `--enable-native-access=javafx.graphics` in run/packaging configuration.
