package com.shiftlog.controller;

import io.swagger.v3.oas.annotations.media.Schema;

public record ApiError(
        @Schema(example = "Request validation failed")
        String message) {
}
