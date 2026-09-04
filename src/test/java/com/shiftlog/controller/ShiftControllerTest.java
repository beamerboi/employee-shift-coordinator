package com.shiftlog.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.shiftlog.domain.Shift;
import com.shiftlog.service.ShiftService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ShiftController.class)
class ShiftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShiftService shiftService;

    /**
     * Verifies that a valid shift creation request returns HTTP 201 and creates a shift with the specified employee IDs.
     */
    @Test
    void createsShift() throws Exception {
        Shift shift = new Shift("shift-1", LocalDate.of(2026, 5, 27), LocalTime.of(9, 0), LocalTime.of(17, 0), "Prep", Set.of("emp-1"));
        when(shiftService.create(
                LocalDate.of(2026, 5, 27),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                "Prep",
                Set.of("emp-1")))
                .thenReturn(shift);

        mockMvc.perform(post("/shifts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"date":"2026-05-27","startTime":"09:00:00","endTime":"17:00:00","notes":"Prep","employeeIds":["emp-1"]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("shift-1"))
                .andExpect(jsonPath("$.date").value("2026-05-27"))
                .andExpect(jsonPath("$.employeeIds[0]").value("emp-1"));
    }

    @Test
    void rejectsInvalidShiftRequest() throws Exception {
        mockMvc.perform(post("/shifts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"date":null,"startTime":"09:00:00","endTime":"17:00:00","notes":"Prep"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"));
    }

    @Test
    void returnsShift() throws Exception {
        when(shiftService.get("shift-1"))
                .thenReturn(new Shift("shift-1", LocalDate.of(2026, 5, 27), LocalTime.of(9, 0), LocalTime.of(17, 0), "", Set.of()));

        mockMvc.perform(get("/shifts/shift-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("shift-1"));
    }

    @Test
    void listsShifts() throws Exception {
        when(shiftService.list()).thenReturn(List.of(
                new Shift("shift-1", LocalDate.of(2026, 5, 27), LocalTime.of(9, 0), LocalTime.of(17, 0), "", Set.of())));

        mockMvc.perform(get("/shifts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void updatesShift() throws Exception {
        Shift shift = new Shift("shift-1", LocalDate.of(2026, 5, 28), LocalTime.of(10, 0), LocalTime.of(18, 0), "Dinner", Set.of("emp-1"));
        when(shiftService.update("shift-1", LocalDate.of(2026, 5, 28), LocalTime.of(10, 0), LocalTime.of(18, 0), "Dinner", Set.of("emp-1")))
                .thenReturn(shift);

        mockMvc.perform(put("/shifts/shift-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"date":"2026-05-28","startTime":"10:00:00","endTime":"18:00:00","notes":"Dinner","employeeIds":["emp-1"]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeIds[0]").value("emp-1"));
    }

    @Test
    void updatesShiftWithoutEmployeeIdsAsEmptySet() throws Exception {
        Shift shift = new Shift("shift-1", LocalDate.of(2026, 5, 28), LocalTime.of(10, 0), LocalTime.of(18, 0), "Dinner", Set.of());
        when(shiftService.update("shift-1", LocalDate.of(2026, 5, 28), LocalTime.of(10, 0), LocalTime.of(18, 0), "Dinner", Set.of()))
                .thenReturn(shift);

        mockMvc.perform(put("/shifts/shift-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"date":"2026-05-28","startTime":"10:00:00","endTime":"18:00:00","notes":"Dinner"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeIds", hasSize(0)));
    }

    @Test
    void assignsEmployee() throws Exception {
        Shift shift = new Shift("shift-1", LocalDate.of(2026, 5, 27), LocalTime.of(9, 0), LocalTime.of(17, 0), "", Set.of("emp-1"));
        when(shiftService.assignEmployee("shift-1", "emp-1")).thenReturn(shift);

        mockMvc.perform(post("/shifts/shift-1/employees/emp-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeIds[0]").value("emp-1"));
    }

    @Test
    void unassignsEmployee() throws Exception {
        Shift shift = new Shift("shift-1", LocalDate.of(2026, 5, 27), LocalTime.of(9, 0), LocalTime.of(17, 0), "", Set.of());
        when(shiftService.unassignEmployee("shift-1", "emp-1")).thenReturn(shift);

        mockMvc.perform(delete("/shifts/shift-1/employees/emp-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeIds", hasSize(0)));
    }

    @Test
    void deletesShift() throws Exception {
        mockMvc.perform(delete("/shifts/shift-1"))
                .andExpect(status().isNoContent());

        verify(shiftService).delete("shift-1");
    }
}
