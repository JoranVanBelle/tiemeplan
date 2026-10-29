# M7 Exports (PDF/CSV/PNG) — Technical Analysis

**Project:** Tiemeplan  
**Last Updated:** 2026-10-29  
**Status:** Analysis — decisions approved; implementation pending  
**Scope:** Milestone M7 of `docs/implementation/implementation-plan.md`, evaluated against `docs/ui-design/screen-spec.md` and `docs/ui-design/design-spec.md`.

## 1. Bottom Line

M7 is feasible with the dependencies already declared in `pom.xml`:

- `javafx-swing` 27
- OpenPDF 1.3.39
- OpenCSV 5.9

However, the M7 plan as written is **not sufficient to satisfy the ui-design specs**. It only specifies:

- `service/ExportService`: PDF via OpenPDF, CSV via OpenCSV, PNG via JavaFX snapshot + `SwingFXUtils`
- Export buttons in `TimetableView`

The ui-design specs additionally require:

- A Timetable toolbar with filters, layout toggle, and export actions.
- A reusable `TimetableGrid` custom control with color-coded cells and export-snapshot support.
- PNG export that matches the visible grid.
- Light/dark theme support, accessible controls, responsive layout, and feedback during long-running operations.

The largest issue is a dependency gap: M5 is marked complete, but the current `TimetableView` has no toolbar, filters, layout toggle, color coding, or reusable grid control. Decision 1 resolves this by moving that work to a new **M9** milestone after M8, where it can be analyzed later. M7 remains focused on export behavior and the export entry points.

In addition to the master timetable, M7 should support **audience-specific exports**:

- One schedule per teacher, showing when and where they teach.
- One schedule per student group, showing when they have which course and where.
- One schedule per classroom, showing courses, blocked periods, and free periods so maintenance can be planned.

These are not just filtered versions of the master timetable. They need a different grid shape, different cell content, and, for classrooms, explicit free/blocked states.

## 2. Compliance Matrix

| ui-design requirement | Source | Current state | M7 impact |
|---|---|---|---|
| Timetable toolbar: filters, layout toggle, export | `screen-spec.md:61-69` | No toolbar; only header and grid | M9 (deferred); M7 adds only the export entry point |
| `TimetableGrid` custom control, color-coded, export snapshot | `design-spec.md:91` | Ad-hoc `GridPane` in `TimetableView` | M9 (deferred); M7 needs an exportable grid node or export model |
| Export PDF/CSV/PNG | `screen-spec.md:67` | Not implemented | Core M7 work |
| PNG matches visible grid | `implementation-plan.md:160` | N/A | Snapshot semantics must be defined |
| Light/dark theme | `design-spec.md:11`, `:105` | Theme CSS exists | PNG should follow theme; PDF should be print-friendly |
| Loading/error feedback for long operations | `design-spec.md:106`, AGENTS | Scenario export is synchronous on the FX thread | M7 should use background tasks and inline status |
| Accessible buttons/focus/tooltips | `design-spec.md:13`, `:103` | Buttons lack focus styling | Add accessible labels, tooltips, and focus styles |
| Responsive at 900×650 | `design-spec.md:109-114` | App minimum is 1000×700 | M9; M7 export dialog/toolbar must still fit |
| State preservation across navigation | `design-spec.md:101` | `TimetableView` is rebuilt on every navigation | M9; M7 export options can be transient |

## 3. Current Implementation Gaps

- `TimetableView.java:119-129` derives rows and columns **only from assigned courses**. Empty classrooms and unused timeslots disappear. That may be acceptable for the current grid, but it is a poor export model and conflicts with a timetable that should show the full scenario.
- `TimetableView.java:53` reads `AppState.getInstance()` directly. Adding export there would deepen the static service-locator pattern that AGENTS explicitly discourages.
- `NavigationService.java:38` constructs `TimetableView` with only navigation; `NavigationService.java:49` calls `getView()` on every navigation. Filter/layout state would reset unless stored in a `TimetableViewModel` or app state.
- `styles.css:303-372` already has unused `.timetable-grid`, `.timetable-header`, and `.timetable-corner` classes, suggesting the intended control was never completed.
- `module-info.java` lacks `requires javafx.swing`, `requires java.desktop`, `requires com.github.librepdf.openpdf`, and `requires com.opencsv`. `javafx-swing` is already in `pom.xml:65-69`, but not wired into the module.
- `ScenarioManagerView.java:81-119` performs file I/O synchronously on the FX thread and uses modal alerts. M7 should not copy that pattern for potentially large PDF/PNG exports.
- There is no export model, filter model, layout model, export status state, or test coverage for exports.
- `ClassSchedule.java:95-107` exposes `assignedTeachers()`, `assignedStudentGroups()`, and `assignedClassRooms()` maps, but `assignedClassRooms` mixes scheduled courses with blocked/unavailable timeslots because `Solver.java:37-41` and `ClassSchedule.addUnavailability()` add both to the same map. Classroom exports must therefore use the scenario’s `Classroom.occupiedTimeslots()` to distinguish **blocked** from **scheduled**.

