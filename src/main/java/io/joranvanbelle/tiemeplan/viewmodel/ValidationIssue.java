package io.joranvanbelle.tiemeplan.viewmodel;

public record ValidationIssue(String code, ValidationSeverity severity, String message) {
}
