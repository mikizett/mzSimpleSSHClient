package com.mz.sshclient.ui.components.tabs;

import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;
import com.mz.sshclient.ssh.SshTtyConnector;
import com.mz.sshclient.ssh.sftp.SFtpConnector;
import com.mz.sshclient.ui.components.tabs.sftp.FileBrowser;

import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.MatteBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;

public class TabContentPanel extends JPanel {
    private JPanel headerComponent;
    private JPanel shellOrBrowserPanel;

    private JToggleButton shellToggleButton = new JToggleButton("Shell", true);
    private JToggleButton browserToggleButton = new JToggleButton("sFTP");

    private JediTermWidget jediTermWidget = new JediTermWidget(new DefaultSettingsProvider());

    private final FileBrowser fileBrowser;

    public TabContentPanel(final SshTtyConnector sshTtyConnector, final SFtpConnector sFtpConnector) {
        fileBrowser = new FileBrowser(sFtpConnector);

        init();

        jediTermWidget.setTtyConnector(sshTtyConnector);

        jediTermWidget.getTerminal().setAutoNewLine(false);
        jediTermWidget.getTerminalPanel().setFocusable(true);

        jediTermWidget.start();
    }

    private void init() {
        setLayout(new BorderLayout());

        headerComponent = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerComponent.setBorder(new MatteBorder(1, 1, 1, 1, Color.GRAY));
        headerComponent.add(shellToggleButton);
        headerComponent.add(browserToggleButton);

        add(headerComponent, BorderLayout.NORTH);

        CardLayout cardLayout = new CardLayout();
        shellOrBrowserPanel = new JPanel(cardLayout);

        JPanel shellPanel = new JPanel(new BorderLayout());
        jediTermWidget.requestFocusInWindow();
        shellPanel.add(jediTermWidget);

        shellOrBrowserPanel.add(shellPanel);
        shellOrBrowserPanel.add(fileBrowser);

        add(shellOrBrowserPanel, BorderLayout.CENTER);

        ActionListener actionListener = createActionListener(cardLayout, shellOrBrowserPanel);
        shellToggleButton.addActionListener(actionListener);
        browserToggleButton.addActionListener(actionListener);

        normalizeButtonSize();

        shellPanel.requestFocusInWindow();
        jediTermWidget.requestFocusInWindow();
        jediTermWidget.requestFocus();
    }

    private void normalizeButtonSize() {
        final Dimension dim = new Dimension(100, 50);
        shellToggleButton.setPreferredSize(dim);
        browserToggleButton.setPreferredSize(dim);
    }

    private ActionListener createActionListener(final CardLayout cardLayout, JPanel parent) {
        return e -> {
            JToggleButton b = (JToggleButton) e.getSource();
            boolean isSelected = b.getModel().isSelected();
            boolean isArmed = b.getModel().isArmed();

            if (!isSelected && isArmed) {
                b.setSelected(true);
                return;
            }

            if (b == shellToggleButton) {
                browserToggleButton.setSelected(false);
                jediTermWidget.requestFocusInWindow();
            }
            if (b == browserToggleButton) {
                if (!fileBrowser.isSFtpConnected()) {
                    fileBrowser.connectSFtp();
                }
                shellToggleButton.setSelected(false);
            }
            cardLayout.next(parent);
        };
    }

}
