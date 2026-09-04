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
import java.util.stream.IntStream;
import javax.swing.JButton;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
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
    private final DefaultListModel<EmployeeOption> employeeModel = new DefaultListModel<>();
    private final JList<EmployeeOption> employeeList = new JList<>(employeeModel);
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0);
    private final JTable table = new JTable(tableModel);
    private final JButton updateButton = new JButton("Update");
    private final JButton deleteButton = new JButton("Delete");
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
        employeeModel.clear();
        for (Employee employee : employees) {
            employeeModel.addElement(new EmployeeOption(employee.id(), employee.name() + " (" + employee.role() + ")"));
        }
        employeeList.clearSelection();
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
        employeeList.setName("shift-employees");
        employeeList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        employeeList.setVisibleRowCount(3);
        table.setName("shift-table");
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultEditor(Object.class, null);
        table.getSelectionModel().addListSelectionListener(event -> populateSelectedShift());
        updateButton.setName("shift-update");
        deleteButton.setName("shift-delete");
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
        controls.add(new JLabel("Employees (Ctrl/Shift-click for multiple; saved on Create/Update)"));
        controls.add(new JScrollPane(employeeList));
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
                selectedEmployeeIds());
        refresh();
        clearForm();
    }

    private void deleteShift() {
        shiftService.delete(selectedShift().id());
        refresh();
        clearForm();
    }

    private Set<String> selectedEmployeeIds() {
        return employeeList.getSelectedValuesList().stream()
                .map(EmployeeOption::id)
                .collect(Collectors.toSet());
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
            selectEmployees(shift.employeeIds());
        }
        updateSelectionState();
    }

    private void selectEmployees(Set<String> employeeIds) {
        int[] selectedIndices = IntStream.range(0, employeeModel.size())
                .filter(index -> employeeIds.contains(employeeModel.get(index).id()))
                .toArray();
        employeeList.setSelectedIndices(selectedIndices);
    }

    private void updateSelectionState() {
        boolean selected = table.getSelectedRow() >= 0;
        updateButton.setEnabled(selected);
        deleteButton.setEnabled(selected);
    }

    private void clearForm() {
        table.clearSelection();
        LocalDate today = LocalDate.now();
        datePicker.setValue(toDate(today, LocalTime.MIDNIGHT));
        startPicker.setValue(toDate(today, DEFAULT_START));
        endPicker.setValue(toDate(today, DEFAULT_END));
        notesField.setText("");
        employeeList.clearSelection();
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