## 4. Export Audiences and Scope

### 4.1 Audiences

Introduce an explicit audience model:

```java
public enum ExportAudience {
    MASTER,          // full timetable, currently rows = timeslots, columns = classrooms
    TEACHER,         // one sheet per teacher
    STUDENT_GROUP,   // one sheet per student group
    CLASSROOM        // one sheet per classroom, including blocked/free periods
}
```

### 4.2 Selection

Each audience should support:

```java
public enum ExportSelection {
    ALL,       // all teachers / all groups / all classrooms / master timetable
    SINGLE     // one selected entity
}
```

“All teachers” means a single export containing every teacher’s schedule, not only teachers who currently have assigned courses. The same principle applies to student groups and classrooms. This makes the export complete and makes empty schedules visible instead of silently omitting them. For `ALL`, PDF and CSV are combined files; PNG is a ZIP of one image per entity.

### 4.3 Export request

```java
public record TimetableExportRequest(
        ExportFormat format,          // PDF, CSV, PNG
        ExportAudience audience,
        ExportSelection selection,
        String entityName,            // null for ALL or MASTER
        boolean applyCurrentFilters,
        double pngScale,              // PNG only
        boolean useCurrentTheme       // PNG only
) {
}
```

The request should be immutable and contain no JavaFX types. The UI builds it from the export dialog; the export service consumes it.

## 5. Recommended Architecture

Keep the pure exporters independent of JavaFX, and isolate snapshot handling:

```text
BacktrackResult.Success + ScenarioSnapshot
                 │
                 ▼
TimetableExportModelFactory  (pure, immutable)
                 │
                 ▼
TimetableExportDocument
   ├── List<TimetableSheet>
   │      ├── master sheet
   │      ├── teacher sheets
   │      ├── student-group sheets
   │      └── classroom sheets
   │
   ├── PdfTimetableExporter   (OpenPDF, background)
   ├── CsvTimetableExporter   (OpenCSV, background)
   └── PngTimetableExporter   (FX snapshot + background write)

TimetableGrid / TimetableView
        │
        └── snapshot on FX thread → WritableImage → PNG writer
```

Suggested types:

- `service/export/ScenarioSnapshot`: immutable snapshot of teachers, subjects, student groups, classrooms, timeslots, and courses. Build it from `ScenarioViewModel.toDomain*()` or from the loaded scenario DTO.
- `service/export/TimetableExportDocument`: immutable record containing title, generated timestamp, audience, selection, filter summary, and `List<TimetableSheet>`.
- `service/export/TimetableSheet`: one printable grid. It has a title, subtitle, column headers, row headers, cells, and optional notes/legend.
- `service/export/TimetableExportModelFactory`: builds the document for the requested audience. Copy `assignedCourses()` into immutable data immediately so a later solve cannot mutate an in-progress export.
- `service/export/PdfTimetableExporter` and `CsvTimetableExporter`: pure, no JavaFX types.
- `service/ExportService`: facade that chooses the exporter, creates a `Task`, handles errors, and optionally writes via a temp file.
- `ui/component/TimetableGrid`: deferred to M9 for the full filter/layout/color-coded control. M7 may use a minimal export-only grid node or snapshot the existing grid for PNG.
- `viewmodel/TimetableViewModel`: M9 for persistent layout/filter state. M7 export-dialog state can remain local to the dialog.

`TimetableView` should receive its dependencies by constructor rather than reading `AppState` directly. A minimal refactor is to pass the `BacktrackResult` supplier, `ScenarioSnapshot`, and `ExportService` through `NavigationService`.

### 5.1 Export document model

A single document can contain many sheets:

```java
public record TimetableExportDocument(
        String title,
        Instant generatedAt,
        ExportAudience audience,
        ExportSelection selection,
        String filterSummary,
        List<TimetableSheet> sheets
) {
}

public record TimetableSheet(
        String title,                 // e.g. "Teacher: Jane Doe"
        String subtitle,              // e.g. "12 scheduled courses"
        List<String> columnHeaders,   // days for all PDF audience sheets
        List<RowHeader> rowHeaders,   // timeslot rows
        Map<CellKey, TimetableCell> cells,
        List<String> notes            // legend, free-slot summary, etc.
) {
}

public record RowHeader(
        DayOfWeek day,
        LocalTime startTime,
        String label
) {
}

public record TimetableCell(
        SlotState state,
        String subject,
        String teacher,
        String classroom,
        List<String> studentGroups,
        int participantCount
) {
}

public enum SlotState {
    COURSE,
    BLOCKED,
    FREE,
    NOT_A_TIMESLOT,
    CONFLICT
}
```

`CellKey` can be a simple `(rowIndex, columnIndex)` record. PDF audience sheets use days as columns and timeslot start times as rows. This gives a familiar “week timetable” layout for teachers, groups, and rooms. The on-screen master grid may keep a room-overview layout, but the PDF export uses the day × timeslot grid for consistency.

