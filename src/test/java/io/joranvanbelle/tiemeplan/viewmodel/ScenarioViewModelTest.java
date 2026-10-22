package io.joranvanbelle.tiemeplan.viewmodel;

import io.joranvanbelle.tiemeplan.model.ScenarioDto;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScenarioViewModelTest {

    @Test
    void moveCourseReordersWithoutLosingEntries() {
        var scenario = scenarioWithCourses("A", "B", "C");

        scenario.moveCourse(0, 2);

        assertThat(courseSubjects(scenario)).containsExactly("B", "C", "A");
    }

    @Test
    void moveCourseUpAndDownRespectBoundaries() {
        var scenario = scenarioWithCourses("A", "B", "C");

        scenario.moveCourseUp(0);
        scenario.moveCourseDown(2);
        assertThat(courseSubjects(scenario)).containsExactly("A", "B", "C");

        scenario.moveCourseDown(0);
        assertThat(courseSubjects(scenario)).containsExactly("B", "A", "C");

        scenario.moveCourseUp(2);
        assertThat(courseSubjects(scenario)).containsExactly("B", "C", "A");
    }

    @Test
    void moveCourseRejectsOutOfBounds() {
        var scenario = scenarioWithCourses("A");

        assertThatThrownBy(() -> scenario.moveCourse(0, 1)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> scenario.moveCourse(-1, 0)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void participantCountUpdatesWhenGroupSizeChanges() {
        var course = new CourseViewModel("Ms. Smith", "Math");
        var group = new StudentGroupViewModel("1A", 10);
        course.studentGroups().add(group);

        assertThat(course.getParticipantCount()).isEqualTo(10);

        group.setSize(15);
        assertThat(course.getParticipantCount()).isEqualTo(15);
    }

    @Test
    void dtoRoundTripPreservesCoursesOrderAndValues() {
        var scenario = scenarioWithCourses("A", "B", "C");
        scenario.timeslots().add(new TimeslotViewModel(DayOfWeek.MONDAY, LocalTime.of(10, 0)));
        scenario.teachers().add("Ms. Smith");
        scenario.subjects().addAll(List.of("A", "B", "C"));
        scenario.studentGroups().add(new StudentGroupViewModel("1A", 10));

        ScenarioDto dto = scenario.toDto();
        var reloaded = new ScenarioViewModel();
        reloaded.loadFrom(dto);

        assertThat(courseSubjects(reloaded)).containsExactly("A", "B", "C");
        assertThat(reloaded.toDto()).isEqualTo(dto);
    }

    private ScenarioViewModel scenarioWithCourses(String... subjects) {
        var scenario = new ScenarioViewModel();
        for (var subject : subjects) {
            var course = new CourseViewModel("Ms. Smith", subject);
            course.studentGroups().add(new StudentGroupViewModel("1A", 10));
            scenario.courses().add(course);
        }
        return scenario;
    }

    private List<String> courseSubjects(ScenarioViewModel scenario) {
        return scenario.courses().stream().map(CourseViewModel::getSubject).toList();
    }
}
