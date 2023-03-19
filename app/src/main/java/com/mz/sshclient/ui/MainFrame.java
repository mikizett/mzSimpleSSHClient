package com.mz.sshclient.ui;

import com.mz.sshclient.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class MainFrame extends JFrame {

    private static final Logger LOG = LogManager.getLogger(MainFrame.class);

    public MainFrame() {
        super(Constants.APP_NAME_AND_VERSION);
        init();
    }

    private void init() {
        try {
            this.setIconImage(ImageIO.read(MainFrame.class.getResource("/img/logo.png")));
        } catch (IOException e) {
            LOG.error("Could not load logo img", e);
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // close all opened ssh sessions
                OpenedSshSessions.closeAllSshSessions();

                dispose();
                setVisible(false);
                System.exit(0);
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
