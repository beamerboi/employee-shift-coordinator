package com.shiftlog.service;

import com.shiftlog.domain.Employee;
import com.shiftlog.domain.Role;
import com.shiftlog.repository.EmployeeRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Employee create(String name, Role role, BigDecimal hourlyRate) {
        return employeeRepository.save(Employee.create(name, role, hourlyRate));
    }

    public Employee get(String id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee", id));
    }

    public List<Employee> list() {
        return employeeRepository.findAll();
    }

    public Employee update(String id, String name, Role role, BigDecimal hourlyRate) {
        ensureExists(id);
        return employeeRepository.save(new Employee(id, name, role, hourlyRate));
    }

    public void delete(String id) {
        ensureExists(id);
        employeeRepository.deleteById(id);
    }

    private void ensureExists(String id) {
        if (!employeeRepository.existsById(id)) {
            throw new NotFoundException("Employee", id);
        }
    }
}
