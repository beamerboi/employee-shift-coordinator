package com.shiftlog.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record Employee(String id, String name, Role role, BigDecimal hourlyRate) {

    public Employee {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Employee name is required");
        }
        if (role == null) {
            throw new IllegalArgumentException("Employee role is required");
        }
        if (hourlyRate == null) {
            throw new IllegalArgumentException("Hourly rate is required");
        }
        if (hourlyRate.signum() < 0) {
            throw new IllegalArgumentException("Hourly rate must be zero or greater");
        }
        id = normalizeId(id);
        name = name.trim();
        hourlyRate = hourlyRate.stripTrailingZeros();
    }

    public static Employee create(String name, Role role, BigDecimal hourlyRate) {
        return new Employee(null, name, role, hourlyRate);
    }

    private static String normalizeId(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return id.trim();
    }

    public Employee withId(String newId) {
        return new Employee(Objects.requireNonNull(newId, "Employee id is required"), name, role, hourlyRate);
    }
}
