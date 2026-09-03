package com.shiftlog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.shiftlog.domain.Employee;
import com.shiftlog.domain.Role;
import com.shiftlog.repository.EmployeeRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void createsEmployeeThroughRepository() {
        Employee unsaved = Employee.create("Ada", Role.MANAGER, BigDecimal.valueOf(30));
        Employee saved = unsaved.withId("emp-1");
        when(employeeRepository.save(unsaved)).thenReturn(saved);

        Employee result = employeeService.create("Ada", Role.MANAGER, BigDecimal.valueOf(30));

        assertThat(result).isEqualTo(saved);
        verify(employeeRepository).save(unsaved);
    }

    @Test
    void returnsEmployeeById() {
        Employee employee = Employee.create("Ada", Role.WAITER, BigDecimal.TEN).withId("emp-1");
        when(employeeRepository.findById("emp-1")).thenReturn(Optional.of(employee));

        assertThat(employeeService.get("emp-1")).isEqualTo(employee);
    }

    @Test
    void throwsWhenEmployeeIsMissing() {
        when(employeeRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.get("missing"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Employee missing was not found");
    }

    @Test
    void listsEmployees() {
        Employee employee = Employee.create("Ada", Role.COOK, BigDecimal.TEN).withId("emp-1");
        when(employeeRepository.findAll()).thenReturn(List.of(employee));

        assertThat(employeeService.list()).containsExactly(employee);
    }

    @Test
    void updatesExistingEmployeeAndPreservesId() {
        when(employeeRepository.existsById("emp-1")).thenReturn(true);
        Employee updated = new Employee("emp-1", "Grace", Role.MANAGER, BigDecimal.valueOf(40));
        when(employeeRepository.save(updated)).thenReturn(updated);

        Employee result = employeeService.update("emp-1", "Grace", Role.MANAGER, BigDecimal.valueOf(40));

        assertThat(result).isEqualTo(updated);
        verify(employeeRepository).save(updated);
    }

    @Test
    void refusesToUpdateMissingEmployee() {
        when(employeeRepository.existsById("missing")).thenReturn(false);

        assertThatThrownBy(() -> employeeService.update("missing", "Grace", Role.MANAGER, BigDecimal.valueOf(40)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Employee missing was not found");
        verify(employeeRepository).existsById("missing");
        verifyNoMoreInteractions(employeeRepository);
    }

    @Test
    void deletesExistingEmployee() {
        when(employeeRepository.existsById("emp-1")).thenReturn(true);

        employeeService.delete("emp-1");

        verify(employeeRepository).deleteById("emp-1");
    }

    @Test
    void refusesToDeleteMissingEmployee() {
        when(employeeRepository.existsById("missing")).thenReturn(false);

        assertThatThrownBy(() -> employeeService.delete("missing"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Employee missing was not found");
        verify(employeeRepository).existsById("missing");
        verifyNoMoreInteractions(employeeRepository);
    }
}
