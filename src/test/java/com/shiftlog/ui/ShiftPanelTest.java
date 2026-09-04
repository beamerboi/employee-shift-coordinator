package com.shiftlog.ui;

import static com.shiftlog.ui.SwingTestSupport.component;
import static com.shiftlog.ui.SwingTestSupport.onEdt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shiftlog.domain.Employee;
import com.shiftlog.domain.Role;
import com.shiftlog.domain.Shift;
import com.shiftlog.service.EmployeeService;
import com.shiftlog.service.ShiftService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ShiftPanelTest {

    private static final LocalDate DATE = LocalDate.of(2026, 9, 3);
    private static final LocalTime START = LocalTime.of(9, 0);
    private static final LocalTime END = LocalTime.of(17, 0);

    @Mock
    private ShiftService shiftService;

    @Mock
    private EmployeeService employeeService;

    private ShiftPanel panel;

    @BeforeEach
    void createPanel() {
        when(shiftService.list()).thenReturn(List.of());
        when(employeeService.list()).thenReturn(List.of());
        panel = onEdt(() -> new ShiftPanel(shiftService, employeeService));
    }

    @Test
    void startsWithSelectionActionsDisabled() {
        onEdt(() -> {
            assertThat(table().getRowCount()).isZero();
            assertThat(button("shift-update").isEnabled()).isFalse();
            assertThat(button("shift-delete").isEnabled()).isFalse();
            assertThat(employees().isSelectionEmpty()).isTrue();
            assertThat(error().isVisible()).isFalse();
        });
    }

    @Test
    void createsShiftAndRefreshesTheTable() {
        Shift saved = new Shift("shift-1", DATE, START, END, "Lunch", Set.of());
        when(shiftService.create(DATE, START, END, "Lunch", Set.of())).thenReturn(saved);
        when(shiftService.list()).thenReturn(List.of(saved));

        onEdt(() -> {
            fillForm(DATE, START, END, "Lunch");
            button("shift-add").doClick();

            assertThat(table().getRowCount()).isOne();
            assertThat(table().getValueAt(0, 0)).isEqualTo(DATE);
            assertThat(text("shift-notes").getText()).isEmpty();
        });
        verify(shiftService).create(DATE, START, END, "Lunch", Set.of());
    }

    @Test
    void createsShiftWithMultipleSelectedEmployees() {
        Employee first = new Employee("emp-1", "Ada", Role.WAITER, BigDecimal.TEN);
        Employee second = new Employee("emp-2", "Grace", Role.COOK, BigDecimal.TEN);
        Shift saved = new Shift("shift-1", DATE, START, END, "Lunch", Set.of("emp-1", "emp-2"));
        when(employeeService.list()).thenReturn(List.of(first, second));
        when(shiftService.create(DATE, START, END, "Lunch", Set.of("emp-1", "emp-2"))).thenReturn(saved);
        when(shiftService.list()).thenReturn(List.of(), List.of(saved));
        panel = onEdt(() -> new ShiftPanel(shiftService, employeeService));

        onEdt(() -> {
            employees().setSelectedIndices(new int[] {0, 1});
            fillForm(DATE, START, END, "Lunch");
            button("shift-add").doClick();

            assertThat(table().getValueAt(0, 4).toString()).contains("Ada", "Grace");
            assertThat(employees().isSelectionEmpty()).isTrue();
        });
        verify(shiftService).create(DATE, START, END, "Lunch", Set.of("emp-1", "emp-2"));
    }

    @Test
    void validatesPickerTextBeforeCallingService() {
        onEdt(() -> {
            fillForm(DATE, START, END, "Lunch");
            editor("shift-start").setText("morning");
            button("shift-add").doClick();

            assertThat(error().getText()).isEqualTo("Choose a valid date and time");
        });
        verify(shiftService, never()).create(DATE, START, END, "Lunch", Set.of());
    }

    @Test
    void presentsBusinessErrors() {
        when(shiftService.create(DATE, END, START, "Invalid", Set.of()))
                .thenThrow(new IllegalArgumentException("Shift end time must be after start time"));

        onEdt(() -> {
            fillForm(DATE, END, START, "Invalid");
            button("shift-add").doClick();

            assertThat(error().getText()).isEqualTo("Shift end time must be after start time");
        });
    }

    @Test
    void selectingShiftPopulatesFormAndEnablesEditActions() {
        Shift shift = new Shift("shift-1", DATE, START, END, "Lunch", Set.of());
        when(shiftService.list()).thenReturn(List.of(shift));
        panel = onEdt(() -> new ShiftPanel(shiftService, employeeService));

        onEdt(() -> {
            table().setRowSelectionInterval(0, 0);

            assertThat(date("shift-date")).isEqualTo(DATE);
            assertThat(time("shift-start")).isEqualTo(START);
            assertThat(time("shift-end")).isEqualTo(END);
            assertThat(text("shift-notes").getText()).isEqualTo("Lunch");
            assertThat(button("shift-update").isEnabled()).isTrue();
            assertThat(button("shift-delete").isEnabled()).isTrue();
        });
    }

    @Test
    void restoresPreviousAssignmentsAndUpdatesTheSelection() {
        Employee first = new Employee("emp-1", "Ada", Role.WAITER, BigDecimal.TEN);
        Employee second = new Employee("emp-2", "Grace", Role.COOK, BigDecimal.TEN);
        Employee third = new Employee("emp-3", "Linus", Role.MANAGER, BigDecimal.TEN);
        Shift shift = new Shift("shift-1", DATE, START, END, "Lunch", Set.of("emp-1", "emp-2"));
        Shift updated = new Shift("shift-1", DATE.plusDays(1), START, END, "Dinner", Set.of("emp-2"));
        when(employeeService.list()).thenReturn(List.of(first, second, third));
        when(shiftService.list()).thenReturn(List.of(shift), List.of(updated));
        when(shiftService.update("shift-1", DATE.plusDays(1), START, END, "Dinner", Set.of("emp-2")))
                .thenReturn(updated);
        panel = onEdt(() -> new ShiftPanel(shiftService, employeeService));

        onEdt(() -> {
            table().setRowSelectionInterval(0, 0);
            assertThat(employees().getSelectedValuesList())
                    .extracting(Object::toString)
                    .containsExactly("Ada (WAITER)", "Grace (COOK)");
            employees().setSelectedIndex(1);
            fillForm(DATE.plusDays(1), START, END, "Dinner");
            button("shift-update").doClick();

            assertThat(table().getValueAt(0, 0)).isEqualTo(DATE.plusDays(1));
            assertThat(table().getValueAt(0, 4)).isEqualTo("Grace");
        });
        verify(shiftService).update("shift-1", DATE.plusDays(1), START, END, "Dinner", Set.of("emp-2"));
    }

    @Test
    void deletesSelectedShift() {
        Shift shift = new Shift("shift-1", DATE, START, END, "Lunch", Set.of());
        when(shiftService.list()).thenReturn(List.of(shift), List.of());
        panel = onEdt(() -> new ShiftPanel(shiftService, employeeService));

        onEdt(() -> {
            table().setRowSelectionInterval(0, 0);
            button("shift-delete").doClick();
            assertThat(table().getRowCount()).isZero();
        });
        verify(shiftService).delete("shift-1");
    }

    private void fillForm(LocalDate date, LocalTime start, LocalTime end, String notes) {
        spinner("shift-date").setValue(toDate(date, LocalTime.MIDNIGHT));
        spinner("shift-start").setValue(toDate(date, start));
        spinner("shift-end").setValue(toDate(date, end));
        text("shift-notes").setText(notes);
    }

    private LocalDate date(String name) {
        return value(name).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private LocalTime time(String name) {
        return value(name).toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
                .withSecond(0)
                .withNano(0);
    }

    private Date value(String name) {
        return (Date) spinner(name).getValue();
    }

    private JSpinner spinner(String name) {
        return component(panel, name, JSpinner.class);
    }

    private JFormattedTextField editor(String name) {
        return ((JSpinner.DefaultEditor) spinner(name).getEditor()).getTextField();
    }

    private static Date toDate(LocalDate date, LocalTime time) {
        return Date.from(date.atTime(time).atZone(ZoneId.systemDefault()).toInstant());
    }

    private JTable table() {
        return component(panel, "shift-table", JTable.class);
    }

    private JTextField text(String name) {
        return component(panel, name, JTextField.class);
    }

    private JButton button(String name) {
        return component(panel, name, JButton.class);
    }

    @SuppressWarnings("unchecked")
    private JList<Object> employees() {
        return component(panel, "shift-employees", JList.class);
    }

    private JLabel error() {
        return component(panel, "shift-error", JLabel.class);
    }
}
