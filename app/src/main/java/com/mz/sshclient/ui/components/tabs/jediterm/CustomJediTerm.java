package com.mz.sshclient.ui.components.tabs.jediterm;

import com.jediterm.terminal.ui.JediTermWidget;

import javax.swing.JScrollBar;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

public class CustomJediTerm extends JediTermWidget {
    public CustomJediTerm(AbstractJediTermColorMode colorMode) {
        super(colorMode);

        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {}

            @Override
            public void ancestorRemoved(AncestorEvent event) {}

            @Override
            public void ancestorMoved(AncestorEvent event) {
                getTerminalPanel().requestFocusInWindow();
            }
        });
    }

    @Override
    protected JScrollBar createScrollBar() {
        return new JScrollBar();
    }
}
