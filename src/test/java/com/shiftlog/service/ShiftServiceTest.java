package com.shiftlog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.shiftlog.domain.Shift;
import com.shiftlog.repository.EmployeeRepository;
import com.shiftlog.repository.ShiftRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShiftServiceTest {

    private static final LocalDate DATE = LocalDate.of(2026, 5, 27);
    private static final LocalTime START = LocalTime.of(10, 0);
    private static final LocalTime END = LocalTime.of(18, 0);

    @Mock
    private ShiftRepository shiftRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private ShiftService shiftService;

    @Test
    void createsShiftWithoutEmployees() {
        Shift unsaved = Shift.create(DATE, START, END, "Lunch", Set.of());
        Shift saved = unsaved.withId("shift-1");
        when(shiftRepository.save(unsaved)).thenReturn(saved);

        Shift result = shiftService.create(DATE, START, END, "Lunch");

        assertThat(result).isEqualTo(saved);
        verify(shiftRepository).save(unsaved);
    }

    @Test
    void createsShiftWithExistingEmployees() {
        Shift unsaved = Shift.create(DATE, START, END, "Lunch", Set.of("emp-1"));
        Shift saved = unsaved.withId("shift-1");
        when(employeeRepository.existsById("emp-1")).thenReturn(true);
        when(shiftRepository.save(unsaved)).thenReturn(saved);

        Shift result = shiftService.create(DATE, START, END, "Lunch", Set.of("emp-1"));

        assertThat(result).isEqualTo(saved);
        verify(employeeRepository).existsById("emp-1");
        verify(shiftRepository).save(unsaved);
    }

    @Test
    void refusesToCreateShiftWithMissingEmployee() {
        when(employeeRepository.existsById("missing")).thenReturn(false);

        assertThatThrownBy(() -> shiftService.create(DATE, START, END, "Lunch", Set.of("missing")))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Employee missing was not found");
        verify(employeeRepository).existsById("missing");
        verifyNoMoreInteractions(employeeRepository, shiftRepository);
    }

    @Test
    void returnsShiftById() {
        Shift shift = Shift.create(DATE, START, END, "", Set.of()).withId("shift-1");
        when(shiftRepository.findById("shift-1")).thenReturn(Optional.of(shift));

        assertThat(shiftService.get("shift-1")).isEqualTo(shift);
    }

    @Test
    void throwsWhenShiftIsMissing() {
        when(shiftRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shiftService.get("missing"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Shift missing was not found");
    }

    @Test
    void listsShifts() {
        Shift shift = Shift.create(DATE, START, END, "", Set.of()).withId("shift-1");
        when(shiftRepository.findAll()).thenReturn(List.of(shift));

        assertThat(shiftService.list()).containsExactly(shift);
    }

    @Test
    void updatesExistingShift() {
        when(shiftRepository.existsById("shift-1")).thenReturn(true);
        when(employeeRepository.existsById("emp-1")).thenReturn(true);
        Shift updated = new Shift("shift-1", DATE, START, END, "Dinner", Set.of("emp-1"));
        when(shiftRepository.save(updated)).thenReturn(updated);

        Shift result = shiftService.update("shift-1", DATE, START, END, "Dinner", Set.of("emp-1"));

        assertThat(result).isEqualTo(updated);
        verify(shiftRepository).save(updated);
    }

    @Test
    void updatesExistingShiftWhenEmployeeIdsAreNull() {
        when(shiftRepository.existsById("shift-1")).thenReturn(true);
        Shift updated = new Shift("shift-1", DATE, START, END, "Dinner", Set.of());
        when(shiftRepository.save(updated)).thenReturn(updated);

        Shift result = shiftService.update("shift-1", DATE, START, END, "Dinner", null);

        assertThat(result.employeeIds()).isEmpty();
        verify(shiftRepository).save(updated);
    }

    @Test
    void refusesToUpdateShiftWithMissingEmployee() {
        when(shiftRepository.existsById("shift-1")).thenReturn(true);
        when(employeeRepository.existsById("missing")).thenReturn(false);

        assertThatThrownBy(() -> shiftService.update("shift-1", DATE, START, END, "Dinner", Set.of("missing")))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Employee missing was not found");
        verify(shiftRepository).existsById("shift-1");
        verify(employeeRepository).existsById("missing");
        verifyNoMoreInteractions(employeeRepository, shiftRepository);
    }

    @Test
    void refusesToUpdateMissingShift() {
        when(shiftRepository.existsById("missing")).thenReturn(false);

        assertThatThrownBy(() -> shiftService.update("missing", DATE, START, END, "", Set.of()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Shift missing was not found");
        verify(shiftRepository).existsById("missing");
        verifyNoMoreInteractions(shiftRepository);
    }

    @Test
    void assignsExistingEmployeeToExistingShift() {
        Shift shift = Shift.create(DATE, START, END, "", Set.of()).withId("shift-1");
        Shift assigned = shift.assignEmployee("emp-1");
        when(shiftRepository.findById("shift-1")).thenReturn(Optional.of(shift));
        when(employeeRepository.existsById("emp-1")).thenReturn(true);
        when(shiftRepository.save(assigned)).thenReturn(assigned);

        Shift result = shiftService.assignEmployee("shift-1", "emp-1");

        assertThat(result.employeeIds()).containsExactly("emp-1");
        verify(shiftRepository).save(assigned);
    }

    @Test
    void refusesToAssignMissingEmployee() {
        Shift shift = Shift.create(DATE, START, END, "", Set.of()).withId("shift-1");
        when(shiftRepository.findById("shift-1")).thenReturn(Optional.of(shift));
        when(employeeRepository.existsById("missing")).thenReturn(false);

        assertThatThrownBy(() -> shiftService.assignEmployee("shift-1", "missing"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Employee missing was not found");
        verify(shiftRepository).findById("shift-1");
        verify(employeeRepository).existsById("missing");
        verifyNoMoreInteractions(employeeRepository, shiftRepository);
    }

    @Test
    void unassignsEmployeeFromShift() {
        Shift shift = Shift.create(DATE, START, END, "", Set.of("emp-1")).withId("shift-1");
        Shift unassigned = shift.unassignEmployee("emp-1");
        when(shiftRepository.findById("shift-1")).thenReturn(Optional.of(shift));
        when(shiftRepository.save(unassigned)).thenReturn(unassigned);

        Shift result = shiftService.unassignEmployee("shift-1", "emp-1");

        assertThat(result.employeeIds()).isEmpty();
        verify(shiftRepository).save(unassigned);
    }

    @Test
    void deletesExistingShift() {
        when(shiftRepository.existsById("shift-1")).thenReturn(true);

        shiftService.delete("shift-1");

        verify(shiftRepository).deleteById("shift-1");
    }

    @Test
    void refusesToDeleteMissingShift() {
        when(shiftRepository.existsById("missing")).thenReturn(false);

        assertThatThrownBy(() -> shiftService.delete("missing"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Shift missing was not found");
        verify(shiftRepository).existsById("missing");
        verifyNoMoreInteractions(shiftRepository);
    }
}
