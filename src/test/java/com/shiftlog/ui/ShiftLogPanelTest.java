package com.shiftlog.ui;

import static com.shiftlog.ui.SwingTestSupport.component;
import static com.shiftlog.ui.SwingTestSupport.onEdt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shiftlog.service.EmployeeService;
import com.shiftlog.service.ShiftService;
import java.util.List;
import javax.swing.JTabbedPane;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShiftLogPanelTest {

    @Mock
    private EmployeeService employeeService;

    @Mock
    private ShiftService shiftService;

    @Test
    void providesEmployeeAndShiftNavigationAndRefreshesSelectedTab() {
        when(employeeService.list()).thenReturn(List.of());
        when(shiftService.list()).thenReturn(List.of());
        ShiftLogPanel panel = onEdt(() -> new ShiftLogPanel(employeeService, shiftService));
        JTabbedPane tabs = component(panel, "main-tabs", JTabbedPane.class);
        clearInvocations(employeeService, shiftService);

        onEdt(() -> tabs.setSelectedIndex(1));

        assertThat(tabs.getTitleAt(0)).isEqualTo("Employees");
        assertThat(tabs.getTitleAt(1)).isEqualTo("Shifts");
        verify(shiftService).list();
        verify(employeeService).list();

        clearInvocations(employeeService, shiftService);
        onEdt(() -> tabs.setSelectedIndex(0));

        verify(employeeService).list();
        verify(shiftService, never()).list();
    }
}
