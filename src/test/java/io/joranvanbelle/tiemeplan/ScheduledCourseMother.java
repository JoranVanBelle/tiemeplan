package io.joranvanbelle.tiemeplan;

import io.joranvanbelle.tiemeplan.backtrack.ScheduledCourse;
import io.joranvanbelle.tiemeplan.variables.Classroom;
import io.joranvanbelle.tiemeplan.variables.Course;
import io.joranvanbelle.tiemeplan.variables.StudentGroup;
import io.joranvanbelle.tiemeplan.variables.Subject;
import io.joranvanbelle.tiemeplan.variables.Teacher;
import io.joranvanbelle.tiemeplan.variables.Timeslot;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class ScheduledCourseMother {

    public static class Builder {

        private String classroom = "room1";
        private int classroomCapacity = 10;
        private DayOfWeek dayOfWeek = DayOfWeek.MONDAY;
        private LocalTime startTime = LocalTime.of(10, 0);
        private String teacher = "Mr. Johnson";
        private String subject = "English";
        private String studentGroup = "1A";
        private int studentGroupSize = 10;

        public Builder classroom(String classroom) {
            this.classroom = classroom;
            return this;
        }

        public Builder classroomCapacity(int classroomCapacity) {
            this.classroomCapacity = classroomCapacity;
            return this;
        }

        public Builder dayOfWeek(DayOfWeek dayOfWeek) {
            this.dayOfWeek = dayOfWeek;
            return this;
        }

        public Builder startTime(LocalTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder teacher(String teacher) {
            this.teacher = teacher;
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder studentGroup(String studentGroup) {
            this.studentGroup = studentGroup;
            return this;
        }

        public Builder studentGroupSize(int studentGroupSize) {
            this.studentGroupSize = studentGroupSize;
            return this;
        }

        public ScheduledCourse build() {
            return new ScheduledCourse(
                    new Classroom(classroom, classroomCapacity),
                    new Timeslot(dayOfWeek, startTime),
                    new Course(
                            new Teacher(teacher),
                            new Subject(subject),
                            new StudentGroup(studentGroup, studentGroupSize)));
        }
    }

}
