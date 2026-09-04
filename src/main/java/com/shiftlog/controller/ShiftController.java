package com.shiftlog.controller;

import com.shiftlog.domain.Shift;
import com.shiftlog.service.ShiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shifts")
@Tag(name = "Shifts", description = "Manage work shifts and employee assignments")
public class ShiftController {

    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a shift")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Shift created"),
            @ApiResponse(responseCode = "400", description = "Invalid shift request")
    })
    public Shift create(@Valid @RequestBody ShiftRequest request) {
        return shiftService.create(
                request.date(),
                request.startTime(),
                request.endTime(),
                request.notes(),
                request.normalizedEmployeeIds());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a shift by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Shift found"),
            @ApiResponse(responseCode = "404", description = "Shift not found")
    })
    public Shift get(@PathVariable String id) {
        return shiftService.get(id);
    }

    @GetMapping
    @Operation(summary = "List shifts")
    @ApiResponse(responseCode = "200", description = "Shifts returned")
    public List<Shift> list() {
        return shiftService.list();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a shift")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Shift updated"),
            @ApiResponse(responseCode = "400", description = "Invalid shift request"),
            @ApiResponse(responseCode = "404", description = "Shift not found")
    })
    public Shift update(@PathVariable String id, @Valid @RequestBody ShiftRequest request) {
        return shiftService.update(
                id,
                request.date(),
                request.startTime(),
                request.endTime(),
                request.notes(),
                request.normalizedEmployeeIds());
    }

    @PostMapping("/{shiftId}/employees/{employeeId}")
    @Operation(summary = "Assign an employee to a shift")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee assigned"),
            @ApiResponse(responseCode = "404", description = "Shift or employee not found")
    })
    public Shift assignEmployee(@PathVariable String shiftId, @PathVariable String employeeId) {
        return shiftService.assignEmployee(shiftId, employeeId);
    }

    @DeleteMapping("/{shiftId}/employees/{employeeId}")
    @Operation(summary = "Unassign an employee from a shift")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee unassigned"),
            @ApiResponse(responseCode = "404", description = "Shift not found")
    })
    public Shift unassignEmployee(@PathVariable String shiftId, @PathVariable String employeeId) {
        return shiftService.unassignEmployee(shiftId, employeeId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a shift")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Shift deleted"),
            @ApiResponse(responseCode = "404", description = "Shift not found")
    })
    public void delete(@PathVariable String id) {
        shiftService.delete(id);
    }
}
