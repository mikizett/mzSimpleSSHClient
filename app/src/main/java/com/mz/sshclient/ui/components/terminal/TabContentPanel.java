package com.mz.sshclient.ui.components.terminal;

import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;
import com.mz.sshclient.ssh.SshTtyConnector;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
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
    private JToggleButton browserToggleButton = new JToggleButton("Browser");

    private JediTermWidget jediTermWidget = new JediTermWidget(new DefaultSettingsProvider());

    private final SshTtyConnector sshTtyConnector;

    public TabContentPanel(final SshTtyConnector sshTtyConnector) {
        this.sshTtyConnector = sshTtyConnector;

        init();

        if (jediTermWidget != null) {
            jediTermWidget.start();
        }
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

        JPanel shellPanel = new JPanel();
        shellPanel.add(jediTermWidget);

        JRootPane rootPane = new JRootPane();
        rootPane.setContentPane(shellPanel);
        add(rootPane);

        JPanel browserPanel = new JPanel();
        browserPanel.add(new JLabel("BROWSER_PANEL"));

        shellOrBrowserPanel.add(shellPanel);
        shellOrBrowserPanel.add(browserPanel);

        add(shellOrBrowserPanel, BorderLayout.CENTER);

        ActionListener actionListener = createActionListener(cardLayout, shellOrBrowserPanel);
        shellToggleButton.addActionListener(actionListener);
        browserToggleButton.addActionListener(actionListener);

        normalizeButtonSize();
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
            }
            if (b == browserToggleButton) {
                shellToggleButton.setSelected(false);
            }
            cardLayout.next(parent);
        };
    }
}
