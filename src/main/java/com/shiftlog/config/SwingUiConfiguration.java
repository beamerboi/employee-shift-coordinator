package com.shiftlog.config;

import com.shiftlog.service.EmployeeService;
import com.shiftlog.service.ShiftService;
import com.shiftlog.ui.ShiftLogPanel;
import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwingUiConfiguration {

    @Bean
    @ConditionalOnProperty(name = "shiftlog.gui.enabled", havingValue = "true", matchIfMissing = true)
    ApplicationRunner swingApplication(EmployeeService employeeService, ShiftService shiftService) {
        return new SwingApplicationRunner(employeeService, shiftService);
    }

    private record SwingApplicationRunner(EmployeeService employeeService, ShiftService shiftService)
            implements ApplicationRunner {

        @Override
        public void run(ApplicationArguments arguments) {
            if (!GraphicsEnvironment.isHeadless()) {
                SwingUtilities.invokeLater(this::showWindow);
            }
        }

        private void showWindow() {
            JFrame frame = new JFrame("ShiftLog");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new ShiftLogPanel(employeeService, shiftService));
            frame.setSize(1000, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }
    }
}
