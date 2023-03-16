package com.mz.sshclient.ui;

import com.mz.sshclient.ui.components.session.panels.SessionOverviewPanel;

import javax.swing.border.MatteBorder;
import java.awt.Color;

public class MainFrameLeftComponent extends SessionOverviewPanel /*JTabbedPane*/ {
    public MainFrameLeftComponent() {
        //super(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        init();
    }

    private void init() {
        //addTab("Sessions", new SessionOverviewPanel());
        //add(new SessionOverviewPanel());
        setBorder(new MatteBorder(1, 1, 1, 1, Color.GRAY));
    }
}
