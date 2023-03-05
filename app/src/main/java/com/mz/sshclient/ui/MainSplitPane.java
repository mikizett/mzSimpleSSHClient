package com.mz.sshclient.ui;

import javax.swing.JSplitPane;

public class MainSplitPane extends JSplitPane {
    public MainSplitPane() {
        init();
    }
    private void init() {
        setOneTouchExpandable(true);
        setDividerLocation(250);

        setLeftComponent(new MainFrameLeftComponent());
        setRightComponent(new MainFrameRightComponent());
    }
}

