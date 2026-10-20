package io.joranvanbelle.tiemeplan.variables;

import java.util.List;

public record Classroom(String name, int capacity, List<Timeslot> occupiedTimeslots) {

    public boolean canHoldStudentGroup(int studentGroupSize) {
        return capacity >= studentGroupSize;
    }
}
