package io.joranvanbelle.tiemeplan;

import io.joranvanbelle.tiemeplan.variables.Course;
import io.joranvanbelle.tiemeplan.variables.StudentGroup;
import io.joranvanbelle.tiemeplan.variables.Subject;
import io.joranvanbelle.tiemeplan.variables.Teacher;

import java.util.List;

public class CourseMother {

    public static Builder newCourse() {
        return new Builder();
    }

    public static class Builder {

        private Teacher teacher = new Teacher("Ms. Smith");
        private Subject subject = new Subject("Math");
        private List<StudentGroup> studentGroups = List.of(new StudentGroup("6A", 10));

        public Builder teacher(String teacher) {
            this.teacher = new Teacher(teacher);
            return this;
        }

        public Builder subject(String subject) {
            this.subject = new Subject(subject);
            return this;
        }

        public Builder studentGroup(String studentGroup, int size) {
            this.studentGroups = List.of(new StudentGroup(studentGroup, size));
            return this;
        }

        public Builder studentGroup(List<StudentGroup> studentGroups) {
            this.studentGroups = studentGroups;
            return this;
        }

        public Course build() {
            return new Course(teacher, subject, studentGroups);
        }
    }

}
