package com.campus.recycle.ui;

import com.campus.recycle.exception.BusinessException;
import com.campus.recycle.model.User;
import com.campus.recycle.util.DBManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/** Desktop application entry point. */
public final class CampusRecycleApp {

    private static final Logger log = LoggerFactory.getLogger(CampusRecycleApp.class);

    private CampusRecycleApp() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CampusRecycleApp::start);
    }

    private static void start() {
        try {
            AppServices services = AppServices.initialize();
            new LoginFrame(services, CampusRecycleApp::showMainWindow).setVisible(true);
        } catch (BusinessException | IllegalStateException e) {
            log.error("桌面应用启动失败", e);
            JOptionPane.showMessageDialog(null, "系统启动失败：" + e.getMessage(),
                    "启动失败", JOptionPane.ERROR_MESSAGE);
            DBManager.getInstance().close();
        }
    }

    private static void showMainWindow(AppServices services, User user) {
        new MainFrame(services, user, () -> new LoginFrame(services,
                CampusRecycleApp::showMainWindow).setVisible(true)).setVisible(true);
    }
}
