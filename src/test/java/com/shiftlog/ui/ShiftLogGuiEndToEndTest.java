package com.shiftlog.ui;

import static com.shiftlog.ui.SwingTestSupport.component;
import static com.shiftlog.ui.SwingTestSupport.onEdt;
import static org.assertj.core.api.Assertions.assertThat;

import com.shiftlog.domain.Employee;
import com.shiftlog.domain.Role;
import com.shiftlog.domain.Shift;
import com.shiftlog.repository.EmployeeRepository;
import com.shiftlog.repository.ShiftRepository;
import com.shiftlog.service.EmployeeService;
import com.shiftlog.service.ShiftService;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTabbedPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
class ShiftLogGuiEndToEndTest {

    private EmployeeService employeeService;
    private ShiftService shiftService;
    private ShiftLogPanel panel;

    @BeforeEach
    void createApplication() {
        InMemoryEmployeeRepository employees = new InMemoryEmployeeRepository();
        employeeService = new EmployeeService(employees);
        shiftService = new ShiftService(new InMemoryShiftRepository(), employees);
        panel = onEdt(() -> new ShiftLogPanel(employeeService, shiftService));
    }

    @Test
    void createsEditsAndDeletesEmployeeThroughGui() {
        onEdt(() -> {
            text("employee-name").setText("Ada");
            combo("employee-role").setSelectedItem(Role.MANAGER);
            text("employee-rate").setText("31.50");
            button("employee-add").doClick();

            JTable table = table("employee-table");
            assertThat(table.getRowCount()).isOne();
            table.setRowSelectionInterval(0, 0);
            text("employee-name").setText("Ada Lovelace");
            button("employee-update").doClick();
            assertThat(table.getValueAt(0, 0)).isEqualTo("Ada Lovelace");

            table.setRowSelectionInterval(0, 0);
            button("employee-delete").doClick();
            assertThat(table.getRowCount()).isZero();
        });

        assertThat(employeeService.list()).isEmpty();
    }

    @Test
    void createsShiftAssignsEmployeeEditsAndDeletesThroughGui() {
        onEdt(() -> {
            text("employee-name").setText("Grace");
            text("employee-rate").setText("25");
            button("employee-add").doClick();
            component(panel, "main-tabs", JTabbedPane.class).setSelectedIndex(1);

            LocalDate date = LocalDate.of(2026, 9, 3);
            spinner("shift-date").setValue(toDate(date, LocalTime.MIDNIGHT));
            spinner("shift-start").setValue(toDate(date, LocalTime.of(9, 0)));
            spinner("shift-end").setValue(toDate(date, LocalTime.of(17, 0)));
            text("shift-notes").setText("Lunch");
            combo("shift-employee").setSelectedIndex(1);
            button("shift-add").doClick();

            JTable table = table("shift-table");
            assertThat(table.getValueAt(0, 4)).isEqualTo("Grace");

            table.setRowSelectionInterval(0, 0);
            text("shift-notes").setText("Dinner");
            button("shift-update").doClick();
            assertThat(table.getValueAt(0, 3)).isEqualTo("Dinner");

            table.setRowSelectionInterval(0, 0);
            button("shift-delete").doClick();
            assertThat(table.getRowCount()).isZero();
        });

        assertThat(shiftService.list()).isEmpty();
        assertThat(employeeService.list()).hasSize(1);
    }

    private JTextField text(String name) {
        return component(panel, name, JTextField.class);
    }

    private JButton button(String name) {
        return component(panel, name, JButton.class);
    }

    private JSpinner spinner(String name) {
        return component(panel, name, JSpinner.class);
    }

    private static Date toDate(LocalDate date, LocalTime time) {
        return Date.from(date.atTime(time).atZone(ZoneId.systemDefault()).toInstant());
    }

    private JTable table(String name) {
        return component(panel, name, JTable.class);
    }

    private JComboBox<Object> combo(String name) {
        return component(panel, name, JComboBox.class);
    }

    private static final class InMemoryEmployeeRepository implements EmployeeRepository {

        private final Map<String, Employee> employees = new LinkedHashMap<>();
        private int nextId = 1;

        @Override
        public Employee save(Employee employee) {
            Employee saved = employee.id() == null ? employee.withId("employee-" + nextId++) : employee;
            employees.put(saved.id(), saved);
            return saved;
        }

        @Override
        public Optional<Employee> findById(String id) {
            return Optional.ofNullable(employees.get(id));
        }

        @Override
        public List<Employee> findAll() {
            return new ArrayList<>(employees.values());
        }

        @Override
        public boolean existsById(String id) {
            return employees.containsKey(id);
        }

        @Override
        public void deleteById(String id) {
            employees.remove(id);
        }
    }

    private static final class InMemoryShiftRepository implements ShiftRepository {

        private final Map<String, Shift> shifts = new LinkedHashMap<>();
        private int nextId = 1;

        @Override
        public Shift save(Shift shift) {
            Shift saved = shift.id() == null ? shift.withId("shift-" + nextId++) : shift;
            shifts.put(saved.id(), saved);
            return saved;
        }

        @Override
        public Optional<Shift> findById(String id) {
            return Optional.ofNullable(shifts.get(id));
        }

        @Override
        public List<Shift> findAll() {
            return new ArrayList<>(shifts.values());
        }

        @Override
        public boolean existsById(String id) {
            return shifts.containsKey(id);
        }

        @Override
        public void deleteById(String id) {
            shifts.remove(id);
        }
    }
}
