package com.shiftlog.ui;

import com.shiftlog.domain.Employee;
import com.shiftlog.domain.Role;
import com.shiftlog.service.EmployeeService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public final class EmployeePanel extends SwingActionPanel {

    private static final long serialVersionUID = 1L;
    private static final String[] COLUMNS = {"Name", "Role", "Hourly rate"};

    private final EmployeeService employeeService;
    private final JTextField nameField = new JTextField(16);
    private final JComboBox<Role> roleBox = new JComboBox<>(Role.values());
    private final JTextField rateField = new JTextField(8);
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0);
    private final JTable table = new JTable(tableModel);
    private final JButton updateButton = new JButton("Update");
    private final JButton deleteButton = new JButton("Delete");
    private List<Employee> employees = new ArrayList<>();

    public EmployeePanel(EmployeeService employeeService) {
        super("employee-error");
        this.employeeService = employeeService;
        configureComponents();
        add(createForm(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(errorLabel(), BorderLayout.SOUTH);
        refresh();
        clearForm();
    }

    public void refresh() {
        employees = new ArrayList<>(employeeService.list());
        tableModel.setRowCount(0);
        for (Employee employee : employees) {
            tableModel.addRow(new Object[] {employee.name(), employee.role(), employee.hourlyRate().toPlainString()});
        }
        updateSelectionState();
    }

    private void configureComponents() {
        nameField.setName("employee-name");
        roleBox.setName("employee-role");
        rateField.setName("employee-rate");
        table.setName("employee-table");
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultEditor(Object.class, null);
        table.getSelectionModel().addListSelectionListener(event -> populateSelectedEmployee());
        updateButton.setName("employee-update");
        deleteButton.setName("employee-delete");
    }

    private JPanel createForm() {
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add");
        addButton.setName("employee-add");
        addButton.addActionListener(event -> runAction(this::addEmployee));
        updateButton.addActionListener(event -> runAction(this::updateEmployee));
        deleteButton.addActionListener(event -> runAction(this::deleteEmployee));
        form.add(new JLabel("Name"));
        form.add(nameField);
        form.add(new JLabel("Role"));
        form.add(roleBox);
        form.add(new JLabel("Hourly rate"));
        form.add(rateField);
        form.add(addButton);
        form.add(updateButton);
        form.add(deleteButton);
        return form;
    }

    private void addEmployee() {
        employeeService.create(nameField.getText(), selectedRole(), parseRate());
        refresh();
        clearForm();
    }

    private void updateEmployee() {
        Employee selected = selectedEmployee();
        employeeService.update(selected.id(), nameField.getText(), selectedRole(), parseRate());
        refresh();
        clearForm();
    }

    private void deleteEmployee() {
        employeeService.delete(selectedEmployee().id());
        refresh();
        clearForm();
    }

    private Role selectedRole() {
        return (Role) roleBox.getSelectedItem();
    }

    private BigDecimal parseRate() {
        try {
            return new BigDecimal(rateField.getText().trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Hourly rate must be a number", exception);
        }
    }

    private Employee selectedEmployee() {
        return employees.get(table.getSelectedRow());
    }

    private void populateSelectedEmployee() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            Employee employee = employees.get(selectedRow);
            nameField.setText(employee.name());
            roleBox.setSelectedItem(employee.role());
            rateField.setText(employee.hourlyRate().toPlainString());
        }
        updateSelectionState();
    }

    private void updateSelectionState() {
        boolean selected = table.getSelectedRow() >= 0;
        updateButton.setEnabled(selected);
        deleteButton.setEnabled(selected);
    }

    private void clearForm() {
        table.clearSelection();
        nameField.setText("");
        roleBox.setSelectedIndex(0);
        rateField.setText("");
        clearError();
        updateSelectionState();
    }

}
