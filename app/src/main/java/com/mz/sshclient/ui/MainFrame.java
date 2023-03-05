package com.mz.sshclient.ui;

import com.mz.sshclient.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {
    private static final Logger LOG = LogManager.getLogger(MainFrame.class);

    public MainFrame() {
        super(Constants.APP_NAME_AND_VERSION);
        init();
    }

    private void init() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                /*if (sessionDataService.hasSessionModelChanged()) {
                    int result = JOptionPane.showConfirmDialog(MainFrame.this, "Do you want to save the created sessions?", "Save...", JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        try {
                            sessionDataService.saveToFile();
                        } catch (SaveSessionDataException ex) {
                            LOG.error(ex.getMessage(), ex);
                            JOptionPane.showMessageDialog(MainFrame.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }*/
                dispose();
                setVisible(false);
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
}
