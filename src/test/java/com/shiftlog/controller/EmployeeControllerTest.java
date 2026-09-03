package com.shiftlog.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.shiftlog.domain.Employee;
import com.shiftlog.domain.Role;
import com.shiftlog.service.EmployeeService;
import com.shiftlog.service.NotFoundException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Test
    void createsEmployee() throws Exception {
        Employee employee = new Employee("emp-1", "Ada", Role.MANAGER, BigDecimal.valueOf(31));
        when(employeeService.create(eq("Ada"), eq(Role.MANAGER), eq(BigDecimal.valueOf(31)))).thenReturn(employee);

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ada","role":"MANAGER","hourlyRate":31}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("emp-1"))
                .andExpect(jsonPath("$.name").value("Ada"))
                .andExpect(jsonPath("$.role").value("MANAGER"))
                .andExpect(jsonPath("$.hourlyRate").value(31));
    }

    @Test
    void rejectsInvalidEmployeeRequest() throws Exception {
        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","role":"WAITER","hourlyRate":10}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"));
    }

    @Test
    void returnsEmployee() throws Exception {
        when(employeeService.get("emp-1"))
                .thenReturn(new Employee("emp-1", "Ada", Role.WAITER, BigDecimal.TEN));

        mockMvc.perform(get("/employees/emp-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("emp-1"));
    }

    @Test
    void listsEmployees() throws Exception {
        when(employeeService.list()).thenReturn(List.of(new Employee("emp-1", "Ada", Role.COOK, BigDecimal.TEN)));

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].role").value("COOK"));
    }

    @Test
    void updatesEmployee() throws Exception {
        Employee employee = new Employee("emp-1", "Grace", Role.MANAGER, BigDecimal.valueOf(40));
        when(employeeService.update("emp-1", "Grace", Role.MANAGER, BigDecimal.valueOf(40))).thenReturn(employee);

        mockMvc.perform(put("/employees/emp-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Grace","role":"MANAGER","hourlyRate":40}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Grace"));
    }

    @Test
    void deletesEmployee() throws Exception {
        mockMvc.perform(delete("/employees/emp-1"))
                .andExpect(status().isNoContent());

        verify(employeeService).delete("emp-1");
    }

    @Test
    void mapsNotFoundTo404() throws Exception {
        when(employeeService.get("missing")).thenThrow(new NotFoundException("Employee", "missing"));

        mockMvc.perform(get("/employees/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee missing was not found"));
    }
}
