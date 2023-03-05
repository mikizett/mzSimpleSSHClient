package com.mz.sshclient.ui.actions;

import com.mz.sshclient.model.SessionFolderModel;

import javax.swing.AbstractAction;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.util.UUID;

public class ActionCreateNewFolder extends AbstractAction {
    private final JTree tree;

    public ActionCreateNewFolder(final JTree tree) {
        super("Folder");
        this.tree = tree;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        tree.setEditable(true);

        final TreePath parentPath = tree.getSelectionPath();
        if (parentPath != null) {
            final DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parentPath.getLastPathComponent();
            addChildNode(parentNode);
        } else {
            final DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) tree.getModel().getRoot();
            addChildNode(rootNode);
        }
    }

    private void addChildNode(final DefaultMutableTreeNode parentNode) {
        final SessionFolderModel folder = new SessionFolderModel();
        folder.setId(UUID.randomUUID().toString());
        folder.setName("New Folder");

        final DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(folder);
        ((DefaultTreeModel) tree.getModel()).insertNodeInto(childNode, parentNode, parentNode.getChildCount());
        final TreePath path = new TreePath(childNode.getPath());
        tree.startEditingAtPath(path);
    }
}
