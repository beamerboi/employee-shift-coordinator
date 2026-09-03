package com.shiftlog.repository;

import com.shiftlog.domain.Employee;
import java.util.List;
import java.util.Optional;

public interface EmployeeRepository {

    Employee save(Employee employee);

    Optional<Employee> findById(String id);

    List<Employee> findAll();

    boolean existsById(String id);

    void deleteById(String id);
}
