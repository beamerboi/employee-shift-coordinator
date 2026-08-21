package com.shiftlog.ui;

import com.shiftlog.service.EmployeeService;
import com.shiftlog.service.ShiftService;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public final class ShiftLogPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final EmployeePanel employeePanel;
    private final ShiftPanel shiftPanel;

    public ShiftLogPanel(EmployeeService employeeService, ShiftService shiftService) {
        super(new BorderLayout());
        employeePanel = new EmployeePanel(employeeService);
        shiftPanel = new ShiftPanel(shiftService, employeeService);
        JTabbedPane tabs = new JTabbedPane();
        tabs.setName("main-tabs");
        tabs.addTab("Employees", employeePanel);
        tabs.addTab("Shifts", shiftPanel);
        tabs.addChangeListener(event -> refreshSelectedTab(tabs));
        add(tabs, BorderLayout.CENTER);
    }

    private void refreshSelectedTab(JTabbedPane tabs) {
        if (tabs.getSelectedComponent() == employeePanel) {
            employeePanel.refresh();
        } else {
            shiftPanel.refresh();
        }
    }
}
