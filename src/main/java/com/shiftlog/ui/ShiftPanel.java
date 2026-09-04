package com.shiftlog.ui;

import com.shiftlog.domain.Employee;
import com.shiftlog.domain.Shift;
import com.shiftlog.service.EmployeeService;
import com.shiftlog.service.ShiftService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableModel;

public final class ShiftPanel extends SwingActionPanel {

    private static final long serialVersionUID = 1L;
    private static final String[] COLUMNS = {"Date", "Start", "End", "Notes", "Employees"};
    private static final LocalTime DEFAULT_START = LocalTime.of(9, 0);
    private static final LocalTime DEFAULT_END = LocalTime.of(17, 0);

    private final ShiftService shiftService;
    private final EmployeeService employeeService;
    private final JSpinner datePicker = createDatePicker();
    private final JSpinner startPicker = createTimePicker(DEFAULT_START);
    private final JSpinner endPicker = createTimePicker(DEFAULT_END);
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
        List<Employee> employees = employeeService.list();
        Map<String, String> employeeNamesById = employees.stream()
                .collect(Collectors.toMap(Employee::id, Employee::name));
        shifts = new ArrayList<>(shiftService.list());
        tableModel.setRowCount(0);
        for (Shift shift : shifts) {
            tableModel.addRow(new Object[] {
                    shift.date(),
                    shift.startTime(),
                    shift.endTime(),
                    shift.notes(),
                    employeeNames(shift, employeeNamesById)
            });
        }
        employeeBox.removeAllItems();
        employeeBox.addItem(new EmployeeOption(null, "Select an employee"));
        for (Employee employee : employees) {
            employeeBox.addItem(new EmployeeOption(employee.id(), employee.name() + " (" + employee.role() + ")"));
        }
        updateSelectionState();
    }

    private static String employeeNames(Shift shift, Map<String, String> employeeNamesById) {
        return shift.employeeIds().stream()
                .map(id -> employeeNamesById.getOrDefault(id, id))
                .collect(Collectors.joining(", "));
    }

    private void configureComponents() {
        datePicker.setName("shift-date");
        startPicker.setName("shift-start");
        endPicker.setName("shift-end");
        notesField.setName("shift-notes");
        employeeBox.setName("shift-employee");
        table.setName("shift-table");
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultEditor(Object.class, null);
        table.getSelectionModel().addListSelectionListener(event -> populateSelectedShift());
        employeeBox.addActionListener(event -> updateSelectionState());
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
        form.add(datePicker);
        form.add(new JLabel("Start"));
        form.add(startPicker);
        form.add(new JLabel("End"));
        form.add(endPicker);
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
        controls.add(new JLabel("Employee (assigned automatically on Create)"));
        controls.add(employeeBox);
        controls.add(assignButton);
        assignment.add(controls, BorderLayout.NORTH);
        assignment.add(errorLabel(), BorderLayout.SOUTH);
        return assignment;
    }

    private void addShift() {
        shiftService.create(
                selectedDate(), selectedStart(), selectedEnd(), notesField.getText(), selectedEmployeeIds());
        refresh();
        clearForm();
    }

    private void updateShift() {
        Shift selected = selectedShift();
        shiftService.update(
                selected.id(),
                selectedDate(),
                selectedStart(),
                selectedEnd(),
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
        EmployeeOption employee = selectedEmployee();
        shiftService.assignEmployee(selectedShift().id(), employee.id());
        refresh();
        clearForm();
    }

    private Set<String> selectedEmployeeIds() {
        EmployeeOption employee = selectedEmployee();
        return employee == null ? Set.of() : Set.of(employee.id());
    }

    private EmployeeOption selectedEmployee() {
        EmployeeOption employee = (EmployeeOption) employeeBox.getSelectedItem();
        return employee == null || employee.id() == null ? null : employee;
    }

    private LocalDate selectedDate() {
        commit(datePicker);
        return pickerDate(datePicker).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private LocalTime selectedStart() {
        return selectedTime(startPicker);
    }

    private LocalTime selectedEnd() {
        return selectedTime(endPicker);
    }

    private static LocalTime selectedTime(JSpinner picker) {
        commit(picker);
        return pickerDate(picker).toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
                .withSecond(0)
                .withNano(0);
    }

    private static void commit(JSpinner picker) {
        try {
            picker.commitEdit();
        } catch (ParseException exception) {
            throw new IllegalArgumentException("Choose a valid date and time", exception);
        }
    }

    private static Date pickerDate(JSpinner picker) {
        return (Date) picker.getValue();
    }

    private Shift selectedShift() {
        return shifts.get(table.getSelectedRow());
    }

    private void populateSelectedShift() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            Shift shift = shifts.get(selectedRow);
            datePicker.setValue(toDate(shift.date(), LocalTime.MIDNIGHT));
            startPicker.setValue(toDate(shift.date(), shift.startTime()));
            endPicker.setValue(toDate(shift.date(), shift.endTime()));
            notesField.setText(shift.notes());
        }
        updateSelectionState();
    }

    private void updateSelectionState() {
        boolean selected = table.getSelectedRow() >= 0;
        updateButton.setEnabled(selected);
        deleteButton.setEnabled(selected);
        assignButton.setEnabled(selected && selectedEmployee() != null);
    }

    private void clearForm() {
        table.clearSelection();
        LocalDate today = LocalDate.now();
        datePicker.setValue(toDate(today, LocalTime.MIDNIGHT));
        startPicker.setValue(toDate(today, DEFAULT_START));
        endPicker.setValue(toDate(today, DEFAULT_END));
        notesField.setText("");
        clearError();
        updateSelectionState();
    }

    private static JSpinner createDatePicker() {
        JSpinner picker = new JSpinner(new SpinnerDateModel(
                toDate(LocalDate.now(), LocalTime.MIDNIGHT), null, null, Calendar.DAY_OF_MONTH));
        picker.setEditor(new JSpinner.DateEditor(picker, "yyyy-MM-dd"));
        return picker;
    }

    private static JSpinner createTimePicker(LocalTime initialTime) {
        JSpinner picker = new JSpinner(new SpinnerDateModel(
                toDate(LocalDate.now(), initialTime), null, null, Calendar.MINUTE));
        picker.setEditor(new JSpinner.DateEditor(picker, "HH:mm"));
        return picker;
    }

    private static Date toDate(LocalDate date, LocalTime time) {
        return Date.from(date.atTime(time).atZone(ZoneId.systemDefault()).toInstant());
    }

    private record EmployeeOption(String id, String label) {

        @Override
        public String toString() {
            return label;
        }
    }
}
