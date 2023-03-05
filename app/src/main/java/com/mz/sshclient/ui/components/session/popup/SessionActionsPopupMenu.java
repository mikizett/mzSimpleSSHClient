package com.mz.sshclient.ui.components.session.popup;

import com.mz.sshclient.ui.actions.ActionCreateNewFolder;
import com.mz.sshclient.ui.actions.ActionOpenNewSessionDialog;
import com.mz.sshclient.ui.actions.ActionRenameSelectedTreeItem;
import com.mz.sshclient.ui.components.common.tree.SessionTreeComponent;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class SessionActionsPopupMenu extends JPopupMenu {
    private final SessionTreeComponent tree;

    public SessionActionsPopupMenu(final SessionTreeComponent tree) {
        this.tree = tree;
        init();
    }

    private void init() {
        final TreePath treePath = tree.getSelectionPath();
        if (treePath != null) {
            final DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) treePath.getLastPathComponent();
            if (!treeNode.getAllowsChildren()) {
                addMenuItem("Connect");

                addSeparator();

                final JMenuItem editItem = addMenuItem("Edit", new ActionOpenNewSessionDialog(tree, "Edit"));
                editItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.ALT_DOWN_MASK));
            } else {
                add(createNewActionMenu());
            }

            final JMenuItem menuItem = addMenuItem("Rename", new ActionRenameSelectedTreeItem(tree));
            menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0, false));

            addMenuItem("Duplicate");
            addMenuItem("Delete");
        } else {
            add(createNewActionMenu());
        }
    }

    private JMenuItem addMenuItem(String menuItemText) {
        return addMenuItem(menuItemText, null);
    }

    private JMenuItem addMenuItem(String menuItemText, Action action) {
        JMenuItem menuItem;
        if (action != null) {
            menuItem = new JMenuItem(action);
        } else {
            menuItem = new JMenuItem();
        }
        menuItem.setText(menuItemText);
        add(menuItem);
        return menuItem;
    }

    private JMenu createNewActionMenu() {
        final JMenu newMenu = new JMenu("New");

        final JMenuItem newFolderMenuItem = new JMenuItem("Folder");
        newFolderMenuItem.setAction(new ActionCreateNewFolder(tree));

        final JMenuItem newSessionMenuItem = new JMenuItem("Session");
        newSessionMenuItem.setAction(new ActionOpenNewSessionDialog(tree, "Session"));

        newMenu.add(newFolderMenuItem);
        newMenu.add(newSessionMenuItem);

        return newMenu;
    }
}
