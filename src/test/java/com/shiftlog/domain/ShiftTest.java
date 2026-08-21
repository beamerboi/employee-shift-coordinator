package com.shiftlog.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ShiftTest {

    @Test
    void createsShiftWithAssignedEmployeesAndTrimmedNotes() {
        Shift shift = Shift.create(
                LocalDate.of(2026, 5, 27),
                LocalTime.of(10, 0),
                LocalTime.of(18, 0),
                " Prep ",
                Set.of("emp-1"));

        assertThat(shift.id()).isNull();
        assertThat(shift.date()).isEqualTo(LocalDate.of(2026, 5, 27));
        assertThat(shift.startTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(shift.endTime()).isEqualTo(LocalTime.of(18, 0));
        assertThat(shift.notes()).isEqualTo("Prep");
        assertThat(shift.employeeIds()).containsExactly("emp-1");
    }

    @Test
    void createsShiftWithEmptyNotesAndEmployeesWhenOptionalValuesAreNull() {
        Shift shift = Shift.create(
                LocalDate.of(2026, 5, 27),
                LocalTime.of(10, 0),
                LocalTime.of(18, 0),
                null,
                null);

        assertThat(shift.notes()).isEmpty();
        assertThat(shift.employeeIds()).isEmpty();
    }

    @Test
    void normalizesBlankIdToNull() {
        Shift shift = new Shift(
                " ",
                LocalDate.of(2026, 5, 27),
                LocalTime.of(10, 0),
                LocalTime.of(18, 0),
                "",
                Set.of());

        assertThat(shift.id()).isNull();
    }

    @Test
    void trimsProvidedId() {
        Shift shift = new Shift(
                " shift-1 ",
                LocalDate.of(2026, 5, 27),
                LocalTime.of(10, 0),
                LocalTime.of(18, 0),
                "",
                Set.of());

        assertThat(shift.id()).isEqualTo("shift-1");
    }

    @Test
    void rejectsMissingDate() {
        assertThatThrownBy(() -> Shift.create(null, LocalTime.of(10, 0), LocalTime.of(18, 0), null, Set.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Shift date is required");
    }

    @Test
    void rejectsMissingStartTime() {
        assertThatThrownBy(() -> Shift.create(LocalDate.of(2026, 5, 27), null, LocalTime.of(18, 0), null, Set.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Shift start time is required");
    }

    @Test
    void rejectsMissingEndTime() {
        assertThatThrownBy(() -> Shift.create(LocalDate.of(2026, 5, 27), LocalTime.of(10, 0), null, null, Set.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Shift end time is required");
    }

    @Test
    void rejectsEndTimeBeforeStartTime() {
        assertThatThrownBy(() -> Shift.create(
                        LocalDate.of(2026, 5, 27),
                        LocalTime.of(18, 0),
                        LocalTime.of(10, 0),
                        null,
                        Set.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Shift end time must be after start time");
    }

    @Test
    void rejectsBlankEmployeeId() {
        assertThatThrownBy(() -> Shift.create(
                        LocalDate.of(2026, 5, 27),
                        LocalTime.of(10, 0),
                        LocalTime.of(18, 0),
                        null,
                        Set.of(" ")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Employee ids cannot be blank");
    }

    @Test
    void rejectsMissingEmployeeId() {
        assertThatThrownBy(() -> Shift.create(
                        LocalDate.of(2026, 5, 27),
                        LocalTime.of(10, 0),
                        LocalTime.of(18, 0),
                        null,
                        java.util.Collections.singleton(null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Employee ids cannot be blank");
    }
}
