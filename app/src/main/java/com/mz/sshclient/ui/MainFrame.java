package com.mz.sshclient.ui;

import com.mz.sshclient.Constants;
import com.mz.sshclient.ui.actions.ActionExitApp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class MainFrame extends JFrame {

    private static final Logger LOG = LogManager.getLogger(MainFrame.class);

    private final ActionExitApp actionExitApp = new ActionExitApp(this);

    public MainFrame() {
        super(Constants.APP_NAME_AND_VERSION);
        init();
        initMacOSConfig();
    }

    private void init() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        try {
            setIconImage(ImageIO.read(MainFrame.class.getResource("/img/logo.png")));
        } catch (IOException e) {
            LOG.error("Could not load logo img", e);
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                actionExitApp.actionPerformed(null);
            }
        });
        setPreferredSize();
        setLocationRelativeTo(null);
        buildForm();
    }

    public void initMacOSConfig() {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
            final Desktop desktop = Desktop.getDesktop();
            desktop.setQuitHandler(actionExitApp);
        }
    }

    private void setPreferredSize() {
        final Insets inset = Toolkit
                .getDefaultToolkit()
                .getScreenInsets(
                        GraphicsEnvironment
                                .getLocalGraphicsEnvironment()
                                .getDefaultScreenDevice()
                                .getDefaultConfiguration()
                );

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
        JPanel contentPane = (JPanel) getContentPane();
        contentPane.setLayout(new BorderLayout());

        contentPane.add(new NorthPanel(), BorderLayout.NORTH);
        contentPane.add(new MainSplitPane(), BorderLayout.CENTER);

        //add(new MainSplitPane());
    }

}
