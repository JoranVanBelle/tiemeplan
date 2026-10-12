package io.joranvanbelle.tiemeplan;

import io.joranvanbelle.tiemeplan.variables.Course;
import io.joranvanbelle.tiemeplan.variables.StudentGroup;
import io.joranvanbelle.tiemeplan.variables.Subject;
import io.joranvanbelle.tiemeplan.variables.Teacher;

public class CourseMother {

    public static Builder newCourse() {
        return new Builder();
    }

    public static class Builder {

        private Teacher teacher = new Teacher("Ms. Smith");
        private Subject subject = new Subject("Math");
        private StudentGroup studentGroup = new StudentGroup("6A");

        public Builder teacher(String teacher) {
            this.teacher = new Teacher(teacher);
            return this;
        }

        public Builder subject(String subject) {
            this.subject = new Subject(subject);
            return this;
        }

        public Builder studentGroup(String studentGroup) {
            this.studentGroup = new StudentGroup(studentGroup);
            return this;
        }

        public Course build() {
            return new Course(teacher, subject, studentGroup);
        }
    }

}
