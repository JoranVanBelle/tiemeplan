package io.joranvanbelle.tiemeplan.viewmodel;

import io.joranvanbelle.tiemeplan.model.TimeslotDto;
import io.joranvanbelle.tiemeplan.variables.Timeslot;

import java.time.DayOfWeek;
import java.time.LocalTime;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class TimeslotViewModel {

    private final ObjectProperty<DayOfWeek> dayOfWeek = new SimpleObjectProperty<>(DayOfWeek.MONDAY);
    private final ObjectProperty<LocalTime> startTime = new SimpleObjectProperty<>(LocalTime.of(8, 0));

    public TimeslotViewModel() {
    }

    public TimeslotViewModel(DayOfWeek dayOfWeek, LocalTime startTime) {
        this.dayOfWeek.set(dayOfWeek);
        this.startTime.set(startTime);
    }

    public static TimeslotViewModel of(Timeslot timeslot) {
        return new TimeslotViewModel(timeslot.dayOfWeek(), timeslot.startTime());
    }

    public static TimeslotViewModel of(TimeslotDto dto) {
        return new TimeslotViewModel(dto.dayOfWeek(), dto.startTime());
    }

    public ObjectProperty<DayOfWeek> dayOfWeekProperty() {
        return dayOfWeek;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek.get();
    }

    public void setDayOfWeek(DayOfWeek value) {
        dayOfWeek.set(value);
    }

    public ObjectProperty<LocalTime> startTimeProperty() {
        return startTime;
    }

    public LocalTime getStartTime() {
        return startTime.get();
    }

    public void setStartTime(LocalTime value) {
        startTime.set(value);
    }

    public Timeslot toDomain() {
        return new Timeslot(dayOfWeek.get(), startTime.get());
    }

    public TimeslotDto toDto() {
        return new TimeslotDto(dayOfWeek.get(), startTime.get());
    }
}
