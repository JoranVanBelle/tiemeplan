package io.joranvanbelle.tiemeplan.variables;

public record Classroom(String name, int capacity) {

    public boolean canHoldStudentGroup(int studentGroupSize) {
        return capacity >= studentGroupSize;
    }
}
