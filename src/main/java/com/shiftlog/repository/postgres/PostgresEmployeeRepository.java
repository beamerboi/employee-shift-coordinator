package com.shiftlog.repository.postgres;

import com.shiftlog.domain.Employee;
import com.shiftlog.domain.Role;
import com.shiftlog.repository.EmployeeRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Profile("postgres")
public class PostgresEmployeeRepository implements EmployeeRepository {

    private final JdbcTemplate jdbcTemplate;

    public PostgresEmployeeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Employee save(Employee employee) {
        String id = employee.id() == null ? UUID.randomUUID().toString() : employee.id();
        Employee employeeToSave = employee.withId(id);
        jdbcTemplate.update("""
                insert into employees (id, name, role, hourly_rate)
                values (?, ?, ?, ?)
                on conflict (id) do update
                set name = excluded.name,
                    role = excluded.role,
                    hourly_rate = excluded.hourly_rate
                """, employeeToSave.id(), employeeToSave.name(), employeeToSave.role().name(), employeeToSave.hourlyRate());
        return employeeToSave;
    }

    @Override
    public Optional<Employee> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "select id, name, role, hourly_rate from employees where id = ?",
                    this::mapEmployee,
                    id));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public List<Employee> findAll() {
        return jdbcTemplate.query("select id, name, role, hourly_rate from employees order by name, id", this::mapEmployee);
    }

    @Override
    public boolean existsById(String id) {
        Boolean exists = jdbcTemplate.queryForObject("select exists(select 1 from employees where id = ?)", Boolean.class, id);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public void deleteById(String id) {
        jdbcTemplate.update("delete from employees where id = ?", id);
    }

    private Employee mapEmployee(ResultSet resultSet, int rowNumber) throws SQLException {
        return new Employee(
                resultSet.getString("id"),
                resultSet.getString("name"),
                Role.valueOf(resultSet.getString("role")),
                resultSet.getBigDecimal("hourly_rate"));
    }
}
