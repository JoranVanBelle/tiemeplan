package io.joranvanbelle.tiemeplan.viewmodel;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScenarioValidatorTest {

    @Test
    void validScenarioHasNoIssues() {
        assertThat(ScenarioValidator.validate(validScenario())).isEmpty();
    }

    @Test
    void reportsMissingSections() {
        var codes = errorCodes(new ScenarioViewModel());

        assertThat(codes).contains("scenario.no-timeslots", "scenario.no-classrooms", "scenario.no-courses");
    }

    @Test
    void issuesIdentifyTheirNavigationArea() {
        var issues = ScenarioValidator.validate(new ScenarioViewModel());

        assertThat(issues).anyMatch(issue -> issue.code().equals("scenario.no-classrooms")
                && issue.area() == ValidationArea.SETUP);
        assertThat(issues).anyMatch(issue -> issue.code().equals("scenario.no-courses")
                && issue.area() == ValidationArea.COURSES);
    }

    @Test
    void detectsDuplicateNamesIgnoringCaseAndWhitespace() {
        var scenario = validScenario();
        scenario.teachers().addAll(List.of(" ms. smith "));

        assertThat(errorCodes(scenario)).contains("teacher.duplicate");
    }

    @Test
    void detectsInvalidCapacitiesAndSizes() {
        var scenario = validScenario();
        scenario.classrooms().getFirst().setCapacity(0);
        scenario.studentGroups().getFirst().setSize(0);

        assertThat(errorCodes(scenario)).contains("classroom.capacity", "studentGroup.size");
    }

    @Test
    void detectsIncompleteCourse() {
        var scenario = validScenario();
        var course = scenario.courses().getFirst();
        course.setTeacher(" ");
        course.setSubject("");
        course.studentGroups().clear();

        assertThat(errorCodes(scenario)).contains("course.teacher-blank", "course.subject-blank", "course.no-groups");
    }

    @Test
    void detectsCourseThatFitsNoClassroom() {
        var scenario = validScenario();
        scenario.classrooms().getFirst().setCapacity(5);

        assertThat(errorCodes(scenario)).contains("course.no-classroom-capacity");
    }

    @Test
    void detectsClassroomTimeslotThatIsNotDefined() {
        var scenario = validScenario();
        scenario.classrooms().getFirst().occupiedTimeslots()
                .add(new TimeslotViewModel(DayOfWeek.TUESDAY, LocalTime.of(10, 0)));

        assertThat(errorCodes(scenario)).contains("classroom.unknown-timeslot");
    }

    @Test
    void warnsAboutUnusedPeople() {
        var scenario = validScenario();
        scenario.teachers().add("Mr. Unused");

        assertThat(codes(scenario, ValidationSeverity.WARNING)).contains("teacher.unused");
    }

    private ScenarioViewModel validScenario() {
        var scenario = new ScenarioViewModel();
        scenario.timeslots().add(new TimeslotViewModel(DayOfWeek.MONDAY, LocalTime.of(10, 0)));
        scenario.classrooms().add(new ClassroomViewModel("room1", 30));
        scenario.teachers().add("Ms. Smith");
        scenario.subjects().add("Math");
        scenario.studentGroups().add(new StudentGroupViewModel("1A", 10));

        var course = new CourseViewModel("Ms. Smith", "Math");
        course.studentGroups().add(new StudentGroupViewModel("1A", 10));
        scenario.courses().add(course);
        return scenario;
    }

    private List<String> errorCodes(ScenarioViewModel scenario) {
        return codes(scenario, ValidationSeverity.ERROR);
    }

    private List<String> codes(ScenarioViewModel scenario, ValidationSeverity severity) {
        return ScenarioValidator.validate(scenario).stream()
                .filter(issue -> issue.severity() == severity)
                .map(ValidationIssue::code)
                .toList();
    }
}
