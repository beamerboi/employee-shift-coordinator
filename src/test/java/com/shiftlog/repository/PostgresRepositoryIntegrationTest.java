package com.shiftlog.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.shiftlog.domain.Employee;
import com.shiftlog.domain.Role;
import com.shiftlog.domain.Shift;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("postgres")
@Testcontainers
class PostgresRepositoryIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("delete from shift_employees");
        jdbcTemplate.update("delete from shifts");
        jdbcTemplate.update("delete from employees");
    }

    @Test
    void storesEmployeesInPostgres() {
        Employee saved = employeeRepository.save(Employee.create("Ada", Role.MANAGER, BigDecimal.valueOf(31)));

        assertThat(saved.id()).isNotBlank();
        assertThat(employeeRepository.existsById(saved.id())).isTrue();
        assertThat(employeeRepository.findById(saved.id())).contains(saved);
        assertThat(employeeRepository.findAll()).containsExactly(saved);

        Employee updated = employeeRepository.save(new Employee(saved.id(), "Ada Lovelace", Role.MANAGER, BigDecimal.valueOf(35)));

        assertThat(employeeRepository.findById(saved.id())).contains(updated);

        employeeRepository.deleteById(saved.id());

        assertThat(employeeRepository.findById(saved.id())).isEmpty();
        assertThat(employeeRepository.existsById(saved.id())).isFalse();
    }

    @Test
    void storesShiftsAndJoinRowsInPostgres() {
        Employee firstEmployee = employeeRepository.save(Employee.create("Ada", Role.MANAGER, BigDecimal.valueOf(31)));
        Employee secondEmployee = employeeRepository.save(Employee.create("Grace", Role.COOK, BigDecimal.valueOf(28)));
        Shift saved = shiftRepository.save(Shift.create(
                LocalDate.of(2026, 5, 27),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                "Prep",
                Set.of(firstEmployee.id())));

        assertThat(saved.id()).isNotBlank();
        assertThat(shiftRepository.existsById(saved.id())).isTrue();
        assertThat(shiftRepository.findById(saved.id())).contains(saved);
        assertThat(shiftRepository.findAll()).containsExactly(saved);

        Shift updated = shiftRepository.save(new Shift(
                saved.id(),
                LocalDate.of(2026, 5, 28),
                LocalTime.of(10, 0),
                LocalTime.of(18, 0),
                "Dinner",
                Set.of(secondEmployee.id())));

        assertThat(shiftRepository.findById(saved.id()))
                .contains(updated)
                .get()
                .extracting(Shift::employeeIds)
                .isEqualTo(Set.of(secondEmployee.id()));

        shiftRepository.deleteById(saved.id());

        assertThat(shiftRepository.findById(saved.id())).isEmpty();
        assertThat(shiftRepository.existsById(saved.id())).isFalse();
    }
}
