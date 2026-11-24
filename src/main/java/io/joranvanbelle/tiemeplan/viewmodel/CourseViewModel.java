package io.joranvanbelle.tiemeplan.viewmodel;

import io.joranvanbelle.tiemeplan.model.CourseDto;
import io.joranvanbelle.tiemeplan.util.StringUtil;
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
    private final StringProperty nextCourseId = new SimpleStringProperty("");

    public CourseViewModel() {
    }

    public CourseViewModel(String teacher, String subject) {
        this.id.set(UUID.randomUUID().toString());
        this.teacher.set(teacher);
        this.subject.set(subject);
        this.nextCourseId.set(null);
    }

    public CourseViewModel(String id, String teacher, String subject, String nextCourseId) {
        this.id.set(id);
        this.teacher.set(teacher);
        this.subject.set(subject);
        this.nextCourseId.set(nextCourseId);
    }

    public static CourseViewModel of(Course course) {
        var viewModel = new CourseViewModel(course.id(), course.teacher().value(), course.subject().value(), course.nextCourseId());
        course.studentGroups().stream().map(StudentGroupViewModel::of).forEach(viewModel.studentGroups::add);
        return viewModel;
    }

    public static CourseViewModel of(CourseDto dto) {
        var viewModel = new CourseViewModel(dto.id(), dto.teacher().value(), dto.subject().value(), dto.nextCourseId());
        dto.studentGroups().stream().map(StudentGroupViewModel::of).forEach(viewModel.studentGroups::add);
        return viewModel;
    }

    public StringProperty teacherProperty() {
        return teacher;
    }

    public String id() {
        return id.get();
    }

    public String teacher() {
        return teacher.get();
    }

    public void teacher(String value) {
        teacher.set(value);
    }

    public StringProperty subjectProperty() {
        return subject;
    }

    public String subject() {
        return subject.get();
    }

    public void subject(String value) {
        subject.set(value);
    }

    public ObservableList<StudentGroupViewModel> studentGroups() {
        return studentGroups;
    }

    public IntegerBinding participantCountBinding() {
        return participantCount;
    }

    public int participantCount() {
        return participantCount.get();
    }

    public StringProperty nextCourseIdProperty() {
        return nextCourseId;
    }

    public String nextCourseId() {
        return nextCourseId.get();
    }

    public String courseLabel() {
        return "%s / %s".formatted(StringUtil.label(teacher.get()), StringUtil.label(subject.get()));
    }

    public Course toDomain() {
        return Course.newBuilder()
            .id(id.get())
            .teacher(new Teacher(teacher.get()))
            .subject(new Subject(subject.get()))
            .studentGroup(studentGroups.stream().map(StudentGroupViewModel::toDomain).toList())
            .nextCourseId(nextCourseId.get())
            .build();
    }
}
