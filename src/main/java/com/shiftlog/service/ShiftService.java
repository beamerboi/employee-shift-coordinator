package com.shiftlog.service;

import com.shiftlog.domain.Shift;
import com.shiftlog.repository.EmployeeRepository;
import com.shiftlog.repository.ShiftRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final EmployeeRepository employeeRepository;

    public ShiftService(ShiftRepository shiftRepository, EmployeeRepository employeeRepository) {
        this.shiftRepository = shiftRepository;
        this.employeeRepository = employeeRepository;
    }

    public Shift create(LocalDate date, LocalTime startTime, LocalTime endTime, String notes) {
        return create(date, startTime, endTime, notes, Set.of());
    }

    public Shift create(
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            String notes,
            Set<String> employeeIds) {
        Shift shift = Shift.create(date, startTime, endTime, notes, employeeIds);
        ensureEmployeesExist(shift.employeeIds());
        return shiftRepository.save(shift);
    }

    public Shift get(String id) {
        return shiftRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shift", id));
    }

    public List<Shift> list() {
        return shiftRepository.findAll();
    }

    public Shift update(String id, LocalDate date, LocalTime startTime, LocalTime endTime, String notes, Set<String> employeeIds) {
        ensureShiftExists(id);
        Shift shift = new Shift(id, date, startTime, endTime, notes, employeeIds);
        ensureEmployeesExist(shift.employeeIds());
        return shiftRepository.save(shift);
    }

    public Shift assignEmployee(String shiftId, String employeeId) {
        Shift shift = get(shiftId);
        if (!employeeRepository.existsById(employeeId)) {
            throw new NotFoundException("Employee", employeeId);
        }
        return shiftRepository.save(shift.assignEmployee(employeeId));
    }

    public Shift unassignEmployee(String shiftId, String employeeId) {
        Shift shift = get(shiftId);
        return shiftRepository.save(shift.unassignEmployee(employeeId));
    }

    public void delete(String id) {
        ensureShiftExists(id);
        shiftRepository.deleteById(id);
    }

    private void ensureShiftExists(String id) {
        if (!shiftRepository.existsById(id)) {
            throw new NotFoundException("Shift", id);
        }
    }

    private void ensureEmployeesExist(Set<String> employeeIds) {
        for (String employeeId : employeeIds) {
            if (!employeeRepository.existsById(employeeId)) {
                throw new NotFoundException("Employee", employeeId);
            }
        }
    }
}
