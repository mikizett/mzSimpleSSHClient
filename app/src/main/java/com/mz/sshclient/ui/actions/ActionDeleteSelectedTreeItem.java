package com.mz.sshclient.ui.actions;

import com.mz.sshclient.ui.components.common.tree.SessionTreeComponent;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class ActionDeleteSelectedTreeItem extends AbstractAction {

    private final SessionTreeComponent tree;

    public ActionDeleteSelectedTreeItem(final SessionTreeComponent tree) {
        this.tree = tree;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        tree.deleteNode();
    }

}
