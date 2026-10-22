package io.joranvanbelle.tiemeplan.model;

import io.joranvanbelle.tiemeplan.variables.Classroom;
import io.joranvanbelle.tiemeplan.variables.Course;
import io.joranvanbelle.tiemeplan.variables.StudentGroup;
import io.joranvanbelle.tiemeplan.variables.Subject;
import io.joranvanbelle.tiemeplan.variables.Teacher;
import io.joranvanbelle.tiemeplan.variables.Timeslot;

import java.util.List;

public final class ScenarioMapper {

    private ScenarioMapper() {
    }

    public static Classroom toDomain(ClassroomDto dto) {
        return new Classroom(dto.name(), dto.capacity(),
                dto.occupiedTimeslots().stream().map(ScenarioMapper::toDomain).toList());
    }

    public static ClassroomDto toDto(Classroom classroom) {
        return new ClassroomDto(classroom.name(), classroom.capacity(),
                classroom.occupiedTimeslots().stream().map(ScenarioMapper::toDto).toList());
    }

    public static Course toDomain(CourseDto dto) {
        return new Course(toDomain(dto.teacher()), toDomain(dto.subject()),
                dto.studentGroups().stream().map(ScenarioMapper::toDomain).toList());
    }

    public static CourseDto toDto(Course course) {
        return new CourseDto(toDto(course.teacher()), toDto(course.subject()),
                course.studentGroups().stream().map(ScenarioMapper::toDto).toList());
    }

    public static Timeslot toDomain(TimeslotDto dto) {
        return new Timeslot(dto.dayOfWeek(), dto.startTime());
    }

    public static TimeslotDto toDto(Timeslot timeslot) {
        return new TimeslotDto(timeslot.dayOfWeek(), timeslot.startTime());
    }

    public static Teacher toDomain(TeacherDto dto) {
        return new Teacher(dto.value());
    }

    public static TeacherDto toDto(Teacher teacher) {
        return new TeacherDto(teacher.value());
    }

    public static Subject toDomain(SubjectDto dto) {
        return new Subject(dto.value());
    }

    public static SubjectDto toDto(Subject subject) {
        return new SubjectDto(subject.value());
    }

    public static StudentGroup toDomain(StudentGroupDto dto) {
        return new StudentGroup(dto.name(), dto.size());
    }

    public static StudentGroupDto toDto(StudentGroup studentGroup) {
        return new StudentGroupDto(studentGroup.value(), studentGroup.size());
    }

    public static List<Course> toDomainCourses(List<CourseDto> dtos) {
        return dtos.stream().map(ScenarioMapper::toDomain).toList();
    }

    public static List<CourseDto> toCourseDtos(List<Course> courses) {
        return courses.stream().map(ScenarioMapper::toDto).toList();
    }

    public static List<Classroom> toDomainClassrooms(List<ClassroomDto> dtos) {
        return dtos.stream().map(ScenarioMapper::toDomain).toList();
    }

    public static List<ClassroomDto> toClassroomDtos(List<Classroom> classrooms) {
        return classrooms.stream().map(ScenarioMapper::toDto).toList();
    }

    public static List<Timeslot> toDomainTimeslots(List<TimeslotDto> dtos) {
        return dtos.stream().map(ScenarioMapper::toDomain).toList();
    }

    public static List<TimeslotDto> toTimeslotDtos(List<Timeslot> timeslots) {
        return timeslots.stream().map(ScenarioMapper::toDto).toList();
    }

    public static List<Teacher> toDomainTeachers(List<TeacherDto> dtos) {
        return dtos.stream().map(ScenarioMapper::toDomain).toList();
    }

    public static List<TeacherDto> toTeacherDtos(List<Teacher> teachers) {
        return teachers.stream().map(ScenarioMapper::toDto).toList();
    }

    public static List<Subject> toDomainSubjects(List<SubjectDto> dtos) {
        return dtos.stream().map(ScenarioMapper::toDomain).toList();
    }

    public static List<SubjectDto> toSubjectDtos(List<Subject> subjects) {
        return subjects.stream().map(ScenarioMapper::toDto).toList();
    }

    public static List<StudentGroup> toDomainStudentGroups(List<StudentGroupDto> dtos) {
        return dtos.stream().map(ScenarioMapper::toDomain).toList();
    }

    public static List<StudentGroupDto> toStudentGroupDtos(List<StudentGroup> studentGroups) {
        return studentGroups.stream().map(ScenarioMapper::toDto).toList();
    }
}
