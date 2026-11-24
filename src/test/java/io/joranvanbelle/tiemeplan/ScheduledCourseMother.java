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
import java.util.ArrayList;
import java.util.List;

public class ScheduledCourseMother {

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private String courseId = "a42a1cd7-1dcf-49aa-842d-c8527aa1f310";
        private String classroom = "room1";
        private int classroomCapacity = 10;
        private List<Timeslot> occupiedTimeslots = new ArrayList<>();
        private DayOfWeek dayOfWeek = DayOfWeek.MONDAY;
        private LocalTime startTime = LocalTime.of(10, 0);
        private String teacher = "Mr. Johnson";
        private String subject = "English";
        private List<StudentGroup> studentGroups = new ArrayList<>(List.of(new StudentGroup("1A", 10)));

        public Builder courseId(String courseId) {
            this.courseId = courseId;
            return this;
        }

        public Builder classroom(String classroom) {
            this.classroom = classroom;
            return this;
        }

        public Builder classroomCapacity(int classroomCapacity) {
            this.classroomCapacity = classroomCapacity;
            return this;
        }

        public Builder classRoomOccupied(List<Timeslot> occupiedTimeslots) {
            this.occupiedTimeslots = occupiedTimeslots;
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

        public Builder addStudentGroups(List<StudentGroup> studentGroups) {
            this.studentGroups = studentGroups;
            return this;
        }

        public ScheduledCourse build() throws IllegalAccessException {
            return new ScheduledCourse(
                    new Classroom(classroom, classroomCapacity, occupiedTimeslots),
                    new Timeslot(dayOfWeek, startTime),
                    new Course(
                            courseId,
                            new Teacher(teacher),
                            new Subject(subject),
                            studentGroups,
                            null));
        }
    }

}
