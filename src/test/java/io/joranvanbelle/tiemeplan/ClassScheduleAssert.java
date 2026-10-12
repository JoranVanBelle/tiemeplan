package io.joranvanbelle.tiemeplan;

import io.joranvanbelle.tiemeplan.backtrack.ClassSchedule;
import io.joranvanbelle.tiemeplan.variables.Classroom;
import org.assertj.core.api.AbstractAssert;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ClassScheduleAssert extends AbstractAssert<ClassScheduleAssert, ClassSchedule> {


    protected ClassScheduleAssert(ClassSchedule actual) {
        super(actual, ClassScheduleAssert.class);
    }

    public static ClassScheduleAssert assertThat(ClassSchedule actual) {
        return new ClassScheduleAssert(actual);
    }

    public void hasNoCourses() {
        isNotNull();

        int actualSize = actual.assignedCourses().size();

        if (actualSize != 0) {
            failWithMessage(
                    "Expected 0 assigned courses but found <%s>",
                    actualSize
            );
        }
    }

    public ClassScheduleAssert hasNumberOfCourses(int expectedSize) {
        isNotNull();

        int actualSize = actual.assignedCourses().size();

        if (actualSize != expectedSize) {
            failWithMessage(
                    "Expected <%s> assigned courses but found <%s>",
                    expectedSize, actualSize
            );
        }

        return this;
    }

    public ClassScheduleAssert containsOnlyScheduledCoursed(ClassSchedule.ScheduledCourse... expectedCourses) {
        isNotNull();

        var expectedCoursesList = Arrays.stream(expectedCourses)
                .sorted(Comparator.comparing((ClassSchedule.ScheduledCourse c) -> c.timeslot().value())
                        .thenComparing(c -> c.classroom().value()))
                .toList();

        List<ClassSchedule.ScheduledCourse> actualCourses = actual.assignedCourses();
        actualCourses.sort(Comparator.comparing((ClassSchedule.ScheduledCourse c) -> c.timeslot().value())
                .thenComparing(c -> c.classroom().value()));

        if (!actualCourses.equals(expectedCoursesList)) {
                failWithMessage(
                        """
                        Expected assigned courses:
                        %s
        
                        Actual assigned courses:
                        %s
                        """,
                        formatCourses(expectedCoursesList),
                        formatCourses(actualCourses)
                );
        }

        return this;
    }

    private String formatCourses(
            List<ClassSchedule.ScheduledCourse> courses
    ) {
        if (courses.isEmpty()) {
            return "  (none)";
        }

        return courses.stream()
                .map(course -> """
                      - Classroom: %s
                        Timeslot:  %s
                        Course:    %s""".formatted(
                        course.classroom(),
                        course.timeslot(),
                        course.course()
                ))
                .collect(Collectors.joining("\n"));
    }
}
