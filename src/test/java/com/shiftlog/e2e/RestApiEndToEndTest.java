package com.shiftlog.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.shiftlog.controller.ApiError;
import com.shiftlog.controller.EmployeeRequest;
import com.shiftlog.controller.ShiftRequest;
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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("mongo")
@Testcontainers
class RestApiEndToEndTest {

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer(DockerImageName.parse("mongo:7.0"));

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
    }

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void cleanDatabase() {
        mongoTemplate.getDb().drop();
    }

    @Test
    void managesAnEmployeeAndAssignedShiftThroughTheRunningApplication() {
        Employee employee = createEmployee();
        Shift shift = createAssignedShift(employee.id());

        assertThat(mongoTemplate.getCollection("employees").countDocuments()).isOne();
        assertThat(mongoTemplate.getCollection("shifts").countDocuments()).isOne();
        assertThat(rest.getForEntity("/employees/{id}", Employee.class, employee.id()).getBody())
                .isEqualTo(employee);
        assertThat(rest.getForEntity("/shifts", Shift[].class).getBody())
                .containsExactly(shift);

        EmployeeRequest employeeUpdate = new EmployeeRequest("Ada Lovelace", Role.MANAGER, new BigDecimal("35.00"));
        ResponseEntity<Employee> updatedEmployee = rest.exchange(
                "/employees/{id}",
                HttpMethod.PUT,
                new HttpEntity<>(employeeUpdate),
                Employee.class,
                employee.id());
        assertThat(updatedEmployee.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updatedEmployee.getBody()).extracting(Employee::name).isEqualTo("Ada Lovelace");

        ResponseEntity<Shift> unassigned = rest.exchange(
                "/shifts/{shiftId}/employees/{employeeId}",
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                Shift.class,
                shift.id(),
                employee.id());
        assertThat(unassigned.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(unassigned.getBody()).extracting(Shift::employeeIds).isEqualTo(Set.of());

        ResponseEntity<Shift> reassigned = rest.postForEntity(
                "/shifts/{shiftId}/employees/{employeeId}",
                null,
                Shift.class,
                shift.id(),
                employee.id());
        assertThat(reassigned.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(reassigned.getBody()).extracting(Shift::employeeIds).isEqualTo(Set.of(employee.id()));

        assertThat(rest.exchange("/shifts/{id}", HttpMethod.DELETE, HttpEntity.EMPTY, Void.class, shift.id())
                .getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(rest.exchange("/employees/{id}", HttpMethod.DELETE, HttpEntity.EMPTY, Void.class, employee.id())
                .getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<ApiError> missingShift = rest.getForEntity("/shifts/{id}", ApiError.class, shift.id());
        assertThat(missingShift.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(missingShift.getBody()).extracting(ApiError::message)
                .isEqualTo("Shift " + shift.id() + " was not found");
        assertThat(mongoTemplate.getCollection("employees").countDocuments()).isZero();
        assertThat(mongoTemplate.getCollection("shifts").countDocuments()).isZero();
    }

    private Employee createEmployee() {
        EmployeeRequest request = new EmployeeRequest("Ada", Role.MANAGER, new BigDecimal("31.00"));
        ResponseEntity<Employee> response = rest.postForEntity("/employees", request, Employee.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isNotBlank();
        return response.getBody();
    }

    private Shift createAssignedShift(String employeeId) {
        ShiftRequest request = new ShiftRequest(
                LocalDate.of(2026, 9, 5),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                "Lunch service",
                Set.of(employeeId));
        ResponseEntity<Shift> response = rest.postForEntity("/shifts", request, Shift.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isNotBlank();
        assertThat(response.getBody().employeeIds()).containsExactly(employeeId);
        return response.getBody();
    }
}