### 5.2 PDF grid shape

All audience-specific PDFs use the same grid convention:

- **Columns:** the days present in the scenario, in `DayOfWeek` order. Normally Monday–Friday; include Saturday/Sunday only if the scenario defines timeslots for them.
- **Rows:** the distinct timeslot start times from the scenario, in chronological order. The example `9am, 10am, 11am, 1pm, 2pm, 3pm` is just the scenario’s set of timeslots, not a fixed hourly grid.
- **Cells:** audience-specific content, listed below. A day/time combination that has no scenario timeslot is blank or an em dash. A real timeslot with no course is “Free” only for classroom exports; for teacher/group exports it is blank.
- **Header:** the first column is the time column; the remaining columns are the days.
- **Multiple entries:** if a master cell contains more than one course, list them compactly (one per line). Teacher/group/classroom cells should normally contain one course because the solver prevents conflicts; if not, mark the cell `CONFLICT`.

| Audience | PDF cell content |
|---|---|
| Master | Course/subject, teacher, student groups, classroom |
| Teacher | Course/subject, student groups, classroom |
| Student group | Course/subject, teacher, classroom |
| Classroom | Student groups, teacher; or `Blocked` / `Free` |

Example grid:

| Time | Mon | Tue | Wed | Thu | Fri |
|---|---|---|---|---|---|
| 09:00 | Mathematics<br>Group A<br>Room 101 | — | — | — | — |
| 10:00 | — | — | — | — | — |
| 11:00 | — | — | — | — | — |
| 13:00 | — | — | — | — | — |
| 14:00 | — | — | — | — | — |
| 15:00 | — | — | — | — | — |

Use the scenario’s actual start times; do not force a fixed 9/10/11/1/2/3 schedule. If different days have different timeslots, use the union of start times and leave missing day/time combinations blank.

## 6. Audience-Specific Export Design

### 6.1 Teacher schedules

**Purpose:** A teacher can see where to give a course and when.

**Data source:** `ScheduledCourse.course().teacher()` from `ClassSchedule.assignedCourses()`. Include all scenario teachers from `ScenarioSnapshot.teachers()` so teachers with no courses still receive an empty schedule.

**Sheet content:**

- Title: `Teacher: <name>`.
- Subtitle: number of scheduled courses, and optionally total teaching periods.
- Grid: days as columns, timeslot start times as rows.
- Cell: course/subject, student groups, classroom. The teacher name is redundant on a teacher sheet and should not be repeated.
- Empty teacher: “No scheduled courses.”
- A course with multiple student groups appears once on the teacher’s schedule, with all groups listed.
- If the same teacher has two courses in the same timeslot, mark the cell `CONFLICT` and list both rather than silently dropping one. The solver should prevent this, but the exporter should not hide corrupt data.

**“All teachers” behaviour:** one sheet per teacher, sorted alphabetically. PDF: one grid page plus one agenda page per teacher, each starting on a new page. The agenda lists that teacher’s courses chronologically per day. CSV: one combined file using the six-column schema. PNG: one image per teacher, packaged as a ZIP for “All” (see 6.4).

### 6.2 Student group schedules

**Purpose:** A student group can see when they have which course and where.

**Data source:** `ScheduledCourse.course().studentGroups()` from `ClassSchedule.assignedCourses()`. Include all scenario student groups from `ScenarioSnapshot.studentGroups()` so groups with no courses still receive an empty schedule.

**Sheet content:**

- Title: `Student group: <name>`.
- Subtitle: group size and number of scheduled courses.
- Grid: days as columns, timeslot start times as rows.
- Cell: course/subject, teacher, classroom. The student group name is redundant on a group sheet and should not be repeated.
- Empty group: “No scheduled courses.”
- A course with multiple student groups must appear in **each** group’s schedule.
- Detect duplicate group assignments in the same timeslot and mark them `CONFLICT`.

**“All student groups” behaviour:** one sheet per group, sorted alphabetically. PDF: one grid page plus one agenda page per group, each starting on a new page. The agenda lists that group’s courses chronologically per day. CSV: one combined file using the six-column schema. PNG: one image per group, packaged as a ZIP for “All”.

### 6.3 Classroom schedules for maintenance

**Purpose:** Maintenance can see when a room is occupied, blocked, or free.

**Data source:**

- Scheduled courses: `ScheduledCourse.classroom()` from `ClassSchedule.assignedCourses()`.
- Blocked/unavailable periods: `Classroom.occupiedTimeslots()` from the scenario snapshot.
- Free periods: scenario timeslots that are neither scheduled nor blocked.
- Do **not** use `ClassSchedule.assignedClassRooms()` alone to determine state, because it merges scheduled and blocked timeslots.

**Sheet content:**

