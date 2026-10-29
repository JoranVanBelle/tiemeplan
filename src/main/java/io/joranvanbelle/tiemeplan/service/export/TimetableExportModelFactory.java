package io.joranvanbelle.tiemeplan.service.export;

import io.joranvanbelle.tiemeplan.backtrack.ClassSchedule;
import io.joranvanbelle.tiemeplan.backtrack.ScheduledCourse;
import io.joranvanbelle.tiemeplan.variables.Classroom;
import io.joranvanbelle.tiemeplan.variables.Course;
import io.joranvanbelle.tiemeplan.variables.StudentGroup;
import io.joranvanbelle.tiemeplan.variables.Teacher;
import io.joranvanbelle.tiemeplan.variables.Timeslot;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public final class TimetableExportModelFactory {

    private static final Comparator<String> NAME_ORDER = String.CASE_INSENSITIVE_ORDER;

    public TimetableExportDocument create(
            ScenarioSnapshot snapshot,
            ClassSchedule schedule,
            TimetableExportRequest request,
            Clock clock,
            Locale locale) {
        List<ScheduledCourse> scheduledCourses = schedule == null ? List.of() : List.copyOf(schedule.assignedCourses());
        ExportFilters filters = effectiveFilters(request);
        List<DayOfWeek> days = days(snapshot);
        List<RowHeader> rows = rows(snapshot);
        Set<Timeslot> scenarioTimeslots = Set.copyOf(snapshot.timeslots());

        List<TimetableSheet> sheets = switch (request.audience()) {
            case MASTER -> List.of(masterSheet(scheduledCourses, filters, days, rows, scenarioTimeslots, locale));
            case TEACHER ->
                teacherSheets(snapshot, scheduledCourses, request, filters, days, rows, scenarioTimeslots, locale);
            case STUDENT_GROUP -> studentGroupSheets(snapshot, scheduledCourses, request, filters, days, rows,
                    scenarioTimeslots, locale);
            case CLASSROOM -> classroomSheets(snapshot, scheduledCourses, request, filters, days, rows,
                    scenarioTimeslots, locale);
        };

        return new TimetableExportDocument(
                documentTitle(request),
                clock.instant(),
                request.audience(),
                request.selection(),
                filters.describe(),
                sheets);
    }

    private ExportFilters effectiveFilters(TimetableExportRequest request) {
        ExportFilters filters = request.applyCurrentFilters() ? request.filters() : ExportFilters.none();
        if (request.selection() == ExportSelection.ALL) {
            filters = filters.withoutAudienceFilter(request.audience());
        }
        return filters;
    }

    private String documentTitle(TimetableExportRequest request) {
        String suffix = request.isSingleEntity() && request.entityName() != null
                ? " — " + request.entityName()
                : "";
        return switch (request.audience()) {
            case MASTER -> "Master timetable";
            case TEACHER -> "Teacher timetables" + suffix;
            case STUDENT_GROUP -> "Student group timetables" + suffix;
            case CLASSROOM -> "Classroom timetables" + suffix;
        };
    }

    private List<DayOfWeek> days(ScenarioSnapshot snapshot) {
        EnumSet<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
        snapshot.timeslots().stream().map(Timeslot::dayOfWeek).filter(day -> day != null).forEach(days::add);
        if (days.isEmpty()) {
            days.addAll(EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.FRIDAY));
        }
        return List.copyOf(days);
    }

    private List<RowHeader> rows(ScenarioSnapshot snapshot) {
        TreeSet<LocalTime> times = snapshot.timeslots().stream()
                .map(Timeslot::startTime)
                .filter(time -> time != null)
                .collect(Collectors.toCollection(TreeSet::new));
        return times.stream()
                .map(time -> new RowHeader(time, ExportText.time(time)))
                .toList();
    }

    private TimetableSheet masterSheet(
            List<ScheduledCourse> scheduledCourses,
            ExportFilters filters,
            List<DayOfWeek> days,
            List<RowHeader> rows,
            Set<Timeslot> scenarioTimeslots,
            Locale locale) {
        Map<CellKey, TimetableCell> cells = new LinkedHashMap<>();
        for (int row = 0; row < rows.size(); row++) {
            for (int column = 0; column < days.size(); column++) {
                Timeslot timeslot = timeslot(days, rows, row, column);
                if (!scenarioTimeslots.contains(timeslot)) {
                    cells.put(new CellKey(row, column), TimetableCell.notATimeslot());
                    continue;
                }
                List<TimetableCellEntry> entries = entriesFor(scheduledCourses, timeslot, filters);
                cells.put(new CellKey(row, column),
                        entries.isEmpty() ? TimetableCell.free() : TimetableCell.master(entries));
            }
        }
        int count = cells.values().stream().filter(TimetableCell::hasCourses)
                .mapToInt(cell -> cell.entries().size()).sum();
        return new TimetableSheet(ExportAudience.MASTER, null, "Master timetable",
                count + " scheduled courses", days, rows, cells, List.of());
    }

    private List<TimetableSheet> teacherSheets(
            ScenarioSnapshot snapshot,
            List<ScheduledCourse> scheduledCourses,
            TimetableExportRequest request,
            ExportFilters filters,
            List<DayOfWeek> days,
            List<RowHeader> rows,
            Set<Timeslot> scenarioTimeslots,
            Locale locale) {
        List<Teacher> teachers = request.selection() == ExportSelection.SINGLE
                ? List.of(findTeacher(snapshot, request.entityName()))
                : snapshot.teachers().stream()
                        .sorted(Comparator.comparing(Teacher::value, NAME_ORDER))
                        .toList();
        List<TimetableSheet> sheets = new ArrayList<>();
        for (Teacher teacher : teachers) {
            sheets.add(teacherSheet(teacher, scheduledCourses, filters, days, rows, scenarioTimeslots, locale));
        }
        return sheets;
    }

    private TimetableSheet teacherSheet(
            Teacher teacher,
            List<ScheduledCourse> scheduledCourses,
            ExportFilters filters,
            List<DayOfWeek> days,
            List<RowHeader> rows,
            Set<Timeslot> scenarioTimeslots,
            Locale locale) {
        Map<CellKey, TimetableCell> cells = new LinkedHashMap<>();
        for (int row = 0; row < rows.size(); row++) {
            for (int column = 0; column < days.size(); column++) {
                Timeslot timeslot = timeslot(days, rows, row, column);
                if (!scenarioTimeslots.contains(timeslot)) {
                    cells.put(new CellKey(row, column), TimetableCell.notATimeslot());
                    continue;
                }
                List<TimetableCellEntry> entries = entriesFor(scheduledCourses, timeslot, filters).stream()
                        .filter(entry -> equalsIgnoreCase(entry.teacher(), teacher.value()))
                        .toList();
                cells.put(new CellKey(row, column),
                        entries.isEmpty() ? TimetableCell.free() : TimetableCell.course(entries));
            }
        }
        int count = cells.values().stream().filter(TimetableCell::hasCourses)
                .mapToInt(cell -> cell.entries().size()).sum();
        return new TimetableSheet(ExportAudience.TEACHER, teacher.value(), "Teacher: " + teacher.value(),
                count == 0 ? "No scheduled courses" : count + " scheduled courses",
                days, rows, cells, List.of());
    }

    private List<TimetableSheet> studentGroupSheets(
            ScenarioSnapshot snapshot,
            List<ScheduledCourse> scheduledCourses,
            TimetableExportRequest request,
            ExportFilters filters,
            List<DayOfWeek> days,
            List<RowHeader> rows,
            Set<Timeslot> scenarioTimeslots,
            Locale locale) {
        List<StudentGroup> groups = request.selection() == ExportSelection.SINGLE
                ? List.of(findStudentGroup(snapshot, request.entityName()))
                : snapshot.studentGroups().stream()
                        .sorted(Comparator.comparing(StudentGroup::value, NAME_ORDER))
                        .toList();
        List<TimetableSheet> sheets = new ArrayList<>();
        for (StudentGroup group : groups) {
            sheets.add(studentGroupSheet(group, scheduledCourses, filters, days, rows, scenarioTimeslots, locale));
        }
        return sheets;
    }

    private TimetableSheet studentGroupSheet(
            StudentGroup group,
            List<ScheduledCourse> scheduledCourses,
            ExportFilters filters,
            List<DayOfWeek> days,
            List<RowHeader> rows,
            Set<Timeslot> scenarioTimeslots,
            Locale locale) {
        Map<CellKey, TimetableCell> cells = new LinkedHashMap<>();
        for (int row = 0; row < rows.size(); row++) {
            for (int column = 0; column < days.size(); column++) {
                Timeslot timeslot = timeslot(days, rows, row, column);
                if (!scenarioTimeslots.contains(timeslot)) {
                    cells.put(new CellKey(row, column), TimetableCell.notATimeslot());
                    continue;
                }
                List<TimetableCellEntry> entries = entriesFor(scheduledCourses, timeslot, filters).stream()
                        .filter(entry -> entry.studentGroups().stream()
                                .anyMatch(name -> equalsIgnoreCase(name, group.value())))
                        .toList();
                cells.put(new CellKey(row, column),
                        entries.isEmpty() ? TimetableCell.free() : TimetableCell.course(entries));
            }
        }
        int count = cells.values().stream().filter(TimetableCell::hasCourses)
                .mapToInt(cell -> cell.entries().size()).sum();
        return new TimetableSheet(ExportAudience.STUDENT_GROUP, group.value(),
                "Student group: " + group.value(),
                "Group size: " + group.size() + " · "
                        + (count == 0 ? "No scheduled courses" : count + " scheduled courses"),
                days, rows, cells, List.of());
    }

    private List<TimetableSheet> classroomSheets(
            ScenarioSnapshot snapshot,
            List<ScheduledCourse> scheduledCourses,
            TimetableExportRequest request,
            ExportFilters filters,
            List<DayOfWeek> days,
            List<RowHeader> rows,
            Set<Timeslot> scenarioTimeslots,
            Locale locale) {
        List<Classroom> classrooms = request.selection() == ExportSelection.SINGLE
                ? List.of(findClassroom(snapshot, request.entityName()))
                : snapshot.classrooms().stream()
                        .sorted(Comparator.comparing(Classroom::name, NAME_ORDER))
                        .toList();
        List<TimetableSheet> sheets = new ArrayList<>();
        for (Classroom classroom : classrooms) {
            sheets.add(classroomSheet(classroom, scheduledCourses, filters, days, rows, scenarioTimeslots, locale));
        }
        return sheets;
    }

    private TimetableSheet classroomSheet(
            Classroom classroom,
            List<ScheduledCourse> scheduledCourses,
            ExportFilters filters,
            List<DayOfWeek> days,
            List<RowHeader> rows,
            Set<Timeslot> scenarioTimeslots,
            Locale locale) {
        Map<CellKey, TimetableCell> cells = new LinkedHashMap<>();
        int scheduled = 0;
        int blocked = 0;
        int free = 0;
        Map<DayOfWeek, List<String>> freeTimesByDay = new LinkedHashMap<>();
        for (int row = 0; row < rows.size(); row++) {
            for (int column = 0; column < days.size(); column++) {
                DayOfWeek day = days.get(column);
                Timeslot timeslot = timeslot(days, rows, row, column);
                if (!scenarioTimeslots.contains(timeslot)) {
                    cells.put(new CellKey(row, column), TimetableCell.notATimeslot());
                    continue;
                }
                List<TimetableCellEntry> entries = entriesFor(scheduledCourses, timeslot, filters).stream()
                        .filter(entry -> equalsIgnoreCase(entry.classroom(), classroom.name()))
                        .toList();
                TimetableCell cell;
                if (!entries.isEmpty()) {
                    cell = TimetableCell.course(entries);
                    scheduled += entries.size();
                } else if (classroom.occupiedTimeslots().contains(timeslot)) {
                    cell = TimetableCell.blocked();
                    blocked++;
                } else {
                    cell = TimetableCell.free();
                    free++;
                    freeTimesByDay.computeIfAbsent(day, key -> new ArrayList<>())
                            .add(ExportText.time(timeslot.startTime()));
                }
                cells.put(new CellKey(row, column), cell);
            }
        }
        List<String> notes = new ArrayList<>();
        notes.add("Legend: Course, Blocked, Free, No period.");
        notes.add(freeSummary(freeTimesByDay, locale));
        return new TimetableSheet(ExportAudience.CLASSROOM, classroom.name(),
                "Classroom: " + classroom.name(),
                "Capacity: " + classroom.capacity() + " · " + scheduled + " scheduled courses · "
                        + blocked + " blocked periods · " + free + " free periods",
                days, rows, cells, notes);
    }

    private String freeSummary(Map<DayOfWeek, List<String>> freeTimesByDay, Locale locale) {
        if (freeTimesByDay.isEmpty()) {
            return "Free periods: none";
        }
        return "Free periods: " + freeTimesByDay.entrySet().stream()
                .map(entry -> ExportText.shortDayName(entry.getKey(), locale) + " "
                        + String.join(", ", entry.getValue()))
                .collect(Collectors.joining("; "));
    }

    private List<TimetableCellEntry> entriesFor(
            List<ScheduledCourse> scheduledCourses,
            Timeslot timeslot,
            ExportFilters filters) {
        return scheduledCourses.stream()
                .filter(course -> timeslot.equals(course.timeslot()))
                .map(this::toEntry)
                .filter(filters::matches)
                .toList();
    }

    private TimetableCellEntry toEntry(ScheduledCourse scheduledCourse) {
        Course course = scheduledCourse.course();
        return new TimetableCellEntry(
                course.subject().value(),
                course.teacher().value(),
                scheduledCourse.classroom().name(),
                course.studentGroups().stream().map(StudentGroup::value).toList(),
                course.numberOfParticipant());
    }

    private Timeslot timeslot(List<DayOfWeek> days, List<RowHeader> rows, int row, int column) {
        return new Timeslot(days.get(column), rows.get(row).startTime());
    }

    private Teacher findTeacher(ScenarioSnapshot snapshot, String name) {
        return snapshot.teachers().stream()
                .filter(teacher -> equalsIgnoreCase(teacher.value(), name))
                .findFirst()
                .orElse(new Teacher(name == null ? "" : name));
    }

    private StudentGroup findStudentGroup(ScenarioSnapshot snapshot, String name) {
        return snapshot.studentGroups().stream()
                .filter(group -> equalsIgnoreCase(group.value(), name))
                .findFirst()
                .orElse(new StudentGroup(name == null ? "" : name, 0));
    }

    private Classroom findClassroom(ScenarioSnapshot snapshot, String name) {
        return snapshot.classrooms().stream()
                .filter(classroom -> equalsIgnoreCase(classroom.name(), name))
                .findFirst()
                .orElse(new Classroom(name == null ? "" : name, 0, List.of()));
    }

    private static boolean equalsIgnoreCase(String expected, String actual) {
        return actual != null && expected != null && expected.equalsIgnoreCase(actual);
    }
}
