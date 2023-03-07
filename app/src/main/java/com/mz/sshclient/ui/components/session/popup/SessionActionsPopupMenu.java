package com.mz.sshclient.ui.components.session.popup;

import com.mz.sshclient.ui.actions.ActionCloneSelectedTreeItem;
import com.mz.sshclient.ui.actions.ActionConnectSelectedTreeItem;
import com.mz.sshclient.ui.actions.ActionCreateNewFolder;
import com.mz.sshclient.ui.actions.ActionDeleteSelectedTreeItem;
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
                addMenuItem("Connect", new ActionConnectSelectedTreeItem(tree), KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_DOWN_MASK));
                addSeparator();
                addMenuItem("Edit", new ActionOpenNewSessionDialog(tree), KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.ALT_DOWN_MASK));
            } else {
                add(createNewActionMenu());
            }

            addMenuItem("Rename", new ActionRenameSelectedTreeItem(tree), KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0, false));

            addMenuItem("Clone", new ActionCloneSelectedTreeItem(tree), KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.ALT_DOWN_MASK));
            addMenuItem("Delete", new ActionDeleteSelectedTreeItem(tree), KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false));
        } else {
            add(createNewActionMenu());
        }
    }

    private JMenuItem addMenuItem(String menuItemText, Action action, KeyStroke keyStroke) {
        JMenuItem menuItem;
        if (action != null) {
            menuItem = new JMenuItem(action);
        } else {
            menuItem = new JMenuItem();
        }

        if (keyStroke != null) {
            menuItem.setAccelerator(keyStroke);
        }

        menuItem.setText(menuItemText);
        add(menuItem);

        return menuItem;
    }

    private JMenu createNewActionMenu() {
        final JMenu newMenu = new JMenu("New");

        final JMenuItem newFolderMenuItem = addMenuItem("Folder", new ActionCreateNewFolder(tree), KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.ALT_DOWN_MASK));
        final JMenuItem newSessionMenuItem = addMenuItem("Session", new ActionOpenNewSessionDialog(tree), KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.ALT_DOWN_MASK));

        newMenu.add(newFolderMenuItem);
        newMenu.add(newSessionMenuItem);

        return newMenu;
    }
}