- Title: `Classroom: <name>`.
- Subtitle: capacity, number of scheduled courses, number of blocked periods, number of free periods.
- Grid: days as columns, timeslot start times as rows.
- Cell states:
  - `COURSE`: student groups, teacher. The subject may be shown as a secondary label if there is room, but the required maintenance-facing content is groups and teacher.
  - `BLOCKED`: “Blocked” / “Unavailable”.
  - `FREE`: “Free”.
  - `NOT_A_TIMESLOT`: blank/dash.
- Legend: Course / Blocked / Free / No period.
- Include a compact free-period summary per day, for example `Mon 09:00, 11:00; Tue 14:00`, for maintenance planning.
- Include every scenario classroom, even rooms with no scheduled courses.
- Optionally include utilization counts: scheduled periods, blocked periods, free periods.

**“All classrooms” behaviour:** one sheet per classroom, sorted alphabetically. PDF: one grid page plus one agenda page per room, each starting on a new page. The agenda lists every timeslot state (`COURSE`, `BLOCKED`, `FREE`) chronologically per day and includes the free-period summary. CSV: the classroom occupancy schema `day;timeslot;classroom;state;teacher;course;studentgroup`, one row per room × timeslot, including `COURSE`, `BLOCKED`, and `FREE`. PNG: one image per room, packaged as a ZIP for “All”.

### 6.4 Format behaviour for “All”

| Audience | PDF | CSV | PNG |
|---|---|---|---|
| Master | Day × timeslot grid with all course details; one table, header repeats across pages | Combined six-column CSV | Single PNG |
| Teachers | Grid page + agenda page per teacher; each starts on a new page | Combined six-column CSV | ZIP of one PNG per teacher |
| Student groups | Grid page + agenda page per group; each starts on a new page | Combined six-column CSV | ZIP of one PNG per group |
| Classrooms | Grid page + agenda page per room; each starts on a new page; free-period summary | Occupancy CSV: `day;timeslot;classroom;state;teacher;course;studentgroup` | ZIP of one PNG per room |

For PNG “All”, use `java.util.zip` from the JDK; no new dependency is needed. Suggested ZIP layout:

```text
teachers/
  Jane-Doe.png
  John-Smith.png
student-groups/
  Group-A.png
classrooms/
  Room-101.png
```

Sanitize entity names for filenames (replace path separators, control characters, and reserved characters; collapse whitespace; truncate; append an index if two sanitized names collide). Include a small `manifest.txt` if useful for traceability.

Bulk PNG ZIP generation is approved for M7. Use `java.util.zip` from the JDK, show progress and support cancellation, and keep the single-entity PNG path as a direct `.png` file.

### 6.5 Filters and “All”

Filters should apply to course content consistently:

- Teacher filter: only that teacher’s courses appear.
- Student-group filter: only courses containing that group appear.
- Classroom filter: only courses in that room appear.
- Subject filter: only that subject appears.

Approved behaviour: apply the current filters by default, but disable the filter that matches the selected audience when exporting “All” (for example, ignore the teacher filter when exporting all teachers), while still applying the other filters. Show a note in the dialog. This avoids confusing empty sections. The filter summary is printed in each PDF/PNG header; the CSV schemas do not include metadata, so filters are implicit in the exported rows.

## 7. Format-Specific Analysis

### 7.1 PDF

Use OpenPDF to render from `TimetableExportDocument`, **not** from a JavaFX snapshot.

- The PDF is a table, not a screenshot. Use OpenPDF `Document` + `PdfPTable`.
- Columns: day names, Monday–Friday by default, plus weekend days if the scenario defines them.
- Rows: distinct timeslot start times from the scenario, sorted chronologically. Use the scenario’s actual times, for example `09:00`, `10:00`, `11:00`, `13:00`, `14:00`, `15:00`; do not invent hourly rows.
- First column: time label. Remaining columns: days.
- Cell content:
  - Master: course/subject, teacher, student groups, classroom.
  - Teacher: course/subject, student groups, classroom.
  - Student group: course/subject, teacher, classroom.
  - Classroom: student groups, teacher; `Blocked`; `Free`.
- Blank cells for day/time combinations that are not scenario timeslots. For teacher/group sheets, real timeslots without a course can also be blank; for classroom sheets, show `Free`.
- Every teacher/group/classroom entity gets **two pages: a grid page followed by an agenda page**. Each page starts on a new page so the combined PDF can be split per recipient. For the master export, use one table; if it spans multiple pages, repeat the header row.
- Agenda page content:
  - Teacher: courses listed chronologically per day, with time, course, student groups, and classroom.
  - Student group: courses listed chronologically per day, with time, course, teacher, and classroom.
  - Classroom: every timeslot state listed chronologically per day, with time, state, teacher, course, student groups, plus the free-period summary.
