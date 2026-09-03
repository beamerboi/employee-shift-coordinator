package com.shiftlog.controller;

import com.shiftlog.domain.Employee;
import com.shiftlog.service.EmployeeService;
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
@RequestMapping("/employees")
@Tag(name = "Employees", description = "Manage restaurant employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an employee")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Employee created"),
            @ApiResponse(responseCode = "400", description = "Invalid employee request")
    })
    public Employee create(@Valid @RequestBody EmployeeRequest request) {
        return employeeService.create(request.name(), request.role(), request.hourlyRate());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an employee by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee found"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public Employee get(@PathVariable String id) {
        return employeeService.get(id);
    }

    @GetMapping
    @Operation(summary = "List employees")
    @ApiResponse(responseCode = "200", description = "Employees returned")
    public List<Employee> list() {
        return employeeService.list();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an employee")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee updated"),
            @ApiResponse(responseCode = "400", description = "Invalid employee request"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public Employee update(@PathVariable String id, @Valid @RequestBody EmployeeRequest request) {
        return employeeService.update(id, request.name(), request.role(), request.hourlyRate());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an employee")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Employee deleted"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public void delete(@PathVariable String id) {
        employeeService.delete(id);
    }
}
