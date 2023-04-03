package com.mz.sshclient.ui;

import com.mz.sshclient.Constants;
import com.mz.sshclient.services.ServiceRegistry;
import com.mz.sshclient.services.exceptions.SaveSessionDataException;
import com.mz.sshclient.services.interfaces.ISessionDataService;
import com.mz.sshclient.ui.utils.AWTInvokerUtils;
import com.mz.sshclient.ui.utils.MessageDisplayUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class MainFrame extends JFrame {

    private static final Logger LOG = LogManager.getLogger(MainFrame.class);

    private final ISessionDataService sessionDataService = ServiceRegistry.get(ISessionDataService.class);

    public MainFrame() {
        super(Constants.APP_NAME_AND_VERSION);
        init();
    }

    private void init() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        try {
            this.setIconImage(ImageIO.read(MainFrame.class.getResource("/img/logo.png")));
        } catch (IOException e) {
            LOG.error("Could not load logo img", e);
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (sessionDataService.hasSessionModelChanged()) {
                    int result = MessageDisplayUtil.showYesNoConfirmDialog(
                            MainFrame.this,
                            "Do you want to save the created session folders?",
                            "Save..."
                    );
                    if (result == JOptionPane.YES_OPTION) {
                        try {
                            sessionDataService.saveToFile();
                        } catch (SaveSessionDataException ex) {
                            LOG.error(ex.getMessage(), ex);
                            MessageDisplayUtil.showErrorMessage(ex.getMessage());
                        }
                    }
                }

                // close all opened ssh sessions
                AWTInvokerUtils.invokeLater(() -> {
                    if (OpenedSshSessions.hasOpenedSessions()) {
                        int answer = MessageDisplayUtil.showYesNoConfirmDialog(
                                "Do you want to close all opened sessions?",
                                "Close opened sessions..."
                        );
                        if (answer == JOptionPane.YES_OPTION) {
                            OpenedSshSessions.closeAllSshSessions();
                            close();
                        }
                    } else {
                        close();
                    }
                });
            }
        });
        setPreferredSize();
        setLocationRelativeTo(null);
        buildForm();
    }

    private void setPreferredSize() {
        final Insets inset = Toolkit
                .getDefaultToolkit()
                .getScreenInsets(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration());

        final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

        int screenWidth = dim.width - inset.left - inset.right;
        int screenHeight = dim.height - inset.top - inset.bottom;

        if (screenWidth > 1024 || screenHeight > 650) {
            screenWidth = (screenWidth * 80) / 100;
            screenHeight = (screenHeight * 80) / 100;
        }

        setSize(screenWidth, screenHeight);
    }

    private void buildForm() {
        add(new MainSplitPane());
    }

    private void close() {
        dispose();
        setVisible(false);
        System.exit(0);
    }

}
