package com.shiftlog.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class EmployeeTest {

    @Test
    void createsEmployeeWithTrimmedNameAndRate() {
        Employee employee = Employee.create(" Ada ", Role.MANAGER, new BigDecimal("25.00"));

        assertThat(employee.id()).isNull();
        assertThat(employee.name()).isEqualTo("Ada");
        assertThat(employee.role()).isEqualTo(Role.MANAGER);
        assertThat(employee.hourlyRate()).isEqualByComparingTo("25");
    }

    @Test
    void normalizesBlankIdToNull() {
        Employee employee = new Employee(" ", "Ada", Role.WAITER, BigDecimal.TEN);

        assertThat(employee.id()).isNull();
    }

    @Test
    void trimsProvidedId() {
        Employee employee = new Employee(" emp-1 ", "Ada", Role.WAITER, BigDecimal.TEN);

        assertThat(employee.id()).isEqualTo("emp-1");
    }

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> Employee.create(" ", Role.WAITER, BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Employee name is required");
    }

    @Test
    void rejectsMissingName() {
        assertThatThrownBy(() -> Employee.create(null, Role.WAITER, BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Employee name is required");
    }

    @Test
    void rejectsMissingRole() {
        assertThatThrownBy(() -> Employee.create("Ada", null, BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Employee role is required");
    }

    @Test
    void rejectsMissingHourlyRate() {
        assertThatThrownBy(() -> Employee.create("Ada", Role.WAITER, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Hourly rate is required");
    }

    @Test
    void rejectsNegativeHourlyRate() {
        assertThatThrownBy(() -> Employee.create("Ada", Role.COOK, BigDecimal.valueOf(-1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Hourly rate must be zero or greater");
    }
}
