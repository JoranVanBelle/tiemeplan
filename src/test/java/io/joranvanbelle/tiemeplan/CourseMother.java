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

        private String id = "a42a1cd7-1dcf-49aa-842d-c8527aa1f310";
        private Teacher teacher = new Teacher("Ms. Smith");
        private Subject subject = new Subject("Math");
        private List<StudentGroup> studentGroups = List.of(new StudentGroup("6A", 10));
        private String nextCourseId = null;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

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

        public Builder nextCourseId(String nextCourseId) {
            this.nextCourseId = nextCourseId;
            return this;
        }

        public Course build() {
            return new Course(id, teacher, subject, studentGroups, nextCourseId);
        }
    }

}
