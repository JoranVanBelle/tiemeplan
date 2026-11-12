package io.joranvanbelle.tiemeplan.viewmodel;

public record ValidationIssue(String code, ValidationSeverity severity, ValidationArea area, String message) {
}
