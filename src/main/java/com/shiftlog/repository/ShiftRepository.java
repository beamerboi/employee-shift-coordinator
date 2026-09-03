package com.shiftlog.repository;

import com.shiftlog.domain.Shift;
import java.util.List;
import java.util.Optional;

public interface ShiftRepository {

    Shift save(Shift shift);

    Optional<Shift> findById(String id);

    List<Shift> findAll();

    boolean existsById(String id);

    void deleteById(String id);
}
