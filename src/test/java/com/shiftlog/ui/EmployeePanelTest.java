package com.shiftlog.ui;

import static com.shiftlog.ui.SwingTestSupport.component;
import static com.shiftlog.ui.SwingTestSupport.onEdt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shiftlog.domain.Employee;
import com.shiftlog.domain.Role;
import com.shiftlog.service.EmployeeService;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class EmployeePanelTest {

    @Mock
    private EmployeeService employeeService;

    private EmployeePanel panel;

    @BeforeEach
    void createPanel() {
        when(employeeService.list()).thenReturn(List.of());
        panel = onEdt(() -> new EmployeePanel(employeeService));
    }

    @Test
    void startsWithAnEmptyFormAndEditActionsDisabled() {
        onEdt(() -> {
            assertThat(table().getRowCount()).isZero();
            assertThat(button("employee-update").isEnabled()).isFalse();
            assertThat(button("employee-delete").isEnabled()).isFalse();
            assertThat(error().isVisible()).isFalse();
        });
    }

    @Test
    void createsEmployeeAndRefreshesTheTable() {
        Employee saved = new Employee("emp-1", "Ada", Role.MANAGER, new BigDecimal("31.5"));
        when(employeeService.create("Ada", Role.MANAGER, new BigDecimal("31.50"))).thenReturn(saved);
        when(employeeService.list()).thenReturn(List.of(saved));

        onEdt(() -> {
            text("employee-name").setText("Ada");
            role().setSelectedItem(Role.MANAGER);
            text("employee-rate").setText("31.50");
            button("employee-add").doClick();

            assertThat(table().getRowCount()).isOne();
            assertThat(table().getValueAt(0, 0)).isEqualTo("Ada");
            assertThat(text("employee-name").getText()).isEmpty();
            assertThat(error().isVisible()).isFalse();
        });
        verify(employeeService).create("Ada", Role.MANAGER, new BigDecimal("31.50"));
    }

    @Test
    void validatesHourlyRateBeforeCallingTheService() {
        onEdt(() -> {
            text("employee-name").setText("Ada");
            text("employee-rate").setText("not-a-rate");
            button("employee-add").doClick();

            assertThat(error().getText()).isEqualTo("Hourly rate must be a number");
            assertThat(error().isVisible()).isTrue();
        });
        verify(employeeService, never()).create(any(), any(), any());
    }

    @Test
    void presentsBusinessValidationErrors() {
        when(employeeService.create(" ", Role.WAITER, BigDecimal.TEN))
                .thenThrow(new IllegalArgumentException("Employee name is required"));

        onEdt(() -> {
            text("employee-name").setText(" ");
            text("employee-rate").setText("10");
            button("employee-add").doClick();

            assertThat(error().getText()).isEqualTo("Employee name is required");
        });
    }

    @Test
    void selectingAnEmployeePopulatesTheFormAndEnablesEditing() {
        Employee employee = new Employee("emp-1", "Ada", Role.COOK, BigDecimal.valueOf(20));
        when(employeeService.list()).thenReturn(List.of(employee));
        panel = onEdt(() -> new EmployeePanel(employeeService));

        onEdt(() -> {
            table().setRowSelectionInterval(0, 0);

            assertThat(text("employee-name").getText()).isEqualTo("Ada");
            assertThat(role().getSelectedItem()).isEqualTo(Role.COOK);
            assertThat(text("employee-rate").getText()).isEqualTo("20");
            assertThat(button("employee-update").isEnabled()).isTrue();
            assertThat(button("employee-delete").isEnabled()).isTrue();
        });
    }

    @Test
    void editsTheSelectedEmployee() {
        Employee employee = new Employee("emp-1", "Ada", Role.COOK, BigDecimal.valueOf(20));
        Employee updated = new Employee("emp-1", "Grace", Role.MANAGER, BigDecimal.valueOf(40));
        when(employeeService.list()).thenReturn(List.of(employee), List.of(updated));
        when(employeeService.update("emp-1", "Grace", Role.MANAGER, BigDecimal.valueOf(40))).thenReturn(updated);
        panel = onEdt(() -> new EmployeePanel(employeeService));

        onEdt(() -> {
            table().setRowSelectionInterval(0, 0);
            text("employee-name").setText("Grace");
            role().setSelectedItem(Role.MANAGER);
            text("employee-rate").setText("40");
            button("employee-update").doClick();

            assertThat(table().getValueAt(0, 0)).isEqualTo("Grace");
            assertThat(button("employee-update").isEnabled()).isFalse();
        });
        verify(employeeService).update("emp-1", "Grace", Role.MANAGER, BigDecimal.valueOf(40));
    }

    @Test
    void deletesTheSelectedEmployee() {
        Employee employee = new Employee("emp-1", "Ada", Role.COOK, BigDecimal.valueOf(20));
        when(employeeService.list()).thenReturn(List.of(employee), List.of());
        panel = onEdt(() -> new EmployeePanel(employeeService));

        onEdt(() -> {
            table().setRowSelectionInterval(0, 0);
            button("employee-delete").doClick();

            assertThat(table().getRowCount()).isZero();
        });
        verify(employeeService).delete("emp-1");
    }

    @Test
    void presentsRepositoryErrorsAndKeepsTheFormValues() {
        when(employeeService.create("Ada", Role.WAITER, BigDecimal.TEN))
                .thenThrow(new IllegalStateException("Database unavailable"));

        onEdt(() -> {
            text("employee-name").setText("Ada");
            text("employee-rate").setText("10");
            button("employee-add").doClick();

            assertThat(error().getText()).isEqualTo("Database unavailable");
            assertThat(text("employee-name").getText()).isEqualTo("Ada");
        });
    }

    private JTable table() {
        return component(panel, "employee-table", JTable.class);
    }

    private JTextField text(String name) {
        return component(panel, name, JTextField.class);
    }

    @SuppressWarnings("unchecked")
    private JComboBox<Role> role() {
        return component(panel, "employee-role", JComboBox.class);
    }

    private JButton button(String name) {
        return component(panel, name, JButton.class);
    }

    private JLabel error() {
        return component(panel, "employee-error", JLabel.class);
    }
}
