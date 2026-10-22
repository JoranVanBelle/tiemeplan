package io.joranvanbelle.tiemeplan.viewmodel;

import io.joranvanbelle.tiemeplan.model.ClassroomDto;
import io.joranvanbelle.tiemeplan.variables.Classroom;

import javafx.beans.Observable;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ClassroomViewModel {

    private final StringProperty name = new SimpleStringProperty("");
    private final IntegerProperty capacity = new SimpleIntegerProperty(0);
    private final ObservableList<TimeslotViewModel> occupiedTimeslots = FXCollections.observableArrayList(
            timeslot -> new Observable[] { timeslot.dayOfWeekProperty(), timeslot.startTimeProperty() });

    public ClassroomViewModel() {
    }

    public ClassroomViewModel(String name, int capacity) {
        this.name.set(name);
        this.capacity.set(capacity);
    }

    public static ClassroomViewModel of(Classroom classroom) {
        var viewModel = new ClassroomViewModel(classroom.name(), classroom.capacity());
        classroom.occupiedTimeslots().stream().map(TimeslotViewModel::of).forEach(viewModel.occupiedTimeslots::add);
        return viewModel;
    }

    public static ClassroomViewModel of(ClassroomDto dto) {
        var viewModel = new ClassroomViewModel(dto.name(), dto.capacity());
        dto.occupiedTimeslots().stream().map(TimeslotViewModel::of).forEach(viewModel.occupiedTimeslots::add);
        return viewModel;
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

    public IntegerProperty capacityProperty() {
        return capacity;
    }

    public int getCapacity() {
        return capacity.get();
    }

    public void setCapacity(int value) {
        capacity.set(value);
    }

    public ObservableList<TimeslotViewModel> occupiedTimeslots() {
        return occupiedTimeslots;
    }

    public Classroom toDomain() {
        return new Classroom(name.get(), capacity.get(),
                occupiedTimeslots.stream().map(TimeslotViewModel::toDomain).toList());
    }

    public ClassroomDto toDto() {
        return new ClassroomDto(name.get(), capacity.get(),
                occupiedTimeslots.stream().map(TimeslotViewModel::toDto).toList());
    }
}
