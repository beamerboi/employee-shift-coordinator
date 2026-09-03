package com.shiftlog.controller;

import com.shiftlog.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record EmployeeRequest(
        @Schema(example = "Alex Rivera")
        @NotBlank
        String name,

        @Schema(example = "WAITER")
        @NotNull
        Role role,

        @Schema(example = "18.50", minimum = "0")
        @NotNull
        @DecimalMin("0.00")
        BigDecimal hourlyRate) {
}
