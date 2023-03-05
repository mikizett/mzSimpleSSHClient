package com.mz.sshclient.ui.actions;

import javax.swing.AbstractAction;
import javax.swing.JTree;
import java.awt.event.ActionEvent;

public class ActionOpenNewSessionDialog extends AbstractAction {
    private final JTree tree;

    public ActionOpenNewSessionDialog(final JTree tree, final String title) {
        super(title);
        this.tree = tree;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        /*final NewSessionDialog dialog = new NewSessionDialog(SwingUtilities.getWindowAncestor(tree), tree);
        InvokerUtils.invokeLaterShowWindow(dialog);*/
    }
}
