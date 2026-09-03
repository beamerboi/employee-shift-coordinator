package com.shiftlog.repository.postgres;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("postgres")
public class PostgresSchema {

    public PostgresSchema(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("""
                create table if not exists employees (
                    id varchar(64) primary key,
                    name varchar(255) not null,
                    role varchar(32) not null,
                    hourly_rate numeric(12, 2) not null
                )
                """);
        jdbcTemplate.execute("""
                create table if not exists shifts (
                    id varchar(64) primary key,
                    shift_date date not null,
                    start_time time not null,
                    end_time time not null,
                    notes text not null
                )
                """);
        jdbcTemplate.execute("""
                create table if not exists shift_employees (
                    shift_id varchar(64) not null references shifts(id) on delete cascade,
                    employee_id varchar(64) not null references employees(id) on delete cascade,
                    primary key (shift_id, employee_id)
                )
                """);
    }
}
