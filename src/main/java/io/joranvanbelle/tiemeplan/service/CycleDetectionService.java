package io.joranvanbelle.tiemeplan.service;

import io.joranvanbelle.tiemeplan.viewmodel.CourseViewModel;
import javafx.collections.ObservableList;

import java.util.List;

public class CycleDetectionService {

    public static CycleDetectionResult containsCycle(ObservableList<CourseViewModel> linkedCourses) {
        // todo - when storing in **affectedCourseLabels**, create courseLabel of it
        return new CycleDetectionResult.NoCycleDetected();
    }


    public sealed interface CycleDetectionResult permits CycleDetectionResult.CycleDetected, CycleDetectionResult.NoCycleDetected {

        record CycleDetected(List<String> affectedCourseLabels) implements CycleDetectionResult {}
        record NoCycleDetected() implements CycleDetectionResult {}

    }
}
