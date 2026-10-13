package io.joranvanbelle.tiemeplan.variables;

public record Course(
        Teacher teacher,
        Subject subject,
        StudentGroup studentGroup
) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private Teacher teacher;
        private Subject subject;

        private StudentGroup studentGroup;

        private Builder() {
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
            this.studentGroup = new StudentGroup(studentGroup, size);
            return this;
        }

        public Builder studentGroup(StudentGroup studentGroup) {
            this.studentGroup = studentGroup;
            return this;
        }

        public Course build() {
            return new Course(teacher, subject, studentGroup);
        }
    }
}