- The agenda page is always present for teacher/group/classroom exports, including empty entities; it is not a fallback only for irregular timeslots.
- Landscape A4 is the sensible default. Keep the time column narrower and give the day columns equal width.
- Add title, audience, entity name, filter summary, and a localized generated timestamp. Use localized day names and localized date/time formatting.
- Use a print-friendly light palette regardless of the app theme.
- Handle long teacher/group/classroom names with wrapping; if a cell becomes too tall, allow the row to grow rather than clipping text.
- For classroom exports, use distinct visual states for course, blocked, and free, but also print the state as text so the meaning is not color-only. Include a compact free-period summary per day.
- Embed **Noto Sans** Regular and Bold via `BaseFont.IDENTITY_H`; store them under `src/main/resources/fonts/` and include them in packaging. Noto Sans is chosen for its professional, modern look and broad Unicode coverage.
- Generate on a background thread and write atomically where practical.

### 7.2 CSV

Use OpenCSV with UTF-8, semicolon (`;`) as the delimiter, and RFC 4180 quoting. Semicolon is intentional for Excel compatibility in locales where comma is the decimal separator.

Required schema:

```text
day;timeslot;teacher;course;studentgroup;classroom
```

Rules:

- **One row per scheduled course per student group.** If a course has groups `Group A` and `Group B`, emit two rows with the same day, timeslot, teacher, course, and classroom, and a different `studentgroup` value.
- `day`: localized day name for the export locale, for example `MONDAY` in English or `maandag` in Dutch. Tests fix the locale.
- `timeslot`: 24-hour `HH:mm`, for example `09:00`, to keep sorting and parsing stable.
- `teacher`: teacher name.
- `course`: course/subject name from `Course.subject().value()`.
- `studentgroup`: one student group name.
- `classroom`: classroom name.
- Master, teacher, and student-group exports use this six-column schema.
- For single-entity exports, filter rows to that teacher or student group. For “All”, emit one combined file with all rows.
- **Classroom exports use a separate occupancy schema** so maintenance can see free/blocked periods without adding a seventh column to the course schema:

```text
day;timeslot;classroom;state;teacher;course;studentgroup
```

- One row per room × scenario timeslot, including `COURSE`, `BLOCKED`, and `FREE`.
- `state`: `COURSE`, `BLOCKED`, or `FREE`.
- For `COURSE`, `teacher`, `course`, and `studentgroup` are populated. If a course has multiple groups, `studentgroup` lists them separated by `, ` and the field is quoted only if needed.
- For `BLOCKED` and `FREE`, `teacher`, `course`, and `studentgroup` are empty.
- Omit `NOT_A_TIMESLOT` rows; only scenario timeslots are emitted.
- For “All” classrooms, combine all rooms into one occupancy CSV.

Examples:

```text
MONDAY;09:00;Jane Doe;Mathematics;Group A;Room 101
MONDAY;09:00;Jane Doe;Mathematics;Group B;Room 101
MONDAY;10:00;John Smith;Physics;Group B;Room 102
```

Classroom occupancy examples:

```text
MONDAY;09:00;Room 101;COURSE;Jane Doe;Mathematics;Group A, Group B
MONDAY;10:00;Room 101;BLOCKED;;;
MONDAY;13:00;Room 101;FREE;;;
```

Implementation notes:

- Configure OpenCSV with `;` as the separator, the default quote character, and `\n` line endings.
- Quote any field containing `;`, a quote, or a newline; double embedded quotes per RFC 4180.
- Write UTF-8. A UTF-8 BOM is optional for Excel compatibility, but if used it must not corrupt the first header cell.
- Do not add a trailing semicolon.

Sorting should be deterministic:

- Day by `DayOfWeek` order (Monday–Sunday), not localized text.
- Timeslot ascending.
- Teacher, course, studentgroup, and classroom in stable natural order.
- Occupancy CSV: day, timeslot, then classroom in stable natural order.

CSV is not theme-dependent and does not use the PDF grid layout.

### 7.3 PNG

This is the most delicate part.

- `Node.snapshot()` must run on the JavaFX Application Thread.
- Snapshot the **grid content node**, not the `ScrollPane` viewport, otherwise only the visible scrolled area is captured.
- The current `ScrollPane` uses `fitToWidth(true)` and `fitToHeight(true)` (`TimetableView.java:160-161`). That can clip or compress a wide grid. Either temporarily disable fit-to-width, or render a dedicated off-screen export node at full content size.
- Call `applyCss()` and `layout()` before snapshotting if the node is not fully laid out.
- Use `SnapshotParameters` with the theme surface color as fill. Consider scale 2 for crisp output, but cap image dimensions to avoid huge textures.
- Convert to `BufferedImage` via `SwingFXUtils`, then write with `ImageIO` off the FX thread.
- If the grid is too large for a single snapshot, tile it or scale it down; define a maximum and report a clear error rather than producing a blank image.
- The PNG should include the current filter/layout state and the current theme, because the spec says it should match the visible grid.
- For “All” PNG exports, snapshot each sheet on the FX thread and write the images into a ZIP in the background. Show progress and support cancellation; snapshotting dozens of teachers on the FX thread can otherwise make the UI feel frozen.

## 8. UI/UX Implementation Plan

