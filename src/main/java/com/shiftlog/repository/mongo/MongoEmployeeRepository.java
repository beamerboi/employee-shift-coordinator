package com.shiftlog.repository.mongo;

import com.shiftlog.domain.Employee;
import com.shiftlog.domain.Role;
import com.shiftlog.repository.EmployeeRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.bson.Document;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
@Profile("mongo")
public class MongoEmployeeRepository implements EmployeeRepository {

    private static final String COLLECTION = "employees";

    private final MongoTemplate mongoTemplate;

    public MongoEmployeeRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Employee save(Employee employee) {
        String id = employee.id() == null ? UUID.randomUUID().toString() : employee.id();
        Employee employeeToSave = employee.withId(id);
        mongoTemplate.save(toDocument(employeeToSave), COLLECTION);
        return employeeToSave;
    }

    @Override
    public Optional<Employee> findById(String id) {
        Document document = mongoTemplate.findById(id, Document.class, COLLECTION);
        return Optional.ofNullable(document).map(this::toEmployee);
    }

    @Override
    public List<Employee> findAll() {
        return mongoTemplate.findAll(Document.class, COLLECTION).stream()
                .map(this::toEmployee)
                .toList();
    }

    @Override
    public boolean existsById(String id) {
        return mongoTemplate.exists(queryById(id), COLLECTION);
    }

    @Override
    public void deleteById(String id) {
        mongoTemplate.remove(queryById(id), COLLECTION);
    }

    private static Query queryById(String id) {
        return Query.query(Criteria.where("_id").is(id));
    }

    private static Document toDocument(Employee employee) {
        return new Document("_id", employee.id())
                .append("name", employee.name())
                .append("role", employee.role().name())
                .append("hourlyRate", employee.hourlyRate().toPlainString());
    }

    private Employee toEmployee(Document document) {
        return new Employee(
                document.getString("_id"),
                document.getString("name"),
                Role.valueOf(document.getString("role")),
                new BigDecimal(document.getString("hourlyRate")));
    }
}
