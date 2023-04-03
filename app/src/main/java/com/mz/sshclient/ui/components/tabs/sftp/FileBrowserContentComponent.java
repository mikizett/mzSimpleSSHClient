package com.mz.sshclient.ui.components.tabs.sftp;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;

public class FileBrowserContentComponent extends JPanel {

    public FileBrowserContentComponent(final String title, final Component component) {
        super(new BorderLayout());
        setBorder(new EmptyBorder(3, 1, 5, 1));
        add(new JLabel(title), BorderLayout.NORTH);
        add(component, BorderLayout.CENTER);
    }
}