- Add the export entry point inside the Timetable view, below the screen header. The full filter/layout toolbar is M9; M7 adds only the export action and status. Do not put export actions in the global app toolbar.
- M9 host toolbar (for reference):
  - Filters: teacher, student group, classroom, subject.
  - Layout toggle: by room / by teacher / by group for the master view.
  - Status label/progress indicator.
  - M7 adds the export action to this host toolbar; until M9 exists, M7 places the export action in the screen header.
- The Export button opens a small export dialog:
  - Format: PDF (default), CSV, PNG.
  - Content: Master timetable, Teachers, Student groups, Classrooms.
  - Which: All (default) or a specific entity from a ComboBox.
  - Apply current filters (checkbox, default on).
  - Classroom exports include free and blocked periods with text labels, color, and a free-period summary. Classroom CSV uses the occupancy schema.
  - PNG scale/theme options (PNG only).
  - A note when “All” is selected: “PDF is one combined multi-page file; CSV is one combined file; PNG is a ZIP of one image per teacher/group/room.”
- Validate the dialog before closing:
  - A specific entity must be selected for `SINGLE`.
  - Classroom-only options are disabled for other audiences.
  - “All” + PNG must produce a `.zip` target.
  - “All” + PDF/CSV must produce a combined `.pdf`/`.csv` target.
- Use existing `primary-button` / `secondary-button` / `ghost-button` styles from `styles.css:57-94`. Add missing styles for `MenuButton`, `Dialog`, `ContextMenu`, disabled buttons, focus rings, progress indicator, and status text.
- Export must be enabled only for `BacktrackResult.Success`. For `NoSolutionFound`, `Cancelled`, or no result, either hide export or disable it with a tooltip explaining why.
- `FileChooser` runs on the FX thread; file writing runs on a background `Task`.
- Show inline status (“Exporting 18 teacher timetables…”, “Saved timetable-teachers.pdf”) and a clear failure message. Avoid a modal dialog for every export; reserve alerts for failures or overwrite confirmation.
- Enforce extensions (`.pdf`, `.csv`, `.png`, `.zip`), default filenames such as `timetable-teachers-2026-10-29-1430.pdf`, and remember the last export directory via `Preferences`.
- Keep the toolbar usable at the minimum window size; JavaFX `ToolBar` overflow or a wrapping layout should be verified at 900×650.
- Add accessible text/tooltips and keyboard activation. The design spec requires visible focus indicators; the current CSS only styles focused text fields.
- Use entity names in PDF/PNG headers and sanitized names in filenames. Avoid exposing internal object IDs.

## 9. Build and Module Changes

Add to `module-info.java`:

```java
requires javafx.swing;
requires java.desktop;
requires com.github.librepdf.openpdf;
requires com.opencsv;
```

`javafx.swing` is an explicit module; `java.desktop` is transitively required by it but should be explicit if `ImageIO`/`BufferedImage` are used directly. OpenPDF and OpenCSV are automatic modules:

- `com.github.librepdf.openpdf`
- `com.opencsv`

OpenCSV also pulls in `commons-lang3`, `commons-text`, `commons-beanutils`, `commons-collections`, and `commons-collections4` (see the Maven dependency tree). These must remain on the module path at runtime. Verify `mvn javafx:run` after adding the `requires` clauses.

Automatic modules are accepted for M7. M8 should still verify `jlink`/`jpackage` with them and keep a fallback. Better options if modular packaging is unreliable:

- Build a **classpath-based `jpackage` app image** instead of relying on `jlink`.
- Use `--add-modules ALL-MODULE-PATH` and verify the resulting runtime.
- Replace OpenPDF/OpenCSV with modular alternatives, which is unnecessary for M7.

Recommended M8 approach: attempt modular `jlink`/`jpackage`, but treat the classpath-based app image as the supported fallback. Do not change M7 dependencies now.

## 10. Testing Strategy

The tests must verify **file content**, not only that a file was created.

### 10.1 Deterministic export fixture

Create a shared test fixture with a small, known schedule:

- Days: `MONDAY`, `TUESDAY`
- Timeslots: `09:00`, `10:00`, `13:00`
- Teachers: `Jane Doe`, `John Smith`
- Subjects: `Mathematics`, `Physics`
- Student groups: `Group A` (size 20), `Group B` (size 25)
- Classrooms: `Room 101` (capacity 30), `Room 102` (capacity 25)
- One multi-group course so duplication is testable
- One blocked classroom timeslot
- One free classroom timeslot

Inject a fixed `Clock` or `generatedAt` value and use a fixed `Locale` in tests so PDF headers and any timestamps are deterministic. Use `Locale.ENGLISH` for primary assertions and add one Dutch-locale test for localized day names. Use `@TempDir` for output files.

### 10.2 Model/content tests

