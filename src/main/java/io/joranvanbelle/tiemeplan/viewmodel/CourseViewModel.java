package io.joranvanbelle.tiemeplan.viewmodel;

import io.joranvanbelle.tiemeplan.model.CourseDto;
import io.joranvanbelle.tiemeplan.model.SubjectDto;
import io.joranvanbelle.tiemeplan.model.TeacherDto;
import io.joranvanbelle.tiemeplan.variables.Course;
import io.joranvanbelle.tiemeplan.variables.Subject;
import io.joranvanbelle.tiemeplan.variables.Teacher;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.UUID;

public class CourseViewModel {

    private final StringProperty id = new SimpleStringProperty("");
    private final StringProperty teacher = new SimpleStringProperty("");
    private final StringProperty subject = new SimpleStringProperty("");
    private final ObservableList<StudentGroupViewModel> studentGroups = FXCollections
            .observableArrayList(group -> new Observable[] { group.nameProperty(), group.sizeProperty() });
    private final IntegerBinding participantCount = Bindings.createIntegerBinding(
            () -> studentGroups.stream().mapToInt(StudentGroupViewModel::getSize).sum(), studentGroups);

    public CourseViewModel() {
    }

    public CourseViewModel(String teacher, String subject) {
        this.id.set(UUID.randomUUID().toString());
        this.teacher.set(teacher);
        this.subject.set(subject);
    }

    public CourseViewModel(String id, String teacher, String subject) {
        this.id.set(id);
        this.teacher.set(teacher);
        this.subject.set(subject);
    }

    public static CourseViewModel of(Course course) {
        var viewModel = new CourseViewModel(course.id(), course.teacher().value(), course.subject().value());
        course.studentGroups().stream().map(StudentGroupViewModel::of).forEach(viewModel.studentGroups::add);
        return viewModel;
    }

    public static CourseViewModel of(CourseDto dto) {
        var viewModel = new CourseViewModel(dto.id(), dto.teacher().value(), dto.subject().value());
        dto.studentGroups().stream().map(StudentGroupViewModel::of).forEach(viewModel.studentGroups::add);
        return viewModel;
    }

    public StringProperty teacherProperty() {
        return teacher;
    }

    public String getTeacher() {
        return teacher.get();
    }

    public void setTeacher(String value) {
        teacher.set(value);
    }

    public StringProperty subjectProperty() {
        return subject;
    }

    public String getSubject() {
        return subject.get();
    }

    public void setSubject(String value) {
        subject.set(value);
    }

    public ObservableList<StudentGroupViewModel> studentGroups() {
        return studentGroups;
    }

    public IntegerBinding participantCountBinding() {
        return participantCount;
    }

    public int getParticipantCount() {
        return participantCount.get();
    }

    public Course toDomain() {
        return new Course(id.get(), new Teacher(teacher.get()), new Subject(subject.get()),
                studentGroups.stream().map(StudentGroupViewModel::toDomain).toList());
    }
}
