package com.shiftlog.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.shiftlog.domain.Employee;
import com.shiftlog.domain.Role;
import com.shiftlog.domain.Shift;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@ActiveProfiles("mongo")
@Testcontainers
class MongoRepositoryIntegrationTest {

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer(DockerImageName.parse("mongo:7.0"));

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
    }

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    void storesEmployeesInMongo() {
        mongoTemplate.getDb().drop();
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
    void storesShiftsWithEmployeeIdsInMongo() {
        mongoTemplate.getDb().drop();
        Shift saved = shiftRepository.save(Shift.create(
                LocalDate.of(2026, 5, 27),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                "Prep",
                Set.of("emp-1")));

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
                Set.of("emp-1", "emp-2")));

        assertThat(shiftRepository.findById(saved.id())).contains(updated);

        shiftRepository.deleteById(saved.id());

        assertThat(shiftRepository.findById(saved.id())).isEmpty();
        assertThat(shiftRepository.existsById(saved.id())).isFalse();
    }
}