- `TimetableExportModelFactoryTest`:
  - Master sheet ordering and empty rows/columns.
  - Teacher sheets include every teacher, including empty ones.
  - Student-group sheets duplicate a multi-group course into each group.
  - Classroom sheets distinguish `COURSE`, `BLOCKED`, `FREE`, and `NOT_A_TIMESLOT`.
  - Sorting is deterministic.
  - Conflicts are surfaced, not silently dropped.
  - Filters and “All” behaviour are correct.
  - Cell content is audience-specific:
    - Teacher: course, student groups, classroom.
    - Student group: course, teacher, classroom.
    - Classroom: student groups, teacher, plus blocked/free states and a free-period summary.
  - Classroom occupancy rows: one per room × timeslot with `COURSE`, `BLOCKED`, or `FREE`.

### 10.3 CSV content tests

`CsvTimetableExporterTest` should write to `@TempDir` and then verify both raw text and parsed rows.

Raw file assertions:

- Course CSV first line is exactly `day;timeslot;teacher;course;studentgroup;classroom`.
- Classroom occupancy CSV first line is exactly `day;timeslot;classroom;state;teacher;course;studentgroup`.
- Fields are separated by `;`, not commas.
- No trailing semicolon on any row.
- Line endings are `\n`.
- The file is valid UTF-8 and non-ASCII names round-trip.
- Expected rows appear in the expected deterministic order.
- A course with two groups produces two rows, one per group.
- Single-teacher and single-group exports contain only matching rows.
- An empty entity export still contains the header row.
- Classroom occupancy CSV contains `COURSE`, `BLOCKED`, and `FREE` rows for every room × timeslot, with empty course fields for `BLOCKED`/`FREE`.
- The course CSV does not contain free/blocked rows.
- Localized day names match the fixed test locale; the Dutch test asserts `maandag` instead of `MONDAY`.
- Fields containing `;`, quotes, or newlines are quoted correctly.

Parsed assertions:

- Read the file with `CSVReaderBuilder` configured with `;` as the separator.
- Assert the exact header array for both schemas.
- Assert the exact row count and the exact field values for each row.
- Assert occupancy state values and course details for occupied rows.
- Assert round-tripping of quoted values, for example a teacher named `Doe; Jane` or a group containing a quote.
- Assert that no accidental extra columns are present.

### 10.4 PDF content tests

`PdfTimetableExporterTest` should generate a PDF, open it with `PdfReader`, and extract text with `PdfTextExtractor`.

Per-page assertions:

- Page count matches the expected number of sheets: two pages per teacher/group/classroom entity (grid + agenda), plus any master pages.
- Each teacher/group/classroom page starts on a new page. Verify that the grid page starts with the entity title and the following agenda page starts with the agenda heading.
- Each agenda page lists the entity’s periods chronologically per day; classroom agendas include `COURSE`, `BLOCKED`, and `FREE` states.
- Page orientation and size match the configured landscape A4 output.
- Each teacher page contains the teacher title/name and that teacher’s course, student groups, and classroom.
- Each student-group page contains the group title/name and that group’s course, teacher, and classroom.
- Each classroom page contains the room title/name, student groups, teacher, the literal `Blocked` and `Free` labels where applicable, and the free-period summary.
- Localized day headers (for example `MONDAY`, `TUESDAY` in English; `maandag`, `dinsdag` in Dutch) and timeslot labels (`09:00`, `10:00`, `13:00`) appear on every sheet.
- The generated timestamp uses the fixed test locale.
- A multi-group course appears on both group pages.
- Single-entity exports do not contain another entity’s course text.
- “All” exports contain every entity name exactly once as a page title and are ordered alphabetically.
- Filters exclude the filtered-out course text.
- Pagination tests with many teachers/groups/rooms assert that all names are present and that the header row repeats.
- Unicode names round-trip through the embedded Noto Sans font.

PDF text extraction does not reliably prove column/row geometry. Verify the grid shape in `TimetableExportModelFactoryTest`; use the PDF test to prove that the expected content is rendered. If strict geometry verification is required later, add a PDF layout test with text coordinates rather than a golden PDF.

### 10.5 PNG content tests

PNG content is visual, so split the checks:

- File-level:
  - `ImageIO.read` returns a non-null image.
  - File signature is PNG.
  - Dimensions match the configured snapshot size.
  - The background pixel matches the expected theme surface color.
  - The image is not uniformly blank.
- Scene-level:
  - Build the export grid for a known fixture and traverse its labels before snapshotting.
  - Assert that the expected course, teacher, group, room, blocked, and free labels are present.
  - This verifies content without OCR.
- ZIP-level for “All”:
  - ZIP entry count and names match the expected entities.
  - Each entry opens as a valid PNG with the expected dimensions.
  - No duplicate entry names.
- Optional visual regression:
  - Avoid golden-image comparisons in normal unit tests because fonts and rendering differ across platforms.
  - If needed, run a controlled CI-only golden-image test with a tolerance.

A real `Node.snapshot()` test needs a JavaFX toolkit. Do not add TestFX just for this. Gate snapshot integration tests on a non-headless environment or run them under xvfb in CI. Manual verification remains necessary for “PNG matches visible grid.”

