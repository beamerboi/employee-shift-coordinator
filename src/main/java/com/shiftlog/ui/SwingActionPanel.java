package com.shiftlog.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JPanel;

abstract class SwingActionPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JLabel errorLabel = new JLabel();

    SwingActionPanel(String errorLabelName) {
        super(new BorderLayout(8, 8));
        errorLabel.setName(errorLabelName);
        errorLabel.setForeground(Color.RED.darker());
    }

    final JLabel errorLabel() {
        return errorLabel;
    }

    final void runAction(Runnable action) {
        clearError();
        try {
            action.run();
        } catch (RuntimeException exception) {
            errorLabel.setText(exception.getMessage());
            errorLabel.setVisible(true);
        }
    }

    final void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }
}
