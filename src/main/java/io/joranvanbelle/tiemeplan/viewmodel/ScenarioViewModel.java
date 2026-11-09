package io.joranvanbelle.tiemeplan.viewmodel;

import io.joranvanbelle.tiemeplan.model.ScenarioDto;
import io.joranvanbelle.tiemeplan.model.ScenarioMapper;
import io.joranvanbelle.tiemeplan.model.SubjectDto;
import io.joranvanbelle.tiemeplan.model.TeacherDto;
import io.joranvanbelle.tiemeplan.variables.Classroom;
import io.joranvanbelle.tiemeplan.variables.Course;
import io.joranvanbelle.tiemeplan.variables.StudentGroup;
import io.joranvanbelle.tiemeplan.variables.Subject;
import io.joranvanbelle.tiemeplan.variables.Teacher;
import io.joranvanbelle.tiemeplan.variables.Timeslot;

import java.util.List;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ScenarioViewModel {

    private final ObservableList<ClassroomViewModel> classrooms = FXCollections.observableArrayList(
            classroom -> new Observable[] { classroom.nameProperty(), classroom.capacityProperty(),
                    classroom.occupiedTimeslots() });
    private final ObservableList<TimeslotViewModel> timeslots = FXCollections.observableArrayList(
            timeslot -> new Observable[] { timeslot.dayOfWeekProperty(), timeslot.startTimeProperty() });
    private final ObservableList<CourseViewModel> courses = FXCollections.observableArrayList(
            course -> new Observable[] { course.teacherProperty(), course.subjectProperty(), course.studentGroups() });
    private final ObservableList<String> teachers = FXCollections.observableArrayList();
    private final ObservableList<String> subjects = FXCollections.observableArrayList();
    private final ObservableList<StudentGroupViewModel> studentGroups = FXCollections.observableArrayList(
            group -> new Observable[] { group.nameProperty(), group.sizeProperty() });

    private int schemaVersion = ScenarioDto.CURRENT_SCHEMA_VERSION;

    public ObservableList<ClassroomViewModel> classrooms() {
        return classrooms;
    }

    public ObservableList<TimeslotViewModel> timeslots() {
        return timeslots;
    }

    public ObservableList<CourseViewModel> courses() {
        return courses;
    }

    public ObservableList<String> teachers() {
        return teachers;
    }

    public ObservableList<String> subjects() {
        return subjects;
    }

    public ObservableList<StudentGroupViewModel> studentGroups() {
        return studentGroups;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void moveCourse(int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex >= courses.size() || toIndex < 0 || toIndex >= courses.size()) {
            throw new IndexOutOfBoundsException(
                    "Cannot move course from %d to %d (size %d)".formatted(fromIndex, toIndex, courses.size()));
        }
        if (fromIndex != toIndex) {
            var course = courses.remove(fromIndex);
            courses.add(toIndex, course);
        }
    }

    public void moveCourseUp(int index) {
        if (index > 0) {
            moveCourse(index, index - 1);
        }
    }

    public void moveCourseDown(int index) {
        if (index < courses.size() - 1) {
            moveCourse(index, index + 1);
        }
    }

    public ScenarioDto toDto() {
        return new ScenarioDto(schemaVersion,
                io.joranvanbelle.tiemeplan.util.AppInfo.getVersion(),
                courses.stream().map(c -> ScenarioMapper.toDto(c.toDomain())).toList(),
                classrooms.stream().map(c -> ScenarioMapper.toDto(c.toDomain())).toList(),
                timeslots.stream().map(t -> ScenarioMapper.toDto(t.toDomain())).toList(),
                teachers.stream().map(t -> ScenarioMapper.toDto(new Teacher(t))).toList(),
                subjects.stream().map(s -> ScenarioMapper.toDto(new Subject(s))).toList(),
                studentGroups.stream().map(g -> ScenarioMapper.toDto(g.toDomain())).toList());
    }

    public void loadFrom(ScenarioDto dto) {
        schemaVersion = dto.schemaVersion();
        teachers.setAll(dto.teachers().stream().map(TeacherDto::value).toList());
        subjects.setAll(dto.subjects().stream().map(SubjectDto::value).toList());
        studentGroups.setAll(dto.studentGroups().stream().map(StudentGroupViewModel::of).toList());
        timeslots.setAll(dto.timeslots().stream().map(TimeslotViewModel::of).toList());
        classrooms.setAll(dto.classrooms().stream().map(ClassroomViewModel::of).toList());
        courses.setAll(dto.courses().stream().map(CourseViewModel::of).toList());
    }

    public List<Course> toDomainCourses() {
        return courses.stream().map(CourseViewModel::toDomain).toList();
    }

    public List<Classroom> toDomainClassrooms() {
        return classrooms.stream().map(ClassroomViewModel::toDomain).toList();
    }

    public List<Timeslot> toDomainTimeslots() {
        return timeslots.stream().map(TimeslotViewModel::toDomain).toList();
    }

    public List<Teacher> toDomainTeachers() {
        return teachers.stream().map(Teacher::new).toList();
    }

    public List<Subject> toDomainSubjects() {
        return subjects.stream().map(Subject::new).toList();
    }

    public List<StudentGroup> toDomainStudentGroups() {
        return studentGroups.stream().map(StudentGroupViewModel::toDomain).toList();
    }
}
