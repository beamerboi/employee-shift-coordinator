package com.shiftlog.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public record ShiftRequest(
        @Schema(example = "2026-05-27")
        @NotNull
        LocalDate date,

        @Schema(example = "09:00:00")
        @NotNull
        LocalTime startTime,

        @Schema(example = "17:00:00")
        @NotNull
        LocalTime endTime,

        @Schema(example = "Lunch service")
        String notes,

        @Schema(description = "Employee ids assigned to the shift", example = "[\"employee-1\",\"employee-2\"]")
        Set<String> employeeIds) {

    public Set<String> normalizedEmployeeIds() {
        return employeeIds == null ? Set.of() : employeeIds;
    }
}
