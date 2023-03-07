package com.mz.sshclient.ui.actions;

import com.mz.sshclient.ui.components.common.tree.SessionTreeComponent;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class ActionCreateNewFolder extends AbstractAction {

    private final SessionTreeComponent tree;

    public ActionCreateNewFolder(final SessionTreeComponent tree) {
        super("Folder");
        this.tree = tree;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        tree.addNewSessionFolder();
    }

}
