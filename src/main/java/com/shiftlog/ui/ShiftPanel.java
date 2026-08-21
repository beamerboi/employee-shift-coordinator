package com.shiftlog.ui;

import com.shiftlog.domain.Employee;
import com.shiftlog.domain.Shift;
import com.shiftlog.service.EmployeeService;
import com.shiftlog.service.ShiftService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
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

public final class ShiftPanel extends SwingActionPanel {

    private static final long serialVersionUID = 1L;
    private static final String[] COLUMNS = {"Date", "Start", "End", "Notes", "Employee ids"};

    private final ShiftService shiftService;
    private final EmployeeService employeeService;
    private final JTextField dateField = new JTextField(10);
    private final JTextField startField = new JTextField(5);
    private final JTextField endField = new JTextField(5);
    private final JTextField notesField = new JTextField(16);
    private final JComboBox<EmployeeOption> employeeBox = new JComboBox<>();
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0);
    private final JTable table = new JTable(tableModel);
    private final JButton updateButton = new JButton("Update");
    private final JButton deleteButton = new JButton("Delete");
    private final JButton assignButton = new JButton("Assign employee");
    private List<Shift> shifts = new ArrayList<>();

    public ShiftPanel(ShiftService shiftService, EmployeeService employeeService) {
        super("shift-error");
        this.shiftService = shiftService;
        this.employeeService = employeeService;
        configureComponents();
        add(createForm(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(createAssignmentArea(), BorderLayout.SOUTH);
        refresh();
        clearForm();
    }

    public void refresh() {
        shifts = new ArrayList<>(shiftService.list());
        tableModel.setRowCount(0);
        for (Shift shift : shifts) {
            tableModel.addRow(new Object[] {
                    shift.date(),
                    shift.startTime(),
                    shift.endTime(),
                    shift.notes(),
                    String.join(", ", shift.employeeIds())
            });
        }
        employeeBox.removeAllItems();
        for (Employee employee : employeeService.list()) {
            employeeBox.addItem(new EmployeeOption(employee.id(), employee.name() + " (" + employee.role() + ")"));
        }
        updateSelectionState();
    }

    private void configureComponents() {
        dateField.setName("shift-date");
        startField.setName("shift-start");
        endField.setName("shift-end");
        notesField.setName("shift-notes");
        employeeBox.setName("shift-employee");
        table.setName("shift-table");
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultEditor(Object.class, null);
        table.getSelectionModel().addListSelectionListener(event -> populateSelectedShift());
        updateButton.setName("shift-update");
        deleteButton.setName("shift-delete");
        assignButton.setName("shift-assign");
    }

    private JPanel createForm() {
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Create");
        addButton.setName("shift-add");
        addButton.addActionListener(event -> runAction(this::addShift));
        updateButton.addActionListener(event -> runAction(this::updateShift));
        deleteButton.addActionListener(event -> runAction(this::deleteShift));
        form.add(new JLabel("Date"));
        form.add(dateField);
        form.add(new JLabel("Start"));
        form.add(startField);
        form.add(new JLabel("End"));
        form.add(endField);
        form.add(new JLabel("Notes"));
        form.add(notesField);
        form.add(addButton);
        form.add(updateButton);
        form.add(deleteButton);
        return form;
    }

    private JPanel createAssignmentArea() {
        JPanel assignment = new JPanel(new BorderLayout(8, 4));
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        assignButton.addActionListener(event -> runAction(this::assignEmployee));
        controls.add(new JLabel("Employee"));
        controls.add(employeeBox);
        controls.add(assignButton);
        assignment.add(controls, BorderLayout.NORTH);
        assignment.add(errorLabel(), BorderLayout.SOUTH);
        return assignment;
    }

    private void addShift() {
        shiftService.create(parseDate(), parseStart(), parseEnd(), notesField.getText());
        refresh();
        clearForm();
    }

    private void updateShift() {
        Shift selected = selectedShift();
        shiftService.update(
                selected.id(),
                parseDate(),
                parseStart(),
                parseEnd(),
                notesField.getText(),
                selected.employeeIds());
        refresh();
        clearForm();
    }

    private void deleteShift() {
        shiftService.delete(selectedShift().id());
        refresh();
        clearForm();
    }

    private void assignEmployee() {
        EmployeeOption employee = (EmployeeOption) employeeBox.getSelectedItem();
        shiftService.assignEmployee(selectedShift().id(), employee.id());
        refresh();
        clearForm();
    }

    private LocalDate parseDate() {
        try {
            return LocalDate.parse(dateField.getText().trim());
        } catch (DateTimeParseException exception) {
            throw invalidDateTime(exception);
        }
    }

    private LocalTime parseStart() {
        return parseTime(startField.getText());
    }

    private LocalTime parseEnd() {
        return parseTime(endField.getText());
    }

    private static LocalTime parseTime(String text) {
        try {
            return LocalTime.parse(text.trim());
        } catch (DateTimeParseException exception) {
            throw invalidDateTime(exception);
        }
    }

    private static IllegalArgumentException invalidDateTime(DateTimeParseException cause) {
        return new IllegalArgumentException("Use date YYYY-MM-DD and time HH:mm", cause);
    }

    private Shift selectedShift() {
        return shifts.get(table.getSelectedRow());
    }

    private void populateSelectedShift() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            Shift shift = shifts.get(selectedRow);
            dateField.setText(shift.date().toString());
            startField.setText(shift.startTime().toString());
            endField.setText(shift.endTime().toString());
            notesField.setText(shift.notes());
        }
        updateSelectionState();
    }

    private void updateSelectionState() {
        boolean selected = table.getSelectedRow() >= 0;
        updateButton.setEnabled(selected);
        deleteButton.setEnabled(selected);
        assignButton.setEnabled(selected && employeeBox.getItemCount() > 0);
    }

    private void clearForm() {
        table.clearSelection();
        dateField.setText("");
        startField.setText("");
        endField.setText("");
        notesField.setText("");
        clearError();
        updateSelectionState();
    }

    private record EmployeeOption(String id, String label) {

        @Override
        public String toString() {
            return label;
        }
    }
}
