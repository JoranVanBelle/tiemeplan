package io.joranvanbelle.tiemeplan.backtrack;

import io.joranvanbelle.tiemeplan.variables.Classroom;
import io.joranvanbelle.tiemeplan.variables.Course;
import io.joranvanbelle.tiemeplan.variables.Timeslot;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public record ScheduledCourse(Classroom classroom, Timeslot timeslot, Course course) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private Classroom classroom;
        private Timeslot timeslot;

        private Course course;

        private Builder() {
        }

        public Builder alwaysFreeClassroom(String name, int capacity) {
            this.classroom = new Classroom(name, capacity, List.of());
            return this;
        }

        public Builder classroom(String name, int capacity, List<Timeslot> occupiedTimeslots) {
            this.classroom = new Classroom(name, capacity, occupiedTimeslots);
            return this;
        }

        public Builder classroom(Classroom classroom) {
            this.classroom = classroom;
            return this;
        }

        public Builder timeslot(DayOfWeek dayOfWeek, LocalTime timeslot) {
            this.timeslot = new Timeslot(dayOfWeek, timeslot);
            return this;
        }

        public Builder timeslot(Timeslot timeslot) {
            this.timeslot = timeslot;
            return this;
        }

        public Builder course(Course course) {
            this.course = course;
            return this;
        }

        public ScheduledCourse build() {
            return new ScheduledCourse(classroom, timeslot, course);
        }
    }
}
