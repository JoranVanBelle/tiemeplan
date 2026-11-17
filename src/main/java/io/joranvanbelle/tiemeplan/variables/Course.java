package io.joranvanbelle.tiemeplan.variables;

import java.util.List;

public record Course(
        String id,
        Teacher teacher,
        Subject subject,
        List<StudentGroup> studentGroups,
        String nextCourseId) {

    public int numberOfParticipant() {
        return studentGroups.stream()
                .map(StudentGroup::size)
                .reduce(0, Integer::sum);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private Teacher teacher;
        private Subject subject;
        private List<StudentGroup> studentGroups;
        private String nextCourseId;

        private Builder() {
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder teacher(String teacher) {
            this.teacher = new Teacher(teacher);
            return this;
        }

        public Builder teacher(Teacher teacher) {
            this.teacher = teacher;
            return this;
        }

        public Builder subject(String subject) {
            this.subject = new Subject(subject);
            return this;
        }

        public Builder subject(Subject subject) {
            this.subject = subject;
            return this;
        }

        public Builder studentGroup(String studentGroup, int size) {
            this.studentGroups = List.of(new StudentGroup(studentGroup, size));
            return this;
        }

        public Builder studentGroup(List<StudentGroup> studentGroup) {
            this.studentGroups = studentGroup;
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
