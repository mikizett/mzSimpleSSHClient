package com.mz.sshclient.ui.actions;

import com.mz.sshclient.ui.components.common.tree.SessionTreeComponent;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class ActionConnectSelectedTreeItem extends AbstractAction {
    private final SessionTreeComponent tree;

    public ActionConnectSelectedTreeItem(final SessionTreeComponent tree) {
        this.tree = tree;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        tree.connectSsh();
    }
}
