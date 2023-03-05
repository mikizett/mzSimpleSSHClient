package com.mz.sshclient.ui.actions;

import javax.swing.AbstractAction;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;

public class ActionRenameSelectedTreeItem extends AbstractAction {
    private final JTree tree;

    public ActionRenameSelectedTreeItem(final JTree tree) {
        this.tree = tree;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        tree.setEditable(true);

        TreePath selectedPath = tree.getSelectionPath();
        tree.startEditingAtPath(selectedPath);
    }
}
