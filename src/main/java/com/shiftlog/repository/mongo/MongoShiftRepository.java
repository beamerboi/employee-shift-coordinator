package com.shiftlog.repository.mongo;

import com.shiftlog.domain.Shift;
import com.shiftlog.repository.ShiftRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.bson.Document;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
@Profile("mongo")
public class MongoShiftRepository implements ShiftRepository {

    private static final String COLLECTION = "shifts";

    private final MongoTemplate mongoTemplate;

    public MongoShiftRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Shift save(Shift shift) {
        String id = shift.id() == null ? UUID.randomUUID().toString() : shift.id();
        Shift shiftToSave = shift.withId(id);
        mongoTemplate.save(toDocument(shiftToSave), COLLECTION);
        return shiftToSave;
    }

    @Override
    public Optional<Shift> findById(String id) {
        Document document = mongoTemplate.findById(id, Document.class, COLLECTION);
        return Optional.ofNullable(document).map(this::toShift);
    }

    @Override
    public List<Shift> findAll() {
        return mongoTemplate.findAll(Document.class, COLLECTION).stream()
                .map(this::toShift)
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

    private static Document toDocument(Shift shift) {
        return new Document("_id", shift.id())
                .append("date", shift.date().toString())
                .append("startTime", shift.startTime().toString())
                .append("endTime", shift.endTime().toString())
                .append("notes", shift.notes())
                .append("employeeIds", List.copyOf(shift.employeeIds()));
    }

    private Shift toShift(Document document) {
        return new Shift(
                document.getString("_id"),
                LocalDate.parse(document.getString("date")),
                LocalTime.parse(document.getString("startTime")),
                LocalTime.parse(document.getString("endTime")),
                document.getString("notes"),
                employeeIds(document));
    }

    private static Set<String> employeeIds(Document document) {
        return new LinkedHashSet<>(document.getList("employeeIds", String.class, List.of()));
    }
}
