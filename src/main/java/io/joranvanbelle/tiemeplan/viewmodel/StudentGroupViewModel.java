package io.joranvanbelle.tiemeplan.viewmodel;

import io.joranvanbelle.tiemeplan.model.StudentGroupDto;
import io.joranvanbelle.tiemeplan.variables.StudentGroup;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class StudentGroupViewModel {

    private final StringProperty name = new SimpleStringProperty("");
    private final IntegerProperty size = new SimpleIntegerProperty(0);

    public StudentGroupViewModel() {
    }

    public StudentGroupViewModel(String name, int size) {
        this.name.set(name);
        this.size.set(size);
    }

    public static StudentGroupViewModel of(StudentGroup studentGroup) {
        return new StudentGroupViewModel(studentGroup.value(), studentGroup.size());
    }

    public static StudentGroupViewModel of(StudentGroupDto dto) {
        return new StudentGroupViewModel(dto.name(), dto.size());
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String value) {
        name.set(value);
    }

    public IntegerProperty sizeProperty() {
        return size;
    }

    public int getSize() {
        return size.get();
    }

    public void setSize(int value) {
        size.set(value);
    }

    public StudentGroup toDomain() {
        return new StudentGroup(name.get(), size.get());
    }

    public StudentGroupDto toDto() {
        return new StudentGroupDto(name.get(), size.get());
    }
}
