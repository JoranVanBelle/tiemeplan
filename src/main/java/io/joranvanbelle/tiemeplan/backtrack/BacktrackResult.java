package io.joranvanbelle.tiemeplan.backtrack;

public sealed interface BacktrackResult permits BacktrackResult.Success, BacktrackResult.NoSolutionFound {

    record Success(ClassSchedule solution) implements BacktrackResult {
    }

    record NoSolutionFound(ClassSchedule previousState) implements BacktrackResult {
    }

}
