package io.joranvanbelle.tiemeplan.service.export;

import io.joranvanbelle.tiemeplan.variables.Classroom;
import io.joranvanbelle.tiemeplan.variables.Course;
import io.joranvanbelle.tiemeplan.variables.StudentGroup;
import io.joranvanbelle.tiemeplan.variables.Subject;
import io.joranvanbelle.tiemeplan.variables.Teacher;
import io.joranvanbelle.tiemeplan.variables.Timeslot;
import io.joranvanbelle.tiemeplan.viewmodel.ScenarioViewModel;

import java.util.List;

public record ScenarioSnapshot(
        List<Teacher> teachers,
        List<Subject> subjects,
        List<StudentGroup> studentGroups,
        List<Classroom> classrooms,
        List<Timeslot> timeslots,
        List<Course> courses) {

    public ScenarioSnapshot {
        teachers = teachers == null ? List.of() : List.copyOf(teachers);
        subjects = subjects == null ? List.of() : List.copyOf(subjects);
        studentGroups = studentGroups == null ? List.of() : List.copyOf(studentGroups);
        classrooms = classrooms == null ? List.of() : List.copyOf(classrooms);
        timeslots = timeslots == null ? List.of() : List.copyOf(timeslots);
        courses = courses == null ? List.of() : List.copyOf(courses);
    }

    public static ScenarioSnapshot from(ScenarioViewModel viewModel) {
        return new ScenarioSnapshot(
                viewModel.toDomainTeachers(),
                viewModel.toDomainSubjects(),
                viewModel.toDomainStudentGroups(),
                viewModel.toDomainClassrooms(),
                viewModel.toDomainTimeslots(),
                viewModel.toDomainCourses());
    }
}
