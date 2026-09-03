package com.shiftlog.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public record Shift(
        String id,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        String notes,
        Set<String> employeeIds) {

    public Shift {
        if (date == null) {
            throw new IllegalArgumentException("Shift date is required");
        }
        if (startTime == null) {
            throw new IllegalArgumentException("Shift start time is required");
        }
        if (endTime == null) {
            throw new IllegalArgumentException("Shift end time is required");
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("Shift end time must be after start time");
        }
        id = normalizeId(id);
        notes = notes == null ? "" : notes.trim();
        employeeIds = normalizeEmployeeIds(employeeIds);
    }

    public static Shift create(
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            String notes,
            Set<String> employeeIds) {
        return new Shift(null, date, startTime, endTime, notes, employeeIds);
    }

    public Shift withId(String newId) {
        return new Shift(Objects.requireNonNull(newId, "Shift id is required"), date, startTime, endTime, notes, employeeIds);
    }

    public Shift assignEmployee(String employeeId) {
        LinkedHashSet<String> ids = new LinkedHashSet<>(employeeIds);
        ids.add(requireEmployeeId(employeeId));
        return new Shift(id, date, startTime, endTime, notes, ids);
    }

    public Shift unassignEmployee(String employeeId) {
        LinkedHashSet<String> ids = new LinkedHashSet<>(employeeIds);
        ids.remove(requireEmployeeId(employeeId));
        return new Shift(id, date, startTime, endTime, notes, ids);
    }

    private static Set<String> normalizeEmployeeIds(Set<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String employeeId : ids) {
            normalized.add(requireEmployeeId(employeeId));
        }
        return Set.copyOf(normalized);
    }

    private static String requireEmployeeId(String employeeId) {
        if (employeeId == null || employeeId.isBlank()) {
            throw new IllegalArgumentException("Employee ids cannot be blank");
        }
        return employeeId.trim();
    }

    private static String normalizeId(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return id.trim();
    }
}
