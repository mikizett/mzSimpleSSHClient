package com.mz.sshclient.ui.components.tab;

import com.mz.sshclient.ui.components.common.tabbedpane.CustomTabbedPaneClosable;

import javax.swing.JPanel;
import javax.swing.border.MatteBorder;
import java.awt.BorderLayout;
import java.awt.Color;

public class TabContainerPanel extends JPanel {
    private CustomTabbedPaneClosable tabbedPane;

    public TabContainerPanel() {
        init();
    }

    private void init() {
        setLayout(new BorderLayout());
        setBorder(new MatteBorder(1, 1, 1, 1, Color.GRAY));

        tabbedPane = new CustomTabbedPaneClosable();

        for (int i = 1; i <= 30; i++) {
            tabbedPane.addTab("Shell-" + i, new TabContentPanel());
        }

        add(tabbedPane);
    }
}
