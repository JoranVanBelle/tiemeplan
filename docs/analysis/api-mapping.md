# API Mapping: UI Workflows → Existing Backend

**Project:** Tiemeplan  
**Last Updated:** 2026-10-22  
**Status:** Planning only  
**Note:** There is **no external HTTP/API or authentication**. The "backend" here is the in-process domain + solver (`variables/`, `backtrack/`).

## Backend Surface (Source of Truth)
| Component | Type | Purpose | Notes |
|---|---|---|---|
| `variables/Teacher` | record | Teacher entity | `String name` |
| `variables/Subject` | record | Subject entity | `String name` |
| `variables/StudentGroup` | record | Student group | `String name`, `int size` |
| `variables/Classroom` | record | Classroom | `String name`, `int capacity`, `List<Timeslot> occupiedTimeslots`. `canHoldStudentGroup(int)` |
| `variables/Timeslot` | record | Time slot | `DayOfWeek dayOfWeek`, `LocalTime startTime` |
| `variables/Course` | record | Course requirement | `Teacher teacher`, `Subject subject`, `List<StudentGroup> studentGroups`. `numberOfParticipant()` sums sizes. Builders provided. |
| `backtrack/Solver` | class | Constraint solver | Constructor `(List<Classroom>, List<Timeslot>)`. `solve(List<Course>)` returns `BacktrackResult`. Tries to place courses in order over (classroom,timeslot). Respects capacity, classroom unavailability, teacher conflicts, student group conflicts. |
| `backtrack/ClassSchedule` | class | State tracker | Tracks assignments; `isValid`, `assign`, `unassign`. `addUnavailability(List<Classroom>)` for pre-occupied rooms. |
| `backtrack/ScheduledCourse` | record | Assignment | `Classroom classroom`, `Timeslot timeslot`, `Course course` |
| `backtrack/BacktrackResult` | sealed interface | Result | `Success(ClassSchedule solution)`, `NoSolutionFound(ClassSchedule previousState)`. **Needs extension:** `Cancelled(...)` for cooperative cancellation. |

## UI Workflow → Backend Mapping

| UI Workflow | UI Action | Backend Call/Objects | Notes |
|---|---|---|---|
| Configure resources | Add/edit classrooms | Create `List<Classroom>` (name, capacity, occupiedTimeslots) | Passed to `Solver` constructor. Unavailability via `ClassSchedule.addUnavailability()` in solver. |
| Configure timeslots | Define timeslots | Create `List<Timeslot>` | Passed to `Solver` constructor. Order affects search space; UI can define any set. |
| Configure people | Add teachers/subjects/groups | Create `Teacher`, `Subject`, `StudentGroup` objects | Used in `Course`. |
| Build courses | Add course with teacher/subject/groups | Create `Course` via builder or constructor | `Course.numberOfParticipant()` used for capacity check. Multiple groups supported. |
| Reorder courses | Drag-drop in Courses Editor | Reorder `List<Course>` before passing to solver | **Explicitly allowed.** Solver tries courses in list order (affects first solution found). |
| Validate | Check constraints overview | In-memory checks using domain methods | No solver call. Use `classroom.canHoldStudentGroup()`, non-empty lists, etc. |
| Solve | Start solve | `SolverService` creates `Solver(rooms, slots)`, calls `solver.solve(courses)` on background thread with `CancellationToken` | Must be off JavaFX thread. Needs cooperative cancellation. |
| Cancel solve | Cancel button | Set cancellation token, `Task.cancel()` | Solver must check token and return `BacktrackResult.Cancelled`. |
| View results | Display timetable | Read `((Success)result).solution().assignedCourses()` → `List<ScheduledCourse>` | Map to UI grid by day/time. |
| View failure | Show details | Read `((NoSolutionFound)result).previousState()` | Display partial state for debugging suggestions. |
| Save scenario | JSON/YAML export | Serialize UI scenario (resources, timeslots, people, courses in order) | Not calling backend; serialization of UI state. Need schema version. |
| Load scenario | JSON/YAML import | Deserialize to UI models → map to domain objects | Validate on load. |
| Export timetable | PDF/CSV/PNG | Read `assignedCourses()` from solution | No backend call; pure presentation export. |

## Required Backend Changes (for approved design)
| Change | Reason | Impact |
|---|---|---|
| **Cooperative cancellation in `Solver`** | Must stop algorithm on cancel (approved). | Add `CancellationToken` parameter/threading; check at loop/recursion points. Modify `solve` signatures or pass token. |
| **Add `BacktrackResult.Cancelled`** | Distinguish cancel from "no solution". | Extend sealed interface `BacktrackResult`. Update all exhaustive matches. |
| **Thread-safety considerations** | Solver called off-thread; current classes not thread-safe by design (mutable state in `ClassSchedule`) but used single-threaded per solve. | Ensure each solve uses fresh `ClassSchedule` instance (current code does). No shared mutable state across concurrent solves. |

## External Dependencies (new)
| Dependency | Purpose | Notes |
|---|---|---|
| `javafx-fxml` | FXML views/controllers | Required for MVVM with FXML option |
| `javafx-swing` | `SwingFXUtils` for PNG export | `Node.snapshot()` + Swing conversion |
| Jackson (databind, yaml, jsr310) | JSON/YAML serialization | For scenarios; JavaTimeModule needed |
| OpenPDF | PDF export | For timetable PDF |
| Apache Commons CSV | CSV export | For timetable CSV |
| JavaFX Maven plugin | `mvn javafx:run` | Dev |
| jpackage Maven plugin | App-images/installers | Packaging |
| JReleaser Maven plugin | Release to GitHub | Publishing |

## Authentication/Endpoints
**None.** This is a standalone desktop app with in-memory solver. No network calls, no auth, no REST endpoints. All data is local.

## Unresolved Questions (blocking/needs clarification)
| Question | Options | Recommendation |
|---|---|---|
| Scenario storage location | `~/.config/tiemeplan/`, `~/.tiemeplan/`, platform-specific app data dir | Use platform-appropriate config dir via `java.nio.file`/`Preferences`. Need confirmation. |
| Theme persistence | `Preferences` API vs scenario/settings JSON in config dir | `Preferences` simple; or unified settings file. Need confirmation. |
| `BacktrackResult.Cancelled` shape | `record Cancelled(ClassSchedule partialState)` or just marker | Returning partial state useful for debugging; propose `Cancelled(ClassSchedule previousState)`. |
| Schema versioning | Version field in JSON/YAML, migration strategy | Add `schemaVersion` (int) to scenario files. |
| Code signing/notarization | Required for distribution? | JReleaser can handle if secrets provided; may need for macOS/Windows. |
| Module system | Keep classpath (easier now) or introduce `module-info.java` | Start with classpath for jpackage simplicity. |