### 10.6 UI-level tests

- Extract export enablement and option state into `TimetableViewModel` so it can be tested without launching JavaFX.
- Verify export is disabled for no result, cancelled, and no-solution states.
- Verify the export dialog disables irrelevant options, selects `.zip` for “All” + PNG, and selects combined `.pdf`/`.csv` for “All” + PDF/CSV.
- Verify classroom CSV selects the occupancy schema and that free/blocked states are included.
- Verify the selected audience, entity, filters, and format are passed into `TimetableExportRequest`.
- Verify failure messages are user-facing and do not include stack traces.

### 10.7 Test execution

Run `mvn clean verify` and Spotless after implementation. CSV/PDF content tests should run headless. PNG snapshot tests should run under xvfb or be explicitly gated. Ensure the module-path launch still works.

## 11. Approved Decisions

1. **M5 UI polish is deferred.** Create a new **M9** milestone after M8 for the Timetable toolbar filters, layout toggle, color-coded grid, and state preservation. Its analysis can happen later; M7 does not include that UI polish.
2. **Bulk packaging.** “All” exports produce a combined multi-page PDF, a combined CSV, and a ZIP of PNGs.
3. **PNG ZIP.** Approved. Use `java.util.zip`; single-entity PNG remains a direct `.png` file.
4. **Empty entities.** Include teachers, student groups, and classrooms with no scheduled courses in “All” exports.
5. **Classroom states.** Include free and blocked periods with text labels plus color, and a free-period summary.
6. **Classroom occupancy CSV.** Add a separate schema: `day;timeslot;classroom;state;teacher;course;studentgroup`, with `state` set to `COURSE`, `BLOCKED`, or `FREE`. This is used for classroom exports; the six-column course schema remains for master/teacher/group exports.
7. **Filter semantics.** Apply current filters, disable the audience-matching filter for “All”, and show the filter summary in PDF/PNG headers.
8. **PDF pagination and agenda page.** Every teacher/group/classroom entity gets a grid page plus an agenda page. Each page starts on a new page so the combined PDF can be split per recipient. The agenda page is always present, including for empty entities.
9. **Font.** Embed **Noto Sans** Regular and Bold. Noto Sans is chosen for its professional, modern look and broad Unicode coverage.
10. **Localization.** Use localized day names and localized generated dates in PDF/PNG and CSV. CSV times remain `HH:mm` for stable sorting. Tests fix the locale. If machine-readable CSV is needed later, add a locale-independent day key.
11. **M8 packaging.** Automatic modules are acceptable for M7. M8 must verify `jlink`/`jpackage` and keep a classpath-based `jpackage` fallback. Better long-term options are modular libraries or a non-modular app image; neither is required now.

## 12. Suggested M7 Task Breakdown

1. Add `ScenarioSnapshot`, `TimetableExportDocument`, `TimetableSheet`, the deterministic export fixture, and the export model factory; add unit tests for master, teacher, group, and classroom audiences.
2. Add the semicolon-delimited CSV exporter and content tests for the six-column course schema and the classroom occupancy schema, including one row per course-group pair, free/blocked rows, quoting, localization, and single-entity filtering.
3. Add PDF exporter with embedded Noto Sans, localized dates, grid + agenda pages per entity, new-page pagination, and content tests using `PdfTextExtractor`, including multi-page “All” exports.
4. Add PNG writer and snapshot support; add PNG content tests for dimensions, theme fill, scene labels, and ZIP entries.
5. Add the export entry point/dialog, async task, progress/status/error states, and disabled-state handling. Full toolbar filters/layout toggle remain in M9.
6. Wire `module-info`, verify `mvn clean verify` and module-path launch.
7. Manually verify all three formats for master, all teachers, all groups, and all classrooms in light and dark themes at 900×650 and with a large timetable.

**Acceptance criteria:** on a successful solve, each format produces a readable file; the PDF uses days as columns and scenario timeslot start times as rows; teacher PDF cells show course, student groups, and classroom; student-group PDF cells show course, teacher, and classroom; classroom PDF cells show student groups and teacher, plus blocked/free states and a free-period summary; each teacher/group/classroom entity has a grid page and an agenda page, each starting on a new PDF page; PDFs embed Noto Sans and use localized dates; the course CSV uses the exact header `day;timeslot;teacher;course;studentgroup;classroom` with semicolon delimiters and one row per course-group pair; classroom CSV uses the exact occupancy header `day;timeslot;classroom;state;teacher;course;studentgroup` and includes free/blocked rows; “All” exports include every scenario entity; “All” produces a combined PDF, combined CSV, and ZIP of PNGs; PNG captures the full grid rather than only the scrolled viewport; exports respect the chosen filters; file I/O never blocks the FX thread; failures produce a clear user-facing message; export controls are unavailable without a solution; and tests assert the actual file content (exact CSV rows, extracted PDF text, PNG dimensions/theme/ZIP entries), not merely that files exist.
