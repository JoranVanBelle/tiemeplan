package io.joranvanbelle.tiemeplan.viewmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class ScenarioValidator {

    private ScenarioValidator() {
    }

    public static List<ValidationIssue> validate(ScenarioViewModel scenario) {
        var issues = new ArrayList<ValidationIssue>();

        if (scenario.timeslots().isEmpty()) {
            issues.add(error(ValidationArea.SETUP, "scenario.no-timeslots", "Add at least one timeslot."));
        }
        if (scenario.classrooms().isEmpty()) {
            issues.add(error(ValidationArea.SETUP, "scenario.no-classrooms", "Add at least one classroom."));
        }
        if (scenario.courses().isEmpty()) {
            issues.add(error(ValidationArea.COURSES, "scenario.no-courses", "Add at least one course."));
        }

        validateClassrooms(scenario, issues);
        validateTimeslots(scenario, issues);
        validateStudentGroups(scenario, issues);
        validateNames(scenario.teachers(), "Teacher", "teacher.duplicate", ValidationArea.SETUP, issues);
        validateNames(scenario.subjects(), "Subject", "subject.duplicate", ValidationArea.SETUP, issues);
        validateCourses(scenario, issues);
        validateUnused(scenario, issues);

        return List.copyOf(issues);
    }

    private static void validateClassrooms(ScenarioViewModel scenario, List<ValidationIssue> issues) {
        validateNames(scenario.classrooms().stream().map(ClassroomViewModel::getName).toList(), "Classroom",
                "classroom.duplicate", ValidationArea.SETUP, issues);

        var definedTimeslots = new HashSet<>(scenario.toDomainTimeslots());

        for (var classroom : scenario.classrooms()) {
            if (isBlank(classroom.getName())) {
                issues.add(error(ValidationArea.SETUP, "classroom.name-blank", "Classroom name cannot be blank."));
            }
            if (classroom.getCapacity() <= 0) {
                issues.add(error(ValidationArea.SETUP, "classroom.capacity",
                        "Classroom '%s' must have a positive capacity.".formatted(label(classroom.getName()))));
            }
            if (!definedTimeslots.isEmpty()) {
                classroom.occupiedTimeslots().stream()
                        .filter(timeslot -> !definedTimeslots.contains(timeslot.toDomain()))
                        .forEach(timeslot -> issues.add(error(ValidationArea.SETUP, "classroom.unknown-timeslot",
                                "Classroom '%s' is unavailable at %s %s, which is not a defined timeslot."
                                        .formatted(label(classroom.getName()), timeslot.getDayOfWeek(),
                                                timeslot.getStartTime()))));
            }
        }
    }

    private static void validateTimeslots(ScenarioViewModel scenario, List<ValidationIssue> issues) {
        var seen = new HashSet<String>();
        for (var timeslot : scenario.timeslots()) {
            if (timeslot.getDayOfWeek() == null || timeslot.getStartTime() == null) {
                issues.add(error(ValidationArea.SETUP, "timeslot.incomplete",
                        "Every timeslot needs a day and a start time."));
                continue;
            }
            var key = timeslot.getDayOfWeek() + "-" + timeslot.getStartTime();
            if (!seen.add(key)) {
                issues.add(error(ValidationArea.SETUP, "timeslot.duplicate",
                        "Timeslot %s %s is defined more than once.".formatted(timeslot.getDayOfWeek(),
                                timeslot.getStartTime())));
            }
        }
    }

    private static void validateStudentGroups(ScenarioViewModel scenario, List<ValidationIssue> issues) {
        validateNames(scenario.studentGroups().stream().map(StudentGroupViewModel::getName).toList(),
                "Student group", "studentGroup.duplicate", ValidationArea.SETUP, issues);

        for (var group : scenario.studentGroups()) {
            validateStudentGroup(group, "studentGroup", ValidationArea.SETUP, issues);
        }
    }

    private static void validateStudentGroup(StudentGroupViewModel group, String codePrefix, ValidationArea area,
            List<ValidationIssue> issues) {
        if (isBlank(group.getName())) {
            issues.add(error(area, codePrefix + ".name-blank", "Student group name cannot be blank."));
        }
        if (group.getSize() <= 0) {
            issues.add(error(area, codePrefix + ".size",
                    "Student group '%s' must have a positive size.".formatted(label(group.getName()))));
        }
    }

    /*
    TODO - validate if next course is known (course.next-unknown)
        validate if next course is not yourself (course.next-self)
        validate if 2 courses do not reference the same course as next (course.next-two-predecessors)
        validate for cycles (infinite loop CourseA -> CourseB -> CourseC -> CourseA) (course.next-cycle)
        validate if chain is not longer then amount of timeslots (chain length > timeslot count) (course.chain-too-long)
     */
    private static void validateCourses(ScenarioViewModel scenario, List<ValidationIssue> issues) {
        var hasAnyClassroom = !scenario.classrooms().isEmpty();

        for (var course : scenario.courses()) {
            var courseLabel = "%s / %s".formatted(label(course.getTeacher()), label(course.getSubject()));

            if (isBlank(course.getTeacher())) {
                issues.add(error(ValidationArea.COURSES, "course.teacher-blank", "Every course needs a teacher."));
            }
            if (isBlank(course.getSubject())) {
                issues.add(error(ValidationArea.COURSES, "course.subject-blank", "Every course needs a subject."));
            }
            if (course.studentGroups().isEmpty()) {
                issues.add(error(ValidationArea.COURSES, "course.no-groups",
                        "Course '%s' needs at least one student group.".formatted(courseLabel)));
            }

            for (var group : course.studentGroups()) {
                validateStudentGroup(group, "course.group", ValidationArea.COURSES, issues);
            }

            if (hasAnyClassroom) {
                var fitsAnyClassroom = scenario.classrooms().stream()
                        .anyMatch(classroom -> classroom.getCapacity() >= course.getParticipantCount());
                if (!fitsAnyClassroom) {
                    issues.add(error(ValidationArea.COURSES, "course.no-classroom-capacity",
                            "Course '%s' has %d participants but no classroom is large enough."
                                    .formatted(courseLabel, course.getParticipantCount())));
                }
            }
        }
    }

    private static void validateUnused(ScenarioViewModel scenario, List<ValidationIssue> issues) {
        if (scenario.courses().isEmpty()) {
            return;
        }

        var usedTeachers = normalizedSet(scenario.courses().stream().map(CourseViewModel::getTeacher).toList());
        var usedSubjects = normalizedSet(scenario.courses().stream().map(CourseViewModel::getSubject).toList());
        var usedStudentGroups = normalizedSet(scenario.courses().stream()
                .flatMap(course -> course.studentGroups().stream())
                .map(StudentGroupViewModel::getName)
                .toList());

        reportUnused(scenario.teachers(), usedTeachers, "teacher.unused", "Teacher", issues);
        reportUnused(scenario.subjects(), usedSubjects, "subject.unused", "Subject", issues);
        reportUnused(scenario.studentGroups().stream().map(StudentGroupViewModel::getName).toList(), usedStudentGroups,
                "studentGroup.unused", "Student group", issues);
    }

    private static void reportUnused(List<String> names, Set<String> used, String code, String label,
            List<ValidationIssue> issues) {
        names.stream()
                .filter(name -> !isBlank(name) && !used.contains(normalize(name)))
                .forEach(name -> issues.add(warning(ValidationArea.SETUP, code,
                        "%s '%s' is not used by any course.".formatted(label, name.trim()))));
    }

    private static void validateNames(List<String> names, String label, String duplicateCode, ValidationArea area,
            List<ValidationIssue> issues) {
        var counts = new HashMap<String, Integer>();
        names.stream()
                .filter(name -> !isBlank(name))
                .map(ScenarioValidator::normalize)
                .forEach(name -> counts.merge(name, 1, Integer::sum));
        counts.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .forEach(entry -> issues.add(error(area, duplicateCode,
                        "%s name is duplicated: '%s'.".formatted(label, entry.getKey()))));
    }

    private static Set<String> normalizedSet(List<String> values) {
        var result = new HashSet<String>();
        values.stream().filter(value -> !isBlank(value)).map(ScenarioValidator::normalize).forEach(result::add);
        return result;
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String label(String value) {
        return isBlank(value) ? "(unnamed)" : value.trim();
    }

    private static ValidationIssue error(ValidationArea area, String code, String message) {
        return new ValidationIssue(code, ValidationSeverity.ERROR, area, message);
    }

    private static ValidationIssue warning(ValidationArea area, String code, String message) {
        return new ValidationIssue(code, ValidationSeverity.WARNING, area, message);
    }
}
