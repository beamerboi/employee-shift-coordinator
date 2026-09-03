package com.shiftlog.repository.postgres;

import com.shiftlog.domain.Shift;
import com.shiftlog.repository.ShiftRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Profile("postgres")
public class PostgresShiftRepository implements ShiftRepository {

    private final JdbcTemplate jdbcTemplate;

    public PostgresShiftRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public Shift save(Shift shift) {
        String id = shift.id() == null ? UUID.randomUUID().toString() : shift.id();
        Shift shiftToSave = shift.withId(id);
        jdbcTemplate.update("""
                insert into shifts (id, shift_date, start_time, end_time, notes)
                values (?, ?, ?, ?, ?)
                on conflict (id) do update
                set shift_date = excluded.shift_date,
                    start_time = excluded.start_time,
                    end_time = excluded.end_time,
                    notes = excluded.notes
                """, shiftToSave.id(), shiftToSave.date(), Time.valueOf(shiftToSave.startTime()),
                Time.valueOf(shiftToSave.endTime()), shiftToSave.notes());
        jdbcTemplate.update("delete from shift_employees where shift_id = ?", shiftToSave.id());
        for (String employeeId : shiftToSave.employeeIds()) {
            jdbcTemplate.update("insert into shift_employees (shift_id, employee_id) values (?, ?)", shiftToSave.id(), employeeId);
        }
        return shiftToSave;
    }

    @Override
    public Optional<Shift> findById(String id) {
        try {
            Shift shift = jdbcTemplate.queryForObject(
                    "select id, shift_date, start_time, end_time, notes from shifts where id = ?",
                    this::mapShift,
                    id);
            return Optional.ofNullable(withEmployeeIds(shift));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public List<Shift> findAll() {
        return jdbcTemplate.query(
                        "select id, shift_date, start_time, end_time, notes from shifts order by shift_date, start_time, id",
                        this::mapShift)
                .stream()
                .map(this::withEmployeeIds)
                .toList();
    }

    @Override
    public boolean existsById(String id) {
        Boolean exists = jdbcTemplate.queryForObject("select exists(select 1 from shifts where id = ?)", Boolean.class, id);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public void deleteById(String id) {
        jdbcTemplate.update("delete from shifts where id = ?", id);
    }

    private Shift mapShift(ResultSet resultSet, int rowNumber) throws SQLException {
        return new Shift(
                resultSet.getString("id"),
                resultSet.getDate("shift_date").toLocalDate(),
                resultSet.getTime("start_time").toLocalTime(),
                resultSet.getTime("end_time").toLocalTime(),
                resultSet.getString("notes"),
                Set.of());
    }

    private Shift withEmployeeIds(Shift shift) {
        List<String> employeeIds = jdbcTemplate.queryForList(
                "select employee_id from shift_employees where shift_id = ? order by employee_id",
                String.class,
                shift.id());
        return new Shift(shift.id(), shift.date(), shift.startTime(), shift.endTime(), shift.notes(), new LinkedHashSet<>(employeeIds));
    }
}
