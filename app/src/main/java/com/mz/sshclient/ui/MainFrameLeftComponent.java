package com.mz.sshclient.ui;

import com.mz.sshclient.ui.components.session.SessionOverviewPanel;

import javax.swing.JTabbedPane;
import javax.swing.border.MatteBorder;
import java.awt.Color;

public class MainFrameLeftComponent extends JTabbedPane {
    public MainFrameLeftComponent() {
        super(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        init();
    }

    private void init() {
        addTab("Sessions", new SessionOverviewPanel());
        setBorder(new MatteBorder(1, 1, 1, 1, Color.GRAY));
    }
}
